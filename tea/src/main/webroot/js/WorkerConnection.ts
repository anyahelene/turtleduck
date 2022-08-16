import { Settings } from '../borb';
import { Messaging, BaseConnection, Message } from '../borb/Messaging';
import { WorkerController } from './WorkerController';

export class WorkerConnection extends BaseConnection {
    _connected = '';
    private _onterminate: () => void;

    constructor(
        id: string,
        router: typeof Messaging,
        public worker: WorkerController,
    ) {
        super(router, id);
        worker.onmessage((msg) => {
            msg = Messaging.fromMap(msg);
            console.log(
                'received from worker:',
                JSON.stringify(msg.header),
                msg.content,
            );
            return this.deliverHost(msg);
        });
        worker.onterminate(() => {
            console.warn('worker terminated');
            if (this._onterminate) this._onterminate();
        });
        worker.onerror((ev) => console.error('received error from worker', ev));
    }
    deliverRemote(msg: Message, transfers: Transferable[] = []): void {
        this.worker.postMessage(msg, transfers);
    }
    onterminate(handler: () => void) {
        this._onterminate = handler;
    }
    async connect(): Promise<string> {
        console.info('connected. sending hello', this);
        if (this._connected) {
            return this.id;
        }
        this._connected = 'connecting';
        const reply = await this.router.send(
            {
                sessionName: Settings.getConfig('session.name', 'unknown'),
                endpoints: {},
            },
            'hello',
            this.id,
        );
        console.info('Received welcome', reply);
        this.connectHandlers.forEach((handler) => handler(this, reply));
        this._connected = 'connected';
        return this.id;
    }
}
