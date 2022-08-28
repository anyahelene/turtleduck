// based on https://pyodide.org/en/stable/usage/webworker.html

import { Messaging } from '../borb/Messaging';
import { LanguageConnection } from './Language';
import { MessagingConnection } from './MessagingConnection';

export class WorkerConnection extends MessagingConnection implements LanguageConnection {
    worker: SharedWorker | Worker;
    port: MessagePort | Worker;
    shared: boolean;
    scriptPath: string;
    constructor(
        router: typeof Messaging,
        id: string,
        scriptName: string,
        shared?: boolean,
        name?: string,
    ) {
        super(router, id, name || scriptName);
        if (!scriptName || !scriptName.match(/^[a-zA-Z][a-zA-Z0-9_]*\.js$/)) {
            this.close();
            throw new Error(`Illegal worker script name '${scriptName}'`);
        }

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
        this.port.addEventListener('error', this.handleError);

        // engage!
        this.port.onmessage = this.receiveMessage;
        if (this.port instanceof MessagePort) this.port.start();
    }

    protected postMessage(msg: any, transfers: Transferable[] = []) {
        if (this.debugEnabled) console.log('%s: postmessage: %o', this.name, msg);
        this.port.postMessage(msg, transfers);
    }

    protected doClose(terminate = false, fromRemote = false) {
        super.doClose(terminate, fromRemote);
        const worker = this.worker;
        const port = this.port;
        if (!worker) return;
        if (!this.shared) terminate = true;

        port.removeEventListener('error', this.handleError);
        port.onmessage = null;
        this.worker = null;
        this.port = null;

        if (worker instanceof Worker) {
            globalThis.setTimeout(() => {
                worker.terminate();
                console.warn('worker terminated');
            }, 100);
        } else if (port instanceof MessagePort) {
            globalThis.setTimeout(() => {
                port.close();
                console.warn('message port closed');
            }, 100);
        }
    }

    toString() {
        return `${this.shared ? 'Shared' : ''}WorkerConnection(${this.id})`;
    }
}
