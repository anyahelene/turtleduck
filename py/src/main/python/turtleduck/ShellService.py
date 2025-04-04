import PIL.ImageShow
import js
import pyodide_js
import pyodide
import sys
import io
import platform
import traceback
import rlcompleter
import turtleduck
import asyncio
import unthrow
import errno
import os
import pprint
import importlib
from pyodide import to_js


def debug(*args):
    if _old_stderr != None:
        _old_stderr.write(" ".join([str(a) for a in args])+'\n')


class Key:
    def __init__(self, key, defaultValue=None):
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
DISPLAY = Key('display', {})
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


cwd = '/'
# the global environment of evaluated code
context = {'__name__': '__main__',
           '__doc__': None,
           '__spec__': None,
           '__annotations__': {},
           '__package__': None,
           '__meta__': globals(),
           '__builtins__': __builtins__.copy()
           }
context['__builtins__']['pprint'] = pprint.pp

completer = rlcompleter.Completer(context)

# unique id for messages
msg_id = 0
# pending requests (sent messages we're waiting for a reply to)
requests = {}
# pending tasks (incomplete evals waiting for I/O)
tasks = []
# queue of messages to send before returning to event loop
outgoing = []
# things that should be shut down if we do an "emergency stop"
closeables = set()
# stack of currently running callbacks
running = []
# last result
result = None
# all results
results = {}

async def run_tasks():
    global tasks
    if len(tasks) > 0:
        # run returns True if not finished
        debug('running tasks', tasks)
        tasks = [t for t in tasks if await t.run()]
    return len(tasks) == 0


async def receive(in_msg_id):
    msg = js._msgs.to_py()[in_msg_id]
    header = msg['header']
    content = msg['content']
    msg_type = header['msg_type']
    ref_id = header.get('ref_id')

    if msg_type == 'failure' or msg_type == 'error_reply':
        if ref_id and ref_id in requests:
            del requests[ref_id]
        return to_js(None)
    if ref_id:
        if ref_id in requests:
            try:
                handler = requests[ref_id]
                del requests[ref_id]
                handler(content, header)
            except Exception as ex:
                debug("reply handler failed", header, ex)
            await run_tasks()
        flush_outgoing()
        return to_js(None)

    await run_tasks()

    global msg_id
    msg_id = msg_id + 1
    reply_header = {'ref_id': header['msg_id'], 'msg_id': f'{msg_id}'}
    reply = {'header': reply_header, 'content': {}}
    if msg_type == "eval_request":
        reply_header['msg_type'] = 'eval_reply'
        e = Evaluation(content['code'], content['ref'], content['opts'], reply)
        if await e.run():
            debug('adding task')
            tasks.append(e)
    elif msg_type == "complete_request":
        reply['content'] = await complete(content['code'], content.get('cursor_pos', 0), content.get('detailLevel', 0))
        reply_header['msg_type'] = 'complete_reply'
        send(reply)
    elif msg_type == "inspect_request":
        reply['content'] = await inspect(content['code'], content.get('cursor_pos', 0), content.get('detailLevel', 0))
        reply_header['msg_type'] = 'inspect_reply'
        send(reply)
    elif msg_type == "refresh":
        refresh_explorer()
    elif msg_type == "receive_img":
        from PIL import Image
        img = Image.open(io.BytesIO(content.get('data')))
        img.filename = content.get('url', '')
        debug(img)
        localVars = {
            '_data': img
        }

        reply_header['msg_type'] = 'evalReply'
        varName = gen_name('img')
        # why do this with eval instead of just context[varName] = img?
        # => to automatically get explorer updates and evalReply
        code = f'global {varName}; {varName} = _data'
        debug(localVars)
        e = Evaluation(code, varName, {}, reply, localVars)
        if await e.run():
            debug('adding task')
            tasks.append(e)
    elif msg_type == "receive_str":
        localVars = {
            '_data': content.get('data')
        }

        reply_header['msg_type'] = 'evalReply'
        varName = content.get('name', None)
        if varName == None:
            varName = gen_name('text')
        # why do this with eval instead of just context[varName] = img?
        # => to automatically get explorer updates and evalReply
        code = f'global {varName}; {varName} = _data'
        debug(localVars)
        e = Evaluation(code, varName, {}, reply, localVars)
        if await e.run():
            debug('adding task')
            tasks.append(e)
    else:
        reply_header['msg_type'] = 'error_reply'
        reply['content'] = {'ename': 'unknown message type',
                            'evalue': '', 'traceback': [], 'data': msg}
        send(reply)
    flush_outgoing()


def gen_name(prefix):
    i = 0
    while f'{prefix}_{i}' in context:
        i = i + 1
    return f'{prefix}_{i}'


def send(message, handler=None):
    global msg_id
    msg_id = msg_id + 1
    message['header']['msg_id'] = f'{msg_id}'
    if handler:
        requests[f'{msg_id}'] = handler
    js._send(to_js(message))


def send_async(message):
    fut = asyncio.get_running_loop().create_future()
    send(message, lambda content, header: fut.set_result(content))
    return fut

def send_queued(message, handler=None):
    global msg_id
    msg_id = msg_id + 1
    message['header']['msg_id'] = f'{msg_id}'
    if handler:
        requests[f'{msg_id}'] = handler
    outgoing.append(message)
    return message

def flush_outgoing():
    while len(outgoing) > 0:
        message = outgoing.pop(0)
        js._send(to_js(message))
        message['header']['sent'] = True

onValueHandler = None


def onValue(handler):
    global onValueHandler
    onValueHandler = handler


onContextChangeHandler = None


def onContextChange(handler):
    global onContextChangeHandler
    onContextChangeHandler = handler


class Evaluation:
    def __init__(self, code, ref, opts, reply, localVars=None):
        #self.unthrow = importlib.import_module('unthrow')
        self.resumer = unthrow.Resumer()
        self.code = code
        self.ref = ref
        self.opts = opts
        self.loc = opts.get('loc', 'shell://').split('#')[0]
        self.reply = reply
        self.evalResult = {}
        self.localVars = localVars or opts.get('localVars')
        REF.putInto(self.evalResult, ref)
        if code is None or code == "":
            SNIP_KIND.putInto(self.evalResult, 'empty')

        self.evalResult['status'] = 'ok'
        COMPLETE.putInto(self.evalResult, True)
        #SNIP_KIND.putInto(self.evalResult, 'py')
        d = {}
        CODE.putInto(d, code)
        COMPLETE.putInto(d, True)
        LOC.putInto(d, self.loc)
        REF.putInto(d, '')
        #SNIP_KIND.putInto(d, 'py')
        self.resultData = d
        self.oldcontext = dict(context)
        self.changeMsg = {"old": self.oldcontext, "code": code}

    async def run(self):
        debug("running task:", self.code)
        if 'await' in self.code:
            try:
                running.append((self,'await'))
                #context['__stop__'] = self.resumer.stop
                global result
                result = await pyodide.eval_code_async(self.code, globals=context, locals=self.localVars, filename=self.loc)
                results[self.ref] = result
                self.encode_result(result)
            except unthrow.ResumableException as ex:
                debug("throw", running)
                raise ex
            except:
                self.encode_except()
            finally:
                debug(running)
                assert running.pop()[0] == self
            self.finish()
            return False

        if not self.resumer.finished:
            running.append((self,'once'))
            try:
                self.resumer.run_once(self.loop, [])
            finally:
                debug(running)
                assert running.pop()[0] == self

        if self.resumer.finished:
            self.finish()
            return False
        else:
            return True

    def loop(self):
        try:
            #context['__stop__'] = self.resumer.stop
            global result
            result = pyodide.eval_code(
                self.code, globals=context, locals=self.localVars, filename=self.loc)
            results[self.ref] = result
            self.encode_result(result)
        except unthrow.ResumableException as ex:
            raise ex
        except:
            self.encode_except()
        # finally:
        #    del context['__stop__']

    def encode_result(self, result):
        if result != None:
            (rep, typename, disp) = encode_data(
                result, self.opts.get('raw', False))
            VALUE.putInto(self.resultData, rep)
            DISPLAY.putInto(self.resultData, disp)
            TYPE.putInto(self.resultData, typename)
            #VALUE.putInto(self.evalResult, VALUE.getFrom(self.resultData))
            #TYPE.putInto(self.evalResult, TYPE.getFrom(self.resultData))
            #DISPLAY.putInto(self.evalResult, DISPLAY.getFrom(self.resultData))
            if onValueHandler != None:
                #debug("calling ", onValueHandler)
                try:
                    changeMsg['new'] = context
                    changeMsg['value'] = result
                    onValueHandler(changeMsg)
                except:
                    debug("onValueHandler failed: ", sys.exc_info())

    def encode_except(self):
        exc_type, exc_value, exc_traceback = sys.exc_info()
        debug("exception:", sys.exc_info())
        tb = traceback.format_tb(exc_traceback)
        tb.reverse()
        ex = {'ename': exc_type.__name__,
              'evalue': "".join(traceback.format_exception_only(exc_type, exc_value)),
              'traceback': tb,
              'eargs': repr(getattr(exc_value, 'args', ()))
              }
        if exc_type == SyntaxError:
            ex['level'] = 'error'
            ex['evalue'] = exc_value.msg
            ex['line'] = exc_value.text
            ex['lineno'] = exc_value.lineno
            ex['offset'] = exc_value.offset
            ex['end_lineno'] = getattr(exc_value, 'end_lineno', None)
            ex['end_offset'] = getattr(exc_value, 'end_offset', None)
            ex['loc'] = f'{exc_value.filename}#L{ex["lineno"]}C{ex["offset"]}'
            DIAG.putInto(self.resultData, [ex])
        else:
            EXCEPTION.putInto(self.resultData, ex)

    def finish(self):
        self.evalResult['results'] = [self.resultData]
        deleted, changed = diffContext(self.oldcontext, context, self.code)
        self.evalResult['deleted'] = deleted
        self.evalResult['changed'] = changed
        self.reply['content'] = self.evalResult
        send(self.reply)

    def __str__(self):
        return f"eval('{self.code}')"


def typenameOf(obj):
    try:
        typename = type(obj).__name__
        if typename == '_Printer' or typename == '_Helper':
            typename = 'NoneType'
        return typename
    except:
        return None


def encode_data(obj, raw=False):
    typename = typenameOf(obj)
    rep = obj if raw else repr(obj)
    disp = None
    if 'Image' in typename:
        disp = encode_image(obj)
    return (rep, typename, disp)


def encode_image(obj):
    fn = getattr(obj, 'filename', None)
    if fn != None and fn != '':
        return {'display': 'img', 'url': fn}
    else:
        with io.BytesIO() as stream:
            try:
                obj.save(stream, 'PNG')
                buf = stream.getbuffer()
                disp = {'display': 'img', 'data': buf.tobytes(),
                        'format': 'image/png'}
                buf.release()
                return disp
            except Exception as e:
                debug("failed to save image", e)


def read_qrcode(img=None):
    '''Scan a QR code from image or camera

    Parameters:
        img:  a PIL image, a file name, or None to use the camera

    Returns:
        str: the scan result, or None if no code was found'''
    if isinstance(img, str):
        content = {'url': img}
    elif img == None:
        content = {'url': 'qrscan://'}
    else:
        content = encode_image(img)
    fut = send_async({'content': content, 'header': {'msg_type': 'qrscan'}})
    unthrow.stop({'wait_for': '$router_reply'})
    result = asyncio.get_running_loop().run_until_complete(fut).result()
    debug("read qrcode:", result)
    if result.get('status') == 'ok':
        return result.get('text')
    else:
        raise RuntimeError(result.get('error'))


class TurtleDuckViewer(PIL.ImageShow.Viewer):

    format = "PNG"
    options = {"compress_level": 1}

    def show_image(self, image, **options):
        disp = encode_image(image)
        _msg_outbuf.display(disp)
        return 1


PIL.ImageShow.register(TurtleDuckViewer)


def diffContext(old, new, code, ns='main'):
    deleted = set()
    changed = {}
    changeMsg = {"old": old, "new": new, "code": code, "iscontext": True}
    for k in old:
        if not k.startswith("__") and k not in new:
            send_update(k, old[k], None)

    for k in new:
        if not k.startswith("__") and (k not in old or not (new[k] is old[k])):
            send_update(k, old.get(k), new.get(k), ns)
            if onContextChangeHandler != None:
                try:
                    changeMsg['name'] = k
                    changeMsg['value'] = new[k]
                    onContextChangeHandler(changeMsg)
                except:
                    debug("onContextChangeHandler failed: ", sys.exc_info())

    return deleted, changed


def send_update(name, oldVal, newVal, ns='main'):
    if not _explorer:
        return
    data = {'kind': 'snippet', 'sym': '', 'new': False}
    if oldVal is None:
        data[VERB.key] = 'created'
        data['new'] = True
        data['event'] = 'new→ok'
    elif newVal is None:
        data[VERB.key] = 'deleted'
        data['event'] = 'ok→del'
    else:
        data[VERB.key] = 'updated'
        data['event'] = 'ok→ok'
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
    data[SNIP_NS.key] = ns

    message = {'header': {'msg_type': 'update',
                          'to': _explorer}, 'content': {'info': data}}
    send(message)


def icon_of(typename):
    return '🐍'


async def inspect(code, cursorPos, detailLevel=0):
    debug(f'inspect: "{code}" at {cursorPos}"')
    result = {}
    code = code[:cursorPos]
    obj = context.get(code[:-1])
    if obj != None:
        TYPE.putInto(result, typenameOf(obj))
        SIGNATURE.putInto(result, code)
        try:
            hlp = getattr(obj, '__doc__')
            debug(f'hlp: {hlp}')
            TEXT.putInto(result, hlp)
        except:
            debug(f'{sys.exc_info()}')
            pass
        FOUND.putInto(result, True)
    else:
        FOUND.putInto(result, False)

    return result


async def complete(code, cursorPos, detailLevel=0):
    debug(f'complete: "{code}" at {cursorPos}"')
    result = {}
    code = code[:cursorPos]
    obj = context.get(code.rstrip("."))
    TYPE.putInto(result, typenameOf(obj))

    completes = []
    i = 0
    c = completer.complete(code, i)
    while c != None:
        debug(f'  => {c}')
        if obj != None and c.endswith('('):
            member = c[len(code):-1]
            debug(f'member: {member}')
            try:
                member = getattr(obj, member)
                debug(f'member => {member}')
                hlp = getattr(member, '__doc__')
                debug(f'hlp: {hlp}')
                c = c + '––' + hlp
            except:
                debug(f'{sys.exc_info()}')
                pass
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
    def __init__(self, terminal, name):
        self.terminal = terminal
        self.name = name

    def writable(self):
        return True

    def seekable(self):
        return False

    def isatty(self):
        return True

    def write(self, data):
        text = data.tobytes().decode('utf-8')
        message = {'header': {'msg_type': 'print', 'to': self.terminal},
                   'content': {'text': text, 'stream': self.name}}
        send(message)
        return len(data)

    def display(self, data):
        message = {'header': {'msg_type': 'display', 'to': self.terminal},
                   'content': {'data': data, 'stream': self.name}}
        send(message)
        return len(data)


def send_and_wait(msg):
    fut = send_async(msg)
    unthrow.stop({'wait_for': 'reply'})
    return asyncio.get_running_loop().run_until_complete(fut).result()


def open_file(file, mode='r', buffering=-1, encoding=None, errors=None, newline=None, closefd=True, opener=None):
    file = os.path.join(cwd, file)
    debug(f'open_file({file})')

    if 'r' in mode:
        if file.startswith('/examples'):
            fut = send_async(
                {'content': {'url': file[1:]}, 'header': {'msg_type': 'fetch'}})
        elif 'b' in mode:
            fut = send_async({'content': {'path': file}, 'header': {
                             'msg_type': 'readbinfile', 'to': 'storage'}})
        else:
            fut = send_async({'content': {'path': file}, 'header': {
                             'msg_type': 'readtextfile', 'to': 'storage'}})
        #fut = send_async({'content':{'command':'summary'}, 'header':{'msg_type':'$router'}})
        unthrow.stop({'wait_for': '$router_reply'})
        content = asyncio.get_running_loop().run_until_complete(fut).result()
        debug("read file:", content)
        if 'b' in mode:
            data = content.get('content')
            if data != None:
                return io.BytesIO(data)
        else:
            data = content.get('text')
            if data != None:
                return io.StringIO(data, newline)
        err = content.get('evalue', 'unknown error')
        raise OSError(errno.ENOENT, "file not found: " + err, file)
    else:
        raise OSError(errno.EACCESS, "writing files not supported", file)


def setup_explorer(explorer):
    global _explorer
    _explorer = explorer


def setup_io(terminal):
    global _old_stdout, _old_stderr, _old_stdin
    global _msg_stdout, _msg_stderr, _msg_stdin
    global _msg_outbuf, _msg_errbuf
    _old_stdout = sys.stdout
    _old_stderr = sys.stderr
    _old_stdin = sys.stdin
    _msg_outbuf = MsgBuffer(terminal, "stdout")
    _msg_errbuf = MsgBuffer(terminal, "stderr")
    _msg_stdout = io.TextIOWrapper(
        io.BufferedWriter(_msg_outbuf), line_buffering=True)
    _msg_stderr = io.TextIOWrapper(
        io.BufferedWriter(_msg_errbuf), line_buffering=True)


def use_msg_io():
    sys.stdout = _msg_stdout
    sys.stderr = _msg_stderr
    context['__builtins__']['open'] = open_file
    context['__builtins__']['qrscan'] = read_qrcode
    context['__builtins__']['load'] = load

def load(path):
    if '/' not in path:
        path = f'./{path}'
    with open_file(path) as f:
        text = f.read()
        code = compile(text, path, 'exec')
        vars = {}
        eval(code, context) # will add everything to current global context
        print(vars)
        return code

def refresh_explorer():
    diffContext({}, context, '', 'refresh')


def banner():
    # from https://github.com/pyodide/pyodide/blob/f890e5b35fbe1b7101cd22278f51bfbff737debc/src/pyodide-py/pyodide/console.py#L243
    # via https://github.com/python/cpython/blob/799f8489d418b7f9207d333eac38214931bd7dcc/Lib/code.py#L214
    info = 'Type "help", "copyright", "credits" or "license" for more information.'
    version = platform.python_version()
    build = f"({', '.join(platform.python_build())})"
    return f'Python {version} ({platform.system()}/WebAssembly), TurtleDuck {turtleduck.__version__}, Pyodide {pyodide.__version__}\n{info}'


def do_imports(imports):
    for imp in imports:
        if imp.startswith('builtin '):
            imp = imp[8:]
            module = importlib.import_module(imp)
            for key in module.__dict__:
                if not key.startswith('_'):
                    context['__builtins__'][key] = module.__dict__[key]
        else:
            pyodide.eval_code(imp, globals=context)
