// based on https://pyodide.org/en/stable/usage/webworker.html

import { Message, Payload } from '../borb/Messaging';

export class WorkerController {
    worker: SharedWorker | Worker;
    port: MessagePort | Worker;
    shared: boolean;
    pong: number;
    private _onterminate: () => void;
    private _onclose: () => void;
    private _onerror: (ev: ErrorEvent | MessageEvent<any>) => any;
    private _onmessage: (ev: MessageEvent<any>) => any;
    name: string;
    scriptPath: string;
    debugEnabled: boolean = false;
    constructor(scriptName: string, shared?: boolean, name?: string) {
        if (!scriptName || !scriptName.match(/^[a-zA-Z][a-zA-Z0-9_]*\.js$/)) {
            throw new Error(`Illegal worker script name '${scriptName}'`);
        }
        if (!name) name = scriptName;
        this.name = name;
        this.scriptPath = `./workers/${scriptName}`;
        const q = false ? '?' + name : '';
        if (shared) {
            this.worker = new SharedWorker(this.scriptPath, { name });
            this.port = this.worker.port;
            this.shared = true;
        } else {
            this.worker = new Worker(this.scriptPath, { name });
            this.port = this.worker;
            this.shared = false;
        }
        this.port.onmessage = (ev: MessageEvent<any>) => {
            if (this.debugEnabled)
                console.log('%s: message event: %o', this.name, ev);
            if (this._onmessage) this._onmessage(ev);
        };
        this.port.addEventListener(
            'error',
            (ev: ErrorEvent | MessageEvent<any>) => {
                if (this.debugEnabled)
                    console.warn('%s: error event: %o', this.name, ev);
                if (this._onerror) this._onerror(ev);
            },
        );
        if (this.port instanceof MessagePort) this.port.start();

        this.pong = 0;
    }

    onterminate(handler: () => void) {
        this._onterminate = handler;
    }

    onmessage(handler: (msg: Message) => void) {
        this._onmessage = (msg) => {
            if (msg.data.header?.msg_id === '_') {
                if (msg.data.header.msg_type === 'pong') {
                    console.info('%s: received pong: %o', this.name, msg);
                    this.pong++;
                } else if (msg.data.header.msg_type === 'ping') {
                    this.port.postMessage({
                        header: { msg_type: 'pong', msg_id: '_' },
                        content: {},
                    });
                } else {
                    console.warn('unknown control message: ', msg.data);
                }
            } else {
                handler(msg.data);
            }
        };
    }

    ping() {
        this.pong--;
        this.port.postMessage({
            header: { msg_type: 'ping', msg_id: '_' },
            content: {},
        });
    }
    debug(enable: boolean) {
        this.debugEnabled = enable;
        this.port.postMessage({
            header: { msg_type: 'debug', msg_id: '_' },
            content: { enable },
        });
    }
    onerror(handler: (ev: ErrorEvent | MessageEvent<any>) => any) {
        this._onerror = handler;
    }

    postMessage(msg: any, transfers: Transferable[]) {
        if (this.debugEnabled)
            console.log('%s: postmessage: %o', this.name, msg);
        this.port.postMessage(msg, transfers);
    }

    close() {
        if (this.shared) {
            if (this.worker) {
                this.port.postMessage({
                    header: { msg_type: 'goodbye', msg_id: '_' },
                    content: {},
                });
                (this.port as MessagePort).close();
                this.worker = null;
                if (this._onclose) {
                    this._onclose();
                }
            }
        } else {
            this.terminate();
        }
    }
    terminate() {
        if (this.worker) {
            this.port.postMessage({
                header: { msg_type: 'shutdown', msg_id: '_' },
                content: {},
            });
            if (this.worker instanceof Worker) this.worker.terminate();
            this.worker = null;
            if (this._onterminate) {
                this._onterminate();
            }
        }
    }
}
