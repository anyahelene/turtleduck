import { Messaging } from '../borb/Messaging';
import { MessagingConnection } from './MessagingConnection';
import { LanguageConnection } from './Language';
export class WebSocketConnection extends MessagingConnection implements LanguageConnection {
    private websocket: WebSocket;
    public lastEvent: any;
    constructor(router: typeof Messaging, id: string, public address: string, name?: string) {
        super(router, id, name || address);
        console.log('Opening WebSocket connection to', address);
        this.websocket = new WebSocket(address);

        this._openPromise = new Promise((resolve, reject) => {
            this.websocket.onmessage = (ev) => {
                console.log(ev);
                this.receiveMessage(ev);
            };
            this.websocket.onopen = (ev) => {
                if (this.debugEnabled)
                    console.log('WebSocket connection %s opened', this.id, this, ev);
                console.log(this.websocket.onmessage);
                this.websocket.onerror = (ev: Event) => {
                    this.lastEvent = ev;
                    this.handleError(ev);
                };
                this.websocket.onclose = (ev: CloseEvent) => {
                    this.lastEvent = ev;
                    this.doClose(false, true);
                };

                resolve();
            };
            this.websocket.onerror = (ev: Event) => {
                console.log('WebSocket error open error on connection %s', this.id, this, ev);
                this.lastEvent = ev;
                reject(new Error('WebSocket error'));
            };
            this.websocket.onclose = (ev: CloseEvent) => {
                this.lastEvent = ev;
                console.log('WebSocket connection closed before opening %s', this.id, this, ev);
            };
        });
    }
    protected postMessage(msg: any, transfers?: Transferable[]) {
        this.websocket.send(JSON.stringify(msg));
    }
}
