
import  getopts from 'getopts';
import Settings from './Settings';
import { Printer, Terminal } from './Terminal';
import type { Options, ParsedOptions } from 'getopts/.';
import SubSystem from './SubSystem';
import { turtleduck } from './TurtleDuck';
import { StorageContext } from './Storage';
//import getopts = require('getopts');
const homeDir = '/home';
const options = {};
type EnvValue = string | ((sh: TShell, val?: string) => string);

function createEnvironment(): Map<string, EnvValue> {
    let tmp: { [propName: string]: EnvValue } = {
        SHELL: '/bin/tsh',
        HOME: homeDir,
        LOGNAME: () => Settings.getConfig('user.username', ''),
        USER: () => Settings.getConfig('user.username', ''),
        PWD: (sh: TShell, dir?: string) => {
            if (dir)
                sh.currentDirectory = dir;
            return sh.currentDirectory;
        },
        SHLVL: '1',
        _: (sh: TShell) => sh.currentCommand,
        '?': (sh: TShell) => String(sh.returnCode),
        PATH: '/home/bin:/bin',
    }
    return new Map(Object.keys(tmp).map(k => [k, tmp[k]]));
}
const defaultEnvironment = createEnvironment();
type Program = {
    params: Options,
    fun?(args: ParsedOptions, sh: TShell, ctx: StorageContext): Promise<number> | number | string | void
};
const programs: Map<string, Program> = new Map();

class TShell {
    private env : Map<string,EnvValue>;
    private _returnCode: number = 0;
    private _programs = programs;
    private _parent?: TShell;
    private _currentCommand?: string;
    private _outputElement?: HTMLElement;
    private _printer?: Printer;
    private _currentArguments?: Array<string>;
    private _cwd : StorageContext;
    constructor(cwd? : StorageContext, parent?: TShell) {
        this._parent = parent;
        this.env = createEnvironment();
        this._cwd = cwd ?? turtleduck.cwd;
        defaultEnvironment.forEach((v, k) => {
            if (!this.env.has(k)) {
                this.env.set(k, v);
            }
        });
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

    async eval(line: string, outputElement?: HTMLElement): Promise<number> {
        console.log("eval", line, outputElement);
        const commands = this.parseCommand(line);

        const oldOutput = this._outputElement;
        const oldPrinter = this._printer;

        if (outputElement) {
            this._outputElement = outputElement;
            this._printer = Terminal.elementPrinter(outputElement, 'shell');
            console.log(this._printer);
        }
        const evalStep = async (i: number): Promise<number> => {
            if (i >= 0 && i < commands.length) {
                if (commands[i + 1] && commands[i+1][0] === '|') {
                    // set up pipe
                }
                const cmd = commands[i];
                const prog = cmd[0];
                if (prog === ';' || prog === '&') {
                    return evalStep(i + 1);
                } else if (prog === '||' && this._returnCode !== 0) {
                    return evalStep(i + 1);
                } else if (prog === '&&' && this._returnCode === 0) {
                    return evalStep(i + 1);
                } else {
                    const args = cmd.splice(1);
                    await this.evalCommand(prog, args);
                    return evalStep(i + 1);
                }
            }

            // reached the end of all steps
            this._outputElement = oldOutput;
            this._printer = oldPrinter;
            return Promise.resolve(0);
        }

        return evalStep(0);
    }

    async evalCommand(prog: string, args: string[]): Promise<number> {
        args = args.map(arg => arg.startsWith('$') ? this.getenv(arg.substring(1)) : arg);
        const { params, fun } = this.findProgram(prog);

        const pArgs = getopts(args, params);

        pArgs.__shell__ = this;
        const prevCommand = this._currentCommand;
        const prevArguments = this._currentArguments;
        try {
            this._currentCommand = prog;
            this._currentArguments = args;
            if (fun) {
                const ret = await fun(pArgs, this, this._cwd);
                this._returnCode = Number(ret) || 0;

                if (typeof ret === 'string')
                    this.println(ret);
            } else {
                throw `Command not found: ${prog}`;
            }
        } catch (e) {
            this._returnCode = 255;
            console.error(e);
        } finally {
            this._currentCommand = prevCommand;
            this._currentArguments = prevArguments;
        }
        return Promise.resolve(this._returnCode);
    }

    findProgram(prog: string): Program {
        return programs.get(prog) || { fun: undefined, params: {} };
    }

    get context() {
        return this._cwd;
    }

    println(...args: string[]): void {
        const text = args.map(a => String(a)).join(' ');
        if (this._printer) {
            this._printer.print(text + '\n');
        } else {
            console.log(text);
        }
    }

    print(...args: string[]): void {
        const text = args.map(a => String(a)).join(' ');
        if (this._printer) {
            this._printer.print(text);
        } else {
            console.log(text);
        }
    }
}

programs.set('ls', {
    params: {
        boolean: ['all', 'long', 'time'], alias: { all: ['a'], long: ['l'], time: '' }
    },
    fun: (args, sh, ctx) => ctx.readdir(args['_'][0]).then((res: string[]) => sh.println(res.join(' '))).then(() => 0),
});
programs.set('echo', {
    params: { boolean: ['n', 'e', 'E'] },
    fun: (args, sh, ctx) => sh.println(args['_'].join(' '))
})
programs.set('cd', {
    params: { boolean: ['L', 'P', 'e', '@'] },
    fun: (args, sh, ctx) => ctx.chdir(args['_'][0]).then(() => 0)
})
programs.set('true', {
    params: {},
    fun: () => 0
})
programs.set('false', {
    params: {},
    fun: () => 1
})
export { TShell };

SubSystem.register({
	api: undefined,
	depends: ['storage'],
	name: 'tshell',
	start(dep) {
       return new TShell();
	},
});

const obj = {};

