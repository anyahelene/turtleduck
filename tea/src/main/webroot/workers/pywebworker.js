importScripts('../py/pyodide.js');

console.warn('PyWebWorker init', self);

const routes = {
    init_python: '',
    eval_request: 'ShellService',
};

util = {
    id: function (arg) {
        return arg;
    },
    log: function (arg) {
        console.log(arg);
    },
};

isMessage = function (msg) {
    if (msg instanceof Map) {
        return msg.has('content') && msg.has('header');
    } else {
        return 'content' in msg && 'header' in msg;
    }
};
debug = true;

waiting_for = null;

clients = [];
seq = 0;
function msgId() {
    return `${self.seq++}`;
}
/** Whether any clients are already connected */
existing = false;

function connect(port) {
    const pymsg = (e) => {
        console.log(e);
        port.postMessage({
            header: { msg_type: 'python_status', msg_id: msgId() },
            content: { status: e },
        });
    };
    const pyerr = (e) => {
        console.error(e);
        port.postMessage({
            header: { msg_type: 'python_error', msg_id: msgId() },
            content: { status: e },
        });
    };
    function send(msg) {
        if (self.debug) {
            console.log('posting: ', msg);
        }
        port.postMessage(msg);
    }
    async function runPython(code) {
        if (self.debug) console.log('run python:', code);
        self._send = send;
        await self.pyodide.loadPackagesFromImports(code);
        return await self.pyodide.runPythonAsync(code, pymsg, pyerr);
    }
    const queue = [];

    port.onmessage = async function (e) {
        const data = e.data;
        if (!data.header) {
            console.error('invalid message', e);
            return;
        }
        const msg_id = data.header.msg_id;
        const msg_type = data.header.msg_type;
        const content = data.content;
        console.log('msg_type', msg_type);

        try {
            if (!self.loadPyodide.inProgress) {
                queue.push(e);
                console.log('loading pyodide');
                port.postMessage({
                    header: {
                        msg_type: 'python_status',
                        msg_id: msgId(),
                    },
                    content: { status: 'Loading Pyodide...', wait: true },
                });
                self.pyodide = await loadPyodide({
                    indexURL: '../py/',
                    fullStdLib: false,
                });
                if (!self.pyodide) {
                    port.postMessage({
                        header: {
                            msg_type: 'error_reply',
                            msg_id: msgId(),
                            ref_id: data.header.msg_id,
                        },
                        content: {
                            status: 'error',
                            ename: 'InitializationError',
                            evalue: 'loading pyodide failed',
                            traceback: [],
                        },
                    }); // if you prefer onerror events
                    return;
                }
                //console.log(self.pyodide._module); // provoke unhashable type bug
                port.postMessage({
                    header: { msg_type: 'python_status', msg_id: msgId() },
                    content: { status: 'Pyodide loaded.' },
                });
                while (queue.length) {
                    const msg = queue.shift();
                    console.log('processing queue – message: ', msg.data);
                    await port.onmessage(msg);
                }
                console.log('pyodide ready!', self.pyodide);
                return;
            } else if (!self.pyodide) {
                console.log('pyodide not ready, enqueuing:', data);
                queue.push(e);
                return;
            }

            if (data.header) {
                if (msg_type === 'hello') {
                    console.log('hello', content);
                    const id = self.clients.push(port);
                    port.postMessage({
                        header: {
                            msg_type: 'welcome',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: {
                            status: 'ok',
                            existing: self.existing,
                            id: id,
                        },
                    });
                    self.existing = true;
                } else if (msg_type === 'echo') {
                    console.log('echo', content);
                    port.postMessage({
                        header: {
                            msg_type: 'echo_reply',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: { status: 'ok', content },
                    });
                } else if (msg_type === 'ping') {
                    console.log('ping', content);
                    port.postMessage({
                        header: {
                            msg_type: 'pong',
                            ref_id: msg_id,
                            msg_id: '_',
                        },
                        content: { status: 'ok' },
                    });
                } else if (msg_type === 'debug') {
                    console.log('debug', content);
                    self.debug = !!content.enable;
                } else if (msg_type === 'goodbye') {
                    console.log('goodbye', content);
                    const id = self.clients.indexOf(port);
                    if (id >= 0) {
                        self.clients.splice(id, 1);
                    }
                } else if (msg_type === 'shutdown') {
                    console.log('shutdown', content);
                    const id = self.clients.indexOf(port);
                    self.close();
                } else if (msg_type === 'langInit') {
                    const res = await initPython(content, runPython);
                    console.log('Python result: ', res);
                    send({
                        header: {
                            msg_type: 'langInit_reply',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: { status: 'ok' },
                    });
                    //} else if(msg_type === 'failure' || msg_type === 'error_reply') {
                    // ignore
                } else {
                    self._msg = data;
                    self._send = send;
                    self.pyodide
                        .runPythonAsync(
                            'ShellService.receive()',
                            self.pymsg,
                            self.pyerr,
                        )
                        .then((res) => {
                            console.log('Python result: ', res);
                            if (res instanceof Map) {
                                if (
                                    !(res.has('content') && res.has('header'))
                                ) {
                                    res = {
                                        header: {
                                            msg_type: msg_type + '_reply',
                                            ref_id: msg_id,
                                            msg_id: msgId(),
                                        },
                                        content: res,
                                    };
                                } else {
                                    res.get('header').set('ref_id', msg_id);
                                }
                            } else if (res) {
                                res = {
                                    header: {
                                        msg_type: msg_type + '_reply',
                                        ref_id: msg_id,
                                        msg_id: msgId(),
                                    },
                                    content: { result: res },
                                };
                            }
                            if (res) send(res);
                        });
                }
            }
        } catch (e) {
            // if you prefer messages with the error
            console.log(e);
            port.postMessage({
                header: {
                    msg_type: 'error_reply',
                    msg_id: msgId(),
                    ref_id: msg_id,
                },
                content: {
                    status: 'error',
                    ename: e.name,
                    evalue: e.message,
                    traceback: e.stack.split('\n'),
                },
            }); // if you prefer onerror events
            // setTimeout(() => { throw err; });
        }
    };
}

async function initPython(content, runPython) {
    const result = [];
    const config = content.config;
    const installs = config.install || [];
    const imports = config.import || [];
    const inits = config.init || [];

    await runPython(
        'import micropip\n' + //
            'import unthrow\n' + //
            // + "import PIL\n"//
            "await micropip.install('../py/turtleduck-0.1.1-py3-none-any.whl')\n",
    );
    for (const name of installs) {
        await runPython(`await micropip.install('${name}')`);
    }
    await runPython('from pyodide import to_js\n');
    await runPython('from turtleduck import ShellService\n');
    for (const init of inits) {
        await runPython(init);
    }
    await runPython(`ShellService.setup_io('${content.terminal || ''}')\n`);
    await runPython(
        `ShellService.setup_explorer('${content.explorer || ''}')\n`,
    );
    await runPython('ShellService.use_msg_io()\n');
    await runPython(
        'ShellService.do_imports([' +
            imports.map((s) => `'${s}'`).join(',') +
            '])\n',
    );
    await runPython('print(ShellService.banner())');
}

console.warn('Starting worker', self);
if (self.constructor.name === 'SharedWorkerGlobalScope') {
    console.warn('Starting shared worker!');
    self.onconnect = function (e) {
        connect(e.ports[0]);
    };
} else {
    console.warn('Starting dedicated worker!');
    connect(self);
}
