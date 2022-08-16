import type { BorbElement } from './BaseElement';
import { interpolate, upgradeElements } from './Common';

// eslint-disable no-unused-vars
enum State {
    STOPPED,
    WAITING,
    STARTING,
    STARTED,
}
export interface SubSystem<T extends object> {
    customElements?: typeof BorbElement[];
    name: string;
    depends?: Iterable<string>;
    start?(
        self: SubSystem<T>,
        dep: Dependency<T>,
    ): Promise<T | void> | T | void;
    stop?(): Promise<void> | void;
    api?: T;
    prototype?: T;
    reloadable?: boolean;
    revision: number;
}

interface TargetObject {
    [propName: string]: object;
}
class SubSysBuilder<T extends object> {
    sys: SubSystem<T>;
    private _depends: string[];

    constructor(name: string, proto: T, revision: number) {
        this._depends = [];
        this.sys = {
            name,
            depends: this._depends,
            reloadable: true,
            revision,
        };
        if (proto) this.sys.prototype = proto;
    }

    depends(...deps: (string | { _id: string })[]): this {
        this._depends.push(
            ...deps.map((d) => (typeof d === 'string' ? d : d._id)),
        );
        return this;
    }

    stop(handler: () => Promise<void> | void): this {
        this.sys.stop = handler;
        return this;
    }

    start(
        handler: (
            self: SubSystem<T>,
            dep: Dependency<T>,
        ) => Promise<T | void> | T | void,
    ): this {
        this.sys.start = handler;
        return this;
    }

    prototype(proto: T): this {
        this.sys.prototype = proto;
        return this;
    }

    api(api: T): this {
        this.sys.api = api;
        return this;
    }

    reloadable(reloadable: boolean): this {
        this.sys.reloadable = reloadable;
        return this;
    }

    register(): T {
        const api = this.sys.api || this.sys.prototype;
        if (!api) throw new Error('no api object');
        Dependency.register(this.sys);
        return api;
    }

    elements(...customElements: typeof BorbElement[]): this {
        this.sys.customElements = customElements;
        return this;
    }
}
function nop() {
    //
}
const AsyncFunction = async function () {}.constructor;
function proxy<T extends object>(
    obj: T,
    dep: Dependency<T>,
    hotReload: boolean,
) {
    return new Proxy(obj, {
        get(target, p, receiver) {
            // get newest version
            if (hotReload) {
                const refreshed = SubSystem.get(dep.name) as Dependency<T>;
                if (refreshed !== dep) {
                    console.log('Subsystem refreshed: %s', dep.name, dep);
                    dep = refreshed;
                }
            }
            if (dep.isStarted()) {
                return dep.api[p];
            } else if (dep.api[p] instanceof AsyncFunction) {
                console.trace(`using proxy for ${dep.name}.${String(p)}`);
                // wrap it in an object to get correct name
                const funcObj: any = {
                    async [p](...args: any[]) {
                        const api = await SubSystem.waitFor(dep.name);
                        if (api) {
                            const fn = api[p];
                            if (typeof fn === 'function' && !fn.proxy) {
                                console.trace(
                                    `calling proxy for ${name}.${String(p)}`,
                                );
                                return fn(args);
                            }
                        }
                        throw new TypeError(`${String(p)} is not a function`);
                    },
                };
                const f = funcObj[p]; // as (...args:any[]) => Promise<any>;
                f.proxy = true;
                return f;
            } else {
                throw new Error(
                    `Accessing ${dep.name}.${String(p)} before ${
                        dep.name
                    } is ready`,
                );
            }
        },
    });
}
export class Dependency<T extends object> {
    public static targetObject: TargetObject;
    private static counter = 0;
    private static systems = new Map<string, Dependency<object>>();
    private static queue: SubSystem<object>[] = [];
    private static makeProxies = false;
    private static hotReload = false;
    private _name: string;
    proxy: T;
    promise: Promise<SubSystem<T>>;
    resolve: (value: SubSystem<T> | PromiseLike<SubSystem<T>>) => void = nop;
    reject: (reason: Error) => void = nop;
    deps: Set<Dependency<object>> = new Set();
    id: number = Dependency.counter++;

    public get name(): string {
        return this._name;
    }

    private _api?: T;
    public get api(): T | undefined {
        return this._api;
    }
    private state = State.STOPPED;
    private subsystem?: SubSystem<T>;
    constructor(name: string) {
        this._name = name;
        this.promise = new Promise((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
        console.log('Created', this.toString());
    }

    dependencies(): Set<Dependency<object>> {
        return new Set(this.deps);
    }

    dependents(): Set<Dependency<object>> {
        const result: Set<Dependency<object>> = new Set();
        Dependency.systems.forEach((sys) => {
            if (sys.deps.has(this)) {
                result.add(sys);
            }
        });
        return result;
    }

    forEach(callback: (value: Dependency<object>) => void): void {
        this.deps.forEach(callback);
    }
    isStarted(): boolean {
        return this.state === State.STARTED;
    }

    status(): string {
        return `${this.state}`;
    }

    toString(): string {
        return `${this.name} (${State[this.state]})`;
    }

    static debugAll(): void {
        console.group('Subsystems:');
        Dependency.systems.forEach((dep) => dep.debug());
        console.groupEnd();
    }

    debug(): void {
        console.log(
            this.toString(),
            ':',
            [...this.deps].map((d) => d.name).join(', '),
        );
    }

    static get(sysName: string): Dependency<object> {
        let sys = Dependency.systems.get(sysName);
        if (!sys) {
            sys = new Dependency(sysName);
            if (Dependency.makeProxies)
                sys.proxy = proxy({}, sys, Dependency.hotReload);
            Dependency.systems.set(sysName, sys);
        }
        return sys;
    }

    static getApi<T extends object>(sysName: string): T {
        const sys = Dependency.get(sysName) as Dependency<T>;
        return sys._api;
    }
    static setup(
        targetObject: TargetObject,
        config?: { proxy: boolean; hotReload: boolean; global: boolean },
    ) {
        if (config?.global) globalThis.SubSystem = _self;
        Dependency.targetObject = targetObject;
        Dependency.makeProxies = !!config?.proxy;
        Dependency.hotReload = !!config?.hotReload;

        if (targetObject && !targetObject['borb']) {
            targetObject['borb'] = { subSystem: _self };
        }
        let sys = Dependency.queue.shift();
        console.log(Dependency.queue);
        while (sys) {
            console.log(`Registering queued subsystem '${sys.name}'`);
            Dependency.register(sys);
            sys = Dependency.queue.shift();
        }
    }
    static async waitFor<T extends object>(sysName: string): Promise<T> {
        const dep = Dependency.get(sysName);
        await dep.promise;
        return Promise.resolve(dep._api as T);
    }
    static register(sys: SubSystem<object>) {
        const depsys = Dependency.get(sys.name);
        if (Dependency.makeProxies) {
            depsys.proxy = proxy(
                sys.prototype ?? {},
                depsys,
                Dependency.hotReload,
            );
        }
        if (!Dependency.targetObject) {
            Dependency.queue.push(sys);
            console.log(`System not ready, enqueing '${sys.name}'`);
            return;
        }
        if (depsys.state !== State.STOPPED && !sys.reloadable) {
            throw new Error(`Already registered: ${sys.name}`);
        }
        depsys.deps.clear();
        const depends = sys.depends ?? [];
        for (const d of depends) {
            depsys.deps.add(Dependency.get(d));
        }

        depsys.state = State.WAITING;
        depsys.subsystem = sys;
        depsys.setApi(sys.api);
        console.log('Registered', depsys.toString());
        return Promise.all([...depsys.deps].map((d) => d.promise)).then(
            () => depsys.init(),
            (reason) => {
                console.error(
                    `Dependencies failed for ${depsys.name}: ${reason}`,
                );
            },
        );
    }
    private setApi(api: T) {
        //if(typeof api === "function")
        //    this._api = api();
        //else
        if (!api) api = this.proxy;
        this._api = api;

        if (this._api) {
            const names = this.name.split('/');
            let target = Dependency.targetObject;
            names.slice(0, -1).forEach((n) => {
                console.log('find', names, n, target);
                if (!target[n]) {
                    target[n] = {};
                }
                target = target[n] as TargetObject;
                console.log('=>', target);
            });
            target[names.slice(-1)[0]] = this._api;
            console.log('=>', target);
        }
    }
    async init(): Promise<void> {
        if (!this.subsystem) return Promise.resolve();
        if (this.state !== State.STARTED) {
            const subsystem = this.subsystem;
            this.state = State.STARTING;
            console.log('Starting', this.toString());
            const start = (sys: SubSystem<T>, dep: Dependency<T>) => {
                console.groupCollapsed(
                    `Starting ${this.subsystem.name} rev.${this.subsystem.revision}`,
                );
                try {
                    (subsystem.customElements ?? []).forEach((eltDef) => {
                        try {
                            console.debug(
                                'defining custom element %s: %o',
                                eltDef.tag,
                                eltDef,
                            );
                            customElements.define(eltDef.tag, eltDef);
                            if (subsystem.revision > 0) {
                                upgradeElements(eltDef);
                            }
                        } catch (e) {
                            console.error(e);
                        }
                    });
                    return Promise.resolve(this.subsystem.start?.(sys, dep));
                } finally {
                    console.groupEnd();
                }
            };

            start(this.subsystem, this).then(
                (obj) => {
                    console.log('Started', this.toString());
                    this.state = State.STARTED;
                    if (obj) this.setApi(obj);
                    else if (subsystem.prototype)
                        this.setApi(subsystem.prototype);
                    this.resolve(subsystem);
                },
                (reason) => {
                    console.error(`While starting ‘${this.name}’:`, reason);
                    this.state = State.STOPPED;
                    if (reason instanceof Error) {
                        reason.message = `In subsystem ‘${this.name}’: ${reason.message}`;
                        this.reject(reason);
                    } else {
                        this.reject(
                            new Error(`In subsystem ‘${this.name}’: ${reason}`),
                        );
                    }
                },
            );
        } else {
            console.warn('Already started', this);
            this.resolve(this.subsystem);
        }
    }

    static declare<T extends object>(
        nameOrObj: string | (T & { _id: string; _revision?: number }),
        prototype?: T,
        revision = 0,
    ): SubSysBuilder<T> {
        if (typeof nameOrObj === 'string')
            return new SubSysBuilder(nameOrObj, prototype, revision);
        else if (nameOrObj._id)
            return new SubSysBuilder(
                nameOrObj._id,
                nameOrObj,
                nameOrObj._revision ?? revision,
            );
    }
}

globalThis.addEventListener?.('DOMContentLoaded', (loadedEvent) => {
    SubSystem.register({
        name: 'dom',
        revision: 0,
        reloadable: true,
    });
});

const _self = {
    debugAll: Dependency.debugAll,
    register: Dependency.register,
    get: Dependency.get,
    getApi: Dependency.getApi,
    setup: Dependency.setup,
    waitFor: Dependency.waitFor,
    declare: Dependency.declare,
    util: {
        interpolate,
    },
    State,
};
export const SubSystem = _self;
export default SubSystem;

export interface BorbSys {
    subSystem: typeof _self;
}

declare module './SubSystem' {
    const BORB: BorbSys;
}
