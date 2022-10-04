/// <reference path="./ShellParser_gen.d.ts">
import { parser } from './ShellParser_gen';
import { Tree, TreeCursor, SyntaxNode, SyntaxNodeRef } from '@lezer/common';
import { isEqual, isString } from 'lodash-es';
import { assert } from '../borb/Common';
import { Hole, html } from 'uhtml';
import { styleTags, tags as t } from '@lezer/highlight';
import { InternalError, ParseError } from './Errors';
import { Location } from './Location';
import { URIComponents } from 'uri-js';
import { TShell } from './TShell';
import { setSelectedCompletion } from '@codemirror/autocomplete';

type StringTree = StringTree[] | string;
type AST = string | ASTNode | LeafNode | AST[];
interface LeafNode {}
interface ASTNode {
    toHTML(): HTML;
    expand(shell: TShell): Promise<string>;
    is(s: string): boolean;
}
type HTML = Hole | string | Node | HTML[];

export class StreamRef implements ASTNode {
    public type = 'StreamRef';
    public fileName: string;
    constructor(public file: ASTNode, public mode: string, public fd?: number) {}
    is(s: string): boolean {
        throw new Error('Method not implemented.');
    }
    async evaluate(shell: TShell): Promise<number> {
        if (!this.fileName) this.fileName = await this.file.expand(shell);
        throw new Error('streams not supported yet: ' + this.fileName);
    }
    async expand(shell: TShell): Promise<string> {
        if (!this.fileName) this.fileName = await this.file.expand(shell);
        return `${this.modeSymbol}${this.fileName}`;
    }
    get modeSymbol() {
        switch (this.mode) {
            case 'r':
                return '<';
            case 'w':
                return '>';
            case 'a':
                return '>>';
            case 'p':
                return '|';
            default:
                return '';
        }
    }
    toString() {
        return '';
    }
    toHTML() {
        return html`<span class="stream" mode=${this.mode} fd=${this.fd}>${this.file}</span>}`;
    }
}
export class Variable implements ASTNode {
    public type = 'Variable';
    public data: string;
    constructor(public name: string) {}
    is(s: string): boolean {
        return s === this.data;
    }
    async expand(shell: TShell): Promise<string> {
        return shell.getenv(this.name.slice(1));
    }
    toString() {
        return this.name;
    }
    toHTML() {
        return html`<span class="cmt-variable">${this.name}</span>`;
    }
}
export class String implements ASTNode {
    public readonly type = 'String';
    constructor(public data: string) {}
    is(s: string): boolean {
        return s === this.data;
    }
    toHTML() {
        return new Text(this.data);
    }
    async expand(shell: TShell): Promise<string> {
        return this.data;
    }
}
// export class ListNode implements ASTNode {
//     constructor(public data: ASTNode[]) {}
//     toHTML(): HTML {
//         return html`${this.data}`;
//     }
//     async expand(shell: TShell): Promise<string> {
//         throw new Error('unimplemented');
//     }
// }

export class Word {
    public type = 'Word';
    public parts: ASTNode[];
    public data: string;
    constructor(...parts: ASTNode[]) {
        this.parts = parts;
    }
    is(s: string): boolean {
        return s === this.data || (this.parts.length == 1 && this.parts[0].is(s));
    }
    async expand(shell: TShell): Promise<string> {
        if (this.data === undefined) {
            const stringParts = [];
            for (const p of this.parts) {
                stringParts.push(await p.expand(shell));
            }
            this.data = stringParts.join('');
        }
        return this.data;
    }

    toString() {
        return `“${this.parts.join('')}”`;
    }

    toHTML() {
        console.log(
            'WORD',
            this.parts.map((p) => p.toHTML()),
        );

        return html`<span class="word">${this.parts.map((p) => p.toHTML())}</span>`;
    }
}
export class Redirect {
    public type = 'Redirect';
    constructor(public redir: string, public path: Word) {}
    expand(shell: TShell): Promise<string> {
        throw new Error('Method not implemented.');
    }
    is(s: string): boolean {
        throw new Error('Method not implemented.');
    }
    toString() {
        return `${this.redir}${this.path}`;
    }
    toHTML() {
        return html`<span class="redirect"
            ><span class="cmt-operator redirectop">${this.redir}</span
            ><span class="cmt-link redirectpath">${this.path.toHTML()}</span></span
        >`;
    }
}
export interface Command {
    evaluate(shell: TShell): Promise<number>;
    background: boolean;
    toHTML(): Hole;
}
export class Call implements Command {
    public type = 'Command';
    background: boolean;
    args: Word[] = [];
    constructor(public cmd: Word, public redir: Redirect[]) {}
    is(s: string): boolean {
        throw new Error('Method not implemented.');
    }

    async evaluate(shell: TShell): Promise<number> {
        const command = await this.cmd.expand(shell);
        const args = await Promise.all(this.args.map((arg) => arg.expand(shell)));
        try {
            return await shell.evalCommand(command, args, this);
        } catch (e) {
            if (e instanceof Error) {
                shell.printElement(
                    html.node`<div class="diag diag-error"><p class="diag-header"><span class="exception-message">${e.message}</span></p></div>`,
                );
                return 2;
            }
        }
    }
    toString() {
        const s = [this.cmd.toString()];
        s.push(...this.redir.map((x) => x.toString()));
        s.push(...this.args.map((x) => x.toString()));
        return s.join(' ');
    }
    toHTML() {
        const cmd = html`<span class="commandName">${this.cmd.toHTML()} </span> `;
        const redir = html`${this.redir.map((a) => a.toHTML())}`;

        const args = html`<span class="arglist"
            >${this.args.map((a) => html`<span class="argument">${a.toHTML()} </span>`)}</span
        >`;
        return html`<p class="command">${cmd}${redir}${args}</p>`;
    }
    static isInstance(arg: any) {
        return arg instanceof Call;
    }
}
export class Operator implements Command {
    public type = 'Operator';
    public args: Command[];
    background: boolean;
    constructor(public op: string, ...args: Command[]) {
        this.args = args;
    }
    is(s: string): boolean {
        throw new Error('Method not implemented.');
    }
    expand(shell: TShell): Promise<string> {
        throw new Error('Method not implemented.');
    }
    async evaluate(shell: TShell): Promise<number> {
        if (this.op === '|') {
            throw new Error('Pipes not implemented');
        } else if (this.op === '&&') {
            let ret = await (this.args[0] as Call).evaluate(shell);
            if (ret === 0) ret = await (this.args[1] as Call).evaluate(shell);
            return ret;
        } else if (this.op === '||') {
            let ret = await (this.args[0] as Call).evaluate(shell);
            if (ret !== 0) ret = await (this.args[1] as Call).evaluate(shell);
            return ret;
        } else {
            // find and execute program
        }
    }
    toString() {
        const s = [this.op];
        if (this.op === '|' || this.op === '||' || this.op === '&&')
            s.push(this.args.map((c) => `[${c}]`).join('  ' + this.op + '  '));
        else s.push(this.args.join(' '));
        return s.join(' ');
    }
    toHTML() {
        return html`<p class="command">${this.op}(${this.args.map((c) => c.toHTML())})</p>`;
    }
    static isInstance(arg: any) {
        return arg instanceof Call;
    }
}

const terminals = [
    'Plain',
    'Variable',
    'RedirectOp',
    'Separator',
    'SQuoteChars',
    'DQuoteChar',
    'DQuotedChar',
    'EscapedSQuote',
    'Escape',
    'PipeOp',
    'LogicOp',
];
const injectors = ['CommandWord'];
type _Visitor<T> = {
    readonly [x: string]: (node: SyntaxNodeRef, ...children: T[]) => T;
};
export type Visitor<T> = {
    [m in keyof _Visitor<T> as `visit_${m}`]: _Visitor<T>[m];
};
export interface GenericVisitor<T> extends Visitor<T> {
    visit: (node: SyntaxNodeRef, ...children: T[]) => T;
}
const escapes = { '\\n': '\n', '\\r': '\r', '\\ ': ' ', '\\\\': '\\', '\\"': '"', "\\'": "'" };
export class ShellParser implements GenericVisitor<ASTNode> {
    [m: `visit_${string}`]: (node: SyntaxNodeRef, ...children: ASTNode[]) => ASTNode;
    [m: `topdown_${string}`]: (node: SyntaxNodeRef, ...children: ASTNode[]) => ASTNode;
    static parser = parser;
    public tree: Tree;
    public env?: Map<string, string>;
    public errors: SyntaxNode[] = [];
    public uri: URIComponents;
    constructor(public input: string, uri?: URIComponents) {
        this.tree = ShellParser.parser.parse(input);
        this.uri = uri || { scheme: 'string' };
    }
    pp(cursor: TreeCursor) {
        return this.accept(cursor || this.tree.cursor(), {
            visit: (node: SyntaxNodeRef, ...children: StringTree[]) => {
                const args = terminals.includes(node.name)
                    ? this.textOf(node)
                    : children.flat().join(', ');
                return `${node.name}(${args})`;
            },
        });
    }

    textOf(node: SyntaxNode | SyntaxNodeRef) {
        return this.input.slice(node.from, node.to);
    }
    buildCommands(): Command[] {
        return this.parse_topCommandList(this.tree.topNode);
    }

    parse_Redirect(node: SyntaxNode): Redirect {
        let redir = node.node.firstChild;
        let path = this.parse_Word(redir.nextSibling);

        return new Redirect(this.textOf(redir), path);
    }
    parse_Word(node: SyntaxNode): Word {
        console.log(['Word', node]);
        if (node.name === 'CommandWord') node = node.firstChild;
        const seq = [];
        let i = 0;
        let n = node.firstChild;
        while (n) {
            const s = this.textOf(n);
            switch (n.type.name) {
                case 'Plain':
                    seq.push(new String(s));
                    break;
                case 'Variable':
                    seq.push(new Variable(s));
                    break;
                case 'Escaped':
                    seq.push(new String(escapes['\\' + s] || s));
                    break;
                case 'SQuoted':
                case 'DQuoted':
                default:
                    seq.push(new String(s));
                    break;
            }
            n = n.nextSibling;
        }
        return new Word(...seq);
    }
    parse_topCommandList(node: SyntaxNodeRef): Command[] {
        return this.parse_CommandList(node);
    }
    parse_CommandList(node: SyntaxNodeRef): Command[] {
        const list: Command[] = [];
        let cmd: Command;
        let n = node.node.firstChild;
        while (n) {
            if (n.type.name === 'Separator') {
                const sep = this.textOf(n);
                if (sep === '&' && cmd) {
                    cmd.background = true;
                }
            } else {
                cmd = this.parse_CommandTree(n);
                if (cmd) list.push(cmd);
            }
            n = n.nextSibling;
        }
        return list;
    }
    parse_CommandTree(node: SyntaxNode): Command {
        let n = node.firstChild;
        switch (node.type.name) {
            case 'List':
            case 'Pipe':
                const left = this.parse_CommandTree(n);
                n = n.nextSibling;
                if (n.type.name !== 'LogicOp' && n.type.name !== 'PipeOp')
                    console.error('Unexpected operator', n.type.name, node);
                const op = this.textOf(n);
                n = n.nextSibling;
                const right = this.parse_CommandTree(n);
                return new Operator(op, left, right);
            case 'Command':
                let cmd: Call;
                const redir: Redirect[] = [];
                while (n) {
                    if (n.name === 'Redirect') redir.push(this.parse_Redirect(n));
                    else if (!cmd) cmd = new Call(this.parse_Word(n), redir);
                    else cmd.args.push(this.parse_Word(n));
                    n = n.nextSibling;
                }
                if (!cmd && redir.length > 0) cmd = new Call(new Word(), redir);
                return cmd;
            case 'Subshell':
            case 'If':
                console.error('Not implemented: ', node);
        }
    }
    visit(node: SyntaxNodeRef, ...children: ASTNode[]) {
        console.log('visit', terminals.indexOf(node.name), node.name, node);
        if (terminals.indexOf(node.name) >= 0) return new String(this.textOf(node));
        else if (injectors.indexOf(node.name) >= 0) {
            assert((a, b) => a === b, children.length, 1);
            return children[0];
        } else if (node.type.isError) {
            this.errors.push(node.node);
            throw new ParseError(
                `Parse error`,
                new Location(this.uri, node.from, node.to - node.from),
            );
        } else {
            console.warn(
                'ShellParser unknown syntax node %s "%s":',
                node.name,
                this.textOf(node),
                node,
            );
            throw new ParseError(
                `unknown syntax node ${node.name}`,
                new Location(this.uri, node.from, node.to - node.from),
            );
        }
    }

    accept<T>(cursor: TreeCursor, visitor: GenericVisitor<T>) {
        const stack: T[][] = [];
        const top: T[] = [];
        cursor.iterate(
            (node) => {
                console.log(node.name, node.type.id, node.node);
                const current: T[] = [];
                const name = `topdown_${node.name}`;
                if (typeof visitor[name] === 'function') {
                    const r = visitor[name](node);
                    if (r) {
                        const parent = stack.at(-1) || top;
                        parent.push(r);
                        return false;
                    }
                }
                stack.push(current);
            },
            (node) => {
                const current = stack.pop();
                const name = `visit_${node.name}`;
                const parent = stack.at(-1) || top;
                if (typeof visitor[name] === 'function') {
                    parent.push(visitor[name](node, ...current));
                } else {
                    parent.push(visitor.visit(node, ...current));
                }
            },
        );
        return top[0];
    }
    hasError() {
        return this.errors.length;
    }
}

function tryIt(text: string, env?: Record<string, string>) {
    globalThis.tree = new ShellParser(text);
    if (env) {
        globalThis.tree.env = new Map(Object.entries(env));
    }
    const pp = globalThis.tree.pp();
    console.log(JSON.stringify(pp, null, '  '));
    const s = globalThis.tree.analyze();
    console.log(JSON.stringify(s, null, '  '));
    console.log(s.toString());
    return s;
}
if (import.meta.webpackHot) {
    import.meta.webpackHot.accept(
        './ShellParser_gen.js',
        function (outdated) {
            console.log(outdated);
            ShellParser.parser = parser;
        },
        (err, context) => {
            console.error('HMR failed:', err, context);
        },
    );
    import.meta.webpackHot.accept();
    import.meta.webpackHot.addDisposeHandler((data) => {
        globalThis.ShellParser = ShellParser;
        globalThis.tryIt = tryIt;
    });
    globalThis.tryIt = tryIt;
}
globalThis.ShellParser = ShellParser;
