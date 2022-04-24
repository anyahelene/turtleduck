package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.async.Async;
import turtleduck.async.Async.Sink;
import turtleduck.messaging.Connection;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.InputService;
import turtleduck.messaging.Message;
import turtleduck.messaging.Reply;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.InputServiceProxy;
import turtleduck.tea.FileSystem.TDFile;
import turtleduck.tea.generated.CMDispatch;
import turtleduck.tea.generated.CanvasDispatch;
import turtleduck.tea.generated.EditorDispatch;
import turtleduck.tea.generated.ExplorerDispatch;
import turtleduck.tea.generated.FileServiceDispatch;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;
import turtleduck.util.Logging;
import static turtleduck.tea.HTMLUtil.*;

public class Client implements JSObject, ClientObject {
	public static final Logger logger = Logging.getLogger(Client.class);
	protected static Client client;

	public static JSMapLike<JSObject> WINDOW_MAP;
	protected JSMapLike<JSObject> map;
	protected Router router;
	protected SockJSConnection sockConn;
	public InputService inputService;

	// protected TDEditor editor;
	private int nextChannelId = 2;
	protected String sessionName;
	private Explorer javaExplorer;
	protected CanvasServer canvas;
	protected EditorServer editorImpl;
	protected FileServer fileServer;
	private int lastMessageIntervalId;

//	public TextCursor cursor;
	private CMTerminalServer jterminal;
	private CMTerminalServer chatTerminal;
	protected OldFileSystem oldFileSystem;
	protected FileSystem fileSystem;
	protected History history;
	private Component shellComponent;
	private Shell jshell;
	private Shell pyshell;
	private PyConnection pyConn;
	private CMTerminalServer pyterminal;
	private Component screenComponent;
	private String projectName;
	private Explorer pyExplorer;
	private Shell chat;
	private Shell markdown;

	public void initialize() {
		try {
			JSObject jsobj = WINDOW_MAP.get("turtleduck");
			if (jsobj != null) {
				logger.info("Found turtleduck map: {}", jsobj);
				map = jsobj.cast();
			} else {
				map = JSObjects.create().cast();
				logger.info("Created turtleduck map: {}", map);
			}
			map.set("actions", (KeyCallback) this::action);
			history = new History(map.get("history").cast());
			router = new Router();

			fileSystem = new FileSystem(map.get("fileSystem"));
			fileServer = new FileServer(fileSystem);
			router.route(new FileServiceDispatch(fileServer));

			if (getConfig("connections.remote-turtleduck.enabled", "").equals("always")) {
				goOnline();
			}
			Dict conns = getConfigDict("connections", Dict.create());
			logger.info("Connections: {}", conns);
			Dict langs = getConfigDict("languages", Dict.create());
			logger.info("Languages: {}", langs);

			setupLanguages();

			Storage localStorage = Storage.getLocalStorage();

			oldFileSystem = new OldFileSystem(localStorage);
			Browser.document.addEventListener("visibilitychange", e -> {
				logger.info("Document visibility: " + Browser.visibilityState());
			}, false);

			Browser.window.addEventListener("pagehide", e -> {
				logger.info("Window hidden: {}", e);
			}, false);

			Component wm = map.get("wm").cast();

			HTMLElement shellElt = Browser.document.getElementById("shell");
			if (shellElt != null) {
				this.shellComponent = JSUtil.createComponent(shellElt);
				shellComponent.setTitle("Shell");
				shellComponent.addWindowTools();
				shellComponent.setParent(wm);
				shellComponent.register();
				map.set("shellComponent", shellComponent);

			}

			sessionName = getConfig("session.name", "?");
			projectName = getConfig("session.project", null);

			editorImpl = new EditorServer(Browser.document.getElementById("editor"), wm);
			editorImpl.initialize();
			router.route(new EditorDispatch(editorImpl));
			map.set("editor", editorImpl.editor);

			/*
			 * JSUtil.declare("loadJava", this::loadJava); JSUtil.declare("loadPython",
			 * this::loadPython); JSUtil.declare("goOnline", this::goOnline);
			 * JSUtil.declare("userlog", (JSStringConsumer) this::userlog);
			 */ map.set("client", (ClientObject) this);

			HTMLElement screenElt = Browser.document.getElementById("screen");
			if (screenElt != null) {
				this.screenComponent = JSUtil.createComponent(screenElt);
				screenComponent.setTitle("Display");
				screenComponent.addWindowTools();
				screenComponent.setParent(wm);
				screenComponent.register();
			}
			canvas = new CanvasServer(screenComponent);
			router.route(new CanvasDispatch(canvas));

			updateInfo();
			loadLanguages();

			WINDOW_MAP.set("turtleduck", map);
			// DocDisplay docDisplay = new DocDisplay(screenComponent);
			// docDisplay.initFromUrl("doc/TODO-PROJECTS.md", "TODO", true);
			if (true) {
				DocDisplay docDisplay2 = new DocDisplay(screenComponent);
				docDisplay2.initFromUrl("examples/ua-py/ch1.md", null, true);
			}
//		ws.setOnClose(() -> NativeTScreen.consoleLog("NO CARRIER"));
//		ws.setOnData((data) -> terminal.write(data));
			updateInfo();
			JSUtil.initializationComplete(null);
		} catch (Throwable ex) {
			JSUtil.initializationComplete("Startup failed: " + ex.getMessage());
			logger.error("Client failed: ", ex);
			throw ex;
		}
	}

	public void setupLanguages() {
		Dict dict = getConfigDict("languages", Dict.create());
		HTMLElement menu = Browser.document.getElementById("language-menu");
		dict.forEach(key -> {
			String id = key.key();
			Dict lang = dict.getDict(id);
			Language def = new Language(id, lang);
			Languages.LANGUAGES.put(id, def);
			for (var e : def.extensions) {
				Languages.LANGUAGES_BY_EXT.put(e, def);
				logger.info("language: {} => {}", e, id);
			}
			for (var e : def.shellExtensions) {
				Languages.LANGUAGES_BY_EXT.put(e, def);
				logger.info("language: {} => {} (shell)", e, id);
			}
			if (def.addToMenu && menu != null) {
				HTMLElement item = element("li", clazz("menu-entry"), //
						attr("data-language", id), attr("data-title", def.title), attr("data-icon", def.icon),
						def.title + " " + def.icon);
				menu.appendChild(item);
			}
		});
	}

	public void loadLanguages() {
		for(Entry<String, Language> entry : Languages.LANGUAGES.entrySet()) {
			if (entry.getValue().isEnabled()) {
				loadLanguage(entry.getValue().id);
			}
			
		}
	}

	public String getConfig(String option, String def) {
		try {
			JSObject value = JSUtil.getConfig(option);
			if (value == null || JSObjects.isUndefined(value))
				return def;
			else
				return ((JSString) value).stringValue();
		} catch (Throwable t) {
			logger.error("oops: {}", t);
			return def;
		}
	}

	public boolean getConfig(String option, boolean def) {
		try {
			JSObject value = JSUtil.getConfig(option);
			if (value == null || JSObjects.isUndefined(value))
				return def;
			else
				return ((JSBoolean) value).booleanValue();
		} catch (Throwable t) {
			logger.error("oops: {}", t);
			return def;
		}
	}

	protected JSObject getConfigObj(String option) {
		try {
			JSObject value = JSUtil.getConfig(option);
			if (value == null || JSObjects.isUndefined(value))
				return null;
			else
				return value;
		} catch (Throwable t) {
			logger.error("oops: {}", t);
			return null;
		}
	}

	public Array getConfigs(String option, Array def) {
		try {
			JSObject obj = getConfigObj(option);
			if (JSUtil.isArray(obj)) {
				return JSUtil.decodeArray((JSArray<?>) obj);
			} else {
				Array result = Array.create();
				String[] names = JSObjects.getOwnPropertyNames(obj);
				for (String n : names)
					result.add(n);
				return result;
			}
		} catch (Throwable t) {
			logger.error("oops: {}", t);
			return def;
		}
	}

	public Dict getConfigDict(String option, Dict def) {
		try {
			return JSUtil.decodeDict((JSMapLike<?>) getConfigObj(option));

		} catch (Throwable t) {
			logger.error("oops: {}", t);
			return def;
		}
	}

	public void goOnline() {
		String config = getConfig("connections.remote-turtleduck.enabled", "optional");
		if (!(config.equals("always") || config.equals("optional")))
			return;
		if (sockConn == null) {
			sockConn = new SockJSConnection("remote-turtleduck", this);
			map.set("remoteTurtleduck", sockConn.map());
			sockConn.addHandlers("Client", this::connected, this::disconnected);
			inputService = new InputServiceProxy(sockConn.id(), router);
			router.connect(sockConn, "default", "jshell", "$remote");
			sockConn.connect();
		} else {
			sockConn.connect();
		}

	}

	public boolean loadLanguage(String lang) {
		Language language = Languages.LANGUAGES.get(lang);
		if (lang == null) {
			logger.warn("Language '{}' not defined in config.json", lang);
			return false;
		}
		if (lang.equals("java")) {
			loadJava(language);
			return true;
		} else if (lang.equals("python")) {
			loadPython(language);
			return true;
		} else if (lang.equals("markdown")) {
			loadMarkdown(language);
			return true;
		} else if (lang.equals("chat")) {
			loadChat(language);
			return true;
		}
		return false;
	}

	public void loadChat(Language lang) {
		String config = getConfig("languages.chat.enabled", "optional");
		if (!(config.equals("always") || config.equals("optional")))
			return;

		if (chat == null) {
			ChatConnection chatConnection = new ChatConnection("local-chat", this);
			router.connect(chatConnection, "chat");
			chat = new Shell(lang, chatConnection);
			chatTerminal = new CMTerminalServer(shellComponent, chat);
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

			jterminal = new CMTerminalServer(shellComponent, jshell);
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

	public void loadGeneric(String name) {
		editorImpl.initializeLanguage(name);
	}

	public void loadPython(Language lang) {
		String config = getConfig("languages.python.enabled", "optional");
		if (!(config.equals("always") || config.equals("optional")))
			return;
		if (pyConn == null) {
			pyConn = new PyConnection("local-python", this);
			map.set("localPython", pyConn.map());
			router.connect(pyConn, "pyshell");
			router.route("python_status", msg -> {
				boolean wait = msg.content().get("wait", false);
				userlog(msg.content(Reply.STATUS), wait);
				if (pyterminal != null) {
					pyterminal.cursor().println(msg.content(Reply.STATUS));
				}
				return null;
			});
			pyConn.connect();
		}
		if (pyshell == null) {
			Client.client.userlog("Initializing Python environment...", true);
			pyshell = new Shell(lang, pyConn);
			pyterminal = new CMTerminalServer(shellComponent, pyshell);
			pyterminal.initialize("pyshell");
			map.set("pyshell", pyterminal.editor);
			map.set("pyterminal", pyterminal);
			router.route(new CMDispatch(pyterminal));

			HTMLElement exElt = Browser.document.getElementById("explorer");
			if (exElt != null) {
				pyExplorer = new Explorer(exElt, map.get("wm").cast(), pyshell.service());
				router.route(new ExplorerDispatch(pyExplorer));
			}

			String installs = getConfigs("languages.python.install", Array.create()).toListOf(String.class).stream()
					.map(s -> "await micropip.install('" + s + "')\n").collect(Collectors.joining());

			String imports = "[" + getConfigs("languages.python.import", Array.create()).toListOf(String.class).stream()//
					.map(s -> "'" + s + "'").collect(Collectors.joining(",")) + "]";

			String extraInits = getConfigs("languages.python.init", Array.create()).toListOf(String.class).stream()
					.map(s -> s + "\n").collect(Collectors.joining());
			Message msg = Message.writeTo("pyshell", "init_python")//
					.putContent(ShellService.CODE, "import micropip\n"//
							+ "import unthrow\n"//
							// + "import PIL\n"//
							+ "await micropip.install('../py/turtleduck-0.1.1-py3-none-any.whl')\n"//
							+ installs//
							+ "from pyodide import to_js\n"//
							+ "from turtleduck import ShellService\n"//
							+ extraInits//
							+ "ShellService.setup_io('pyshell')\n"//
							+ "ShellService.use_msg_io()\n"//
							+ "ShellService.do_imports(" + imports + ")\n" + "print(ShellService.banner())")
					.done();
			router.send(msg).onSuccess(res1 -> {
				pyshell.logger.info("python init result: {}", res1);
				Client.client.userlog("Python ready.");
				editorImpl.initializeLanguage(pyshell, pyterminal.console());
				if (isDesktop()) {
					pyterminal.focus();
				}
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
		}
	}

	public boolean isDesktop() {
		JSBoolean isDesktop = map.get("isDesktop").cast();
		Browser.consoleLog("isDesktop: ", isDesktop);
		return isDesktop != null && isDesktop.booleanValue();
	}

	public static void main(String[] args) {
		Logging.setLogDest(JSUtil::logger);
		Logging.useCustomLoggerFactory();
		WINDOW_MAP = Window.current().cast();
		client = new Client();
		client.initialize();
	}

	protected void connected(Connection conn, Dict msg) {
		String username = msg.get(HelloService.USERNAME);
		Dict userInfo = null;
		try {
			userInfo = msg.get(HelloService.USER);
			JSMapLike<JSObject> configMap = JSObjects.create();
			configMap.set("user", JSUtil.encode(userInfo));
			JSUtil.setConfig(configMap, "remote");
			JSUtil.saveConfig();
		} catch (Throwable ex) {
			logger.error("failed to get userInfo:", ex);
		}
		try {
			if (!sessionName.contains("?")) {
				JSMapLike<JSObject> configMap1 = JSObjects.create();
				JSMapLike<JSObject> configMap2 = JSObjects.create();
				configMap2.set("name", JSString.valueOf(sessionName));
				configMap1.set("session", configMap2);
				JSUtil.setConfig(configMap1, "session");
				JSUtil.saveConfig();
			}
		} catch (Throwable ex) {
			logger.error("failed to get userInfo:", ex);
		}
		router.init(sessionName, username); // TODO: escapes
		Storage sess = map.get("sessionStorage").cast();
		if (sess != null) {
			sess.setItem("turtleduck.username", username);
		}
	}

	protected void updateInfo() {
		String userName = getConfig("user.nickname", getConfig("user.username", null));
		HTMLElement status = Browser.document.getElementById("status");
		HTMLElement statusBtn = Browser.document.getElementById("status-button");
		if (status != null && statusBtn != null) {
			if (sockConn != null) {
				if (sockConn.socketConnected) {
					status.setClassName("online");
					statusBtn.withText("🖧 ONLINE");
				} else {
					status.setClassName("offline");
					statusBtn.withText("OFFLINE");
				}
				statusBtn.setAttribute("title", userName + "@" + sockConn.toString());
			} else {
				if (getConfig("session.private", false)) {
					status.setClassName("private");
					statusBtn.withText("PRIVATE");
				} else if (getConfig("session.offline", false)) {
					status.setClassName("offline");
					statusBtn.withText("OFFLINE");
				} else {
					status.setClassName("");
					statusBtn.withText("OFFLINE");
				}
				statusBtn.setAttribute("title", userName);
			}
		}

		HTMLElement imgBox = Browser.document.getElementById("user-picture");
		String imgUrl = getConfig("user.picture", null);
		if (imgBox != null && imgUrl != null) {
			imgBox.setAttribute("src", imgUrl);
			imgBox.getStyle().setProperty("visibility", "visible");
		}
		HTMLElement nameBox = Browser.document.getElementById("user-name");
		if (nameBox != null) {
			nameBox.withText(userName); // TODO: escapes
		}

		JSUtil.updateInfo();
	}

	protected void disconnected(Connection conn) {
		HTMLElement status = Browser.document.getElementById("status");
		if (status != null) {
			status.setClassName("active offline");
		}
		HTMLElement statusBtn = Browser.document.getElementById("status-button");
		if (statusBtn != null) {
			statusBtn.withText("OFFLINE");
		}
	}

	public void userlog(String message) {
		userlog(message, false);
	}

	public void userlogWait(String message) {
		userlog(message, true);
	}

	private void userlog(String message, boolean wait) {
		HTMLElement log = Browser.document.getElementById("last-message");
		if (log != null) {
			if (lastMessageIntervalId != 0)
				Window.clearInterval(lastMessageIntervalId);
			logger.info("userlog({})", message);
			log.withText(message).withAttr("data-wait", String.valueOf(wait)).setClassName("");
			if (wait) {
				String[] dots = { "" };
				lastMessageIntervalId = Window.setInterval(() -> {
					dots[0] = dots[0] + ".";
					log.withText(message + dots[0]);
				}, 1000);
			} else {
				lastMessageIntervalId = Window.setTimeout(() -> {
					log.setClassName("hidden");
				}, 3000);
			}
		}
	}

	Storage storage() {
		return map.get("localStorage").cast();
	}

	void withStorage(Consumer<Storage> fun) {
		Storage s = map.get("localStorage").cast();
		if (s != null)
			fun.accept(s);
	}

	Promise<JSObject> action(String key, JSMapLike<JSObject> data, Event ev) {
		logger.info("action: {}, {}", key, ev);
		if (key.equals("f1")) {
			Async<Dict> async = editorImpl.save(ev);
			Promise<JSObject> p;
			if (async != null) {
				logger.info("making promise: {}", async);
				p = Promise.Util.promise((resolve, reject) -> {
					async.onComplete(dict -> resolve.accept(JSUtil.encode(dict)),
							dict -> reject.accept(JSUtil.encode(dict)));
				});
			} else {
				p = Promise.Util.resolve(JSString.valueOf("no result"));
			}
			logger.info("promise {}", p);
			return p;
		} else if (key.equals("f2")) {
		} else if (key.equals("f4")) {
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
		} else if (key.startsWith("f")) {
			JSObject button = data.get("button");
			if (button != null && Math.random() < .5) {
				HTMLElement elt = (HTMLElement) button;
				JSUtil.addClass(elt, "disappear");
			} else {
				JSUtil.displayHint("Warning", "Please do not press this button again.", "", "warning");
			}
			userlog("Sorry! Not implemented. 😕");
			return Promise.Util.resolve(JSString.valueOf("unknown key:" + key));
		}
		return Promise.Util.resolve(JSString.valueOf("unknown key:" + key));
	}

//	<T> T withStorage(Function<Storage, T> fun) {
//		Storage s = map.get("localStorage").cast();
//		if (s != null)
//			return fun.apply(s);
//		else
//			return null;
//	}

	protected int nextChannelId() {
		int id = nextChannelId;
		nextChannelId += 2;
		return id;
	}

//	Async<Dict> sendToServer(Message msg) {
//		return router.send(sockConn.id(), msg);
//	}

	void showHeap(Dict msg) {
		int heapUse = msg.get(ShellService.HEAP_USE, 0);
		int heapTot = msg.get(ShellService.HEAP_TOTAL, 0);
		if (heapUse > 0 && heapTot > 0) {
			String unit = "k";
			if (heapTot > 9999) {
				heapUse /= 1024;
				heapTot /= 1024;
				unit = "M";
			}
			HTMLElement elt = Window.current().getDocument().getElementById("heapStatus");
			if (elt != null)
				elt.withText(String.format("%d%s / %d%s", heapUse, unit, heapTot, unit));
		}
	}

	public void route(String msgType, MessageHandler handler) {
		// Dict d = JSUtil.decodeDict(data);
		// turtleduck.messaging.Message msg = turtleduck.messaging.Message.fromDict(d);

		Function<Message, Async<Message>> jh = msg -> {
			JSMapLike<?> obj = JSUtil.encode(msg.toDict());
			Sink<Message> sink = Async.create();

			handler.handle(obj).then(res -> {
				if (res != null && !JSObjects.isUndefined(res)) {
					logger.info("result: {}", res);
					sink.success(Message.fromDict(JSUtil.decodeDict(res)));
				} else {
					sink.success(null);
				}
				return res;
			}).onRejected(err -> {
				sink.fail(JSUtil.decodeDict((JSMapLike<?>)err.cast()));
			});
			return sink.async();
		};
		router.route(msgType, jh);
	}
}

@JSFunctor
interface MessageHandler extends JSObject {
	Promise<JSMapLike<?>> handle(JSMapLike<?> msg);
}

@JSFunctor
interface ClientObject extends JSObject {

	boolean loadLanguage(String name);

	void setupLanguages();

	boolean isDesktop();

	void goOnline();

	void userlog(String msg);

	void route(String msgType, MessageHandler handler);
}
