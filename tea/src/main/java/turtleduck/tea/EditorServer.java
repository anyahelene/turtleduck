package turtleduck.tea;

import static turtleduck.tea.Browser.tryListener;

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
import turtleduck.tea.TDEditor.Callback;
import turtleduck.tea.TDEditor.State;
import turtleduck.text.Location;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

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
	private ShellService shellService;
	private int nextSessionId = 0;
	private EventListener<?> saveListener;
	private EventListener<?> closeListener;
	private Shell shell;
	private JSArray<JSObject> cmDiags;
	private TDEditor editor;

	@JSBody(params = { "state" }, script = "return state.doc.toString()")
	native static String getDoc(State state);

	@JSBody(params = { "state",
			"text" }, script = "return state.update({changes: [{from: 0, to: state.doc.length, insert: text}]}).state;")
	native static State setDoc(State state, String text);

	@JSBody(params = { "state" }, script = "return state.doc.length == 0")
	native static boolean isEmpty(State state);

	@JSBody(params = { "elt", "wrap", "text" }, script = "return turtleduck.createEditor(elt, wrap, text)")
	native static TDEditor createEditor(HTMLElement elt, HTMLElement wrap, String text);

	@JSBody(params = { "elt", "wrap", "text", "handler" }, script = "return turtleduck.createLineEditor(elt, wrap, text, handler)")
	native static TDEditor createLineEditor(HTMLElement elt, HTMLElement wrap, String text, Callback handler);

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

	@JSBody(params = { "state",
			"diags" }, script = "return state.update(turtleduck.editor.setDiagnostics(state, diags)).state")
	native static State setDiagnostics(State sess, JSArray<JSObject> diags);

	@JSBody(params = { "from", "to", "severity",
			"message" }, script = "return turtleduck.editor.diagnostic(from, to, severity, message, [])")
	native static JSObject diagnostic(int from, int to, String severity, String message);

	EditorServer(HTMLElement element, HTMLElement wrapper, HTMLElement tabs, Client client, ShellService shell) {
		this.client = client;
		this.tabs = tabs;
		this.wrapper = wrapper;
		this.shellService = shell;
		this.editor = createEditor(element, wrapper, "");

		this.shell = new Shell(client.cursor, (d, l) //
		-> cmDiags.push(
				diagnostic(l.start(), l.end(), d.getString("level"), d.get(Reply.ENAME) + ": " + d.get(Reply.EVALUE))),
				null, null);
	}

	public void initialize() {
		String name = "*scratch*";
		HTMLDocument document = Window.current().getDocument();
//		wrapper.getStyle().setProperty("display", "flex");
		saveButton = document.getElementById("tb-save");
		closeButton = document.getElementById("tb-close");

		if (saveButton != null)
			saveButton.addEventListener("click", saveListener = tryListener(this::save));
		if (closeButton != null)
			closeButton.addEventListener("click", closeListener = tryListener(this::close));
		logger.info("editor save button: {}", saveButton);

		open(name, "", "Java");
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
			shellService.eval(contents, current.id, opts).onSuccess(msg -> {
				cmDiags = JSArray.create();
				shell.processResult(msg, true, null);
				current.state = setDiagnostics(editor.state(), cmDiags);
				editor.switchState(current.state);
			}).onFailure(msg -> {

				logger.info("exec error: " + msg);
				String ename = msg.get(Reply.ENAME);
				String evalue = msg.get(Reply.EVALUE, null);
				Array trace = msg.get(Reply.TRACEBACK);
				client.cursor.println("INTERNAL ERROR: " + ename + (evalue != null ? (" : " + evalue) : ""),
						Colors.RED);
				for (String frame : trace.toListOf(String.class)) {
					client.cursor.println(frame, Colors.MAROON);
				}
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
				editor.switchState(currentSession.state);
			}
		}
	}

	@Override
	public Async<Dict> content(String content, String language) {
		logger.info("Contents: " + content);
		State newState = setDoc(editor.state(), content);
		editor.switchState(newState);
		return null;
	}

	public String content() {
		return getDoc(editor.state());
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
		return Async.succeeded(Dict.create().put(EditorService.TEXT, getDoc(editor.state())));
	}

	private void switchTo(EditSession sess) {
		if (currentSession != null) {
			currentSession.tab.setClassName(TABCLASS_INACTIVE);
			currentSession.state = editor.state();
		}
		sess.tab.setClassName(TABCLASS_ACTIVE);
		currentSession = sess;
		editor.switchState(currentSession.state);
	}

	@Override
	public Async<Dict> open(String filename, String text, String language) {
		logger.info("Open: " + filename + " " + language + ":\n" + (text != null ? text : "null"));

		if (currentSession != null) {
			if (currentSession.filename.equals(filename)) {
				editor.switchState(setDoc(editor.state(), text));
				return null;
			} else if (currentSession.filename.equals("*scratch*") && isEmpty(editor.state())) {
				logger.info("replacing *scratch*");
				if (text == null)
					text = client.fileSystem.read(filename);
				if (text == null)
					text = "";
				logger.info("text: " + text);
				editor.switchState(setDoc(editor.state(), text));
				currentSession.filename = filename;
				currentSession.tabLink.withText(filename);
				return null;
			}
		}
		if (text == null)
			text = client.fileSystem.read(filename);

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
			sess.state = editor.createState(text);
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
