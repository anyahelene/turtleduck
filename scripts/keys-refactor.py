import sys
import re

glnames = {}
for line in open('glfw-keynames.txt', 'r').readlines():
    name = line.strip()
    glnames[name[9:].replace('_','')] = name

# 0xUUUUUU
clspattern = re.compile(r'^.*public static class (\w+)')
pattern = re.compile(r'^.*final int (\w+) = defineKey\(([^,]*),([^,]*),([^,]*),([^,]*),([^,]*)\)')
group = None
start = 0x200000
first = start
last = start
offset = 0
inits = ''
defs = 'key,id,name,js,fx,awt,gl,cat,doc\n'
for line in sys.stdin.readlines():
    mo = pattern.match(line)
    if mo != None:
        name = mo.group(1)
        namestr = mo.group(2)
        jsname = mo.group(3)
        fxname = mo.group(4)
        awtname = mo.group(5)
        cat = mo.group(6)
        doc = ' '.join([n.capitalize() for n in name.split('_')])
        doc = f'The {doc} key'
        glname = "null"
        for n in (name,jsname,fxname,awtname):
            n = n.replace('_','').replace('"','')
            if n in glnames:
                glname = f'"{glnames[n]}"'
                del glnames[n]
                break
        inits += f'\t\tdefineKey({group}.{name}, {namestr}, {jsname}, {fxname}, {awtname}, {glname}, {cat});\n'
        defs += f'{group}.{name},0x{start+offset:x},{namestr},{jsname},{fxname},{awtname},{glname},{cat},{doc.replace(" ", "%20")}\n'
        print(f'\t\t/** {doc}. */')
        print(f'\t\tpublic static final int {name} = 0x{start+offset:06x};')
        last = start+offset
        offset += 1
        continue

    mo = clspattern.match(line)
    if mo != None:
        group = mo.group(1)
        print(line, end='')
        start += 0x10000
        offset = 0
        continue
    if 'static {' in line:
        print(f'\n\t\tprotected static final int FIRST_ID = 0x{first:06x}, LAST_ID = 0x{last:06x};')
        print(line, end='')
        print(inits)
        continue
    elif line.strip() == '}' and offset > 0:
        print(f'\n\t\tprotected static final int FIRST_ID = 0x{start:06x}, LAST_ID = 0x{start+offset-1:06x};')
        offset = 0
        

    print(line, end='')


with open('keys.csv','w') as csvfile:
    csvfile.write(defs.replace(' ','').replace('"','').replace('null','').replace("%20", " "))

with open('missing-gl', 'w') as f:
    f.write('\n'.join(glnames.values()))
