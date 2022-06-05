package turtleduck.tea;

import static turtleduck.tea.HTMLUtil.attr;
import static turtleduck.tea.HTMLUtil.clazz;
import static turtleduck.tea.HTMLUtil.div;
import static turtleduck.tea.HTMLUtil.element;
import static turtleduck.tea.HTMLUtil.span;
import static turtleduck.tea.HTMLUtil.style;
import static turtleduck.tea.HTMLUtil.text;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.colors.Colors;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.Connection;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.tea.LanguageConsole.ConsoleImpl;
import turtleduck.tea.TDEditor.State;
import turtleduck.terminal.LineInput;
import turtleduck.terminal.TerminalPrintStream;
import turtleduck.text.Location;
import turtleduck.text.TextCursor;
import turtleduck.text.impl.HtmlCursorImpl;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;
import turtleduck.util.Strings;

@MessageDispatch("turtleduck.tea.generated.CMDispatch")
public class CMTerminalServer implements TerminalService, ExplorerService, HtmlCursorImpl.HTMLWriter, JSTerminal {
	public final Logger logger = Logging.getLogger(CMTerminalServer.class);
//	private Client client;
	private HTMLElement outputElt;
	private TextCursor cursor;
	TDEditor editor;
	private HTMLElement wrapperElt;
	private HTMLElement outerElt;
	private HTMLElement anchorElt;
	private PrintStream out;
	private final Shell shell;
	private Map<Integer, HistEntry> history = new HashMap<>();
	private String historyId;
	private int currentLine = 0;
	private int lastLine = 0;
	private int redoLine = 0;
	private HTMLElement outputContainer;
	private String user;
	private Component parent;
	private String name;
	private Language lang;
	private LanguageConsole console;
	private boolean historyEnabled = true;

	CMTerminalServer(Component parent, Shell shell) {
		this.parent = parent;
		this.shell = shell;
		this.console = new LanguageConsole.ConsoleImpl(this);
	}

	public void initialize(String name) {
		this.name = name;
		this.lang = shell.language();
		logger.info("initialize({}, {},{})", name, lang, parent.element());
		wrapperElt = element("main", attr("id", name), clazz("waiting"));
		outerElt = element("div", clazz("terminal"));
		wrapperElt.appendChild(outerElt);
		wrapperElt.addEventListener("keydown", (EventListener<KeyboardEvent>) this::keydown);

		outputContainer = element("div", clazz("terminal-out-container")/* , style("overflow-anchor", "none") */);
		outerElt.appendChild(outputContainer);

		anchorElt = element("div", clazz("terminal-anchor")/* , style("overflow-anchor", "auto") */,
				style("height", ".25rem"));
		outputContainer.appendChild(anchorElt);

		outputElt = element("div", clazz("terminal-out")/* , style("overflow-anchor", "none") */);
		outputContainer.appendChild(outputElt);

		editor = EditorServer.createLineEditor(wrapperElt, "", lang.editMode, this::eventHandler);
		editor.setParent(parent);
		editor.set("anchorElt", anchorElt);
		editor.set("outerElt", outerElt);
		editor.set("outputElt", outputElt);
		editor.set("terminal", this);
		editor.register();

		cursor = new HtmlCursorImpl(this, (s) -> {
		});
		out = new TerminalPrintStream(cursor, true);
		console.cursor(cursor);
		console.promptHandler(this::prompt);

		shell.connection().addHandlers(name, this::connected, this::disconnected);

	}

	public void disableHistory() {
		historyEnabled = false;
	}

	public LanguageConsole console() {
		return console;
	}

	public boolean eventHandler(String eventName, State state) {
		if (eventName.equals("enter")) {
			String line = EditorServer.getDoc(state);
			if (line.isEmpty()) {
				return false;
			}
			if (currentLine != redoLine)
				redoLine = 0;
			if (redoLine != 0) {
				logger.info("Saving line #{} again as #{}: {}", currentLine, redoLine, line);
			}
			HistEntry current = history.get(currentLine);
			if (current != null) {
				current.current = line;
				logger.info("current histentry: {}, currentLine: {}, lastLine: {}", current, currentLine, lastLine);
				if (currentLine == lastLine && current.isUnchanged()) {
					enterLine(line, currentLine);
					return true;
				}
			}

			if (historyEnabled) {
				Client.client.history.put(historyId, line, redoLine).onSuccess(id -> {
					enterLine(line, id);
				});
			} else {
				logger.info("enterLine (w/o hist): {} {}", currentLine, line);
				history.put(currentLine, new HistEntry(currentLine, line, line));
				enterLine(line, currentLine);
			}
			return true;
		} else if (eventName.equals("arrowUp")) {
			if (currentLine > 1) {
				int next = currentLine - 1;
				goHistory(next, state);

				return true;
			} else {
				return false;
			}
		} else if (eventName.equals("arrowDown")) {
			if (currentLine <= lastLine) {
				int next = currentLine + 1;
				goHistory(next, state);
				return true;
			} else {
				return false;
			}
		}

		return false;

	}

	private void enterLine(String line, int id) {
		HTMLElement elt = Browser.document.createElement("span")//
				.withAttr("class", "prompt")//
				.withAttr("data-user", user)//
				.withText("[" + id + "] ");
		HTMLElement highlighted = editor.highlightTree(elt);
		if (historyEnabled)
			history.clear();
		redoLine = 0;
		lastLine = id;
		currentLine = id + 1;
//		highlighted.insertBefore(elt, highlighted.getFirstChild());
		outputElt.appendChild(highlighted);
		lineHandler(line, id, highlighted);
		State newState = editor.createState(lang.editMode, "");
		editor.switchState(newState);
	}

	public void appendElement(HTMLElement element) {
		outputElt.appendChild(element);
	}
	public HTMLElement outputElement() {
		return outputElt;
	}

	public HTMLElement appendBlock() {
		return appendBlock(null);
	}

	public HTMLElement appendBlock(String style) {
		HTMLElement element = Browser.document.createElement("div");
		if (style != null) {
			element.setClassName(style);
		}
		outputElt.appendChild(element);
		return element;
	}

	private void keydown(KeyboardEvent e) {
		if (e.isCtrlKey() && e.getKey().equals("c")) { // Ctrl-C
			return;
		} else if (e.getKey().equals("Control")) {
			return;
		} else {
			editor.focus();
		}
	}

	public void focus() {
		editor.focus();
	}

	public void scrollIntoView() {
		outputContainer.setScrollTop(0);
		JSUtil.scrollToBottom(wrapperElt);
		// JSUtil.scrollIntoView(anchorElt);
	}

	private void goHistory(int next, State state) {
		HistEntry current = history.get(currentLine);
		if (current == null)
			current = new HistEntry(currentLine, null, EditorServer.getDoc(state));
		else
			current.current = EditorServer.getDoc(state);
		history.put(currentLine, current);
		HistEntry nextLine = history.get(next);
		if (nextLine != null) {
			currentLine = next;
			State newState = editor.createState(lang.editMode, nextLine.current, -1);
			editor.switchState(newState);
			scrollIntoView();
		} else if (historyEnabled) {
			Client.client.history.get(historyId, next).onSuccess(line -> {
				history.put(next, new HistEntry(next, line, line));
				currentLine = next;
				State newState = editor.createState(lang.editMode, line, -1);
				editor.switchState(newState);
				scrollIntoView();
			}).onFailure(err -> {
				logger.error("history.get: {}", err);
				if (next > 0) {
					goHistory(next - 1, state);
				}
			});
		}
	}

	public void lineHandler(String s, int id, HTMLElement output) {
		if (s.startsWith("!refresh")) {
			shell.service().refresh();
			promptReady();
			return;
		} else if (s.startsWith("/") && specialCommand(s, console.withOutputElement(output))) {
			return;
		} else {
			Dict opts = Dict.create();
			Location loc = new Location("shell", "", historyId + "/" + id, 0, s.length(), -1);
			opts.put(ShellService.LOC, loc.toString());
			promptRunning();
			shell.evalLine(s, id, opts, console.withOutputElement(output));
		}

	}

	Async<Array> history() {
		return Client.client.history.list(historyId);
	}

	boolean specialCommand(String cmd, LanguageConsole console) {
		if (cmd.equals("/history")) {
			console.promptBusy();
			history().onComplete(res -> {
				res.toListOf(Dict.class).forEach(entry -> {
					console.print(String.format("%4d %s\n", entry.get("id", 0), entry.get("data", "")));
				});
				console.promptNormal();
			}, ex -> {
				logger.error("/history failed: {}", ex);
				console.promptNormal();
			});
			return true;
		} else if (cmd.equals("/sessions")) {
			console.promptBusy();
			Client.client.history.sessions().onComplete(res -> {
				res.toListOf(Dict.class).forEach(entry -> {
					console.print(entry.toJson());
				});
				console.promptNormal();
			}, ex -> {
				logger.error("/sessions failed: {}", ex);
				console.promptNormal();
			});
			return true;
		} else if (cmd.startsWith("/session=")) {
			return true;
		} else if(shell.specialCommand(cmd, console)) {
			return true;
		} else {
			Promise<JSNumber> evalShell = JSUtil.evalShell(cmd.substring(1), console.outputElement());
			console.promptBusy();
			evalShell.onFinally(res -> {
				console.promptNormal();
			});
			return true;
		}
	}

	private void promptWaiting() {
		prompt(-1, null);
	}

	private void promptRunning() {
		prompt(0, null);
	}

	public void promptReady() {
		prompt(1, null);
	}

	private void prompt(int variant, String code) {
		if (variant < 0) {
			JSUtil.addClass(wrapperElt, "waiting");
			JSUtil.removeClass(wrapperElt, "running");

		} else if (variant == 0) {
			JSUtil.addClass(wrapperElt, "running");
			JSUtil.removeClass(wrapperElt, "waiting");
		} else {
			JSUtil.removeClass(wrapperElt, "waiting");
			JSUtil.removeClass(wrapperElt, "running");

			if (code != null) {
				State newState = editor.createState(lang.editMode, code, -1);
				editor.switchState(newState);
				redoLine = lastLine;
				currentLine = lastLine;
			}
			scrollIntoView();
		}
	}

	public boolean keyHandler(KeyEvent ev, LineInput li) {
		return false;
	}

	public void connected(Connection conn, Dict msg) {
		String session = Client.client.getConfig("session.name", "default");
		this.user = Client.client.getConfig("user.username", "tduck");
		historyId = (session + "/" + name).replace(" ", "");
		String newOrExisting = msg.get(HelloService.EXISTING) ? "existing" : "new";
		promptReady();
		if (historyEnabled) {
			logger.info("connected! history:");
			Client.client.history.currentId(historyId).onSuccess(id -> {
				lastLine = id;
				currentLine = id + 1;
				logger.info("history currentId: {}", id);
//			for (int i = 1; i <= id; i++) {
//				int x = i;
//				client.history.get(session, i).onSuccess(data -> logger.info("history {}: '{}'", x, data));
//			}
				cursor.println(Colors.GREEN.applyFg("Welcome ") + Colors.LIME.applyFg(user) //
						+ Colors.GREEN.applyFg("! Using " + newOrExisting + " session ") //
						+ Colors.LIME.applyFg(session));
			});
		}
	}

	public void disconnected(Connection conn) {
		cursor.println("\nDISCONNECTED", Colors.MAROON);
		promptWaiting();
	}

	@Override
	public Async<Dict> prompt(String prompt, String language) {
		logger.info("Prompt: " + language + " «" + Strings.termEscape(prompt) + "»");
//		terminal.write(prompt);
		return null;
	}

	@Override
	public Async<Dict> display(Dict data, String stream) {
		String url = data.getString("url");
		ArrayView<?> arr = data.get("data", ArrayView.class);
		if (arr != null) {
			Browser.consoleLog("data", arr.get());
			url = JSUtil.createObjectURL(arr.get(), data.get("format", "image/png"));
			logger.info("image: {}", url);
		}
		if (url != null) {
			outputElt.appendChild(element("img", attr("src", url)));

			scrollIntoView();
		}

		return null;
	}

	@Override
	public Async<Dict> write(String text, String stream) {
		if (stream.equals("err") || stream.equals(name + "err"))
			cursor.print(text, Colors.YELLOW);
		else if (stream.equals("out") || stream.equals(name + "out"))
			cursor.print(text);
//		logger.info("«" + Strings.termEscape(text) + "»");
//		HTMLElement elt = Browser.document.createElement("span");
//		elt.withText(text);
//		outputElt.appendChild(elt);
//		terminal.write(text);
		scrollIntoView();
		return null;
	}

	@Override
	public Async<Dict> readline(String prompt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Async<Dict> update(Dict msg) {
		try {
			if (!msg.get(ShellService.PERSISTENT, true))
				return null;
			String sig = msg.get("signature", String.class);
			if (!sig.isEmpty() && !sig.contains("$")) {
				if ("main".equals(msg.get(ShellService.SNIP_NS, "main"))) {
					String snipid = msg.get(ShellService.SNIP_ID);
					String s = " " + msg.get("sym", String.class) + " " + msg.get("verb", String.class) + " ";
					String t = msg.get(ShellService.TYPE);
					HTMLElement div = div(element("a", clazz("prompt"), "[" + snipid + "]"), s, //
							span(msg.get("category", String.class), clazz("cmt-keyword")), " ",
							span(sig, clazz("cmt-variableName"), attr("data-snipid", snipid)));
					if (t != null) {
						div.appendChild(text(" : "));
						div.appendChild(span(t, clazz("cmt-typeName")));
					}
					outputElt.appendChild(div);// , span(def, type)));
				}
				scrollIntoView();
			}
			return null;
		} catch (Throwable t) {
			logger.error("update trouble: {}", t);
			throw t;
		}
	}

	public TextCursor cursor() {
		return cursor;
	}

	public LanguageConsole printer() {
		return console.withOutputElement(appendBlock());
	}

	@Override
	public void writeGlyph(String glyph, String attrs) {
		HTMLElement elt = Browser.document.createElement("span");
		elt.withText(glyph).withAttr("style", attrs);
		outputElt.appendChild(elt);
	}

	@Override
	public void move(int dx, int dy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void column(int x) {
		// TODO Auto-generated method stub

	}

	@Override
	public void row(int y) {
		// TODO Auto-generated method stub

	}

	static class HistEntry {
		int id;
		String orig;
		String current;

		public HistEntry(int id, String orig, String current) {
			this.id = id;
			this.orig = orig;
			this.current = current;
		}

		public boolean isUnchanged() {
			return orig != null && orig.equals(current);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("HistEntry [id=").append(id).append(", orig=").append(orig).append(", current=")
					.append(current).append("]");
			return builder.toString();
		}

	}

}

@JSFunctor
interface JSTerminal extends JSObject {
	public void disableHistory();

	public LanguageConsole console();

	public boolean eventHandler(String eventName, State state);

	public HTMLElement appendBlock(String style	);

	public void appendElement(HTMLElement element);

	public void scrollIntoView();

	public void focus();

	public LanguageConsole printer();

}
