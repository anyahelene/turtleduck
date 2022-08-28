import { Messaging } from '../borb/Messaging';
import SockJS from 'sockjs-client';
import { MessagingConnection } from './MessagingConnection';
import { LanguageConnection } from './Language';
export class SockJSConnection extends MessagingConnection implements LanguageConnection {
    private sockjs: WebSocket;
    public lastEvent: any;
    constructor(router: typeof Messaging, id: string, public address: string, name?: string) {
        super(router, id, name || address);
        this.sockjs = new SockJS(address, undefined, {});

        this._openPromise = new Promise((resolve, reject) => {
            this.sockjs.onmessage = (ev) => {
                console.log(ev);
                this.receiveMessage(ev);
            };
            this.sockjs.onopen = (ev) => {
                if (this.debugEnabled)
                    console.log('SockJS connection %s opened', this.id, this, ev);
                console.log(this.sockjs.onmessage);
                this.sockjs.onerror = (ev: Event) => {
                    this.lastEvent = ev;
                    this.handleError(ev);
                };
                this.sockjs.onclose = (ev: CloseEvent) => {
                    this.lastEvent = ev;
                    this.doClose(false, true);
                };

                resolve();
            };
            this.sockjs.onerror = (ev: Event) => {
                console.log('SockJS error open error on connection %s', this.id, this, ev);
                this.lastEvent = ev;
                reject(new Error('SockJS error'));
            };
            this.sockjs.onclose = (ev: CloseEvent) => {
                this.lastEvent = ev;
                console.log('SockJS connection closed before opening %s', this.id, this, ev);
            };
        });
    }
    protected postMessage(msg: any, transfers?: Transferable[]) {
        this.sockjs.send(JSON.stringify(msg));
    }
}
