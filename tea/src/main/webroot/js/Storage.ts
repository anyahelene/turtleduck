/// <reference path="../../../../node_modules/@isomorphic-git/lightning-fs/index.d.ts" />
import path from '@isomorphic-git/lightning-fs/src/path';
import MagicPortal from 'magic-portal';
import { turtleduck } from './TurtleDuck';
import { Printer, Terminal } from './Terminal';
import { Systems } from '../borb/SubSystem';
import Settings from '../../../../../borb/src/Settings';
import FS from '@isomorphic-git/lightning-fs';
import { MessagingConnection } from './MessagingConnection';
import { BaseConnection, Message, Messaging, MessagingError } from '../borb/Messaging';
import { PosixError } from './Errors';

export class StorageContext {
    fs: FS.PromisifiedFS;
    umask = 0o022;
    private _path: any;
    public cwd: string;
    constructor(fs: any, cwd: string) {
        this.fs = fs;
        this._path = path;
        this.cwd = this._path.normalize(cwd);
    }
    realpath(path: string): string {
        return this._path.resolve(this.cwd, path);
    }
    resolve(dirpath: string, filepath: string): string {
        console.log('resolve', dirpath, filepath);
        return this._path.resolve(this.cwd, dirpath || '', filepath);
    }
    async withCwd(path: string = ''): Promise<StorageContext> {
        const ctx = new StorageContext(this.fs, this.cwd);
        return ctx.chdir(path);
    }
    async chdir(path: string = '', createMissing = false): Promise<this> {
        const newCwd = this.realpath(path);
        console.log('cd to: ', newCwd);
        if (createMissing) {
            await this.#getOrCreateDirectory(newCwd, 0o777); // throws otherwise
            this.cwd = newCwd;
        } else {
            const stat = await this.fs.stat(path);
            if (stat.type !== 'dir') throw new PosixError('ENOTDIR: ' + path);
            this.cwd = newCwd;
        }

        return this;
    }
    async #getOrCreateDirectory(path: string, mode: number) {
        let stat: FS.Stats;
        const parts = path.split('/');
        let p = '/';
        while (parts.length > 0) {
            p = this.resolve(p, parts.shift());
            try {
                stat = await this.fs.stat(p);
            } catch (e) {
                await this.mkdir(p, false, mode);
                stat = await this.fs.stat(p);
            }
            console.log(stat);
            if (stat.type !== 'dir') throw new PosixError('ENOTDIR: ' + p);
        }
        return stat;
    }
    async mkdir(path: string, createMissing = true, mode = 0o777): Promise<void> {
        if (createMissing) {
            await this.#getOrCreateDirectory(this.realpath(path), mode);
        } else {
            await this.fs.mkdir(this.realpath(path), {
                mode: mode & ~this.umask,
            });
        }
    }
    async rmdir(path: string): Promise<void> {
        return this.fs.rmdir(this.realpath(path));
    }
    async readdir(path: string = ''): Promise<string[]> {
        return this.fs.readdir(this.realpath(path));
    }
    async writetextfile(path: string, data: string | any, mode: number = 0o666): Promise<void> {
        return this.fs.writeFile(this.realpath(path), typeof data === 'string' ? data : `{data}`, {
            encoding: 'utf8',
            mode: mode & ~this.umask,
        });
    }
    async writebinfile(
        path: string,
        data: Uint8Array | Iterable<number>,
        mode = 0o666,
    ): Promise<void> {
        return this.fs.writeFile(
            this.realpath(path),
            data instanceof Uint8Array ? data : Uint8Array.from(data),
            { encoding: undefined, mode: mode & ~this.umask },
        );
    }
    async readtextfile(path: string): Promise<string> {
        return this.fs.readFile(this.realpath(path), {
            encoding: 'utf8',
        }) as Promise<string>;
    }
    async readbinfile(path: string): Promise<Uint8Array> {
        return this.fs.readFile(this.realpath(path), {
            encoding: undefined,
        }) as Promise<Uint8Array>;
    }
    async unlink(path: string): Promise<void> {
        return this.fs.unlink(this.realpath(path));
    }
    async rename(oldFilepath: string, newFilepath: string): Promise<void> {
        return this.fs.rename(this.realpath(oldFilepath), this.realpath(newFilepath));
    }
    async stat(path: string = ''): Promise<FS.Stats> {
        return this.fs.stat(this.realpath(path));
    }
    async lstat(path: string = ''): Promise<FS.Stats> {
        return this.fs.lstat(this.realpath(path));
    }
    async symlink(target: string = '.', path: string): Promise<void> {
        return this.fs.symlink(target, this.realpath(path));
    }
    async readlink(path: string = ''): Promise<string> {
        return this.fs.readlink(this.realpath(path));
    }
    async backFile(path: string, opts?: FS.BackFileOptions): Promise<void> {
        return this.fs.backFile(this.realpath(path), opts);
    }
    async du(path: string = ''): Promise<number> {
        return this.fs.du(this.realpath(path));
    }
}
export class StorageImpl {
    public static readonly _id = 'storage';
    public static readonly _revision = 0;
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
    conn: StorageConnection;

    constructor() {
        this._MagicPortal = MagicPortal;
        this._initialized = false;
    }

    context() {
        return new StorageContext(this.fs, '/');
    }
    async init(fsConfig = { fsName: 'lfs' }): Promise<StorageImpl> {
        console.warn('Storage init', fsConfig);
        this.worker = new Worker(new URL('./StorageWorker.js', import.meta.url));
        this.portal = new MagicPortal(this.worker);
        this.worker.addEventListener('message', ({ data }) =>
            console.log('from storage worker:', data),
        );
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
                const progress = 100 * (evt.total ? evt.loaded / evt.total : 0.5);
                if (evt.total) {
                    turtleduck.userlog(`${evt.phase}: ${evt.loaded} / ${evt.total}`);
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
        this.conn = new StorageConnection(this.fs);
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
        const requested = Settings.getConfig('storage.persistenceRequested', false);
        const allowed = Settings.getConfig('storage.persistenceAllowed', false);
        if (navigator.storage) {
            var persisted = false;
            var estimate: StorageEstimate;
            if (navigator.storage.persisted) persisted = await navigator.storage.persisted();
            if (navigator.storage.estimate) estimate = await navigator.storage.estimate();
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
        printer.print('BRANCHES:\n' + branches.map((b) => `  ${b}`).join('\n') + '\n');

        const files = await this.git.listFiles({});
        printer.print('FILES:\n' + files.map((b) => `  ${b}`).join('\n') + '\n');

        const commits = await this.git.log({});
        printer.print(
            'LOG:\n' +
                commits.map((c) => `  ${c.oid.slice(0, 7)}: ${c.commit.message}`).join('\n') +
                '\n',
        );
    }
}

export class StorageConnection extends BaseConnection {
    private _path: typeof path;
    static readonly umask = 0o022;
    private ctx: StorageContext;

    constructor(fs: any) {
        super(Messaging, 'storage');
        this.ctx = new StorageContext(fs, '/');
        this._path = path;
    }

    public async deliverRemote(msg: Message, transfers: Transferable[]): Promise<void> {
        try {
            const reply = await this.call(msg, transfers);
            await this.deliverHost(this.router.reply(msg, reply));
        } catch (e) {
            let ex = new PosixError(e);
            console.log(e, ex);
            const err_reply = await this.router.errorMsg(msg.header.msg_id, ex);
            await this.deliverHost(err_reply);
        }
    }
    protected async call(msg: Message, transfers?: Transferable[]): Promise<{}> {
        this.ctx.cwd = typeof msg.content.cwd === 'string' ? msg.content.cwd : '/';
        this.ctx.umask =
            typeof msg.content.umask === 'number' ? msg.content.umask : StorageConnection.umask;
        switch (msg.header.msg_type) {
            case 'realpath': {
                const { path } = msg.content as { path: string };
                return { path: this.ctx.realpath(path) };
            }
            case 'resolve': {
                const { dirpath, filepath } = msg.content as { dirpath: string; filepath: string };
                return { path: this.ctx.resolve(dirpath, filepath) };
            }
            case 'chdir': {
                const { path = '', createMissing = false } = msg.content as {
                    path: string;
                    createMissing: boolean;
                };
                await this.ctx.chdir(path, createMissing);
                return { cwd: this.ctx.cwd };
            }
            case 'mkdir': {
                const {
                    path,
                    createMissing = true,
                    mode = 0o777,
                } = msg.content as {
                    path: string;
                    createMissing: boolean;
                    mode: number;
                };
                await this.ctx.mkdir(path, createMissing, mode);
                return {};
            }
            case 'rmdir': {
                const { path } = msg.content as { path: string };
                await this.ctx.rmdir(path);
                return {};
            }
            case 'readdir': {
                const { path = '' } = msg.content as { path: string };
                const result = await this.ctx.readdir(path);
                return { entries: result };
            }
            case 'writetextfile': {
                const {
                    path,
                    data,
                    mode = 0o666,
                } = msg.content as { path: string; data: string | any; mode: number };
                await this.ctx.writetextfile(path, data, mode);
                return {};
            }
            case 'writebinfile': {
                const {
                    path,
                    data,
                    mode = 0o666,
                } = msg.content as {
                    path: string;
                    data: Uint8Array | Iterable<number>;
                    mode: number;
                };
                await this.ctx.writebinfile(path, data, mode);
                return {};
            }
            case 'readtextfile': {
                const { path } = msg.content as { path: string };
                return { text: await this.ctx.readtextfile(path) };
            }
            case 'readbinfile': {
                const { path } = msg.content as { path: string };
                return { content: await this.ctx.readbinfile(path) };
            }
            case 'unlink': {
                const { path } = msg.content as { path: string };
                await this.ctx.unlink(path);
                return {};
            }
            case 'rename': {
                const { oldFilepath, newFilepath } = msg.content as {
                    oldFilepath: string;
                    newFilepath: string;
                };
                await this.ctx.rename(oldFilepath, newFilepath);
                return {};
            }
            case 'stat': {
                const { path = '' } = msg.content as { path: string };
                return { stats: await this.ctx.stat(path) };
            }
            case 'lstat': {
                const { path = '' } = msg.content as { path: string };
                return { stats: await this.ctx.lstat(path) };
            }
            case 'symlink': {
                const { target = '', path } = msg.content as {
                    target: string;
                    path: string;
                };
                await this.ctx.symlink(target, path);
                return {};
            }
            case 'readlink': {
                const { path = '' } = msg.content as { path: string };
                return { path: await this.ctx.readlink(path) };
            }
            case 'du': {
                const { path = '' } = msg.content as { path: string };
                return { size: await this.ctx.du(path) };
            }
            default: {
                throw new PosixError(`ENOTSUP: ${msg.header.msg_type}`);
            }
        }
    }
}
export const Storage = Systems.declare(StorageImpl)
    .depends(Settings)
    .start(() => new StorageImpl().init())
    .reloadable(false)
    .register();

export default Storage;
