#! /usr/bin/python3

import sys
import re


prop = re.compile(r'^\s*(readonly|static)?\s*(\w[\w\d]*)(\??)\s*:(.*);\s*$')
func = re.compile(r'^\s*(readonly|static)?\s*(\w[\w\d]*)(\??)\(([^)]*)\)\s*:(.*);\s*$')

def javaType(typ : str):
    if typ == 'string':
        return 'String'
    elif typ == 'number':
        return 'int'
    elif "'" in typ:
        return 'String'
    else:
        return typ

def javaArgs(args : list):
    result = ''
    args = args.split(',')
    for a in args:
        a = a.split(':')
        if result != '':
            result += ', '
        if len(a) == 1:
            result += a[0]
        else:
            result += javaType(a[1].strip())
            result += ' '
            result += a[0].strip()
    return result

comment = ''

for line in sys.stdin.readlines():
    mo = prop.match(line)
    if mo != None:
        mod = mo.group(1)
        name = mo.group(2)
        q = "@Optional" if mo.group(3) == '?' else ''
        typ = javaType(mo.group(4).strip())
        extra = ''
        print(f'\n{comment}@JSProperty {q}\n{typ} get{name[:1].capitalize()}{name[1:]}(){extra};')
        if mod != 'readonly':
            print(f'\n{comment}@JSProperty {q}\nvoid set{name[:1].capitalize()}{name[1:]}({typ} val){extra};')
        comment = ''
        continue
    mo = func.match(line)
    if mo != None:
        mod = mo.group(1)
        if mod == None:
            mod = ''
        name = mo.group(2)
        q = "@Optional\n" if mo.group(3) == '?' else ''
        args = javaArgs(mo.group(4))
        typ = javaType(mo.group(5).strip())
        extra = ''
        print(f'\n{comment}{q}{mod} {typ} {name}({args}){extra};')
        comment = ''
        continue

    if '/**' in line:
        if comment != '':
            print(comment)
        comment = line
    elif comment != '' and '*/' not in comment:
        comment += line
    else:
        print(line)

        
    
