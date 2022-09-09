// based on https://pyodide.org/en/stable/usage/webworker.html

import { Settings } from '../borb';
import { BaseConnection, Message, Messaging, MessagingError } from '../borb/Messaging';
import { LanguageConnection } from './Language';

export abstract class MessagingConnection extends BaseConnection implements LanguageConnection {
    pong: number;
    private _onterminate: () => void;
    private _onclose: () => void;
    private _onerror: (ev: ErrorEvent | MessageEvent<any> | Event) => any = (ev) =>
        console.error('received error from worker', ev);
    private _onmessage: (msg: Message, ev: MessageEvent<any>) => any;
    name: string;
    debugEnabled: boolean = true;
    protected _openPromise: Promise<void> = Promise.resolve();
    constructor(router: typeof Messaging, id: string, name: string) {
        super(router, id);
        this.name = name;
    }
    /*
    onclose(handler: () => void) {
        this._onclose = handler;
    }
    onerror(handler: (ev: ErrorEvent | MessageEvent<any>) => any) {
        this._onerror = handler;
    }
    onmessage(handler: (msg: Message) => void) {
        this._onmessage = handler;
    }
    onterminate(handler: () => void) {
        this._onterminate = handler;
    }
    */
    protected handleError = (ev: ErrorEvent | MessageEvent<any> | Event) => {
        if (this.debugEnabled) console.warn('%s: error event: %o', this.name, ev);
        if (this._onerror) this._onerror(ev);
    };

    protected receiveMessage = (ev: MessageEvent<any>) => {
        if (this.debugEnabled) console.log('%s: message event: %o', this.name, ev);
        let data = ev.data;
        if (typeof data === 'string') data = JSON.parse(data);
        if (data instanceof Map) data = Messaging.fromMap(data);
        if (data.header?.msg_id === '_') {
            if (data.header.msg_type === 'pong') {
                console.info('%s: received pong: %o', this.name, ev);
                this.pong++;
            } else if (data.header.msg_type === 'ping') {
                this.postMessage(
                    {
                        header: { msg_type: 'pong', msg_id: '_' },
                        content: {},
                    },
                    [],
                );
            } else if (data.header.msg_type === 'debug') {
                this.debugEnabled = !!data.content?.enable;
                console.info(
                    `${this.id} debug ${this.debugEnabled ? 'enabled' : 'disabled'} by remote end`,
                );
            } else {
                console.warn('unknown control message: ', ev);
            }
        } else if (data?.header) {
            const msg = data as Message;
            if (!msg.content) msg.content = {};
            if (this.debugEnabled)
                console.log('received from worker:', JSON.stringify(msg.header), msg.content);
            if (this._onmessage) this._onmessage(msg, ev);
            this.deliverHost(msg);
        } else {
            console.error('malformed message:', ev);
        }
    };

    public ping() {
        this.pong--;
        this.postMessage({
            header: { msg_type: 'ping', msg_id: '_' },
            content: {},
        });
    }
    public debug(enable: boolean) {
        console.info(`${this.id} debug ${enable ? 'enabled' : 'disabled'}`);
        this.debugEnabled = enable;
        this.postMessage({
            header: { msg_type: 'debug', msg_id: '_' },
            content: { enable },
        });
    }

    protected abstract postMessage(msg: any, transfers?: Transferable[]);

    public close() {
        this.doClose(false, false);
    }

    protected doClose(terminate: boolean, fromRemote: boolean): void {
        super.close();

        if (!fromRemote && this.isReady) {
            this.postMessage({
                header: { msg_type: terminate ? 'shutdown' : 'goodbye', msg_id: '_' },
                content: {},
            });
        }
    }

    public deliverRemote(msg: Message, transfers: Transferable[] = []): void {
        if (!this.isReady)
            throw new MessagingError(`Connection ${this.id} not ready: ${this._status}`, msg);
        this.postMessage(msg, transfers);
    }

    public async connect(): Promise<string> {
        console.info('connected. sending hello', this);
        switch (this._status) {
            case 'connected':
            case 'working':
                return this.id;
            case 'closed':
                throw new MessagingError('Connection closed: ' + this.id, this);
            case 'connecting':
                console.warn('Already attempting connection', this);
            case 'starting':
                this._status = 'connecting';
                await this._openPromise;
                const reply = await this.router.send(
                    {
                        sessionName: Settings.getConfig('session.name', 'unknown'),
                        endpoints: {},
                    },
                    'hello',
                    this.id,
                );
                console.info('Received welcome', reply);
                this._status = 'connected';
                this.connectHandlers.forEach((handler) => handler(this, reply));

                return this.id;
        }
    }

    toString() {
        return `MessagingConnection(${this.id})`;
    }
}
