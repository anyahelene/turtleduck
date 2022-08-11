import { html } from 'uhtml';
import { BorbTerminal, DisplayData } from '../borb/Terminal';
import {
    Messaging,
    BaseConnection,
    Payload,
    Method,
    Message,
} from '../borb/Messaging';

interface ShellMessage {
    category: any;
    /**
     * Request/(Reply): Code to be executed. In a reply, this is the code that was
     * actually executed, which may be different (e.g, minor syntax corrections,
     * such as missing semicolon)
     */
    code: string;
    /**
     * Request/Reply: The code location
     */
    loc: string;
    /**
     * Reply:
     */
    value: string;
    prompt: string;
    /**
     * Request: Additional options to the evaluator
     */
    opts: Payload;

    /**
     * Reply: The kind of snippet that was evaluated.
     *
     * One of error, expression, import, method, statement, type or var; with
     * optional subtypes provided after a dot. (E.g., <code>var.decl.init</code>)
     */
    snipkind: string;
    /**
     * Reply: Identifier for an evaluated snippet; this will match the id sent in
     * any Explorer updates
     */
    snipid: string;
    snipns: string;
    doc: string;
    /**
     * Request/Reply: A numeric reference provided by the caller
     */
    ref: number;
    /**
     * Reply: True if evaluation involved executing code (i.e., not just declaring
     * something)
     */
    exec: boolean;
    /**
     * Reply: True if source code is complete, false if more input is needed
     */
    complete: boolean; // = true
    /**
     * Reply: True if evaluation involved defining/declaring something
     *
     * @see #SYMBOL
     */
    def: boolean;
    persistent: boolean;
    active: boolean;

    /**
     * Reply: The symbol that was (re)declared/defined, if any.
     *
     * @see #DEF
     */
    name: string;
    signature: string;
    names: string[];
    docs: string[];
    /**
     * Reply: The full name of symbol that was (re)declared/defined, if any.
     *
     * Includes type and parameters
     *
     * @see #DEF
     */
    fullname: string;
    /**
     * Reply: The type of the result, if any.
     *
     * @see #VALUE
     */
    type: string;

    /**
     * Icon for the result type, if any
     */
    icon: string;
    /**
     * Reply: An array of multiple eval replies, if the input code was split into
     * multiple snippets
     */
    multi: Payload[];
    /**
     * Reply: An array of error/diagnostic messages.
     *
     * Message fields include "msg", "start", "end", "pos"
     */
    diag: Payload[];
    /**
     * Reply: An exception, if one was thrown.
     *
     * Includes "exception" (exception class name), "message" (the message), "trace"
     * (array with stack trace), and optional "cause" (another exception)
     */
    exception: Payload;
    text: string;
    heapUse: number;
    heapTotal: number;
    heapMax: number;
    cpuTime: number; // = 0.0

    sym: string;
    verb: string;
    info: Payload;
}

interface ShellService {
    eval(msg: EvalRequest): Promise<EvalReply>;
    refresh(msg: Pick<ShellMessage, 'info'>): Promise<{}>;
}
interface ExplorerService {
    update(msg: UpdateRequest): Promise<{}>;
}

interface TerminalService {
    prompt(msg: { prompt: string; language: string }): Promise<{}>;

    print: Method<{ text: string; stream: string }>;

    display: Method<{ data: DisplayData; stream: string }>;

    read_request: Method<
        {
            prompt: string;
            language?: string;
        },
        { text: string }
    >;
}
/*
interface TerminalService {
    prompt(msg: { prompt: string; language: string }): Promise<{}>;

    print(msg: { text: string; stream: string }): Promise<{}>;

    display(msg: { data: DisplayData; stream: string }): Promise<{}>;

    read_request(msg: {
        prompt: string;
        language?: string;
    }): Promise<{ text: string }>;
}
*/
type EvalRequest = Pick<ShellMessage, 'code' | 'ref' | 'opts'>;
type EvalReply = Pick<
    ShellMessage,
    | 'ref'
    | 'value'
    | 'snipkind'
    | 'snipid'
    | 'code'
    | 'def'
    | 'sym'
    | 'multi'
    | 'diag'
    | 'exception'
>;
type UpdateRequest = Pick<
    ShellMessage,
    | 'snipkind'
    | 'snipid'
    | 'snipns'
    | 'verb'
    | 'category'
    | 'sym'
    | 'signature'
    | 'persistent'
    | 'type'
>;
export class ShellConnection extends BaseConnection {
    constructor(
        router: typeof Messaging,
        public shell: Shell | ChangeReporter,
    ) {
        super(router, shell.terminal.id);
    }

    deliverRemote(msg: Message, transfers: Transferable[]): void {
        const m = this.shell[msg.header.msg_type] as (
            msg: Payload,
        ) => Promise<Payload>;
        if (typeof m === 'function') {
            m(msg.content).then((reply) => {
                this.deliverHost(this.router.reply(msg, reply));
            });
        }
    }
}
export class Shell implements ShellService, TerminalService {
    constructor(public terminal: BorbTerminal) {}
    async prompt(msg: { prompt: string; language: string }): Promise<{}> {
        console.error('Method not implemented.');
        return {};
    }
    print: Method<{ text: string; stream: string }, {}> = async ({
        text,
        stream,
    }) => {
        this.terminal.print(text);
        return {};
    };
    display: Method<{ data: DisplayData; stream: string }, {}> = async ({
        data,
        stream,
    }) => {
        this.terminal.display(data);
        return {};
    };
    read_request: Method<
        { prompt: string; language?: string },
        { text: string }
    > = async ({ prompt, language = '' }) => {
        return { text: 'not implemented' };
    };
    async eval({ code, ref, opts }: EvalRequest): Promise<EvalReply> {
        throw new Error('Method not implemented.');
    }
    async refresh({ info }: Pick<ShellMessage, 'info'>): Promise<{}> {
        throw new Error('Method not implemented.');
    }
}
export class ChangeReporter implements ExplorerService {
    constructor(public terminal: BorbTerminal) {}
    async update({
        persistent,
        snipid,
        snipns,
        sym,
        verb,
        type,
        signature,
        category,
    }: UpdateRequest): Promise<{}> {
        try {
            if (!persistent) return;
            const sig = signature;
            if (sig && !sig.includes('$')) {
                if ((snipns || 'main') === 'main') {
                    const s = `${sym} ${verb}`;
                    const div = html.node`<div><a class="prompt">[${snipid}]</a> ${sym} ${verb} <span class="cmt-keyword">${category}</span> <span class="cmt-variableName" data-snipid=${snipid}>${sig}</span> ${
                        type
                            ? html`: <span class="cmt-typeName">${type}</span>`
                            : ''
                    }</div>`;
                    this.terminal.printElement(div);
                }
            }
        } catch (e) {}
        return {};
    }
}
