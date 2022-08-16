/// <reference path="../../../../node_modules/@isomorphic-git/lightning-fs/index.d.ts" />
import path from '@isomorphic-git/lightning-fs/src/path';
import MagicPortal from 'magic-portal';
import { turtleduck } from './TurtleDuck';
import { Printer, Terminal } from './Terminal';
import { SubSystem } from '../borb/SubSystem';
import Settings from '../../../../../borb/src/Settings';
import FS from '@isomorphic-git/lightning-fs';

class StorageContext {
    fs: FS.PromisifiedFS;
    private _path: any;
    public cwd: string;
    constructor(fs: any, cwd: string) {
        this.fs = fs;
        this._path = path;
        this.cwd = this._path.normalize(cwd);
    }
    realpath(filepath: string): string {
        return this._path.resolve(this.cwd, filepath);
    }
    async withCwd(filepath: string = ''): Promise<StorageContext> {
        const ctx = new StorageContext(this.fs, this.cwd);
        return ctx.chdir(filepath);
    }
    async chdir(filepath: string = ''): Promise<this> {
        const newCwd = this.realpath(filepath);
        console.log('cd to: ', newCwd);
        return this.fs.stat(newCwd).then((res) => {
            this.cwd = newCwd;
            return this;
        });
    }
    async mkdir(filepath: string, opts?: FS.MKDirOptions): Promise<void> {
        return this.fs.mkdir(this.realpath(filepath), opts);
    }
    async rmdir(filepath: string): Promise<void> {
        return this.fs.rmdir(this.realpath(filepath));
    }
    async readdir(filepath: string = ''): Promise<string[]> {
        return this.fs.readdir(this.realpath(filepath));
    }
    async writetextfile(
        filepath: string,
        data: string | any,
        mode: number = 0o777,
    ): Promise<void> {
        return this.fs.writeFile(
            this.realpath(filepath),
            typeof data === 'string' ? data : `{data}`,
            { encoding: 'utf8', mode: mode },
        );
    }
    async writebinfile(
        filepath: string,
        data: Uint8Array | Iterable<number>,
        mode = 0o777,
    ): Promise<void> {
        return this.fs.writeFile(
            this.realpath(filepath),
            data instanceof Uint8Array ? data : Uint8Array.from(data),
            { encoding: undefined, mode: mode },
        );
    }
    async readtextfile(filepath: string): Promise<string> {
        return this.fs.readFile(this.realpath(filepath), {
            encoding: 'utf8',
        }) as Promise<string>;
    }
    async readbinfile(filepath: string): Promise<Uint8Array> {
        return this.fs.readFile(this.realpath(filepath), {
            encoding: undefined,
        }) as Promise<Uint8Array>;
    }
    async unlink(filepath: string): Promise<void> {
        return this.fs.unlink(this.realpath(filepath));
    }
    async rename(oldFilepath: string, newFilepath: string): Promise<void> {
        return this.fs.rename(
            this.realpath(oldFilepath),
            this.realpath(newFilepath),
        );
    }
    async stat(filepath: string = ''): Promise<FS.Stats> {
        return this.fs.stat(this.realpath(filepath));
    }
    async lstat(filepath: string = ''): Promise<FS.Stats> {
        return this.fs.lstat(this.realpath(filepath));
    }
    async symlink(target: string = '.', filepath: string): Promise<void> {
        return this.fs.symlink(target, this.realpath(filepath));
    }
    async readlink(filepath: string = ''): Promise<string> {
        return this.fs.readlink(this.realpath(filepath));
    }
    async backFile(filepath: string, opts?: FS.BackFileOptions): Promise<void> {
        return this.fs.backFile(this.realpath(filepath), opts);
    }
    async du(filepath: string = ''): Promise<number> {
        return this.fs.du(this.realpath(filepath));
    }
}
class Storage {
    fs: any;
    _MagicPortal: any;
    _initialized: boolean;
    worker?: Worker;
    portal: any;
    printer?: Printer;
    ui: any;
    git: any;
    cwd?: StorageContext;
    persisted: boolean = false;

    constructor() {
        this._MagicPortal = MagicPortal;
        this._initialized = false;
    }

    context() {
        return new StorageContext(this.fs, '/');
    }
    async init(fsConfig = { fsName: 'lfs' }) {
        console.warn('Storage init', fsConfig);
        this.worker = new Worker(
            new URL('./StorageWorker.js', import.meta.url),
        );
        this.portal = new MagicPortal(this.worker);
        //this.worker.addEventListener("message", ({ data }) => console.log("from storage worker:", data));
        const storage = this;

        this.ui = {
            async config() {
                return fsConfig;
            },
            async print(message: string) {
                if (storage.printer) {
                    storage.printer.print(message);
                }
            },
            async progress(evt: any) {
                const progress =
                    100 * (evt.total ? evt.loaded / evt.total : 0.5);
                if (evt.total) {
                    turtleduck.userlog(
                        `${evt.phase}: ${evt.loaded} / ${evt.total}`,
                    );
                } else {
                    turtleduck.userlog(`${evt.phase}`);
                }
                //$("progress-txt").textContent = evt.phase;
                // $("progress").value = evt.total ? evt.loaded / evt.total : 0.5;
                return;
            },
            async fill(url: URL) {
                let username = window.prompt('Username:');
                let password = window.prompt('Password:');
                return { username, password };
            },
            async rejected({ url, auth }: any) {
                window.alert('Authentication rejected');
                return;
            },
        };
        this.portal.set('ui', this.ui, {
            void: ['print', 'progress', 'rejected'],
        });

        this.fs = await this.portal.get('fsWorker');
        this.git = await this.portal.get('gitWorker');
        this._initialized = true;
        console.log('File system ready:', fsConfig);
        this.cwd = this.context();
        return this;
    }

    async clone(url, dest, proxy) {
        this.printer = Terminal.consolePrinter('git');
        this.printer.print(`Cloning ${url} into /`);
        await this.git.setDir(dest);

        await this.git.clone({
            corsProxy: proxy, // "https://cors.isomorphic-git.org",
            url: url,
        });
        this.printer = undefined;
        turtleduck.userlog('clone complete');
    }

    async info(): Promise<{
        quota?: number;
        usage?: number;
        persisted: boolean;
        requested: false;
        allowed?: false;
    }> {
        const requested = Settings.getConfig(
            'storage.persistenceRequested',
            false,
        );
        const allowed = Settings.getConfig('storage.persistenceAllowed', false);
        if (navigator.storage) {
            var persisted = false;
            var estimate: StorageEstimate;
            if (navigator.storage.persisted)
                persisted = await navigator.storage.persisted();
            if (navigator.storage.estimate)
                estimate = await navigator.storage.estimate();
            this.persisted = persisted;
            return { persisted, requested, allowed, ...estimate };
        } else {
            this.persisted = false;
            return {
                persisted: false,
                requested: requested,
            };
        }
    }

    async requestPersistence() {
        if (navigator.storage && navigator.storage.persist) {
            return navigator.storage.persist().then((persistent) => {
                if (persistent)
                    console.info(
                        'Storage is persistent: Storage will not be cleared except by explicit user action',
                    );
                else
                    console.warn(
                        'Storage is not persistent: Storage may be cleared by the UA under storage pressure.',
                    );
                Settings.setConfig(
                    {
                        'storage.persistenceRequested': true,
                        'storage.persistenceAllowed': persistent,
                    },
                    'user',
                );
                return persistent;
            });
        } else {
            return false;
        }
    }
    async showInfo() {
        const printer = Terminal.consolePrinter('git');
        const branches = await this.git.listBranches({ remote: 'origin' });
        printer.print(
            'BRANCHES:\n' + branches.map((b) => `  ${b}`).join('\n') + '\n',
        );

        const files = await this.git.listFiles({});
        printer.print(
            'FILES:\n' + files.map((b) => `  ${b}`).join('\n') + '\n',
        );

        const commits = await this.git.log({});
        printer.print(
            'LOG:\n' +
                commits
                    .map((c) => `  ${c.oid.slice(0, 7)}: ${c.commit.message}`)
                    .join('\n') +
                '\n',
        );
    }
}

export { Storage, StorageContext };

const systemSpec = {
    depends: ['borb/settings'],
    name: 'storage',
    start: (sys) => sys.api.init(),
    api: new Storage(),
    revision: 0,
};

SubSystem.register(systemSpec);
