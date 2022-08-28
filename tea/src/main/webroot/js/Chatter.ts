import { html } from 'uhtml';
import { BaseConnection, Message, Messaging, MessagingError, Payload } from '../borb/Messaging';
import { LangInit, LanguageConnection } from './Language';
import { EvalRequest } from './Shell';

let seq = 0;
export class Chatter extends BaseConnection implements LanguageConnection {
    private _terminal: string;
    private _explorer: string;
    private _printer: { print: (text: string) => Promise<void> };

    constructor() {
        super(Messaging, `chatter:${seq++}`);
    }
    connect(): Promise<string> {
        return Promise.resolve(this.id);
    }

    deliverRemote(msg: Message, transfers: Transferable[] = []): void {
        const m = this[msg.header.msg_type] as (msg: Payload) => Promise<Payload>;
        console.log('got request', msg, transfers, m);
        if (typeof m === 'function') {
            m.bind(this)(msg.content).then((reply) => {
                this.deliverHost(this.router.reply(msg, reply));
            });
            return;
        }
        throw new MessagingError('Method not implemented', msg);
    }
    chat(from: string, text: string) {
        window.setTimeout(
            () =>
                Messaging.send(
                    { elt: html.node`[<span>${from}</span>] <span>${text}</span>` },
                    'printElement',
                    this._terminal,
                ),
            700,
        );
    }
    async eval_request({ content, code, ref }: EvalRequest) {
        switch (Math.floor(Math.random() * 10)) {
            case 0:
                this.chat('Turtleduck', "I'll try to keep that in mind.");
                break;
            case 1:
                this.chat('Turtleduck', "Sorry, I'm sleeping... 💤");
                break;
            case 2:
                this.chat(
                    'Turtleduck',
                    "Sorry, I'm actually not even hatched yet. Please try again later.",
                );
                break;
            default:
                this.chat('Turtleduck', "Why do you say '" + code + "'?");
                break;
        }
        return {
            code,
            ref,
            diag: [],
            complete: true,
            multi: [],
        };
    }

    async langInit({ config, terminal, explorer, session }: LangInit): Promise<{}> {
        this._terminal = terminal;
        this._explorer = explorer;
        this._printer = {
            print: (text: string) =>
                Messaging.send({ text }, 'print', this._terminal).then(() => Promise.resolve()),
        };
        return {};
    }
}
