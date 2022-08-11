import { Settings } from '../borb';
import {
    Messaging,
    BaseConnection,
    Payload,
    Method,
    Message,
} from '../borb/Messaging';
import { BorbTerminal } from '../borb/Terminal';
import { WorkerController } from './WorkerController';

export class WorkerConnection extends BaseConnection {
    constructor(
        id: string,
        router: typeof Messaging,
        public terminal: BorbTerminal,
        public worker: WorkerController,
    ) {
        super(router, id);
        worker.onmessage((msg) => {
            console.log('received from worker:', msg);
            return this.deliverHost(msg);
        });
        worker.onerror((ev) => console.error('received error from worker', ev));
    }
    deliverRemote(msg: Message, transfers: Transferable[] = []): void {
        this.worker.postMessage(msg, transfers);
    }

    connect(): void {
        console.info('connected. sending hello', this);
        this.router
            .send(
                {
                    sessionName: Settings.getConfig('session.name', 'unknown'),
                    endpoints: {},
                },
                'hello',
                this.id,
            )
            .then((reply) => {
                console.info('Received welcome', reply);
                this.connectHandlers.forEach((handler) => handler(this, reply));
            });
    }
}
