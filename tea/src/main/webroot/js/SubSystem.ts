enum State {
    STOPPED, WAITING, STARTING, STARTED
}


export interface SubSystem {
    name: string;
    depends: Iterable<string>;
    start(dep: Dependency): Promise<void>;
    api : object;
}

export class Dependency {
    public static targetObject : object;
    private static counter = 0;
    private static systems = new Map<string, Dependency>();
    private promise: Promise<SubSystem>;
    private resolve: (value: SubSystem | PromiseLike<SubSystem>) => void = (v) => { };
    private reject: (reason: Error) => void = (r) => { };
    private deps: Set<Dependency> = new Set();
    private id: number = Dependency.counter++;
    
    private _name : string;
    public get name() : string {
        return this._name        ;
    }

    private _api : object;
    public get api() : object {
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
        Dependency.systems.forEach(dep => dep.debug());
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
    static register(sys: SubSystem) {
        const depsys = Dependency.get(sys.name);
        if (depsys.state !== State.STOPPED) {
            throw new Error(`Already registered: ${sys.name}`);
        }
        depsys.deps.clear();
        for (let d of sys.depends) {
            depsys.deps.add(Dependency.get(d));
        }

        depsys.state = State.WAITING;
        depsys.subsystem = sys;
        if(typeof sys.api === "function")
            depsys._api = sys.api();
        else
            depsys._api = sys.api;
        Dependency.targetObject[sys.name] = depsys._api;
        console.log("Registered", depsys.toString());
        return Promise.all([...depsys.deps].map(d => d.promise)).then(() => depsys.init(), reason => {
            console.error(`Dependencies failed for ${depsys.name}: ${reason}`);
        });
    }
    async init(): Promise<void> {
        if (!this.subsystem)
            return Promise.resolve();
        if (this.state !== State.STARTED) {
            const subsystem = this.subsystem;
            this.state = State.STARTING;
            console.log("Starting", this.toString());
            this.subsystem.start(this).then(_ => {
                this.state = State.STARTED;
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
