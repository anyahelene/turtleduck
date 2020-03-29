import re

jscodes = open('js-keyvalues.txt', 'r').readlines()
fxcodes = open('fx-keynames.txt', 'r').readlines()
awtcodes = open('awt-keynames.txt', 'r').readlines()

fxnames = []
for line in fxcodes:
    (n1,n2) = line.split(',')
    fxnames.append((n1.strip(),n2.strip()))
awtnames = []
for line in awtcodes:
    awtnames.append(line.strip())

jsalias = []
jsnames = []
names = []
cats = {}
cat = ''
for line in jscodes:
    if line.startswith('#'):
        if cat != '':
            print("}\n")
        cat = line[1:].strip()
        print(f'public static class {cat} ' + '{')
    elif '=' in line:
        (n1,n2) = line.split('=')
        jsalias.append((n1.strip(),n2.strip().replace('"','')))
    else:
        jskey = line.strip()
        #key = '_'.join(re.split('(?<=[a-z0-9])(?=[A-Z0-9][a-z0-9])', jskey)).upper()
        key = '_'.join(re.split('\B(?<=[a-z0-9])(?=[A-Z])|\B(?=[A-Z0-9][a-z])', jskey)).upper()
#        key = jskey.upper()
        fxkey = None
        awtkey = None
        for (n1,n2) in fxnames:
            if jskey.lower() == n1.replace('_','').lower() or jskey.lower() == n2.replace(' ','').lower():
                fxkey = n1
                break
        for n in awtnames:
            if jskey.lower() == n.replace('VK_','').replace('_','').lower():
                awtkey = n
                break
        fxnames = [(x,y) for (x,y) in fxnames if x != fxkey]
        awtnames = [x for x in awtnames if x != awtkey]
        fxkey = f'"{fxkey}"' if fxkey != None else "null"
        awtkey = f'"{awtkey}"' if awtkey != None else "null"
        print(f'\tpublic static final int {key} = defineKey("{key}", "{jskey}", {fxkey}, {awtkey}, "{cat}");')
        cats[key] = cat

print("\tstatic {")
for (fn1, fn2) in fxnames:
    awtname = None
    for an in awtnames:
        if fn1.lower().replace('_','') == an[3:].lower().replace('_',''):
            awtname = an
            break
    if awtname != None:
        awtnames.remove(awtname)
    print(f'\t\tmissingkey("{fn1}", "{awtname}");')
for n in awtnames:
    print(f'\t\tmissingkey("None", "{n}");')

for (n1,n2) in jsalias:
    print(f'\t\tjsAlias("{n1}", "{n2}");')
print("}")

