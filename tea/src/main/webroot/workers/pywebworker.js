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
_msgs = {};

function connect(port) {
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
        const LOG = `[${msg_id}: ${msg_type}]  `;

        const pymsg = (e) => {
            if (self.debug) console.log(LOG, 'MSG:', e);
            port.postMessage({
                header: { msg_type: 'python_status', msg_id: msgId() },
                content: { status: e, whileProcessing: { msg_id, msg_type } },
            });
        };
        const pyerr = (e) => {
            console.error(LOG, 'ERR:', e);
            port.postMessage({
                header: { msg_type: 'python_error', msg_id: msgId() },
                content: { status: e, whileProcessing: { msg_id, msg_type } },
            });
        };
        self.pymsg = pymsg;
        self.pyerr = pyerr;
        async function runPython(code) {
            if (self.debug) console.log(LOG, 'python<<<', code);
            self._send = send;
            const r1 = await self.pyodide.loadPackagesFromImports(code, pymsg, pyerr);
            const r2 = await self.pyodide.runPythonAsync(code, pymsg, pyerr);
            if (self.debug) console.log(LOG, 'python>>>', r2);
        }
        function send(msg) {
            if (self.debug) {
                console.log(LOG, 'posting: ', msg);
            }
            port.postMessage(msg);
        }

        try {
            if (self.debug) console.log(LOG, 'processing', JSON.stringify(content));
            if (!self.loadPyodide.inProgress) {
                queue.push(e);
                if (self.debug) console.log(LOG, 'loading pyodide');
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
                    console.log(LOG, 'processing queue – message: ', msg.data);
                    await port.onmessage(msg);
                }
                if (self.debug) console.log(LOG, 'pyodide ready!', self.pyodide);
                return;
            }

            if (data.header) {
                if (msg_type === 'hello') {
                    if (self.debug) console.log(LOG, 'hello', content);
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
                    if (self.debug) console.log(LOG, 'echo', content);
                    port.postMessage({
                        header: {
                            msg_type: 'echo_reply',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: { status: 'ok', content },
                    });
                } else if (msg_type === 'ping') {
                    if (self.debug) console.log(LOG, 'ping', content);
                    port.postMessage({
                        header: {
                            msg_type: 'pong',
                            ref_id: msg_id,
                            msg_id: '_',
                        },
                        content: { status: 'ok' },
                    });
                } else if (msg_type === 'debug') {
                    if (self.debug) console.log(LOG, 'debug', content);
                    self.debug = !!content.enable;
                } else if (msg_type === 'goodbye') {
                    if (self.debug) console.log(LOG, 'goodbye', content);
                    const id = self.clients.indexOf(port);
                    if (id >= 0) {
                        self.clients.splice(id, 1);
                    }
                } else if (msg_type === 'shutdown') {
                    if (self.debug) console.log(LOG, 'shutdown', content);
                    const id = self.clients.indexOf(port);
                    self.close();
                } else if (msg_type === 'python_status_reply') {
                    //ignore
                } else if (!self.pyodide) {
                    if (self.debug)
                        console.log(
                            LOG,
                            'pyodide not ready, enqueuing:',
                            JSON.stringify(data.header),
                            data,
                        );
                    queue.push(e);
                    return;
                } else if ((LOG, msg_type === 'langInit')) {
                    const res = await initPython(content, runPython);
                    pymsg('Python environment ready!');
                    send({
                        header: {
                            msg_type: 'langInit_reply',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: { status: 'ok' },
                    });
                } else {
                    self._msgs[msg_id] = data;
                    self._msg = data;
                    self._send = send;
                    self.pyodide
                        .runPythonAsync(`ShellService.receive('${msg_id}')`, pymsg, pyerr)
                        .then((res) => {
                            if (res instanceof Map) {
                                if (!(res.has('content') && res.has('header'))) {
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
                        })
                        .finally(() => {
                            delete self._msgs[msg_id];
                        });
                }
            }
        } catch (e) {
            // if you prefer messages with the error
            console.log(LOG, e);
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

    await runPython('import micropip');
    await runPython('import unthrow');
    await runPython("await micropip.install('../py/turtleduck-0.1.1-py3-none-any.whl')");
    for (const name of installs) {
        await runPython(`await micropip.install('${name}')`);
    }
    await runPython('from pyodide import to_js\n');
    await runPython('from turtleduck import ShellService\n');
    for (const init of inits) {
        await runPython(init);
    }
    await runPython(`ShellService.setup_io('${content.terminal || ''}')\n`);
    await runPython(`ShellService.setup_explorer('${content.explorer || ''}')\n`);
    await runPython('ShellService.use_msg_io()\n');
    await runPython('ShellService.do_imports([' + imports.map((s) => `'${s}'`).join(',') + '])\n');
    await runPython('print(ShellService.banner())');
}

console.warn('Starting worker', self);
if (self.constructor.name === 'SharedWorkerGlobalScope') {
    console.warn('Starting shared worker!');
    self.onconnect = function (e) {
        console.warn('onconnect:', e.ports);
        connect(e.ports[0]);
        e.ports[0].start();
    };
    self.s;
} else {
    console.warn('Starting dedicated worker!');
    connect(self);
}
