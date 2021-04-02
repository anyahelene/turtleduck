package turtleduck.tea;

import static turtleduck.tea.Browser.trying;

import java.util.function.Consumer;

import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Navigator;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.websocket.CloseEvent;

import ace.AceEditor;
import turtleduck.messaging.CodeService;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.InputService;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.CodeServiceProxy;
import turtleduck.messaging.generated.HelloServiceProxy;
import turtleduck.messaging.generated.InputServiceProxy;
import turtleduck.messaging.generated.ShellServiceProxy;
import turtleduck.tea.generated.CanvasDispatch;
import turtleduck.tea.generated.EditorDispatch;
import turtleduck.tea.generated.ExplorerDispatch;
import turtleduck.tea.generated.TerminalDispatch;
import turtleduck.tea.net.SockJS;
import turtleduck.text.TextWindow;
import turtleduck.util.Dict;
import xtermjs.Terminal;

public class Client implements JSObject {
protected static Client client;

	public static JSMapLike<JSObject> WINDOW_MAP;
	protected JSMapLike<JSObject> map;
	protected TeaRouter router;
	SockJS socket;
	protected boolean socketConnected = false;
	private Terminal terminal;
	public ShellService shellService;
	public HelloService welcomeService;
	public InputService inputService;
	public CodeService codeService;

	protected AceEditor editor;
	private int nextChannelId = 2;
	private String sessionName;
	protected TerminalClient terminalClient;
	private Explorer explorer;
	protected CanvasServer canvas;
	private EditorServer editorImpl;
	private int reconnectIntervalId;
	private int reconnectInterval = 2000;

	public void initialize() {
		try {
			JSObject jsobj = WINDOW_MAP.get("turtleduck");
			if (jsobj != null) {
				Browser.consoleLog("Found turtleduck map:");
				Browser.consoleLog(jsobj);
				map = jsobj.cast();
			} else {
				map = JSObjects.create().cast();
				Browser.consoleLog("Created turtleduck map:");
				Browser.consoleLog(map);
			}
			map.set("actions", (KeyCallback) this::action);
			Storage localStorage = Storage.getLocalStorage();

			TextWindow window = new NativeTTextWindow(terminal);

			Browser.document.addEventListener("visibilitychange", e -> {
				Browser.consoleLog("Document visibility: " + Browser.visibilityState());
			}, false);

			Browser.window.addEventListener("pagehide", e -> {
				Browser.consoleLog("Window hidden: ", e);
			}, false);
			HTMLElement xtermjsWrap = Browser.document.getElementById("xtermjs-wrap");
			terminalClient = new TerminalClient(xtermjsWrap, "jshell", this);
			terminalClient.initialize();

			sessionName = ((JSString) map.get("sessionName")).stringValue();
			router = new TeaRouter(sessionName, "", this);
			router.route(new TerminalDispatch(terminalClient));

			explorer = new Explorer();
			router.route(new ExplorerDispatch(explorer));

			canvas = new CanvasServer();
			router.route(new CanvasDispatch(canvas));

			map.set("client", this);
			welcomeService = new HelloServiceProxy("turtleduck.server", router::send);
			shellService = new ShellServiceProxy("turtleduck.shell.server", router::send);
			codeService = new CodeServiceProxy("turtleduck.code.server", router::send);
			inputService = new InputServiceProxy("turtleduck.input", router::send);

			socket = SockJS.create("socket?session=" + JSUtil.encodeURIComponent(sessionName));
			map.set("socket", socket);
			socket.onClose(trying(this::disconnect));
			socket.onOpen(trying(this::connect));

			HTMLElement editor = Browser.document.getElementById("editor");
			HTMLElement tabs = Browser.document.getElementById("editor-tabs");
			HTMLElement wrapper = Browser.document.getElementById("editor-wrap");
			editorImpl = new EditorServer(editor, wrapper, tabs, "editor", this, shellService);
			editorImpl.initialize();
			router.route(new EditorDispatch(editorImpl));
			// Route route = router.route();
//			route.registerHandler("welcome", (msg,r) -> Welcome.accept(msg, this));
//		terminal.onData((d) -> {sockJS.send(d);});
			WINDOW_MAP.set("turtleduck", map);
			JSUtil.export("eval", this::eval);
//		ws.setOnClose(() -> NativeTScreen.consoleLog("NO CARRIER"));
//		ws.setOnData((data) -> terminal.write(data));
		} catch (Throwable ex) {
			Browser.addError(ex);
			throw ex;
		}
	}

	public void eval(JSString code) {
		shellService.eval(code.stringValue(), terminalClient.lineNum++, Dict.create());
	}
	public static void main(String[] args) {
		WINDOW_MAP = Window.current().cast();
		client = new Client();
		client.initialize();
	}

	protected void connect(Event ev) {
		Browser.consoleLog("Connect: " + ev.getType() + ", " + router);
		socketConnected = true;
		router.connect(socket);
		reconnectInterval = 2000;
		HTMLElement status = Browser.document.getElementById("status");
		if (status != null) {
			status.setClassName("active online");
		}
		status = Browser.document.getElementById("status-button");
		if (status != null) {
			status.withText("ONLINE");
		}
		welcomeService.hello(sessionName, Dict.create()).onSuccess(msg -> {
			Browser.consoleLog("Received welcome: " + msg);
			String username = msg.get(HelloService.USERNAME);
			username(username);
			String ex = msg.get(HelloService.EXISTING) ? "existing" : "new";
			if (terminalClient != null)
				terminalClient.connected(username, ex, sessionName);

		});

	}

	protected void disconnect(CloseEvent ev) {
		if (socketConnected) {
			Browser.consoleLog("Disconnect: " + router + ", retry " + reconnectInterval);
			if (terminalClient != null)
				terminalClient.disconnected("");
			socketConnected = false;
		} else
			Browser.consoleLog("Connection failed: " + router + ", retry " + reconnectInterval);
		router.disconnect();
		socket = null;
		map.set("socket", null);
		HTMLElement status = Browser.document.getElementById("status");
		if (status != null) {
			status.setClassName("active offline");
		}
		HTMLElement statusBtn = Browser.document.getElementById("status-button");
		if (statusBtn != null) {
			statusBtn.withText("OFFLINE");
		}

		reconnectIntervalId = Window.setInterval(() -> {
			Browser.consoleLog("Retrying SockJS connection...");
			if (statusBtn != null) {
				statusBtn.withText("Connecting...");
			}
			Window.clearInterval(reconnectIntervalId);
			reconnectInterval *= 1.5;
			socket = SockJS.create("socket?session=" + JSUtil.encodeURIComponent(sessionName));
			socket.onClose(trying(this::disconnect));
			socket.onOpen(trying(this::connect));
			map.set("socket", socket);
		}, reconnectInterval);

	}

	public void username(String name) {
		map.set("username", JSString.valueOf(name));
		router.init(sessionName, name);
		Storage sess = map.get("sessionStorage").cast();
		if (sess != null) {
			sess.setItem("turtleduck.username", name);
		}
		HTMLElement status = Browser.document.getElementById("status-button");
		if (status != null) {
			status.setAttribute("title", name + "@" + socket.url());
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

	void action(String key, Event ev) {
		Browser.consoleLog("action: " + key);
		if (key.equals("f1")) {
			editorImpl.save(ev);
		} else if (key.equals("f2")) {
			terminalClient.lineHandler(editorImpl.content());
		}
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
}
