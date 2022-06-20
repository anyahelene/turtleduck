import { keys } from "lodash-es";

enum State {
    STOPPED, WAITING, STARTING, STARTED
}


export interface SubSystem {
    name: string;
    depends?: Iterable<string>;
    start?(dep: Dependency): Promise<object|void> | object | void;
    stop?(): Promise<void> | void;
    api? : object;
}

interface TargetObject {
    [propName : string]: object;
}

export class Dependency {
    public static targetObject : TargetObject;
    private static counter = 0;
    private static systems = new Map<string, Dependency>();
    private static queue : SubSystem[] = [];
    private promise: Promise<SubSystem>;
    private resolve: (value: SubSystem | PromiseLike<SubSystem>) => void = (v) => { };
    private reject: (reason: Error) => void = (r) => { };
    private deps: Set<Dependency> = new Set();
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
    private subsystem?: SubSystem;
    constructor(name: string) {
        this._name = name;
        this.promise = new Promise((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
        console.log("Created", this.toString());
    }

    dependencies(): Set<Dependency> {
        return new Set(this.deps);
    }

    dependents(): Set<Dependency> {
        let result: Set<Dependency> = new Set();
        Dependency.systems.forEach(sys => {
            if (sys.deps.has(this)) {
                result.add(sys);
            }
        });
        return result;
    }

    forEach(callback : (value:Dependency) => void) : void {
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

    static get(sysName: string): Dependency {
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
    static async waitFor(sysName : string): Promise<Dependency> {
        const dep = Dependency.get(sysName);
        await dep.promise;
        return Promise.resolve(dep);
    }
    static register(sys: SubSystem) {
        const depsys = Dependency.get(sys.name);
        if(!Dependency.targetObject) {
            Dependency.queue.push(sys);
            console.log(`System not ready, enqueing '${sys.name}'`);
            return;
        }
        if (depsys.state !== State.STOPPED) {
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
            Promise.resolve(start(this)).then(obj => {
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
}

export default { 
        debugAll: Dependency.debugAll,
        register: Dependency.register,
        get: Dependency.get,
        setup: Dependency.setup,
        waitFor: Dependency.waitFor
}