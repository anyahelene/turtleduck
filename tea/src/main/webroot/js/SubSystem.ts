import { keys } from "lodash-es";

enum State {
    STOPPED, WAITING, STARTING, STARTED
}


export interface SubSystem<T extends object> {
    name: string;
    depends?: Iterable<string>;
    start?(self:SubSystem<T>, dep: Dependency<T>): Promise<object|void> | object | void;
    stop?(): Promise<void> | void;
    api? : T;
    prototype? : T;
    reloadable?: boolean
}

interface TargetObject {
    [propName : string]: object;
}
class SubSysBuilder<T extends object> {
    sys:SubSystem<T>;
    private _depends: string[];

    constructor(name:string, proto?:T) {
        this._depends = [];
        this.sys = {
            name,
            depends: this._depends,
            reloadable:true
        };
        if(proto)
            this.sys.prototype = proto;
    }

    depends(...deps:string[]): this {
        this._depends.push(...deps);
        return this;
    }

    stop(handler: () => Promise<void>|void): this {
        this.sys.stop = handler;
        return this;
    }

    start(handler: (self:SubSystem<T>, dep: Dependency<T>) => Promise<T|void> | object | void): this {
        this.sys.start = handler;
        return this;
    }

    prototype(proto:T): this {
        this.sys.prototype = proto;
        return this;
    }

    api(api:T): this {
        this.sys.api = api;
        return this;
    }

    reloadable(reloadable:boolean):this {
        this.sys.reloadable = reloadable;
        return this;
    }

    register(): Promise<void> {
        return Dependency.register(this.sys);
    }
}
export class Dependency<T extends object> {
    public static targetObject : TargetObject;
    private static counter = 0;
    private static systems = new Map<string, Dependency<object>>();
    private static queue : SubSystem<object>[] = [];
    private promise: Promise<SubSystem<T>>;
    private resolve: (value: SubSystem<T> | PromiseLike<SubSystem<T>>) => void = (v) => { };
    private reject: (reason: Error) => void = (r) => { };
    private deps: Set<Dependency<object>> = new Set();
    private id: number = Dependency.counter++;
    
    private _name : string;
    public get name() : string {
        return this._name        ;
    }

    private _api? : object;;
    public get api() : object | undefined {
        return this._api;
    }
    private state: State = State.STOPPED;
    private subsystem?: SubSystem<T>;
    constructor(name: string) {
        this._name = name;
        this.promise = new Promise((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
        console.log("Created", this.toString());
    }

    dependencies(): Set<Dependency<object>> {
        return new Set(this.deps);
    }

    dependents(): Set<Dependency<object>> {
        let result: Set<Dependency<object>> = new Set();
        Dependency.systems.forEach(sys => {
            if (sys.deps.has(this)) {
                result.add(sys);
            }
        });
        return result;
    }

    forEach(callback : (value:Dependency<object>) => void) : void {
        this.deps.forEach(callback);
    }
    isStarted() : boolean {
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
        Dependency.systems.forEach(dep => dep.debug());
        console.groupEnd();
    }

    debug(): void {
        console.log(this.toString(), ":", [...this.deps].map(d => d.name).join(", "));
    }

    static get(sysName: string): Dependency<object> {
        let sys = Dependency.systems.get(sysName);
        if (!sys) {
            sys = new Dependency(sysName);
            Dependency.systems.set(sysName, sys);
        }
        return sys;
    }
    static setup(targetObject : any) {
        Dependency.targetObject = targetObject;
        let sys = Dependency.queue.shift()
        console.log(Dependency.queue);
        while(sys) {
            console.log(`Registering queued subsystem '${sys.name}'`);
            Dependency.register(sys);
            sys = Dependency.queue.shift();
        }
    }
    static async waitFor(sysName : string): Promise<Dependency<object>> {
        const dep = Dependency.get(sysName);
        await dep.promise;
        return Promise.resolve(dep);
    }
    static register(sys: SubSystem<object>) {
        const depsys = Dependency.get(sys.name);
        if(!Dependency.targetObject) {
            Dependency.queue.push(sys);
            console.log(`System not ready, enqueing '${sys.name}'`);
            return;
        }
        if (depsys.state !== State.STOPPED && !sys.reloadable) {
            throw new Error(`Already registered: ${sys.name}`);
        }
        depsys.deps.clear();
        const depends = sys.depends ?? [];
        for (let d of depends) {
            depsys.deps.add(Dependency.get(d));
        }

        depsys.state = State.WAITING;
        depsys.subsystem = sys;
        depsys.setApi(sys.api);
        console.log("Registered", depsys.toString());
        return Promise.all([...depsys.deps].map(d => d.promise)).then(() => depsys.init(), reason => {
            console.error(`Dependencies failed for ${depsys.name}: ${reason}`);
        });
    }
    private setApi(api:any) {
        //if(typeof api === "function")
        //    this._api = api();
        //else
            this._api = api;
        if(this._api)
            Dependency.targetObject[this.name] = this._api;
    }
    async init(): Promise<void> {
        if (!this.subsystem)
            return Promise.resolve();
        if (this.state !== State.STARTED) {
            const subsystem = this.subsystem;
            this.state = State.STARTING;
            console.log("Starting", this.toString());
            const start = this.subsystem.start ?? (() => Promise.resolve());

            Promise.resolve(start(this.subsystem, this)).then(obj => {
                this.state = State.STARTED;
                if(obj)
                    this.setApi(obj);
                console.log("Started", this.toString());
                this.resolve(subsystem);
            }, reason => {
                console.error(`While starting ‘${this.name}’:`, reason);
                this.state = State.STOPPED;
                if (reason instanceof Error) {
                    reason.message = `In subsystem ‘${this.name}’: ${reason.message}`;
                    this.reject(reason);
                } else {
                    this.reject(new Error(`In subsystem ‘${this.name}’: ${reason}`));
                }
            });
        } else {
            console.warn("Already started", this);
            this.resolve(this.subsystem);
        }
    }
    static declare<T extends object>(name:string, prototype?:T):SubSysBuilder<T> {
        return new SubSysBuilder(name);
    }
}

export default { 
        debugAll: Dependency.debugAll,
        register: Dependency.register,
        get: Dependency.get,
        setup: Dependency.setup,
        waitFor: Dependency.waitFor,
        declare: Dependency.declare
}