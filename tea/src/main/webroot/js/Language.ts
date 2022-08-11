import Settings, { ConfigDict } from '../borb/Settings';
import { cloneDeep } from 'lodash-es';
import { LanguageSupport } from '@codemirror/language';
import { WorkerController } from './WorkerController';
import { WorkerConnection } from './WorkerConnection';
import { BorbTerminal } from '../borb/Terminal';
import { Messaging, Payload } from '../borb/Messaging';
import { SubSystem } from '../borb';
interface LanguageConfig extends ConfigDict {
    icon: string;
    extensions: string[];
    shellName: string;
    shellTitle: string;
    services: Record<string, string>;
    title: string;
    enabled: string;
    worker: boolean | string;
    scriptName: string;
}
export class Language {
    private _name: string;
    private _config: LanguageConfig;
    private _sharedWorker: boolean;
    private _mainTerminal: BorbTerminal;
    private _controller: WorkerController;
    private _connection: WorkerConnection;
    constructor(name: string, config?: LanguageConfig) {
        if (!name || !name.match(/^[a-zA-Z][a-zA-Z0-9_]*$/)) {
            throw new Error(`Illegal language name '${name}'`);
        }
        if (!config)
            config = Settings.getConfig<LanguageConfig>(
                'languages.' + name,
                null,
            );

        if (!config)
            throw new Error(`No language configuration found for ${name}`);

        this._name = name;
        this._config = cloneDeep(config);
        this._config.enabled = Settings.toLowerCase(config.enabled);
        this._sharedWorker = Settings.toLowerCase(config.worker) === 'shared';
    }

    get name() {
        return this._name;
    }

    get icon() {
        return this._config.icon || '';
    }
    get extensions(): string[] {
        return this._config.extensions || [];
    }
    get shellName() {
        return this._config.shellName || this._name + 'shell';
    }
    get shellTitle() {
        return (
            this._config.shellTitle ||
            this._config.shellName ||
            this._name + 'Shell'
        );
    }
    get title() {
        return this._config.title || this._name;
    }
    get services() {
        if (this._config.services) return cloneDeep(this._config.services);
        else return {};
    }
    get enabled() {
        return this._config.enabled || 'optional';
    }
    get worker() {
        return this._config.worker || false;
    }
    get useWorker() {
        return Settings.toBoolean(this._config.worker);
    }

    load(): Promise<Payload> {
        if (this.useWorker) {
            this._controller = new WorkerController(
                this._config.scriptName,
                this._sharedWorker,
                this.name,
            );
            this._mainTerminal = new BorbTerminal();
            this._mainTerminal.setAttribute('shell', this.shellName);
            this._mainTerminal.setAttribute('frame-title', this.shellTitle);
            this._mainTerminal.setAttribute('language', this.name);
            this._mainTerminal.setAttribute('icon', this.icon);
            this._connection = new WorkerConnection(
                this.shellName,
                Messaging,
                this._mainTerminal,
                this._controller,
            );
            return Messaging.send(this._config, 'langInit', this.shellName);
        } else {
            return Promise.reject(
                new Error(`Don't know how to load language ${this.name}`),
            );
        }
    }
    send(msg: Payload, msgType: string): Promise<Payload> {
        return Messaging.send(msg, msgType, this.shellName);
    }
}
export const Languages = {
    _id: 'Language',
    _revision: 0,
    langs: new Map<string, Language>(),
    byExtension(ext: string): Language {
        for (const [k, v] of Languages.langs) {
            if (v.extensions.indexOf(ext) >= 0) return v;
        }
    },
    Language,
};

SubSystem.declare(Languages).depends('dom', Settings, Messaging).register();
