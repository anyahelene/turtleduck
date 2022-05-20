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
import org.teavm.jso.core.JSString;
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
	public static final String TABCLASS_INACTIVE = "tab saveable clickable";
	public static final String TABCLASS_ACTIVE = "tab saveable clickable selected";
	private Map<String, EditSession> sessionsByName = new LinkedHashMap<>();
	private Map<Integer, EditSession> sessionsById = new HashMap<>();
	private EditSession currentSession;
	private HTMLElement saveButton;
	private HTMLElement closeButton;
	private HTMLElement tabs;

	private int nextSessionId = 0;
	private EventListener<?> saveListener;
	private EventListener<?> closeListener;
	private JSArray<JSObject> cmDiags;
	protected TDEditor editor;
	private Map<String, EditLanguage> languages = new HashMap<>();
	private boolean langInitialized = false;
	private HTMLElement modeFooterElement;

	@JSBody(params = { "state" }, script = "return state.doc.toString()")
	native static String getDoc(State state);

	@JSBody(params = { "state",
			"text" }, script = "return state.update({changes: [{from: 0, to: state.doc.length, insert: text}]}).state;")
	native static State setDoc(State state, String text);

	@JSBody(params = { "state" }, script = "return state.doc.length == 0")
	native static boolean isEmpty(State state);

	@JSBody(params = { "wrap", "text", "lang" }, script = "return turtleduck.createEditor(wrap, text, lang)")
	native static TDEditor createEditor(HTMLElement wrap, String text, String lang);

	@JSBody(params = { "wrap", "text", "lang",
			"handler" }, script = "return turtleduck.createLineEditor(wrap, text, lang, handler)")
	native static TDEditor createLineEditor(HTMLElement wrap, String text, String lang, Callback handler);

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

	EditorServer(HTMLElement element, Component parent) {
		this.tabs = JSUtil.getElementsByClassName(element, "tabs").get(0);
		this.modeFooterElement = element.querySelector(".editor-mode-foot");
		this.editor = createEditor(element, "", "");
		if (parent != null)
			editor.setParent(parent);
		editor.setTitle("Editor");
		editor.addWindowTools();
		editor.set("_paste_to_file", (JSTriConsumer<JSString, JSString, JSString>) (obj1, obj2,
				obj3) -> paste(obj1.stringValue(), obj2.stringValue(), obj3.stringValue()));

	}

	private String defaultName() {
		if (currentSession != null && currentSession.lang != null)
			return "*" + currentSession.lang.shell.shellName() + "*";
		else
			return "*scratch*";
	}

	protected void addDiag(Dict d, Location l) {
		logger.info("diagnostic({}, {}, {}, {})", l.start(), l.end(), //
				d.getString("level"), d.get(Reply.ENAME) + ": " + d.get(Reply.EVALUE));
		if ("editor".equals(l.host())) { // TODO: also connect messages to the right file
			cmDiags.push(diagnostic(l.start(), l.end(), //
					d.getString("level"), d.get(Reply.ENAME) + ": " + d.get(Reply.EVALUE)));
		}
	}

	public void initializeLanguage(String name) {
		EditLanguage l = languages.get(name);
		if (l == null)
			l = new EditLanguage();
		l.shell = null;
		l.console = null;
		l.name = name;
		languages.put(name, l);
		if (!langInitialized && currentSession.filename.startsWith("*") && isEmpty(editor.state())) {
			open("*" + name + "*", "", l.name);
		}
		langInitialized = true;
	}

	public void initializeLanguage(Shell shell, LanguageConsole console) {
		logger.info("initializing editor language services for {}", shell.language());
		String name = shell.language().id;
		EditLanguage l = languages.get(name);
		if (l == null)
			l = new EditLanguage();
		l.shell = shell;
		l.lang = shell.language();
		if (console != null) {
			l.console = console.create().diagHandler(this::addDiag);
		}
		l.name = name;
		languages.put(name, l);
		if (!langInitialized && currentSession.filename.startsWith("*") && isEmpty(editor.state())) {
			open("*" + shell.shellName() + "*", "", l.lang);
		}
		langInitialized = true;
	}

	public EditLanguage getLanguage(Language lang) {
		if (lang == null) {
			lang = Languages.LANGUAGES.get("plain");
			if (lang == null) {
				// TODO: replicates stuff from Client::setupLanguages
				lang = new Language("plain", Dict.create());
				Languages.LANGUAGES.put("plain", lang);
			}
		}

		EditLanguage l = languages.get(lang.id);
		if (l == null) {
			logger.info("adding dummy editor language for {}", lang.id);
			l = new EditLanguage();
			l.name = lang.id;
			l.lang = lang;
			languages.put(lang.id, l);
			if (l.lang != null)
				Client.client.loadLanguage(lang.id);
		}
		return l;
	}

	public void initialize() {
		HTMLDocument document = Window.current().getDocument();
		saveButton = document.getElementById("tb-save");
		closeButton = document.getElementById("tb-close");
		editor.register();
		if (tabs != null)
			tabs.addEventListener("click", (e) -> {
				logger.info("Tab op: {}", e);
				HTMLElement target = (HTMLElement) e.getTarget();
				HTMLElement tab = target;
				while (tab.getAttribute("data-filename") == null && tab.getParentNode() != null)
					tab = (HTMLElement) tab.getParentNode();
				String session = tab.getAttribute("data-session");
				String filename = tab.getAttribute("data-filename");
				logger.info("session {}, filename {}", session, filename);
				if (session != null && session.matches("^\\d+$")) {
					int id = Integer.valueOf(session);
					EditSession sess = sessionsById.get(id);
					logger.info("session {}", sess);
					if (target.getClassName().contains("tab-close")) {
						close(sess);
					} else {
						switchTo(sess);
					}
					editor.focus();
				}
				e.stopPropagation();
				e.preventDefault();
				editor.focus();
			});
		if (saveButton != null)
			saveButton.addEventListener("click", saveListener = tryListener(this::save));
		if (closeButton != null)
			closeButton.addEventListener("click", closeListener = tryListener(this::close));
		logger.info("editor save button: {}", saveButton);

		open(defaultName(), "", "");
	}

	public void focus() {
		editor.focus();
	}

	public void dispose() {
		if (saveButton != null)
			saveButton.removeEventListener("click", saveListener);
		if (closeButton != null)
			closeButton.removeEventListener("click", closeListener);
	}

	protected Async<Dict> save(Event e) {
		logger.info("save: {}", e);
		if (currentSession != null) {
			EditSession current = currentSession;
			String contents = content();
			Async<Void> tmp = null;
			if (!current.filename.startsWith("*")) {
				Client.client.userlog("Saving " + current.filename);
				tmp = Client.client.fileSystem.writetextfile("/home/" + current.filename, contents);
				current.lastSave = contents;
			}
			Async<Void> writePromise = tmp != null ? tmp : Async.succeeded(null);
			EditLanguage lang = current.lang;
			writePromise.then(res -> {
				logger.info("write complete");
				if (lang.shell != null) {
					return doEval(current, contents, writePromise, lang);
				} else {
					Client.client.userlog("'" + current.filename + "' saved (no evaluator available)");
					return null;
				}
				
			}).onFailure(msg -> {
				logger.info("save error: " + msg);
				String ename = msg.get(Reply.ENAME);
				String evalue = msg.get(Reply.EVALUE, "");
				if (lang != null && lang.shell != null) {
					lang.shell.printError("", msg, lang.console);
				}
				Client.client.userlog("Saving '"+current.filename +"' failed: " + ename + ":" + evalue);

			});
		} else
			Client.client.userlog("Save: nothing to do");
		return null;
	}

	private Async<Dict> doEval(EditSession current, String contents, Async<Void> writePromise, EditLanguage lang) {
		Client.client.userlog("'" + current.filename + "' saved, evaluating " + lang.name);
		Dict opts = Dict.create();
		Location loc = new Location("file", "editor", current.filename, 0, contents.length(), -1);
		opts.put(ShellService.LOC, loc.toString());
		opts.put(ShellService.COMPLETE, true);
		if (lang.console != null) {
			lang.console.println();
		}
		return current.lang.shell.service().eval(contents, current.id, opts).then(msg -> {
			cmDiags = JSArray.create();
			if (lang.console != null) {
				lang.shell.processResult(msg, true, current.lang.console);
				current.lang.console.promptNormal();
			}
			current.state = setDiagnostics(editor.state(), cmDiags);
			editor.switchState(current.state);
			if (writePromise != null) {
				return writePromise.map(i -> {
					logger.info("write complete: {}", i);
					return Dict.create();
				});
			} else {
				return Async.succeeded(Dict.create());
			}
		}).mapFailure(msg -> {
			lang.shell.printError("INTERNAL ERROR: ", msg, lang.console);

	/*		logger.info("exec error: " + msg);
			String ename = msg.get(Reply.ENAME);
			String evalue = msg.get(Reply.EVALUE, null);
			Array trace = msg.get(Reply.TRACEBACK);
			if (lang.console != null) {
				lang.console.println("INTERNAL ERROR: " + ename + (evalue != null ? (" : " + evalue) : ""), Colors.RED);
				for (String frame : trace.toListOf(String.class)) {
					lang.console.println(frame, Colors.MAROON);
				}
			}*/
			return msg;
		});

	}

	protected void close(EditSession sess) {
		logger.info("close: {}", sess);
		if (sess != null) {
			if (!sess.filename.startsWith("*")) {
				String content = content();
				if (!content.equals(sess.lastSave))
					Client.client.storage().setItem("autosave://" + sess.filename, content);
			}
			sessionsByName.remove(sess.filename);
			sessionsById.remove(sess.id);
			tabs.removeChild(sess.tab);
			logger.info("sessions: {}, {}", sessionsByName.size(), sessionsById.size());
			if (sessionsByName.isEmpty()) {
				EditLanguage lang = currentSession.lang;
				currentSession = null;
				open(defaultName(), "", lang != null ? lang.name : null);
			} else if (currentSession == sess) {
				currentSession = null;
				switchTo(sessionsByName.entrySet().iterator().next().getValue());
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
			currentSession.scrollTop = editor.scrollDOM().getScrollTop();
		}
		sess.tab.setClassName(TABCLASS_ACTIVE);
		currentSession = sess;
		setFooter();
		editor.switchState(currentSession.state);
		if (currentSession.scrollTop >= 0) {
			editor.scrollDOM().setScrollTop(currentSession.scrollTop);
		}
	}

	private void setFooter() {
		if (modeFooterElement != null) {
			if (currentSession.lang != null)
				modeFooterElement.withText("(" + currentSession.lang.name + ")");
			else
				modeFooterElement.withText("(plain)");
		}
	}

	public void paste(String filename, String text, String langName) {
		logger.info("paste({}, {}, {})", filename, text, langName);
		if (langName == null || langName.isEmpty())
			langName = Languages.extToLang(filename);
		Language language = Languages.LANGUAGES.get(langName);
		EditLanguage lang = getLanguage(language);
		logger.info("language {}", lang);
		EditSession sess = sessionsByName.get(filename);
		if (sess == null) {
			switchTo(createSession(filename, text, lang));
		} else {
			switchTo(sess);
			editor.paste(text);
		}
	}

	@Override
	public Async<Dict> open(String filename, String text, String langName) {
		if (langName == null || langName.isEmpty())
			langName = Languages.extToLang(filename);
		Language language = Languages.LANGUAGES.get(langName);
		return open(filename, text, language);
	}

	public Async<Dict> open(String filename, String text, Language language) {
		logger.info("Open: file " + filename + " language" + language + " text:\n" + (text != null ? text : "null"));
		EditLanguage lang = getLanguage(language);
		logger.info("language {}", lang);
		if (currentSession != null) {
			if (currentSession.filename.equals(filename)) {
				if (text != null && !text.isEmpty())
					editor.switchState(setDoc(editor.state(), text));
				return null;
			} else if (currentSession.filename.startsWith("*") && isEmpty(editor.state())) {
				logger.info("replacing *scratch*");
				sessionsByName.remove(currentSession.filename);
				if (text == null)
					text = Client.client.storage().getItem("file://" + filename); // client.fileSystem.read(filename);
				if (text == null)
					text = "";
				logger.info("text: " + text);
				if (lang == currentSession.lang)
					editor.switchState(setDoc(editor.state(), text));
				else
					editor.switchState(editor.createState(lang.lang.editMode, text));
				currentSession.filename = filename;
				currentSession.lang = lang;
				sessionsByName.put(filename, currentSession);
				currentSession.tab.withAttr("data-session", String.valueOf(currentSession.id))//
						.withAttr("data-filename", filename)//
						.withAttr("data-language", lang.lang.editMode);
				currentSession.tabName.withText(filename);
				setFooter();
				return null;
			}
		}
		if (text == null)
			text = Client.client.storage().getItem("file://" + filename); // client.fileSystem.read(filename);
		if (text == null)
			text = "";

		EditSession sess = sessionsByName.get(filename);
		if (sess == null) {
			sess = createSession(filename, text, lang);
		} else {
			sess.state = setDoc(sess.state, text);
		}
		switchTo(sess);
		return null;
	}

	public EditSession createSession(String filename, String text, EditLanguage lang) {
		logger.info("createSession({},{},{})", filename, text, lang.name);
		int id = nextSessionId++;
		EditSession sess = new EditSession();
		sess.tab = Browser.document.createElement("span").withAttr("class", "tab")
				.withAttr("data-session", String.valueOf(id))//
				.withAttr("data-filename", filename) //
				.withAttr("data-language", lang.name);
		sess.tabName = Browser.document.createElement("span").withAttr("class", "tab-name")//
				.withText(filename);
		sess.tab.appendChild(sess.tabName);
		sess.closeLink = Browser.document.createElement("button") //
//				.withAttr("class", "ui-icon ui-icon-close") //
				.withAttr("type", "button") //
				.withAttr("class", "tab-close").withAttr("role", "presentation")//
				.withText("×");
		sess.tab.appendChild(sess.closeLink);

		tabs.appendChild(sess.tab);
		logger.info("editor tabs: {}", tabs);
		sess.state = editor.createState(lang.name, text);
		sess.id = id;
		sess.filename = filename;
		sess.lang = lang;
		sessionsByName.put(filename, sess);
		sessionsById.put(id, sess);
		return sess;
	}

	public class EditSession {
		int scrollTop = -1;
		int id;
		String filename;
		String lastSave;
		TDEditor.State state;
		HTMLElement tab;
		HTMLElement tabName;
		HTMLElement closeLink;
		EditLanguage lang;
		List<JSObject> decorations = new ArrayList<>();
	}

	public class EditLanguage {
		Shell shell;
		LanguageConsole console;
		String name;
		Language lang;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EditLanguage [shell=").append(shell).append(", lang=").append(lang).append(", console=")
					.append(console).append(", name=").append(name).append("]");
			return builder.toString();
		}

	}
}
