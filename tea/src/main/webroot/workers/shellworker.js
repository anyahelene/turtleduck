importScripts('./shellcommand.js');

console.warn('ShellWorker init', self, initModule);
const PRELOAD_FILE = '../bin/pwnexercise';
const PATH = '../bin/';

debug = false;

clients = [];
seq = 0;
function msgId() {
    return `${self.seq++}`;
}
/** Whether any clients are already connected */
existing = false;
_msgs = {};
moduleStatus = 0;
wasmBinaries = new Map();
dataFiles = new Map();
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
        self.LOG = LOG;
        const pymsg = (e) => {
            if (self.debug) console.log(LOG, 'MSG:', e);
            port.postMessage({
                header: { msg_type: 'shellworker_status', msg_id: msgId() },
                content: { status: e, whileProcessing: { msg_id, msg_type } },
            });
        };
        const pyerr = (e) => {
            console.error(LOG, 'ERR:', e);
            port.postMessage({
                header: { msg_type: 'shellworker_error', msg_id: msgId() },
                content: { status: e, whileProcessing: { msg_id, msg_type } },
            });
        };
        self.pymsg = pymsg;
        self.pyerr = pyerr;

        function send(msg) {
            if (self.debug) {
                console.log(LOG, 'posting: ', msg);
            }
            port.postMessage(msg);
        }

        try {
            if (self.debug) console.log(LOG, 'processing', JSON.stringify(content));

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
                } else if (msg_type.endsWith('_reply')) {
                    //ignore;
                } else if (msg_type === 'langInit') {
                    const res = await load();
                    self.ready = true;
                    self.langConfig = content;
                    send({
                        header: {
                            msg_type: 'langInit_reply',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: { status: 'ok' },
                    });
                } else if (msg_type === 'upload') {
                    const { filename, data } = content;
                    dataFiles.set(filename, data);
                    send({
                        header: { msg_type: 'upload_reply', ref_id: msg_id, msg_id: msgId() },
                        content: { status: 'ok' },
                    });
                } else if (msg_type === 'execve') {
                    const { command, args } = content;
                    if (!command || !command.match(/^[a-zA-z0-9_-]+$/)) {
                        throw new Error(`Illegal command '${command}'`);
                    }
                    await self.load(command);
                    const CmdModule = await self.init(port, command);
                    const result = CmdModule.callMain(args);
                    send({
                        header: {
                            msg_type: 'execve_reply',
                            ref_id: msg_id,
                            msg_id: msgId(),
                        },
                        content: {
                            command,
                            value: result,
                            status: 'ok',
                        },
                    });
                } else {
                    throw new Error('unknown message type ' + msg_type);
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
async function load(cmd) {
    if (!self.pwnData) {
        console.log('loading data');
        const pwnresponse = await fetch(PRELOAD_FILE);
        console.log(pwnresponse);
        dataFiles.set('pwnexercise', new Uint8Array(await pwnresponse.arrayBuffer()));
        console.debug(dataFiles);
    }
    if (cmd && !wasmBinaries.has(cmd)) {
        console.log(`loading ${PATH}${cmd}.wasm`);
        const wasmresponse = await fetch(`${PATH}${cmd}.wasm`);
        if (!wasmresponse.ok) {
            throw new Error(`Command not found: '${cmd}'`);
        }
        const wasmBinary = await wasmresponse.arrayBuffer();
        console.debug(wasmresponse, wasmBinary);
        self.wasmBinaries.set(cmd, wasmBinary);
    }
}

async function init(port, cmd) {
    console.log(`initializing ${cmd}`, self.langConfig);
    const Module = {
        wasmBinary: self.wasmBinaries.get(cmd),
        preRun: [],
        postRun: [],
        preInit: async () => {
            //console.debug(data.getUint8(0),data.getUint8(1),data.getUint8(2),data.getUint8(3))
            Module.FS.mkdir('/work');
            Module.FS.chdir('/work');
            dataFiles.forEach((data, filename) => Module.FS.writeFile(filename, data)); //,{encoding:'binary'});
            console.debug(Module.FS.readdir('/work'));
            console.debug(Module.FS.readFile('/work/pwnexercise'));
            //   FS.init();
            //  FS.mkdir('/');
        },
        noExitRuntime: false,
        thisProgram: cmd,
        //    arguments: ['-f', '/tmp/pwnexercise'],
        print: (...text) => {
            if (self.debug) console.log(self.LOG, 'PRINT:', text);
            port.postMessage({
                header: { msg_type: 'print', msg_id: msgId(), to: self.langConfig.terminal },
                content: { text: text.join(' ') + '\n', stream: 'stdout' },
            });
        },
        printErr: (...text) => {
            if (self.debug) console.log(self.LOG, 'PRINT:', text);
            port.postMessage({
                header: { msg_type: 'print', msg_id: msgId(), to: self.langConfig.terminal },
                content: { text: text.join(' ') + '\n', stream: 'stderr' },
            });
        },
        setStatus: (text) => {
            if (self.debug) console.log(self.LOG, 'MSG:', text);
            port.postMessage({
                header: { msg_type: 'shellworker_status', msg_id: msgId() },
                content: { status: text },
            });
        },
        port,
    };

    return initModule(Module);
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
