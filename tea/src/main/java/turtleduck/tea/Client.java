package turtleduck.tea;

import static turtleduck.tea.Browser.tryListener;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.teavm.jso.JSObject;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Navigator;
import org.teavm.jso.browser.Storage;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.websocket.CloseEvent;

import turtleduck.messaging.CodeService;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.InputService;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.CodeServiceProxy;
import turtleduck.messaging.generated.HelloServiceProxy;
import turtleduck.messaging.generated.InputServiceProxy;
import turtleduck.messaging.generated.ShellServiceProxy;
import turtleduck.tea.generated.CMDispatch;
import turtleduck.tea.generated.CanvasDispatch;
import turtleduck.tea.generated.EditorDispatch;
import turtleduck.tea.generated.ExplorerDispatch;
import turtleduck.tea.generated.TerminalDispatch;
import turtleduck.tea.net.SockJS;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;
import turtleduck.util.Dict;
import turtleduck.util.Logging;
import xtermjs.Terminal;

public class Client implements JSObject {
	public static final Logger logger = Logging.getLogger(Client.class);
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

	//protected TDEditor editor;
	private int nextChannelId = 2;
	protected String sessionName;
	protected TerminalClient terminalClient;
	private Explorer explorer;
	protected CanvasServer canvas;
	protected EditorServer editorImpl;
	private int reconnectIntervalId;
	private int reconnectInterval = 2000;

	public TextCursor cursor;
	private CMTerminalServer cmTerminalServer;
	protected FileSystem fileSystem;
	protected History history;

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
			Storage localStorage = Storage.getLocalStorage();

			TextWindow window = new NativeTTextWindow(terminal);

			fileSystem = new FileSystem(localStorage);
			Browser.document.addEventListener("visibilitychange", e -> {
				logger.info("Document visibility: " + Browser.visibilityState());
			}, false);

			Browser.window.addEventListener("pagehide", e -> {
				logger.info("Window hidden: {}", e);
			}, false);

//			HTMLElement xtermjsWrap = Browser.document.getElementById("xtermjs-wrap");
//			if (xtermjsWrap != null && Terminal.Util.hasTerminal()) {
//				terminalClient = new TerminalClient(xtermjsWrap, this);
//				terminalClient.initialize();
//				cursor = terminalClient.cursor();
//			}

			HTMLElement cmshellWrap = Browser.document.getElementById("cmshell-wrap");
			if (cmshellWrap != null) {
				cmTerminalServer = new CMTerminalServer(Browser.document.getElementById("shell"), cmshellWrap, this);
				cmTerminalServer.initialize();
				cursor = cmTerminalServer.cursor();
			}

			sessionName = ((JSString) map.get("sessionName")).stringValue();
			router = new TeaRouter(sessionName, "", this);
			if (terminalClient != null)
				router.route(new TerminalDispatch(terminalClient));
			router.route(new CMDispatch(cmTerminalServer));

			HTMLElement exElt = Browser.document.getElementById("explorer");
			if (exElt != null) {
				explorer = new Explorer(exElt);
				router.route(new ExplorerDispatch(explorer));
			}
			
			canvas = new CanvasServer();
			router.route(new CanvasDispatch(canvas));

			map.set("client", this);
			welcomeService = new HelloServiceProxy("turtleduck.server", router::send);
			shellService = new ShellServiceProxy("turtleduck.shell.server", router::send);
			codeService = new CodeServiceProxy("turtleduck.code.server", router::send);
			inputService = new InputServiceProxy("turtleduck.input", router::send);

			socket = SockJS.create("socket?session=" + JSUtil.encodeURIComponent(sessionName));
			map.set("socket", socket);
			socket.onClose(tryListener(this::disconnect));
			socket.onOpen(tryListener(this::connect));

			HTMLElement tabs = Browser.document.getElementById("editor-tabs");
			HTMLElement wrapper = Browser.document.getElementById("editor-wrap");
			editorImpl = new EditorServer(Browser.document.getElementById("editor"), wrapper, tabs, this, shellService);
			editorImpl.initialize();
			router.route(new EditorDispatch(editorImpl));

			// Route route = router.route();
//			route.registerHandler("welcome", (msg,r) -> Welcome.accept(msg, this));
//		terminal.onData((d) -> {sockJS.send(d);});
			WINDOW_MAP.set("turtleduck", map);
			JSUtil.export("eval", this::eval);

			XMLHttpRequest req = XMLHttpRequest.create();
			req.onComplete(() -> {
				if (req.getReadyState() == 4 && req.getStatus() == 200) {
					HTMLElement elt = Browser.document.getElementById("doc-display");
					JSUtil.renderSafeMarkdown(elt, req.getResponseText());
				} else {
					logger.warn("Unexpected request result: {}", req);
				}
			});
			req.open("GET", "doc/TODO-PROJECTS.md", true);
			req.setRequestHeader("Accept", "text/markdown, text/plain, text/*;q=0.9");
			req.send();

//		ws.setOnClose(() -> NativeTScreen.consoleLog("NO CARRIER"));
//		ws.setOnData((data) -> terminal.write(data));
		} catch (Throwable ex) {
			logger.error("Client failed: ", ex);
			throw ex;
		}
	}

	public void eval(JSString code) {
		// TODO: don't use terminalClient
		shellService.eval(code.stringValue(), terminalClient.lineNum++, Dict.create());
	}

	public static void main(String[] args) {
		Logging.setLogDest(JSUtil::logger);
		Logging.useCustomLoggerFactory();
		WINDOW_MAP = Window.current().cast();
		client = new Client();
		client.initialize();
	}

	protected void connect(Event ev) {
		logger.info("Connect: " + ev.getType() + ", " + router);
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
			logger.info("Received welcome: {}", msg);
			String username = msg.get(HelloService.USERNAME);
			Dict userInfo = null;
			try {
				userInfo = msg.get(HelloService.USER);
			} catch (Throwable ex) {
				logger.error("failed to get userInfo:", ex);
			}
			username(username, userInfo);
			String ex = msg.get(HelloService.EXISTING) ? "existing" : "new";
			if (terminalClient != null)
				terminalClient.connected(username, ex, sessionName);
			if (cmTerminalServer != null)
				cmTerminalServer.connected(username, ex, sessionName);
			if (msg.get(HelloService.EXISTING))
				client.shellService.refresh();
		});

	}

	protected void disconnect(CloseEvent ev) {
		if (socketConnected) {
			logger.info("Disconnect: " + router + ", retry " + reconnectInterval);
			if (terminalClient != null)
				terminalClient.disconnected("");
			if (cmTerminalServer != null)
				cmTerminalServer.disconnected("");
			socketConnected = false;
		} else
			logger.error("Connection failed: " + router + ", retry " + reconnectInterval);
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
			logger.info("Retrying SockJS connection...");
			if (statusBtn != null) {
				statusBtn.withText("Connecting...");
			}
			Window.clearInterval(reconnectIntervalId);
			reconnectInterval *= 1.5;
			socket = SockJS.create("socket?session=" + JSUtil.encodeURIComponent(sessionName));
			socket.onClose(tryListener(this::disconnect));
			socket.onOpen(tryListener(this::connect));
			map.set("socket", socket);
		}, reconnectInterval);

	}

	public void username(String name, Dict userInfo) {
		map.set("username", JSString.valueOf(name));
		router.init(sessionName, name); // TODO: escapes
		Storage sess = map.get("sessionStorage").cast();
		if (sess != null) {
			sess.setItem("turtleduck.username", name);
		}
		HTMLElement status = Browser.document.getElementById("status-button");
		if (status != null) {
			status.setAttribute("title", name + "@" + socket.url());
		}
		HTMLElement imgBox = Browser.document.getElementById("user-picture");
		if (imgBox != null && userInfo != null) {
			String imgUrl = userInfo.getString("picture");
			if (imgUrl != null) {
				imgBox.setAttribute("src", imgUrl);
				imgBox.getStyle().setProperty("visibility", "visible");
			}
		}
		HTMLElement nameBox = Browser.document.getElementById("user-name");
		if (nameBox != null) {
			nameBox.withText(name); // TODO: escapes
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
		logger.info("action: {}, {}", key, ev);
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
}
