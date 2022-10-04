/// <reference types="webpack/module" />

import { Storage, StorageContext } from './Storage';
// import type { LineHistory, HistorySession } from '../borb/LineHistory';
import type { ConfigDict, Settings } from '../borb/Settings';
import {
    SubSystem,
    Styles,
    DragNDrop,
    Borb,
    MDRender,
    Frames,
    History,
    Editors,
    LineEditors,
} from '../borb';
import { BorbFrame, BorbPanelBuilder } from '../borb/Frames';
import Systems from '../borb/SubSystem';
import { Language, LanguageConnection, Languages } from './Language';
import { TShell } from './TShell';
import { Chatter } from './Chatter';

Borb.tagName('foo ');
// export { History, HistorySession };
declare global {
    interface Window {
        EyeDropper: new () => {
            open(): Promise<{ sRGBHex: string } | undefined>;
        };
    }
}

function proxy<Name extends keyof TurtleDuck, Type extends TurtleDuck[Name]>(
    name: Name,
    subsys?: string,
): Type {
    if (subsys === undefined) subsys = name;
    const obj = new Proxy({} as Type, {
        get(target, p, receiver) {
            console.trace(`using proxy for ${name}.${String(p)}`);
            const funcObj: any = {
                async [p](...args: any[]) {
                    await SubSystem.waitFor(subsys);
                    const api = turtleduck[name];
                    if (api) {
                        const fn = api[p];
                        if (typeof fn === 'function' && !fn.proxy) {
                            console.trace(`calling proxy for ${name}.${String(p)}`);
                            return await fn(args);
                        }
                    }
                    throw new TypeError(`${String(p)} is not a function`);
                },
            };
            const f = funcObj[p]; // as (...args:any[]) => Promise<any>;
            f.proxy = true;
            return f;
        },
    });
    return obj;
}
let lastMessageIntervalId = 0;
function userlog(message: string, wait = false) {
    const log = document.getElementById('last-message');
    if (log) {
        if (lastMessageIntervalId != 0) window.clearInterval(lastMessageIntervalId);
        console.info('userlog(%s)', message);
        log.innerText = message;
        log.dataset.wait = `${wait}`;
        log.hidden = false;
        if (wait) {
            let dots = '';
            lastMessageIntervalId = window.setInterval(() => {
                dots += '.';
                log.innerText = message + dots;
            }, 1000);
        } else {
            lastMessageIntervalId = window.setTimeout(() => {
                log.hidden = true;
            }, 3000);
        }
    }
}
let sockConn = { socketConnected: false };
function updateInfo() {
    const userName = turtleduck.settings.getConfig(
        'user.nickname',
        turtleduck.settings.getConfig('user.username', undefined),
    );
    const status = document.getElementById('status');
    const statusBtn = document.getElementById('status-button');
    if (status && statusBtn) {
        if (sockConn) {
            if (sockConn.socketConnected) {
                status.className = 'online';
                statusBtn.textContent = '🖧 ONLINE';
            } else {
                status.className = 'offline';
                statusBtn.textContent = 'OFFLINE';
            }
            statusBtn.setAttribute('title', userName + '@' + sockConn.toString());
        } else {
            if (turtleduck.settings.getConfig('session.private', false)) {
                status.className = 'private';
                statusBtn.textContent = 'PRIVATE';
            } else if (turtleduck.settings.getConfig('session.offline', false)) {
                status.className = 'offline';
                statusBtn.innerText = 'OFFLINE';
            } else {
                status.className = '';
                statusBtn.innerText = 'OFFLINE';
            }
            statusBtn.setAttribute('title', userName);
        }
    }

    const imgBox = document.getElementById('user-picture');
    const imgUrl = turtleduck.settings.getConfig('user.picture', null);
    if (imgBox && imgUrl) {
        imgBox.setAttribute('src', imgUrl);
        imgBox.style.visibility = 'visible';
    }
    const nameBox = document.getElementById('user-name');
    if (nameBox) {
        nameBox.innerText = userName; // TODO: escapes
    }

    document.querySelectorAll('[data-from]').forEach((elt: HTMLElement) => {
        const froms = (elt.dataset.from || '').split('||');
        for (var i = 0; i < froms.length; i++) {
            const from = froms[i].trim();
            if (from) {
                const val = turtleduck.settings.getConfig(from, '');
                if (val) {
                    elt.innerText = val;
                    return;
                }
            }
        }
        elt.innerText = '';
    });
}
function showHeap({ heapUse, heapTotal }: { heapUse?: number; heapTotal?: number }) {
    const elt = document.getElementById('heapStatus');

    if (heapUse && heapTotal && elt) {
        let unit = 'k';
        if (heapTotal > 9999) {
            heapUse /= 1024;
            heapTotal /= 1024;
            unit = 'M';
        }
        elt.innerText = `${heapUse}${unit} / ${heapTotal}/${unit}`;
    }
}
interface EditorOrShell {
    paste(txt: string): void;
    iconified(i: boolean): boolean;
    focus(): void;
    paste_to_file(filename: string, text: string, language: string): void;
    getText(): string;
}
interface TurtleDuck {
    openCamera(config: ConfigDict);
    displayPopup(arg0: string, arg1: any, arg2: any, arg3: string);
    hints: any;
    eyeDropper(): Promise<string | undefined>;
    handleKey(key: string, button?: HTMLElement, event?: Event): Promise<any>;
    client: any;
    userlog(msg: string, wait?: boolean): void;
    updateInfo: typeof updateInfo;
    showHeap: typeof showHeap;
    cwd: StorageContext;
    storage: typeof Storage;
    history: typeof history;
    settings: typeof Settings;
    makeProxy: typeof proxy;
    pyshell: EditorOrShell;
    editor: EditorOrShell;
    builtinLanguages: Record<string, LanguageConnection>;
    wm: any;
    mdRender: typeof MDRender;
    styles: typeof Styles;
    borb: { dragndrop: typeof DragNDrop };
    createPanel(): BorbPanelBuilder;
    openFiles(): Promise<void>;
    tshell: TShell;
    chatter: Chatter;
}

export const turtleduck: TurtleDuck = {
    /** TODO: EyeDropper https://developer.mozilla.org/en-US/docs/Web/API/EyeDropper */
    async eyeDropper(): Promise<string | undefined> {
        if (window.EyeDropper) {
            const dropper = new window.EyeDropper();
            const result = await dropper.open(); // .open({ signal: abortController.signal });
            if (result && result.sRGBHex) {
                return result.sRGBHex;
            }
        }
        return Promise.resolve(undefined);
    },
    async handleKey(key, button, event) {
        return Promise.resolve();
    },
    userlog,
    history,
    updateInfo,
    showHeap,
    cwd: proxy('cwd', 'storage'),
    storage: proxy('storage'),
    makeProxy: proxy,
    styles: Styles,
    createPanel() {
        return new BorbPanelBuilder();
    },
} as TurtleDuck;

Systems.waitForAll(Editors, Storage).then(() => {
    Editors.BorbEditor.io = {
        readtextfile: (path) => turtleduck.cwd.readtextfile(path),
        rename: (oldPath, newPath) => turtleduck.cwd.rename(oldPath, newPath),
        requestfile: (mode, lang, currentPath) => Promise.resolve(''),
        resolve: (path) => turtleduck.cwd.realpath(path),
        unlink: (path) => turtleduck.cwd.unlink(path),
        writetextfile: (path, text) => turtleduck.cwd.writetextfile(path, text),

        detectLanguage: (pathOrText) => Languages.detect(pathOrText),
    };
});
/*
declare global {
	interface ImportMeta {
		webpackHot: {
			accept(errorHandler: (err: any, { moduleId, dependencyId }) => void): void;
			accept(dependencies: string | string[], callback?: () => void, errorHandler?: (err: any, { moduleId, dependencyId }) => void): void;
			decline(dependencies?: string | string[]): void;
			dispose(handler:(data:any) => void):void;
			invalidate():void;
		}
	}
}
*/

if (import.meta.webpackHot) {
    console.warn('WebpackHot enabled');
    import.meta.webpackHot.addStatusHandler((status) => {
        console.log('HMR status', status);
    });
}
