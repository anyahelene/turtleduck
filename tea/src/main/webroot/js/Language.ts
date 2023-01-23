import Settings, { ConfigDict } from '../borb/Settings';
import { cloneDeep, isMatch } from 'lodash-es';
import { WorkerConnection } from './WorkerConnection';
import { BorbTerminal } from '../borb/Terminal';
import { Connection, Messaging, Payload } from '../borb/Messaging';
import { SubSystem } from '../borb';
import { Shell } from './Shell';
import { turtleduck } from './TurtleDuck';
import path from 'path';
import { SockJSConnection } from './SockJSConnection';
import { WebSocketConnection } from './WebSocketConnection';
import URI from 'urijs';

export interface LanguageConfig extends ConfigDict {
    icon: string;
    extensions: string[];
    shellName: string;
    shellTitle: string;
    services: Record<string, string>;
    title: string;
    enabled: string;
    editMode: string;
    worker?: string;
    builtin?: string;
    remote?: string;
    shared?: boolean;
    transport?: string;
}
export interface LanguageConnection extends Connection {
    connect(): Promise<string>;
}

export interface LangInit {
    config: LanguageConfig;
    terminal: string;
    explorer: string;
    session: string;
}

export class Language {
    private _name: string;
    private _config: LanguageConfig;
    private _mainTerminal: BorbTerminal;
    private _connection: LanguageConnection;
    private _mainShell: Shell;
    private _initPromise: Promise<Language>;
    private _initRes: [(value: Language | PromiseLike<Language>) => void, (reason?: Error) => void];
    private _loading = false;
    constructor(name: string, config?: LanguageConfig) {
        if (!name || !name.match(/^[a-zA-Z][a-zA-Z0-9_]*$/)) {
            throw new Error(`Illegal language name '${name}'`);
        }
        if (!config) config = Settings.getConfig<LanguageConfig>('languages.' + name, null);

        if (!config) throw new Error(`No language configuration found for ${name}`);

        this._name = name;
        this._config = cloneDeep(config);
        this._config.enabled = Settings.toLowerCase(config.enabled);
        if (this._config.remote) {
            this._config.transport = Settings.toLowerCase(config.transport) || 'websocket';
        }
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
    get editMode() {
        return this._config.editMode || this._name;
    }
    get shellTitle() {
        return this._config.shellTitle || this._config.shellName || this._name + 'Shell';
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
    get shared() {
        return !!this._config.shared;
    }
    get isLoaded() {
        return !!this._mainShell;
    }
    get promise() {
        return this._initPromise;
    }
    async load(connection?: LanguageConnection, background = false): Promise<Language> {
        if (this._loading) return this.promise;
        if (this._mainShell) return this.promise;
        this._initPromise = new Promise((resolve, reject) => {
            this._initRes = [resolve, reject];
        });
        this._loading = true;
        if (connection) this._connection = connection;
        if (
            !this._connection &&
            !(this._config.worker || this._config.builtin || this._config.remote)
        ) {
            const err = new Error(`Don't know how to load language ${this.name}`);
            this._initRes[1](err);
            return Promise.reject(err);
        }
        try {
            this._mainShell = new Shell(this);
            await this._mainShell.init(this, this.shellName);

            Messaging.route(`${this._name}_status`, (msg: { wait?: boolean; status?: string }) => {
                const wait = !!msg.wait;
                if (typeof msg.status === 'string') {
                    console.log(msg);
                    turtleduck.userlog(msg.status, wait);
                    this._mainTerminal.println(msg.status);
                }
                return Promise.resolve({});
            });
            Messaging.route(`${this._name}_error`, (msg: { status?: string }) => {
                if (typeof msg.status === 'string') {
                    console.error(msg);
                    turtleduck.userlog(msg.status);
                    this._mainTerminal.println(msg.status);
                }
                return Promise.resolve({});
            });
            if (this._config.builtin) {
                this._connection = turtleduck.builtinLanguages[this._config.builtin];
            } else if (this._config.worker) {
                const evalId = `${this.shellName}_worker`;
                this._connection = new WorkerConnection(
                    Messaging,
                    evalId,
                    this._config.worker,
                    this._config.shared,
                    this.name,
                );
            } else if (this._config.remote) {
                const evalId = `${this.shellName}_socket`;
                if (this._config.transport === 'sockjs') {
                    this._connection = new SockJSConnection(
                        Messaging,
                        evalId,
                        this._config.remote,
                        this.name,
                    );
                } else if ((this._config.transport = 'websocket')) {
                    const address = new URI(this._config.remote, document.URL).normalize();
                    const protocol = address.protocol() === 'https' ? 'wss' : 'ws';
                    console.log(address, protocol);
                    this._connection = new WebSocketConnection(
                        Messaging,
                        evalId,
                        address.protocol(protocol).toString(),
                        this.name,
                    );
                }
            }
            this._connection.addHandlers(
                'Language',
                (conn, info) => console.log('Connected: ', conn, info),
                (conn) => {
                    console.log('Disconnected: ', conn);
                    this._connection = null;
                    if (this._mainShell) {
                        this._mainShell.close();
                        this._mainShell = null;
                    }
                    this._loading = false;
                },
            );
            await this._connection.connect();
            this._mainTerminal = this._mainShell.terminal;
            this._mainShell.mountTerminal();
            if (!background) this._mainTerminal.select();
            await Messaging.send(
                {
                    config: this._config,
                    terminal: this._mainShell.id,
                    explorer: `${this.shellName}_explorer`,
                    session: Settings.getConfig('session.name', ''),
                },
                'langInit',
                this._connection.id,
            );
            this._initRes[0](this);
        } catch (e) {
            this._mainShell = null;
            this._loading = false;
            if (this._connection) {
                this._connection.close();
                this._connection = null;
            }
            this._initRes[1](e);
        }
        return this.promise;
    }

    get connectionId() {
        return this._connection?.id;
    }
    set connection(conn: LanguageConnection) {
        this._connection = conn;
    }
    send(msg: Payload, msgType: string): Promise<Payload> {
        return Messaging.send(msg, msgType, this.shellName);
    }

    addFileExt(pathName: string) {
        if (!path.extname(pathName) && this.extensions[0]) {
            return `${pathName}.${this.extensions[0]}`;
        } else {
            return pathName;
        }
    }

    toString() {
        return this.name;
    }

    get mainShell() {
        return this._mainShell;
    }

    get mainTerminal() {
        return this._mainTerminal;
    }
}
export const Languages = {
    _id: 'Language',
    _revision: 0,
    languages: new Map<string, Language>(),
    byExtension(ext: string): Language {
        for (const [k, v] of Languages.languages) {
            if (v.extensions.indexOf(ext) >= 0) return v;
        }
    },
    Language,
    get(name: string) {
        let l = Languages.languages.get(name);
        if (!l) {
            l = new Language(name);
            Languages.languages.set(name, l);
        }
        return l;
    },
    async create(name: string, conn?: LanguageConnection, background = false) {
        const l = Languages.get(name);
        if (!l.isLoaded) {
            await l.load(conn, background);
        }
        return l;
    },
    detect(
        pathOrText: { path: string; text?: string } | { text: string; path?: string },
    ): Language {
        console.log('detect:', pathOrText);
        if (pathOrText.path) {
            const ext = path.extname(pathOrText.path).substring(1);
            console.log('ext', ext);
            if (ext) return Languages.byExtension(ext);
        }
        return undefined;
    },
};

SubSystem.declare(Languages).depends('dom', Settings, Messaging).register();

/*

				JSUtil.changeButton("f9", "🐍", "Python ↓");
				lang.enable();
				pyshell.service().refresh();
			}).onFailure(err -> {
				logger.error("failed to initialize python: {}", err);
			});
			Camera.Statics.addSubscription("qpaste:pyshell", "builtin", "qr", "→ PyShell", "📋",
					"Paste in Python Shell");
			Camera.Statics.addSubscription("receive_str", "pyshell", "qr", "→ Python", "🐍",
					"Store in Python variable");
			Camera.Statics.addSubscription("receive_img", "pyshell", "camera", "→ Python", "🐍",
					"Store in Python variable");
		} else {
			if (isDesktop()) {
				pyterminal.focus();
			}
			JSUtil.changeButton("f9", "🐍", "Python ↓");




                public void loadChat(Language lang) {
        String config = getConfig("languages.chat.enabled", "optional");
        if (!(config.equals("always") || config.equals("optional")))
            return;

        if (chat == null) {
            ChatConnection chatConnection = new ChatConnection("local-chat", this);
            router.connect(chatConnection, "chat");
            chat = new Shell(lang, chatConnection);
            chatTerminal = new CMTerminalServer(shellComponent.element(), chat);
            chatTerminal.disableHistory();
            chatTerminal.initialize("chat");
            map.set("chat", chatTerminal.editor);
            map.set("chatterminal", chatTerminal);
            router.route(new CMDispatch(chatTerminal));
            Dict welcome = Dict.create();
            welcome.put(HelloService.USERNAME, "T.Duck");
            welcome.put(HelloService.EXISTING, false);
            chatTerminal.connected(pyConn, welcome);
            chatConnection.chat("TurtleDuck", "Hi, and welcome to TurtleDuck!\n", 450);
            chatConnection.chat("TurtleDuck", "I'm kind of busy right now, please try the chat later.", 1500);
            HTMLElement notif = Browser.document.getElementById("chat-notification");
            notif.getStyle().setProperty("display", "none");
            lang.enable();
        }
    }


                if (def.addToMenu && menu != null) {
                HTMLElement item = element("li", clazz("menu-entry"), //
                        attr("data-language", id), attr("data-title", def.title), attr("data-icon", def.icon),
                        def.title + " " + def.icon);
                menu.appendChild(item);
            }



 
else if (key.equals("chat")) {
            if (chatTerminal == null) {
                loadLanguage("chat");
                chatTerminal.promptReady();
            }
            chatTerminal.editor.focus();
        } else if (key.equals("menu:languages")) {
            Dict d = JSUtil.decodeDict(data);
            String lang = d.get("language", "");
            if (!loadLanguage(lang)) {
                return Promise.Util.resolve(JSString.valueOf("unknown language:" + lang));
            }
            JSUtil.changeButton("f9", d.getString("icon"), d.getString("title") + " ↓");
        }


                public void loadMarkdown(Language lang) {
        String config = getConfig("languages.markdown.enabled", "optional");
        if (!(config.equals("always") || config.equals("optional")))
            return;
        if (markdown == null) {
            markdown = new Shell(lang, new MarkdownService(screenComponent), null);
            LanguageConsole console = null;
            if (pyterminal != null)
                console = pyterminal.console();
            else if (jterminal != null)
                console = jterminal.console();
            editorImpl.initializeLanguage(markdown, null);
            lang.enable();
        }
    }





            	public void loadJava(Language lang) {
		String config = getConfig("languages.java.enabled", "optional");
		if (!(config.equals("always") || config.equals("optional")))
			return;
		if (sockConn == null)
			goOnline();
		if (jshell == null) {
			Client.client.userlog("Initializing Java environment...", true);
			jshell = new Shell(lang, sockConn);

			jterminal = new CMTerminalServer(shellComponent.element(), jshell);
			jterminal.initialize("jshell");
			map.set("jshell", jterminal.editor);
			map.set("jterminal", jterminal);
			router.route(new CMDispatch(jterminal));

			HTMLElement exElt = Browser.document.getElementById("explorer");
			if (exElt != null) {
				javaExplorer = new Explorer(exElt, map.get("wm").cast(), jshell.service());
				router.route(new ExplorerDispatch(javaExplorer));
			}
			Camera.Statics.addSubscription("qpaste:jshell", "builtin", "qr", "→ JShell", "📋", "Paste in Java Shell");
			Camera.Statics.addSubscription("receive_str", "jshell", "qr", "→ Java", "☕", "Store in Java variable");
			Camera.Statics.addSubscription("receive_img", "jshell", "camera", "→ Java", "☕", "Store in Java variable");

			editorImpl.initializeLanguage(jshell, jterminal.console());
			lang.enable();
			Client.client.userlog("Java environment initialized");
			jshell.service().refresh();
		}
		if (isDesktop()) {
			jterminal.focus();
		}
		JSUtil.changeButton("f9", "☕", "Java ↓");

		// should send refresh on reconnect
//		if (msg.get(HelloService.EXISTING) && jshell != null)
//			jshell.service().refresh();
	}

            */
