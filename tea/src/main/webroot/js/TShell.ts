import getopts from 'getopts';
import Settings from '../borb/Settings';
import { Printer, Terminal } from './Terminal';
import type { Options, ParsedOptions } from 'getopts/.';
import Systems from '../borb/SubSystem';
import { turtleduck } from './TurtleDuck';
import { StorageContext, Storage } from './Storage';
import type { HistorySession } from '../borb/LineHistory';
import { LangInit, LanguageConnection, Languages } from './Language';
import { BaseConnection, Message, Messaging, MessagingError, Payload } from '../borb/Messaging';
import { errorResult, EvalReply, EvalRequest, EvalResult, Shell, valueResult } from './Shell';
import { eq } from 'lodash-es';
import { html } from 'uhtml';
import FS from '@isomorphic-git/lightning-fs';
import { Call, Command, ShellParser } from './ShellParser';
import { GenericException } from './Errors';
import { StateCommand } from '@codemirror/state';
import { WorkerConnection } from './WorkerConnection';
import { displayStack } from './Stack';
import { borbName } from '../borb/Common';
import Editor from '../borb/Editor';
//import getopts = require('getopts');
const homeDir = '/home';
const options = {};
type EnvValue = string | ((sh: TShell, val?: string) => string);
let seq = 0;

function createEnvironment(): Map<string, EnvValue> {
    let tmp: { [propName: string]: EnvValue } = {
        SHELL: '/bin/tsh',
        HOME: homeDir,
        LOGNAME: () => Settings.getConfig('user.username', ''),
        USER: () => Settings.getConfig('user.username', ''),
        PWD: (sh: TShell, dir?: string) => {
            if (dir) sh.currentDirectory = dir;
            return sh.currentDirectory;
        },
        SHLVL: '1',
        _: (sh: TShell) => sh.currentCommand,
        '?': (sh: TShell) => String(sh.returnCode),
        PATH: '/home/bin:/bin',
    };
    return new Map(Object.keys(tmp).map((k) => [k, tmp[k]]));
}
const defaultEnvironment = createEnvironment();
type Program = {
    params: Options;
    fun?(
        args: ParsedOptions,
        sh: TShell,
        ctx: StorageContext,
    ): Promise<number> | number | string | void;
};
const programs: Map<string, Program> = new Map();

export class TShell extends BaseConnection implements LanguageConnection {
    private env: Map<string, EnvValue>;
    private _returnCode: number = 0;
    private _programs = programs;
    private _parent?: TShell;
    private _currentCommand?: string;
    private _outputElement?: HTMLElement;
    private _printer?: Printer;
    private _currentArguments?: Array<string>;
    private _cwd: StorageContext;
    history?: HistorySession;
    private _terminal: string;
    private _explorer: string;
    _ref: number;
    _execConnection: WorkerConnection;
    private _config: import('/home/anya/git/turtleduck/tea/src/main/webroot/js/Language').LanguageConfig;
    constructor(cwd?: StorageContext, parent?: TShell) {
        super(Messaging, `tshell:${seq++}`);
        this._parent = parent;
        this.env = createEnvironment();
        if (cwd) this._cwd = cwd;
        else
            Systems.waitFor(Storage).then((st) => {
                this._cwd = st.context();
            });
        defaultEnvironment.forEach((v, k) => {
            if (!this.env.has(k)) {
                this.env.set(k, v);
            }
        });
    }
    async connect(): Promise<string> {
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
    deliverHost: (msg: Message) => Promise<void>;

    async eval_request({ content, code, ref }: EvalRequest): Promise<EvalReply> {
        const res: EvalResult = {
            code,
            diag: [],
        };
        try {
            const value = await this.eval(code, ref);
            const r = valueResult(res, undefined);
            console.log('eval_request: got value', value, r);
            return { ref, status: 'ok', results: [r], complete: true };
        } catch (err) {
            const r = await errorResult(res, err);
            console.log('eval_request: got error', err, r);
            return { ref, status: 'error', results: [r], complete: true };
        }
    }

    async langInit({ config, terminal, explorer, session }: LangInit): Promise<Payload> {
        this._terminal = terminal;
        this._explorer = explorer;
        this._config = config;
        this.setenv('SESSION', session);
        this._printer = {
            print: (text: string) =>
                Messaging.send({ text }, 'print', this._terminal).then(() => Promise.resolve()),
        };
        await this.initExecutor();
        return {};
    }
    get printer(): Printer {
        return this._printer;
    }

    set printer(p: Printer) {
        this._printer = p;
    }
    async printElement(elt: HTMLElement): Promise<void> {
        if (this._terminal) {
            await Messaging.send({ elt }, 'printElement', this._terminal);
        }
    }
    get currentCommand(): string {
        return this._currentCommand || '/bin/tsh';
    }

    get returnCode(): number {
        return this._returnCode;
    }

    set returnCode(code) {
        this._returnCode = Number(code);
    }

    get currentDirectory(): string {
        return this._cwd.cwd;
    }

    set currentDirectory(path: string) {
        const ctx = this._cwd;
        const newCwd = ctx.realpath(path);
        ctx.cwd = newCwd;
    }

    getenv(varname: string): string {
        const value = this.env.get(varname);
        if (typeof value === 'function') {
            return value(this, undefined);
        } else if (value !== undefined) {
            return value;
        } else {
            return '';
        }
    }

    setenv(varname: string, value: any): void {
        const old = this.env.get(varname);
        if (typeof old === 'function') {
            old(this, value);
        } else {
            this.env.set(varname, value);
        }
    }

    parseCommand(line: string): string[][] {
        const commands = line.trim().split(/(;|&&?|\|\|?)/);
        return commands.map((c) => c.trim().split(' '));
    }

    async eval(line: string, ref: number, outputElement?: HTMLElement): Promise<number> {
        console.log('eval', line, outputElement);
        const sp = globalThis.ShellParser;
        const parser: ShellParser = new sp(line);
        const cmds = parser.buildCommands();
        //console.log(JSON.stringify(cmds, null, '    '));
        // const tree = html.node`<span>${cmds.map((c) => c.toHTML())}</span>`;
        // console.log(tree);
        // if (outputElement) {
        //     outputElement.appendChild(tree);
        // } else {
        //     await Messaging.send({ elt: tree }, 'printElement', this._terminal);
        // }

        const oldOutput = this._outputElement;
        const oldPrinter = this._printer;

        if (outputElement) {
            this._outputElement = outputElement;
            this._printer = Terminal.elementPrinter(outputElement, 'shell');
            console.log(this._printer);
        }
        try {
            this._ref = ref;
            while (cmds.length > 0) {
                const cmd = cmds.shift();
                console.log(cmd);
                this._returnCode = await cmd.evaluate(this);
            }
        } finally {
            // reached the end of all steps
            this._ref = 0;
            this._outputElement = oldOutput;
            this._printer = oldPrinter;
        }
        return this._returnCode;
    }

    async evalCommand(cmdName: string, args: string[], cmd: Command): Promise<number> {
        console.log('eval command: ', this, cmdName, ...args);
        const prog = this.findProgram(cmdName);
        if (prog) {
            const pArgs = prog.params ? getopts(args, prog.params) : { _: args };
            pArgs.__shell__ = this;
            pArgs.__command__ = cmd;
            pArgs.__background__ = cmd.background;
            const prevCommand = this._currentCommand;
            const prevArguments = this._currentArguments;
            this._currentCommand = cmdName;
            this._currentArguments = args;
            try {
                if (prog.fun) {
                    const ret = await prog.fun(pArgs, this, this._cwd);
                    this._returnCode = Number(ret) || 0;
                    if (typeof ret === 'string') {
                        this.println('Returned string: ', ret);
                        this._returnCode = 0;
                    }
                } else {
                    throw new Error(`Command not found: ${cmdName}`);
                }
            } catch (e) {
                this._returnCode = 255;
                throw e;
            } finally {
                this._currentCommand = prevCommand;
                this._currentArguments = prevArguments;
            }
            return Promise.resolve(this._returnCode);
        } else {
            this._returnCode = 255;
            throw new Error(`Command not found: ${cmdName}`);
        }
    }

    findProgram(prog: string): Program {
        return programs.get(prog) || { fun: undefined, params: null };
    }

    get context() {
        return this._cwd;
    }

    println(...args: string[]): void {
        const text = args.map((a) => String(a)).join(' ');
        if (this._printer) {
            this._printer.print(text + '\n');
        } else {
            console.log(text);
        }
    }

    print(...args: string[]): void {
        const text = args.map((a) => String(a)).join(' ');
        if (this._printer) {
            this._printer.print(text);
        } else {
            console.log(text);
        }
    }

    async initExecutor() {
        const evalId = `${this.id}_worker`;
        Messaging.route(`shellworker_status`, (msg: { wait?: boolean; status?: string }) => {
            const wait = !!msg.wait;
            if (typeof msg.status === 'string') {
                console.log(msg);
                turtleduck.userlog(msg.status, wait);
                this.println(msg.status);
            }
            return Promise.resolve({});
        });
        Messaging.route(`shellworker_error`, (msg: { status?: string }) => {
            if (typeof msg.status === 'string') {
                console.error(msg);
                turtleduck.userlog(msg.status);
                this.println(msg.status);
            }
            return Promise.resolve({});
        });
        this._execConnection = new WorkerConnection(
            Messaging,
            evalId,
            'shellworker.js',
            false,
            'shellworker',
        );
        await this._execConnection.connect();

        await Messaging.send(
            {
                config: this._config,
                terminal: this._terminal,
                explorer: this._explorer,
                session: this.getenv('SESSION'),
            },
            'langInit',
            this._execConnection.id,
        );

        const files = await this._cwd.readdir();
        for (let file of files) {
            const data = await this._cwd.readbinfile(file);
            console.log('uploading to executor:', file);
            try {
                await Messaging.send(
                    {
                        filename: file,
                        data,
                    },
                    'upload',
                    this._execConnection.id,
                );
                console.log('uploading ok:', file);
            } catch (e) {
                console.error(e);
            }
        }
    }
}
const inode_types = {
    file: '-',
    dir: 'd',
    symlink: 'l',
};
const modes = ['---', '--x', '-w-', '-wx', 'r--', 'r-x', 'rw-', 'rwx'];
export function fileModes(stat: { mode: number; type: 'file' | 'dir' | 'symlink' }) {
    let result = inode_types[stat.type];
    result += '-r'[(stat.mode >>> 8) & 1];
    result += '-w'[(stat.mode >>> 7) & 1];
    result += '-xSs'[((stat.mode >>> 6) & 1) | ((stat.mode >>> 10) & 2)];
    result += '-r'[(stat.mode >>> 5) & 1];
    result += '-w'[(stat.mode >>> 4) & 1];
    result += '-xSs'[((stat.mode >>> 3) & 1) | ((stat.mode >>> 9) & 2)];
    result += '-r'[(stat.mode >>> 2) & 1];
    result += '-w'[(stat.mode >>> 1) & 1];
    result += '-xTt'[((stat.mode >>> 0) & 1) | ((stat.mode >>> 8) & 2)];
    return result;
}
programs.set('ls', {
    params: {
        boolean: ['all', 'long', 'time'],
        alias: { all: ['a'], long: ['l'], time: '' },
    },
    fun: async (args, sh, ctx) => {
        const arg = args['_'][0];
        function toLink(file: string) {
            const path = ctx.resolve(arg, file);
            return `fs://${path}`;
        }
        function clickAction(ev: MouseEvent) {
            console.log('File open:', ev.currentTarget);
            ev.preventDefault();
        }
        let res = await ctx.readdir(args['_'][0]);
        if (!args['all']) res = res.filter((entry) => !entry.startsWith('.'));
        if (args['long']) {
            const entries = res.map((entry) => ctx.resolve(arg, entry));
            const stats = [];
            function stat(idx: number): Promise<void> {
                if (idx >= entries.length) {
                    return Promise.resolve();
                } else {
                    return ctx.stat(entries[idx]).then((s) => {
                        stats[idx] = s;
                        return stat(idx + 1);
                    });
                }
            }
            await stat(0);
            console.log(stats);
            const user = sh.getenv('USER');
            sh.printElement(
                html.node`<table class="file-list">${
                    stats.map(
                        (stat, idx) =>
                            html`<tr
                                ><td class="file-mode">${fileModes(stat)}</td
                                ><td class="file-user">${user}</td
                                ><td class="file-group">${user}</td
                                ><td class="file-size">${stat.size}</td
                                ><td class="file-date"
                                    >${new Date(stat.mtimeMs).toLocaleString()}</td
                                ><td class="file-name"
                                    ><a onclick=${clickAction} href=${toLink(res[idx])}
                                        >${res[idx]}${stat.type === 'dir' ? '/' : ''}</a
                                    ></td
                                ></tr
                            >`,
                    )
                    // sh.println(
                    //     `${inode_types[stat.type] || ' '}${
                    //         modes[(stat.mode >> 6) & 7]
                    //     }${modes[(stat.mode >> 3) & 7]}${modes[stat.mode & 7]}`,
                    //     `${stat.size}`,
                    //     `${new Date(stat.mtimeMs).toLocaleString()}`,
                    //     res[idx],
                    // );
                }</table>`,
            );
        } else {
            formatColumns(res, sh);
        }
        return 0;
    },
});
function formatColumns(entries: string[], sh: TShell) {
    const longest = entries.reduce((prev, cur) => Math.max(prev, cur.length + 1), 0);
    const perLine = Math.floor(80 / (longest || 1));
    const len = 80 / perLine;
    let newline = true;
    console.log('ls formatting: longest=%d, perLine=%d, len=%d', longest, perLine, len);
    entries.forEach((entry, idx) => {
        sh.print(entry.padEnd(len));
        if (!newline && idx % perLine === 0) {
            sh.println();
            newline = true;
        } else {
            newline = false;
        }
    });
    if (!newline) sh.println();
}
programs.set('show_mode', {
    params: {},
    fun: (args, sh, ctx) =>
        sh.println(fileModes({ mode: parseInt(args['_'][0], 8), type: 'file' })),
});
programs.set('echo', {
    params: { boolean: ['n', 'e', 'E'] },
    fun: (args, sh, ctx) => sh.println(args['_'].join(' ')),
});
programs.set('pwd', {
    params: {},
    fun: (args, sh, ctx) => sh.println(ctx.cwd),
});
programs.set('cd', {
    params: { boolean: ['L', 'P', 'e', '@'] },
    fun: async (args, sh, ctx) => {
        const oldwd = ctx.cwd;
        await ctx.chdir(args['_'][0]);
        sh.setenv('OLDPWD', oldwd);
        return 0;
    },
});
programs.set('mkdir', {
    params: { boolean: ['p'] },
    fun: (args, sh, ctx) => ctx.mkdir(args['_'][0]).then(() => 0),
});
programs.set('true', {
    params: {},
    fun: () => 0,
});
programs.set('false', {
    params: {},
    fun: () => 1,
});
programs.set('history', {
    params: {},
    fun: async (args, sh, ctx) => {
        if (sh.history) {
            const entries = await sh.history.list();
            const l = Math.max(...entries.map((e) => e.id)).toString().length;
            entries.forEach((e) => sh.println(`[${e.id.toString().padStart(l)}] ${e.data}`));
            return 0;
        } else {
            return 1;
        }
    },
});
programs.set('help', {
    params: {},
    fun: async (args, sh, ctx) => {
        formatColumns([...programs.keys()].sort(), sh);
        return 0;
    },
});
function languageLoader(
    languageName: string,
): (args: getopts.ParsedOptions, sh: TShell, ctx: StorageContext) => Promise<number> {
    return async (args, sh, ctx) => {
        let lang = Languages.get(languageName);
        console.log(lang, lang.isLoaded);
        if (!lang.isLoaded) {
            await lang.load(undefined, args['__background__']);
            globalThis[languageName] = lang;
        }
        console.log(lang);
        const term = lang.mainTerminal;
        console.log(term);
        if (term && !args['__background__']) term.select();
        return 0;
    };
}
programs.set('chat', {
    params: {},
    fun: languageLoader('chat'),
});
programs.set('java', {
    params: {},
    fun: languageLoader('java'),
});
programs.set('python', {
    params: {},
    fun: languageLoader('python'),
});
programs.set('open', {
    params: { boolean: ['c'] },
    fun: async (args, sh, ctx) => {
        //        if (!editor) throw new Error('editor unavailable');
        if (!args['_'][0]) {
            sh.println('usage: open [-c] file1...fileN');
        }
        for (let arg of args['_']) {
            if (args['c']) {
                await Editor.openFile(arg).catch((e) => Editor.openText('', arg));
            } else {
                await Editor.openFile(arg);
            }
        }
        return 0;
    },
});
programs.set('frames', {
    params: {},
    fun: async (args, sh, ctx) => {
        const arg = args['_'][0];
        if (!arg) {
            sh.println('usage: frames FILENAME');
            return 1;
        }
        const ref = sh._ref;
        let py = Languages.get('python');
        console.log(py, py.isLoaded);
        if (!py.isLoaded) {
            sh.println('loading Python...');
            await py.load(undefined, true);
        }
        const path = ctx.realpath(arg);
        turtleduck.userlog('loading ELF file', true);
        try {
            const result = await py.mainShell.eval({
                code: 'from turtleduck import stack_frames\nwith open(__filename, "rb") as __f:\n    __r = stack_frames.process_stream(__f,__filename)\n__r',
                opts: { localVars: { __filename: path }, raw: true },
                ref,
            });
            turtleduck.userlog('preparing display...');

            const ds = displayStack(result).display();
            const panel = (globalThis.stackFramePanel = turtleduck
                .createPanel()
                .frame('screen')
                .panel('div', 'tshell_stackFramePanel')
                .title('Stack Frame Layout')
                .select()
                .done());
            panel.replaceChildren(ds);
            return 0;
        } finally {
            turtleduck.userlog('ok');
        }
    },
});
function external(cmd: string) {
    programs.set(cmd, {
        params: null,
        fun: async (args, sh, ctx) => execve(cmd, args, sh, ctx),
    });
}
['addr2line', 'bfdtest1', 'bfdtest2', 'elfedit', 'nm-new', 'objdump', 'size', 'strings'].forEach(
    (cmd) => external(cmd),
);
async function execve(
    command: string,
    args: getopts.ParsedOptions,
    sh: TShell,
    ctx: StorageContext,
) {
    const reply = (await Messaging.send(
        {
            command,
            args: args['_'],
        },
        'execve',
        sh._execConnection.id,
    )) as unknown as EvalReply;
    return (reply.value as number) || 0;
}

programs.set('upload', {
    params: {},
    fun: async (args, sh, ctx) => {
        const input = document.getElementById('file-input') as HTMLInputElement;
        if (input) {
            input.onchange = async (ev) => {
                input.onchange = null;
                const files = input.files || ([] as File[]);
                for (const file of files) {
                    console.log(file);
                    sh.println(`received file '${file.name}' (${file.size} bytes)`);
                    const data = new Uint8Array(await file.arrayBuffer());
                    console.log(data);
                    await ctx.writebinfile(file.name, data);
                    sh.println(`saved file '${file.name}' (${file.size} bytes)`);
                    try {
                        await Messaging.send(
                            {
                                filename: file.name,
                                data,
                            },
                            'upload',
                            sh._execConnection.id,
                        );
                    } catch (e) {
                        console.error(e);
                    }
                }
            };
            input.click();
            return 0;
        } else {
            return 1;
        }
    },
});

// TODO: load language
// TODO: open in editor
// TODO: router info, router echo
// TODO: send message
// TODO: file IO
// TODO: slash commands in language shells
// TODO:
