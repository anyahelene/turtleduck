package turtleduck.tea;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.colors.Colors;
import turtleduck.messaging.EditorService;
import turtleduck.messaging.Reply;
import turtleduck.messaging.ShellService;
import turtleduck.tea.TDEditor.State;
import turtleduck.tea.terminal.KeyHandler;
import turtleduck.text.Location;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

import static turtleduck.tea.Browser.trying;

@MessageDispatch("turtleduck.tea.generated.EditorDispatch")
public class EditorServer implements EditorService {
	public static final Logger logger = Logging.getLogger(EditorServer.class);
	public static final String ENDPOINT_ID = "turtleduck.editor";
	public static final String TABCLASS_INACTIVE = "tab saveable";
	public static final String TABCLASS_ACTIVE = "tab saveable selected";
	private Map<String, EditSession> sessionsByName = new LinkedHashMap<>();
	private Map<Integer, EditSession> sessionsById = new HashMap<>();
//	private HTMLElement element;
	private Client client;
	private EditSession currentSession;
	private HTMLElement saveButton;
	private HTMLElement closeButton;
	private HTMLElement tabs;
	private HTMLElement wrapper;
//	private HTMLElement tabItem;
//	private String service;
	private ShellService shell;
	private int nextSessionId = 0;
	private EventListener<?> saveListener;
	private EventListener<?> closeListener;

	@JSBody(params = { "state" }, script = "return state.doc.toString()")
	native static String getDoc(State state);

	@JSBody(params = { "state",
			"text" }, script = "return state.update({changes: [{from: 0, to: state.doc.length, insert: text}]}).state;")
	native static State setDoc(State state, String text);

	@JSBody(params = { "state" }, script = "return state.doc.length == 0")
	native static boolean isEmpty(State state);

	@JSBody(params = { "elt", "text" }, script = "return turtleduck.createEditor(elt, text)")
	native static TDEditor createEditor(HTMLElement elt, String text);
	@JSBody(params = { "elt", "text" }, script = "return turtleduck.createLineEditor(elt, text)")
	native static TDEditor createLineEditor(HTMLElement elt, String text);

	@JSBody(params = { "sess", "start", "len", "type",
			"message" }, script = "{const anno = sess.doc.indexToPosition(start);\n"
					+ "const rowColEnd = sess.doc.indexToPosition(start+len);\n" + "anno.type = type;\n"
					+ "const annos = sess.getAnnotations();" + "anno.text = message;\n" + "annos.push(anno);"
					+ "sess.setAnnotations(annos); console.log(annos);\n" + "}")
	native static void addAnno(State sess, int start, int len, String type, String message);

	@JSBody(params = { "sess", "start", "len",
			"className" }, script = "{const startRow = sess.doc.indexToPosition(start).row;\n"
					+ "const endRow = sess.doc.indexToPosition(start+len).row;\n"
					+ "for(var i = startRow; i <= endRow; i++) sess.addGutterDecoration(i, className);\n"
					+ "return {startRow: startRow, endRow: endRow, className: className};}")
	native static JSObject addGutterDecoration(State sess, int start, int len, String className);

	@JSBody(params = { "sess",
			"ref" }, script = "for(var i = ref.startRow; i <= ref.endRow; i++) sess.removeGutterDecoration(i, ref.className);")
	native static void removeGutterDecoration(State sess, JSObject ref);

	@JSBody(params = { "sess" }, script = "sess.setAnnotations([])")
	native static void clearAnnos(State sess);

	@JSBody(params = { "state", "diags" }, script = "return state.update(turtleduck.editor.setDiagnostics(state, diags)).state")
	native static State setDiagnostics(State sess, JSArray<JSObject> diags);

	@JSBody(params = { "from", "to", "severity", "message" }, script = "return turtleduck.editor.diagnostic(from, to, severity, message, [])")
	native static JSObject diagnostic(int from, int to, String severity, String message);

	public EditorServer(HTMLElement wrapper, HTMLElement tabs, Client client, ShellService shell) {
		this.client = client;
		this.tabs = tabs;
		this.wrapper = wrapper;
		this.shell = shell;
	}

	public void initialize() {
		String name = "*scratch*";
		HTMLDocument document = Window.current().getDocument();
//		wrapper.getStyle().setProperty("display", "flex");
		saveButton = document.getElementById("tb-save");
		closeButton = document.getElementById("tb-close");

		if (saveButton != null)
			saveButton.addEventListener("click", saveListener = trying(this::save));
		if (closeButton != null)
			closeButton.addEventListener("click", closeListener = trying(this::close));
		logger.info("editor save button: {}", saveButton);

		if (client.editor == null) {
			client.editor = createEditor(wrapper, "");
			open("*scratch*", "", "Java");
			client.map.set("editor", client.editor);
		} else {
			throw new IllegalStateException("EditorServer initialized again?!");
		}
	}

	public void dispose() {
		if (saveButton != null)
			saveButton.removeEventListener("click", saveListener);
		if (closeButton != null)
			closeButton.removeEventListener("click", closeListener);
	}

	protected void save(Event e) {
		logger.info("save: {}", e);
		if (currentSession != null) {
			EditSession current = currentSession;
			String contents = content();
			if (!current.filename.startsWith("*")) {
				client.storage().setItem("file://" + current.filename, contents);
			}
			Dict opts = Dict.create();
			Location loc = new Location("file", "editor", current.filename, 0, contents.length(), -1);
			opts.put(ShellService.LOC, loc.toString());
			opts.put(ShellService.COMPLETE, true);
			client.cursor.println();
			shell.eval(contents, current.id, opts).onSuccess(msg -> {
//				clearAnnos(state);
//				while (!current.decorations.isEmpty()) {
//					removeGutterDecoration(state, current.decorations.remove(0));
//				}
				JSArray<JSObject> cmDiags = JSArray.create();
				for (Dict result : msg.get(ShellService.MULTI).toListOf(Dict.class)) {
					Array diags = result.get(ShellService.DIAG);
					for (Dict diag : diags.toListOf(Dict.class)) {
						client.cursor.println(diag.get(Reply.ENAME) + " at " + diag.get(ShellService.LOC),
								Colors.MAROON);
						client.cursor.println(diag.get(Reply.EVALUE), Colors.MAROON);
						try {
							URI uri = new URI(diag.get(ShellService.LOC));
							Location l = new Location(uri);
							cmDiags.push(diagnostic(l.start(), l.end(), "error", diag.get(Reply.ENAME) + ": " + diag.get(Reply.EVALUE)));
//							addAnno(state, l.start(), l.length(), "error",
//									diag.get(Reply.ENAME) + ": " + diag.get(Reply.EVALUE));
						} catch (URISyntaxException ex) {
							Browser.addError(ex);
						}
					}
					if (msg.get(ShellService.COMPLETE)) {
						String value = result.get(ShellService.VALUE);
						String name = result.get(ShellService.NAME);
						String type = result.get(ShellService.TYPE);
						if (value != null) {
							String v = TerminalClient.TEXTCOLOR.applyFg(value) + "\n";
							if (name != null && !name.isEmpty()) {
								v = TerminalClient.VARCOLOR.applyFg(name) + " = " + v;
							}
							if (type != null && !type.isEmpty()) {
								v = TerminalClient.TYPECOLOR.applyFg(type) + " " + v;
							}
							client.cursor.print(v);
						}
						if (diags.isEmpty()) {
							try {
								URI uri = new URI(result.get(ShellService.LOC));
								Location l = new Location(uri);
//								current.decorations.add(addGutterDecoration(state, l.start(), l.length(), "ok"));
							} catch (URISyntaxException ex) {
								Browser.addError(ex);
							}
						}
					}
				}
				current.state = setDiagnostics(client.editor.state(), cmDiags);
				client.editor.switchState(current.state);
				client.terminalClient.prompt();
			}).onFailure(msg -> {
//				clearAnnos(state);
				while (!current.decorations.isEmpty()) {
//					removeGutterDecoration(state, current.decorations.remove(0));
				}
				logger.info("exec error: " + msg);
				String ename = msg.get(Reply.ENAME);
				String evalue = msg.get(Reply.EVALUE, null);
				Array trace = msg.get(Reply.TRACEBACK);
				client.cursor.println("INTERNAL ERROR: " + ename + (evalue != null ? (" : " + evalue) : ""),
						Colors.RED);
				for (String frame : trace.toListOf(String.class)) {
					client.cursor.println(frame, Colors.MAROON);
				}
				client.terminalClient.prompt();
			});
		}
	}

	protected void close(Event e) {
		logger.info("close: {}", e);
		if (currentSession != null) {
			if (!currentSession.filename.startsWith("*")) {
				client.storage().setItem("autosave://" + currentSession.filename, content());
			}
			sessionsByName.remove(currentSession.filename);
			sessionsById.remove(currentSession.id);
			if (sessionsByName.isEmpty()) {
				open("*scratch*", "", "Java");
			} else {
				currentSession = sessionsByName.entrySet().iterator().next().getValue();
				client.editor.switchState(currentSession.state);
			}
		}
	}

	@Override
	public Async<Dict> content(String content, String language) {
		logger.info("Contents: " + content);
		State newState = setDoc(client.editor.state(), content);
		client.editor.switchState(newState);
		return null;
	}

	public String content() {
		return getDoc(client.editor.state());
	}

	@Override
	public Async<Dict> mark(String message, int cursorPos, int start, int end) {
		logger.info("Contents2: " + message + ", " + cursorPos + ", " + start + ", " + end);
		return null;
	}

	@Override
	public Async<Dict> code(String text) {
		HTMLDocument document = Window.current().getDocument();
		document.getElementById("instructions").getStyle().setProperty("display", "block");
		HTMLElement disp = document.getElementById("bytecode-display");
		disp.setInnerHTML(text);
		return null;
	}

	@Override
	public Async<Dict> read() {
		return Async.succeeded(Dict.create().put(EditorService.TEXT, getDoc(client.editor.state())));
	}

	private void switchTo(EditSession sess) {
		if (currentSession != null) {
			currentSession.tab.setClassName(TABCLASS_INACTIVE);
			currentSession.state = client.editor.state();
		}
		sess.tab.setClassName(TABCLASS_ACTIVE);
		currentSession = sess;
		client.editor.switchState(currentSession.state);
	}

	@Override
	public Async<Dict> open(String filename, String text, String language) {
		logger.info("Open: " + filename + " " + language + ":\n" + (text != null ? text : "null"));

		if (currentSession != null) {
			if (currentSession.filename.equals(filename)) {
				client.editor.switchState(setDoc(client.editor.state(), text));
				return null;
			} else if (currentSession.filename.equals("*scratch*") && isEmpty(client.editor.state())) {
				logger.info("replacing *scratch*");
				if (text == null)
					text = client.storage().getItem("file://" + filename);
				if (text == null)
					text = "";
				logger.info("text: " + text);
				client.editor.switchState(setDoc(client.editor.state(), text));
				currentSession.filename = filename;
				currentSession.tabLink.withText(filename);
				return null;
			}
		}
		if (text == null)
			text = client.storage().getItem("file://" + filename);

		EditSession sess = sessionsByName.get(filename);
		if (sess == null) {
			int id = nextSessionId++;
			sess = new EditSession();
			sess.tab = Browser.document.createElement("span");
			sess.tabLink = Browser.document.createElement("a") //
					.withAttr("href", "#") //
					.withAttr("data-session", String.valueOf(id))//
					.withAttr("data-filename", filename) //
					.withAttr("class", "tab-name").withText(filename);
//					.withAttr("class", "nav-link") //
			sess.tab.appendChild(sess.tabLink);
			sess.closeLink = Browser.document.createElement("a") //
//					.withAttr("class", "ui-icon ui-icon-close") //
					.withAttr("href", "#") //
					.withAttr("data-session", String.valueOf(id))//
					.withAttr("data-filename", filename) //
					.withAttr("class", "tab-close").withAttr("role", "presentation")//
					.withText("×");
			sess.tab.appendChild(sess.closeLink);

			tabs.appendChild(sess.tab);
			logger.info("editor tabs: {}", tabs);
			sess.state = client.editor.createState(text);
			sess.id = id;
			sess.filename = filename;

			EditSession theSession = sess;
			sess.tabLink.addEventListener("click", (e) -> {
				logger.info("Switch to tab: " + filename);
				switchTo(theSession);
			});
			sess.closeLink.addEventListener("click", (e) -> {
				logger.info("Close tab: " + filename);
			});
		} else {
			sess.state = setDoc(sess.state, text);

		}
		switchTo(sess);
		return null;
	}

	public class EditSession {
		int id;
		String filename;
		TDEditor.State state;
		HTMLElement tab;
		HTMLElement tabLink;
		HTMLElement closeLink;
		List<JSObject> decorations = new ArrayList<>();
	}
}
