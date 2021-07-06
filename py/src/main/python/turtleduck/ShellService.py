import js
import pyodide_js
import pyodide
import sys
import io
import platform
import traceback
import rlcompleter
import turtleduck
from pyodide import eval_code_async
from pyodide import to_js


def debug(*args):
    if _old_stderr != None:
        _old_stderr.write(" ".join([str(a) for a in args])+'\n')

class Key:
    def __init__(self, key, defaultValue = None):
        self.key = key
        self.defaultValue = defaultValue

    def key(self):
        return self.key

    def defaultValue(self):
        return self.defaultValue

    def getFrom(self, src):
        if self.key in src:
            return src[self.key]
        else:
            return defaultValue

    def putInto(self, dst, value):
        dst[self.key] = value

CODE = Key('code')
LOC = Key('loc')
VALUE = Key('value')
PROMPT = Key('prompt')
OPTIONS = Key('opts', {})
SNIP_KIND = Key('snipkind')
SNIP_ID = Key('snipid')
SNIP_NS = Key('snipns')
DOC = Key('doc')
REF = Key('ref')
EXEC = Key('exec')
COMPLETE = Key('complete', True)
DEF = Key('def')
PERSISTENT = Key('persistent')
ACTIVE = Key('active')
NAME = Key('name')
SIGNATURE = Key('signature')
NAMES = Key('names')
DOCS = Key('docs')
VERB = Key('verb')
FULL_NAME = Key('fullname')
TYPE = Key('type')
ICON = Key('icon')
MULTI = Key('multi', [])
DIAG = Key('diag', [])
EXCEPTION = Key('exception')
TEXT = Key('text')
HEAP_USE = Key('heapUse')
HEAP_TOTAL = Key('heapTotal')
HEAP_MAX = Key('heapMax')
CPU_TIME = Key('cpuTime', 0.0)
SILENT = Key('silent', False)
STORE_HISTORY = Key('store_history', True)
CODE = Key('code')
USER_EXPRESSIONS = Key('user_expressions', {})
STOP_ON_ERROR = Key('stop_on_error', True)
ALLOW_STDIN = Key('allow_stdin', True)
EXECUTION_COUNT = Key('execution_count')
PAYLOAD = Key('payload', [])
CODE = Key('code')
CURSOR_POS = Key('cursor_pos')
ANCHOR = Key('anchor')
COMPLETES = Key('completes', [])
DETAIL_LEVEL = Key('detail_level', 0)
FOUND = Key('found')
MATCHES = Key('matches')
METADATA = Key('metadata')
DATA = Key('data')
TRANSIENT = Key('data')



context = {'__name__' : '__main__',
        '__doc__' : None,
        '__spec__': None,
        '__annotations__': {},
        '__package__': None,
        '__meta__': globals()
        }
completer = rlcompleter.Completer(context)
msg_id = 0

async def receive():
    msg = js._msg.to_py()
    js._msg = None
    #debug(msg)
    header = msg['header']
    content = msg['content']
    msg_type = header['msg_type']
    global msg_id
    msg_id = msg_id + 1
    reply_header = {'ref_id' : header['msg_id'], 'msg_id': f'{msg_id}'}
    reply = {'header' : reply_header, 'content' : {}}
    if msg_type == "eval_request":
        reply['content'] = await eval(content['code'], content['ref'], content['opts'])
        reply_header['msg_type'] = 'evalReply'
    elif msg_type == "complete_request":
        reply['content'] = await complete(content['code'], content.get('cursorPos',0), content.get('detailLevel', 0))
        reply_header['msg_type'] = 'complete_reply'
    else:
        reply_header['msg_type'] = 'error_reply'
        reply['content'] = {'ename':'unknown message type', 'evalue':'', 'traceback':[]}

    return to_js(reply)

def send(message):
    global msg_id
    msg_id = msg_id + 1
    message['header']['msg_id'] = f'{msg_id}'
    js.send(to_js(message))

onValueHandler = None
def onValue(handler):
    global onValueHandler
    onValueHandler = handler

onContextChangeHandler = None
def onContextChange(handler):
    global onContextChangeHandler
    onContextChangeHandler = handler

async def eval(code, ref, opts):
    evalResult = {}
    REF.putInto(evalResult, ref)
    if code is None or code == "":
        SNIP_KIND.putInto(evalResult, 'empty')
        return evalResult

    COMPLETE.putInto(evalResult, True)

    d = {}
    CODE.putInto(d, code)
    COMPLETE.putInto(d, True)
    LOC.putInto(d, '')
    REF.putInto(d, '')
    oldcontext = dict(context)
    changeMsg = {"old":oldcontext, "code":code}
    try:
        result = await eval_code_async(code=code, globals=context)
        if result != None:
            typename = type(result).__name__
            if typename == '_Printer' or typename == '_Helper':
                typename = None
            VALUE.putInto(d, repr(result))
            TYPE.putInto(d, typename)
            VALUE.putInto(evalResult, VALUE.getFrom(d))
            TYPE.putInto(evalResult, TYPE.getFrom(d))
            if onValueHandler != None:
                #debug("calling ", onValueHandler)
                try:
                    changeMsg['new'] = context
                    changeMsg['value'] = result
                    onValueHandler(changeMsg)
                except:
                    debug("onValueHandler failed: ", sys.exc_info())
        SNIP_KIND.putInto(d, 'py')
        SNIP_KIND.putInto(evalResult, 'py')
    except:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        ex = {'ename': exc_type.__name__, 
              'evalue': "".join(traceback.format_exception_only(exc_type, exc_value)),
              'traceback': traceback.format_tb(exc_traceback)}
        EXCEPTION.putInto(d, ex)
    MULTI.putInto(evalResult, [d])
    deleted, changed = diffContext(oldcontext, context, code)
    evalResult['deleted'] = deleted
    evalResult['changed'] = changed
    return evalResult

def diffContext(old, new, code):
    deleted = set()
    changed = {}
    changeMsg = {"old":old, "new":new, "code":code, "iscontext": True}
    for k in old:
        if not k.startswith("__") and k not in new:
            send_update(k, old[k], None)

    for k in new:
        if not k.startswith("__") and (k not in old or not(new[k] is old[k])):
            send_update(k, old.get(k), new.get(k))
            if onContextChangeHandler != None:
                try:
                    changeMsg['name'] = k
                    changeMsg['value'] = new[k]
                    onContextChangeHandler(changeMsg)
                except:
                    debug("onContextChangeHandler failed: ", sys.exc_info())

    return deleted, changed

def send_update(name, oldVal, newVal):
    data = {'kind':'snippet','sym':'','new':False}
    if oldVal is None:
        data[VERB.key] = 'created'
        data['new'] = True
    elif newVal is None:
        data[VERB.key] = 'deleted'
    else:
        data[VERB.key] = 'updated'
    data[SNIP_ID.key] = name
    typename = type(newVal).__name__
    data['signature'] = name
    data[ICON.key] = icon_of(typename)
    if typename in ['function', 'builtin_function_or_method']:
        data['signature'] = f'{name}()'
        data['snipkind'] = 'method'
    elif typename == 'module':
        data['snipkind'] = 'import'
    elif typename == 'type':
        data['snipkind'] = 'type'
    else:
        data['snipkind'] = 'var'
    data['category'] = data['snipkind']
    data[NAME.key] = name
    data[FULL_NAME.key] = name
    data[TYPE.key] = typename

    message = {'header':{'msg_type':'update'}, 'content':{'info':data}}
    send(message)

def icon_of(typename):
    return '🐍'





async def complete(code, cursorPos, detailLevel = 0):
    result = {}
    code = code[:cursorPos]
    completes = []
    i = 0
    c = completer.complete(code, i)
    while c != None:
        completes.append(c)
        i = i + 1
        c = completer.complete(code, i)

    if i > 0:
        FOUND.putInto(result, True)
        ANCHOR.putInto(result, 0)
        COMPLETES.putInto(result, completes)
    else:
        FOUND.putInto(result, False)

    return result


class MsgBuffer(io.RawIOBase):
    def __init__(self, name):
        self.name = name

    def writable(self):
        return True

    def seekable(self):
        return False

    def isatty(self):
        return True

    def write(self, data):
        text = data.tobytes().decode('utf-8')
        message = {'header':{'msg_type':'print'},
                    'content':{'text':text, 'stream':self.name}}
        send(message)
        return len(data)

def setup_io(stream_name):
    global _old_stdout, _old_stderr, _old_stdin
    global _msg_stdout, _msg_stderr, _msg_stdin
    _old_stdout = sys.stdout
    _old_stderr = sys.stderr
    _old_stdin = sys.stdin
    _msg_stdout = io.TextIOWrapper(io.BufferedWriter(MsgBuffer(stream_name+"out")), line_buffering=True)
    _msg_stderr = io.TextIOWrapper(io.BufferedWriter(MsgBuffer(stream_name+"err")), line_buffering=True)

def use_msg_io():
    sys.stdout = _msg_stdout
    sys.stderr = _msg_stderr


def banner():
    # from https://github.com/pyodide/pyodide/blob/f890e5b35fbe1b7101cd22278f51bfbff737debc/src/pyodide-py/pyodide/console.py#L243
    # via https://github.com/python/cpython/blob/799f8489d418b7f9207d333eac38214931bd7dcc/Lib/code.py#L214
    info = 'Type "help", "copyright", "credits" or "license" for more information.'
    version = platform.python_version()
    build = f"({', '.join(platform.python_build())})"
    return f'Python {version} ({platform.system()}/WebAssembly), TurtleDuck {turtleduck.__version__}, Pyodide {pyodide.__version__}\n{info}'

async def do_imports(code):
    if code != '':
        await eval_code_async(code=code, globals=context)

