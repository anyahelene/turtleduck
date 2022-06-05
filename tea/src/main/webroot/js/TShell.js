
import getopts from 'getopts';

const homeDir = '/home';
const options = {};
function createEnvironment() {
    let tmp = {
        SHELL: '/bin/tsh',
        HOME: homeDir,
        LOGNAME: () => turtleduck.getConfig('user.username'),
        USER: () => turtleduck.getConfig('user.username'),
        PWD: (sh, dir) => {
            if (dir)
                sh.currentDirectory = dir;
            else
                return sh.currentDirectory;
        },
        SHLVL: 1,
        _: (sh) => sh.currentCommand,
        '?': (sh) => sh.returnCode,
        PATH: '/home/bin:/bin'
    }
    return new Map(Object.keys(tmp).map(k => [k, tmp[k]]));
}
const defaultEnvironment = createEnvironment();
const programs = new Map();

class TShell {
    constructor(controller, parent) {
        this._controller = controller ? controller : globalThis.turtleduck;
        this._parent = parent;
        this._returnCode = 0;
        this._programs = programs;
        if (!this._controller.env) {
            this._controller.env = createEnvironment();
        }
        defaultEnvironment.forEach((v, k) => {
            if (!this._controller.env.has(k)) {
                this._controller.env.set(k, v);
            }
        });
    }

    get currentCommand() {
        return this._currentCommand || '/bin/tsh';
    }

    get returnCode() {
        return this._returnCode;
    }

    set returnCode(code) {
        this._returnCode = Number(code);
    }

    get currentDirectory() {
        return this._controller.cwd.cwd;
    }

    set currentDirectory(path) {
        const ctx = this._controller.cwd;
        const newCwd = ctx.realpath(path);
        ctx.cwd = newCwd;
    }

    getenv(varname) {
        const value = this._controller.env.get(varname);
        if (typeof value === 'function') {
            return value(this, undefined);
        } else if (value !== undefined) {
            return value;
        } else {
            return '';
        }
    }

    setenv(varname, value) {
        const old = this._controller.env.get(varname);
        if (typeof old === 'function') {
            old(this, value);
        } else {
            this._controller.env.set(varname, value);
        }
    }

    parseCommand(line) {
        const commands = line.trim().split(/(;|&&?|\|\|?)/);
        return commands.map((c) => c.trim().split(' '));
    }

    async eval(line, outputElement) {
        console.log("eval", line, outputElement);
        const commands = this.parseCommand(line);

        const oldOutput = this._outputElement;
        const oldPrinter = this._printer;

        if (outputElement) {
            this._outputElement = outputElement;
            this._printer = turtleduck.elementPrinter(outputElement, 'shell');
            console.log(this._printer);
        }
        const evalStep = i => {
            if (i >= 0 && i < commands.length) {
                if (commands[i + 1] === '|') {
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
                    return this.evalCommand(prog, args).then(() => evalStep(i + 1));
                }
            }

            // reached the end of all steps
            this._outputElement = oldOutput;
            this._printer = oldPrinter;
            return Promise.resolve(0);
        }

        return evalStep(0);

        commands.forEach(async cmd => {
            if (cmd.length > 0) {
                const prog = cmd[0];
                const args = cmd.splice(1);
                switch (prog) {
                    case ';':
                    case '||':
                    case '|':
                    case '&&':
                    case '&':
                        break;
                    default:
                        await this.evalCommand(prog, args);
                }
            }
        });
    }

    async evalCommand(prog, args) {
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
                const ret = await fun(pArgs, this, this._controller.cwd);
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

    }

    findProgram(prog) {
        return programs.get(prog) || { fun: undefined, params: {} };
    }

    get context() {
        return this._controller.cwd;
    }

    println(...args) {
        const text = args.map(a => String(a)).join(' ');
        if (this._printer) {
            this._printer.print(text + '\n');
        } else {
            console.log(text);
        }
    }

    print(...args) {
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
    fun: (args, sh, ctx) => ctx.readdir(args['_'][0]).then(res => sh.println(res.join(' '))),
});
programs.set('echo', {
    params: { boolean: ['n', 'e', 'E'] },
    fun: (args, sh, ctx) => sh.println(args['_'].join(' '))
})
programs.set('cd', {
    params: { boolean: ['L', 'P', 'e', '@'] },
    fun: (args, sh, ctx) => ctx.chdir(args['_'][0])
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
