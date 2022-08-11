// based on https://pyodide.org/en/stable/usage/webworker.html

import { Payload } from '../borb/Messaging';

class PyController {
    pyodideWorker: SharedWorker | Worker;
    port: MessagePort | Worker;
    shared: boolean;
    pong: number;
    private _onterminate: () => void;
    private _onclose: () => void;
    private _onerror: (ev: ErrorEvent | MessageEvent<any>) => any;
    private _onmessage: (ev: MessageEvent<any>) => any;
    constructor(shared = false, name = undefined) {
        const q = false ? '?' + name : '';
        if (shared) {
            this.pyodideWorker = new SharedWorker('./js/pywebworker.js' + q);
            this.port = this.pyodideWorker.port;
            this.shared = true;
        } else {
            this.pyodideWorker = new Worker('./js/pywebworker.js' + q, name);
            this.port = this.pyodideWorker;
            this.shared = false;
        }
        this.port.onmessage = (ev: MessageEvent<any>) => {
            console.log('message event', ev);
            if (this._onmessage) this._onmessage(ev);
        };
        this.port.addEventListener(
            'error',
            (ev: ErrorEvent | MessageEvent<any>) => {
                console.log('error event', ev);
                if (this._onerror) this._onerror(ev);
            },
        );
        if (this.port instanceof MessagePort) this.port.start();

        this.pong = 0;
    }

    onterminate(handler: () => void) {
        this._onterminate = handler;
    }

    onmessage(handler: (ev: MessageEvent<any>) => void) {
        console.log('onmessage: ', handler);
        this._onmessage = (msg) => {
            if (msg.data === 'pong') {
                console.log('received pong', msg);
                this.pong++;
            } else {
                handler(msg);
            }
        };
    }

    ping() {
        this.pong--;
        this.port.postMessage('ping');
    }
    debug(enable: boolean) {
        if (enable) this.port.postMessage('debug');
        else this.port.postMessage('!debug');
    }
    onerror(handler: (ev: ErrorEvent | MessageEvent<any>) => any) {
        this._onerror = handler;
    }

    postMessage(msg: any, transfers:Transferable[]) {
        console.log('postmessage: ', msg);
        this.port.postMessage(msg, transfers);
    }

    _post(
        msg: any,
        onSuccess: (arg0: MessageEvent<any>) => any,
        onError: (arg0: ErrorEvent | MessageEvent<any>) => any,
    ) {
        const _oldOnSuccess = this.port.onmessage;
        const _oldOnError = this._onerror;
        this.port.onmessage = (m) => {
            this.port.onmessage = _oldOnSuccess;
            return onSuccess(m);
        };
        this._onerror = (m) => {
            this._onerror = _oldOnError;
            return onError(m);
        };
        this.port.postMessage(msg);
    }

    run(
        script: string,
        context: Payload,
        onSuccess: (arg0: MessageEvent<any>) => any,
        onError: (arg0: MessageEvent<any> | ErrorEvent) => any,
    ) {
        this._post(
            {
                ...context,
                python: script,
            },
            onSuccess,
            onError,
        );
    }
    send(
        to: string,
        msg: Payload,
        onSuccess: (arg0: MessageEvent<any>) => any,
        onError: (arg0: ErrorEvent | MessageEvent<any>) => any,
    ) {
        this._post(
            {
                _msg: msg,
                to: to,
            },
            onSuccess,
            onError,
        );
    }

    close() {
        if (this.shared) {
            if (this.pyodideWorker) {
                this.port.postMessage({
                    header: { msg_type: 'goodbye', msg_id: 'exit' },
                    content: {},
                });
                this.pyodideWorker = null;
                if (this._onclose) {
                    this._onclose();
                }
            }
        } else {
            this.terminate();
        }
    }
    terminate() {
        if (this.pyodideWorker) {
            this.port.postMessage({
                header: { msg_type: 'goodbye', msg_id: 'exit' },
                content: {},
            });
            if (this.pyodideWorker instanceof Worker)
                this.pyodideWorker.terminate();
            this.pyodideWorker = null;
            if (this._onterminate) {
                this._onterminate();
            }
        }
    }
}

export { PyController };
