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
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.colors.Colors;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
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
	public static final Logger logger = Logging.getLogger(CMTerminalServer.class);
	private Client client;
	private HTMLElement outputElt;
	private TextCursor cursor;
	TDEditor editor;
	private HTMLElement wrapperElt;
	private HTMLElement outerElt;
	private HTMLElement anchorElt;
	private PrintStream out;
	private Shell shell;
	private Map<Integer, String> history = new HashMap<>();
	private String currentSession;
	private int currentLine = 0;
	private int lastLine = 0;
	private HTMLElement outputContainer;
	private String user;
	private HTMLElement componentElt;

	CMTerminalServer(HTMLElement element, HTMLElement wrapper, Client client) {
		logger.info("CMTerminalServer({},{},{})", element,wrapper, client);
		this.wrapperElt = wrapper;
		this.componentElt = element;
		this.client = client;
	}

	public void initialize() {
		outerElt = element("div", clazz("terminal"));
		wrapperElt.appendChild(outerElt);

		outputContainer = element("div", clazz("terminal-out-container")/* , style("overflow-anchor", "none") */);
		outerElt.appendChild(outputContainer);

		anchorElt = element("div", clazz("terminal-anchor")/* , style("overflow-anchor", "auto") */,
				style("height", ".25rem"));
		outputContainer.appendChild(anchorElt);

		outputElt = element("div", clazz("terminal-out")/* , style("overflow-anchor", "none") */);
		outputContainer.appendChild(outputElt);

		editor = EditorServer.createLineEditor(componentElt, outerElt, "", this::eventHandler);
		editor.set("anchorElt", anchorElt);
		editor.set("outerElt", outerElt);
		editor.set("outputElt", outputElt);
		editor.set("print", (JSConsumer<JSString>) (s -> cursor.print(s.stringValue())));

		cursor = new HtmlCursorImpl(this, (s) -> {
		});
		out = new TerminalPrintStream(cursor, true);
		shell = new Shell(cursor, null, this::prompt, elt -> outputElt.appendChild(elt));
	}

	public boolean eventHandler(String eventName, State state) {
		if (eventName.equals("enter")) {
			String line = EditorServer.getDoc(state);
			if (line.isEmpty()) {
				return false;
			}
			client.history.put(currentSession, line).onSuccess(id -> {
				HTMLElement elt = Browser.document.createElement("span")//
						.withAttr("class", "prompt")//
						.withAttr("data-user", user)//
						.withText("[" + id + "] ");
				HTMLElement highlighted = editor.highlightTree(elt);
				history.clear();
				lastLine = id;
				currentLine = id + 1;
//				highlighted.insertBefore(elt, highlighted.getFirstChild());
				outputElt.appendChild(highlighted);
				lineHandler(line, id, highlighted);
				State newState = editor.createState("");
				editor.switchState(newState);
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

	private void scrollIntoView() {
		JSUtil.scrollIntoView(anchorElt);
	}

	private void goHistory(int next, State state) {
		history.put(currentLine, EditorServer.getDoc(state));
		String nextLine = history.get(next);
		if (nextLine != null) {
			currentLine = next;
			State newState = editor.createState(nextLine, -1);
			editor.switchState(newState);
			scrollIntoView();
		} else {
			client.history.get(currentSession, next).onSuccess(line -> {
				currentLine = next;
				State newState = editor.createState(line, -1);
				editor.switchState(newState);
				scrollIntoView();
			}).onFailure(err -> {
				logger.error("history.get: {}", err);
			});
		}
	}

	public void lineHandler(String s, int id, HTMLElement output) {
		if (s.startsWith("!refresh")) {
			client.shellService.refresh();
			scrollIntoView();
			prompt(1);
			return;
		} else if (s.startsWith("/") && shell.specialCommand(s)) {
			return;
		} else {
			Dict opts = Dict.create();
			Location loc = new Location("file", "", "/history/" + currentSession + "/" + id, 0, s.length(), -1);
			opts.put(ShellService.LOC, loc.toString());
			shell.evalLine(s, id, opts, output);
		}

	}

	private void prompt(int variant) {
		scrollIntoView();
	}

	public boolean keyHandler(KeyEvent ev, LineInput li) {
		return false;
	}

	public void connected(String user, String newOrExisting, String session) {
		currentSession = session.replace(" ", "");
		this.user = user;
		logger.info("connected! history:");
		client.history.currentId(currentSession).onSuccess(id -> {
			lastLine = id;
			currentLine = id + 1;
			logger.info("history currentId: {}", id);
//			for (int i = 1; i <= id; i++) {
//				int x = i;
//				client.history.get(session, i).onSuccess(data -> logger.info("history {}: '{}'", x, data));
//			}
		});

		cursor.println(Colors.GREEN.applyFg("Welcome ") + Colors.LIME.applyFg(user) //
				+ Colors.GREEN.applyFg("! Using " + newOrExisting + " session ") //
				+ Colors.LIME.applyFg(session));
	}

	public void disconnected(String reason) {
		cursor.println("\nDISCONNECTED", Colors.MAROON);
	}

	@Override
	public Async<Dict> prompt(String prompt, String language) {
		logger.info("Prompt: " + language + " «" + Strings.termEscape(prompt) + "»");
//		terminal.write(prompt);
		return null;
	}

	@Override
	public Async<Dict> write(String text) {
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
		if (!msg.get(ShellService.PERSISTENT))
			return null;
		String sig = msg.get("signature", String.class);
		if (!sig.isEmpty() && !sig.contains("$")) {
			if ("main".equals(msg.get(ShellService.SNIP_NS))) {
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
	}

	public TextCursor cursor() {
		return cursor;
	}

	@Override
	public void write(String glyph, String attrs) {
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

}