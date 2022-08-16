import { html } from 'uhtml';
import { BorbTerminal, DisplayData } from '../borb/Terminal';
import {
    Messaging,
    BaseConnection,
    Payload,
    Method,
    Message,
    Connection,
} from '../borb/Messaging';
import { Language } from './Language';
import Settings from '../borb/Settings';
import { LineEditor } from '../borb/LineEditor';
import { History, turtleduck } from './TurtleDuck';
import { uniqueId } from '../borb/Common';
import { Line } from '@codemirror/state';

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
    loc?: string;
    /**
     * Reply:
     */
    value?: unknown;
    prompt?: string;
    /**
     * Request: Additional options to the evaluator
     */
    opts?: Payload;

    display?: DisplayData;
    /**
     * Reply: The kind of snippet that was evaluated.
     *
     * One of error, expression, import, method, statement, type or var; with
     * optional subtypes provided after a dot. (E.g., <code>var.decl.init</code>)
     */
    snipkind?: string;
    /**
     * Reply: Identifier for an evaluated snippet; this will match the id sent in
     * any Explorer updates
     */
    snipid?: string;
    snipns?: string;
    doc?: string;
    /**
     * Request/Reply: A numeric reference provided by the caller
     */
    ref: number;
    /**
     * Reply: True if evaluation involved executing code (i.e., not just declaring
     * something)
     */
    exec?: boolean;
    /**
     * Reply: True if source code is complete, false if more input is needed
     */
    complete: boolean; // = true
    /**
     * Reply: True if evaluation involved defining/declaring something
     *
     * @see #SYMBOL
     */
    def?: boolean;
    persistent?: boolean;
    active?: boolean;

    /**
     * Reply: The symbol that was (re)declared/defined, if any.
     *
     * @see #DEF
     */
    name?: string;
    signature?: string;
    names?: string[];
    docs?: string[];
    /**
     * Reply: The full name of symbol that was (re)declared/defined, if any.
     *
     * Includes type and parameters
     *
     * @see #DEF
     */
    fullname?: string;
    /**
     * Reply: The type of the result, if any.
     *
     * @see #VALUE
     */
    type?: string;

    /**
     * Icon for the result type, if any
     */
    icon?: string;
    /**
     * Reply: An array of multiple eval replies, if the input code was split into
     * multiple snippets
     */
    multi: EvalReply[];
    /**
     * Reply: An array of error/diagnostic messages.
     *
     * Message fields include "msg", "start", "end", "pos"
     */
    diag: Diag[];
    /**
     * Reply: An exception, if one was thrown.
     *
     * Includes "exception" (exception class name), "message" (the message), "trace"
     * (array with stack trace), and optional "cause" (another exception)
     */
    exception?: Exception;
    text?: string;
    heapUse?: number;
    heapTotal?: number;
    heapMax?: number;
    cpuTime?: number; // = 0.0

    sym?: string;
    verb?: string;
    info?: Payload;
}
interface Diag {
    msg: string;
    start: number;
    end: number;
    pos: number;
}
interface Exception {
    exception: string;
    ename: string;
    evalue: string;
    traceback: string[];
    cause: Exception;
}
interface ShellService {
    eval(msg: EvalRequest): Promise<EvalReply>;
    refresh(msg: Pick<ShellMessage, 'info'>): Promise<{}>;
}
interface ExplorerService {
    update(msg: { info: UpdateRequest }): Promise<{}>;
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
export type EvalRequest = Pick<ShellMessage, 'code' | 'ref' | 'opts'>;
export type EvalReply = Pick<
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
    | 'complete'
    | 'name'
    | 'type'
    | 'display'
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
        public shell: Shell,
        id: string,
        exId: string,
    ) {
        super(router, id, exId);
    }

    deliverRemote(msg: Message, transfers: Transferable[]): void {
        const m = this.shell[msg.header.msg_type] as (
            msg: Payload,
        ) => Promise<Payload>;
        console.log('got request', msg, transfers, m);
        if (typeof m === 'function') {
            m(msg.content).then((reply) => {
                this.deliverHost(this.router.reply(msg, reply));
            });
        }
    }
}
export class Shell implements ShellService, TerminalService, ExplorerService {
    id: string;
    terminal: BorbTerminal;
    language: Language;
    conn: Connection;
    languageName: string;
    shellName: string;
    history: History;
    lineEditor: LineEditor;

    constructor(language: Language) {
        this.language = language;
    }

    async init(language: Language, id: string) {
        if (!language || !id) throw new Error('Illegal arguments');
        this.id = `${id}_shell`;
        if (!this.terminal) this.terminal = new BorbTerminal();
        this.terminal.setAttribute('shell', language.shellName);
        this.terminal.setAttribute('frame-title', language.shellTitle);
        this.terminal.setAttribute('language', language.name);
        this.terminal.setAttribute('icon', language.icon);
        this.languageName = language.name;
        this.shellName = language.name;
        if (!this.terminal.id) this.terminal.id = `${id}_terminal`;
        this.conn = new ShellConnection(
            Messaging,
            this,
            this.id,
            `${id}_explorer`,
        );
        this.lineEditor = new LineEditor(this.terminal, this.language);
        this.lineEditor.onEnter = this.onEnter;
        console.log('waiting for line editor...');
        return Promise.resolve();
    }
    mountTerminal(parent?: HTMLElement) {
        if (parent) {
            parent.appendChild(this.terminal);
            return;
        }
        const defaultFrame = document.getElementById(
            Settings.getConfig('terminal.defaultFrame', 'terminal'),
        );
        if (defaultFrame) {
            defaultFrame.appendChild(this.terminal);
            return;
        }
        const existingTerminal = document.querySelector(
            'borb-terminal',
        ) as HTMLElement;
        if (existingTerminal) {
            existingTerminal.insertAdjacentElement('afterend', this.terminal);
            return;
        }
    }
    onEnter = async (
        line: string,
        lineId: number,
        outputElement: HTMLElement,
        lineEditor: LineEditor,
    ) => {
        const payload = { code: line, ref: `${lineId}`, opts: {} };
        const result = await Messaging.send(
            payload,
            'eval_request',
            this.language.connectionId,
        );
        if (processResult(result as EvalReply, this.terminal, false)) return {};
        else return { more: line };
    };
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
    eval = async ({ code, ref, opts }: EvalRequest): Promise<EvalReply> => {
        throw new Error('Method eval_request not implemented.');
    };
    refresh = async ({ info }: Pick<ShellMessage, 'info'>): Promise<{}> => {
        throw new Error('Method refresh not implemented.');
    };

    update = async ({ info }): Promise<{}> => {
        try {
            const {
                persistent,
                snipid,
                snipns,
                sym,
                verb,
                type,
                signature,
                category,
            } = info;
            // if (persistent !== undefined && !persistent) return;
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
    };

    terminalTransition() {
        const elt = html.node`${this.language.icon}`;
        elt.animate(
            [
                { transform: 'scale(0)', opacity: '0%' },
                { transform: 'scale(8)', opacity: '100%' },
            ],
            { duration: 3000, iterations: 1 },
        );
    }
}

function printExceptions(msg: EvalReply) {
    const ex = msg.exception;
    if (ex) {
        const ename = ex.ename || 'Error';
        const evalue = ex.evalue || '';
        const trace = ex.traceback || [];
        const tb = html`<details class="traceback fold">${trace.map((s, i) =>
            i == 0 ? html`<summary>${s}</summary>` : html`<li>${s}</li>`,
        )}</details`;
        return html`<div class="diag diag-error">
            <p class="diag-header">
                <span class="exception-name">${ename}</span>:
                <span class="exception-message">${evalue}</span>
            </p>
            ${tb}
        </div>`;
    } else {
        return '';
    }
}
function printDiags(msg: EvalReply) {
    /*
    public Array printDiags(Dict msg, LanguageConsole console) {
        Array diags = msg.get(ShellService.DIAG);
        String worstLevel = "none";
        HTMLElement output = console.outputElement();
        for (Dict diag : diags.toListOf(Dict.class)) {
            String name = diag.get(Reply.ENAME);
            String level = levelOf(name);
            worstLevel = worstOf(worstLevel, level);
            Location loc = Location.fromString(diag.get(ShellService.LOC));
            if (output != null) {
                String code = msg.get(ShellService.CODE);
                HTMLElement codefrag = null;
                if (code != null) {
                    String before = loc.before(code).replaceAll("(?m)^.*\n", "");
                    String after = loc.after(code).replaceAll("(?m)\n.*$", "");

                    if (loc.length() > 0)
                        codefrag = element("p", clazz("diag-code"), span(before),
                                span(loc.substring(code), clazz("diag-" + level)), span(after));
                    else
                        codefrag = element("p", clazz("diag-code"), span(before),
                                span(clazz("diag-between diag-" + level)), span(after));
                }
                HTMLElement elt = div(clazz("diag diag-" + levelOf(name)), //
                        element("p", clazz("diag-header"), name + " at ", //
                                element("a", loc.toString(), attr("href", loc.path()), attr("target", "_blank")), ":"),
                        codefrag, //
                        element("p", clazz("diag-message"), diag.get(Reply.EVALUE)));

                output.appendChild(elt);
            } else {
                console.println(name + " at " + loc, colorOf(levelOf(name)));
                console.println(diag.get(Reply.EVALUE), colorOf(levelOf(name)));
            }
            if (console.hasDiagnostics()) {
                try {
                    URI uri = new URI(diag.get(ShellService.LOC));
                    Location l = new Location(uri);
                    diag.put("level", level);
                    console.diagnostic(diag, l);

                    // addAnno(state, l.start(), l.length(), "error",
                    // diag.get(Reply.ENAME) + ": " + diag.get(Reply.EVALUE));
                } catch (URISyntaxException ex) {
                    Browser.addError(ex);
                }
            }
        }
        if (output != null) {
            output.setClassName(output.getClassName() + " with-diag with-diag-" + worstLevel);
        }
        return diags;
    }

*/
}
function processResult(
    msg: EvalReply,
    terminal: BorbTerminal,
    processIncomplete = false,
) {
    if (msg.complete || processIncomplete) {
        (msg.multi || [msg]).forEach((result, index) => {
            const diags = printDiags(result);
            const ex = printExceptions(result);
            const value = result.value;
            const name = result.name
                ? html`<span class="cmt-variableName">${result.name}</span> = `
                : '';
            const type = result.type
                ? html`<span class="cmt-typeName">${result.type} </span>`
                : '';
            const display = result.display;
            console.log('result', value, name, type);
            if (value) {
                terminal.printElement(
                    html.node`${diags}${ex}<span class="eval-result">${type}${name}<span class="cmt-literal">${result.value}</span>`,
                );
            }
        });
        // TODO turtleduck.showHeap(msg);
        turtleduck.userlog('Eval: done');
        return true;
    } else {
        turtleduck.userlog('Eval: incomplete input');
        terminal.printElement(html.node`<span class="diag-error">…</span>`);
        return false;
    }
}
