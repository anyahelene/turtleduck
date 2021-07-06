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
import turtleduck.util.Dict;
import turtleduck.util.Logging;
import turtleduck.util.Strings;

@MessageDispatch("turtleduck.tea.generated.CMDispatch")
public class CMTerminalServer implements TerminalService, ExplorerService, HtmlCursorImpl.HTMLWriter {
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
	private String lang;
	private LanguageConsole console;

	CMTerminalServer(Component parent, Shell shell) {
		logger.info("CMTerminalServer({},{},{})", parent.element(), name);
		this.parent = parent;
		this.shell = shell;
		this.console = new LanguageConsole.ConsoleImpl();
	}

	public void initialize(String name) {
		this.name = name;
		this.lang = shell.language();
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

		editor = EditorServer.createLineEditor(wrapperElt, "", lang, this::eventHandler);
		editor.setParent(parent);
		editor.set("anchorElt", anchorElt);
		editor.set("outerElt", outerElt);
		editor.set("outputElt", outputElt);
		editor.set("print", (JSConsumer<JSString>) (s -> cursor.print(s.stringValue())));
		editor.register();

		cursor = new HtmlCursorImpl(this, (s) -> {
		});
		out = new TerminalPrintStream(cursor, true);
		console.cursor(cursor);
		console.promptHandler(this::prompt);

		shell.connection().addHandlers(name, this::connected, this::disconnected);

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
			Client.client.history.put(historyId, line, redoLine).onSuccess(id -> {
				enterLine(line, id);
			});
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
		history.clear();
		redoLine = 0;
		lastLine = id;
		currentLine = id + 1;
//		highlighted.insertBefore(elt, highlighted.getFirstChild());
		outputElt.appendChild(highlighted);
		lineHandler(line, id, highlighted);
		State newState = editor.createState(lang, "");
		editor.switchState(newState);
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

	private void scrollIntoView() {
		JSUtil.scrollIntoView(anchorElt);
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
			State newState = editor.createState(lang, nextLine.current, -1);
			editor.switchState(newState);
			scrollIntoView();
		} else {
			Client.client.history.get(historyId, next).onSuccess(line -> {
				history.put(next, new HistEntry(next, line, line));
				currentLine = next;
				State newState = editor.createState(lang, line, -1);
				editor.switchState(newState);
				scrollIntoView();
			}).onFailure(err -> {
				logger.error("history.get: {}", err);
			});
		}
	}

	public void lineHandler(String s, int id, HTMLElement output) {
		if (s.startsWith("!refresh")) {
			shell.service().refresh();
			promptReady();
			return;
		} else if (s.startsWith("/") && shell.specialCommand(s, console.withOutputElement(output))) {
			return;
		} else {
			Dict opts = Dict.create();
			Location loc = new Location("file", "", "/history/" + historyId + "/" + id, 0, s.length(), -1);
			opts.put(ShellService.LOC, loc.toString());
			promptRunning();
			shell.evalLine(s, id, opts, console.withOutputElement(output));
		}

	}

	private void promptWaiting() {
		prompt(-1, null);
	}

	private void promptRunning() {
		prompt(0, null);
	}

	private void promptReady() {
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
				State newState = editor.createState(lang, code, -1);
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