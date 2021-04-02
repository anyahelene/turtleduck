package turtleduck.tea;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import ace.Ace;
import ace.AceSession;
import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.messaging.EditorService;
import turtleduck.messaging.ShellService;
import turtleduck.util.Dict;

@MessageDispatch("turtleduck.tea.generated.EditorDispatch")
public class EditorServer implements EditorService {
	public static final String ENDPOINT_ID = "turtleduck.editor";
	public static final String TABCLASS_INACTIVE = "title saveable";
	public static final String TABCLASS_ACTIVE = "title saveable active";
	private Map<String, EditSession> sessionsByName = new LinkedHashMap<>();
	private Map<String, EditSession> sessionsById = new HashMap<>();
	private HTMLElement element;
	private Client client;
	private EditSession currentSession;
	private HTMLElement saveButton;
	private HTMLElement closeButton;
	private HTMLElement tabs;
	private HTMLElement wrapper;
//	private HTMLElement tabItem;
	private String service;
	private ShellService shell;
	private int nextSessionId = 0;

	public EditorServer(HTMLElement element, HTMLElement wrapper, HTMLElement tabs, String service, Client client,
			ShellService shell) {
		this.client = client;
		this.tabs = tabs;
		this.wrapper = wrapper;
		this.service = service;
		this.shell = shell;
	}

	public void initialize() {
		String name = "*scratch*";
		HTMLDocument document = Window.current().getDocument();
		wrapper.getStyle().setProperty("display", "flex");
		saveButton = document.getElementById("tb-save");
		closeButton = document.getElementById("tb-close");

		element = document.getElementById(service + "-embed");
		Browser.consoleLog(saveButton);
		if (saveButton != null)
			saveButton.addEventListener("click", this::save);
		if (closeButton != null)
			closeButton.addEventListener("click", this::close);
		Browser.consoleLog("editor save button: ");
		Browser.consoleLog(saveButton);

		if (client.editor == null) {
			client.editor = Ace.edit(element);
			client.editor.setTheme("ace/theme/terminal");
			client.editor.setOption("enableBasicAutocompletion", true);
			client.editor.setOption("enableLiveAutocompletion", true);
			client.editor.setOption("enableSnippets", true);
			open("*scratch*", "", "Java");
			client.map.set("editor", client.editor);
		} else {
			throw new IllegalStateException("EditorServer initialized again?!");
		}
	}

	public void dispose() {
		if (saveButton != null)
			saveButton.removeEventListener("click", this::save);
		if (closeButton != null)
			closeButton.removeEventListener("click", this::close);
	}

	protected void save(Event e) {
		Browser.consoleLog("save: ");
		Browser.consoleLog(e);
		if (currentSession != null) {
			String contents = currentSession.session.getValue();
			if (!currentSession.filename.startsWith("*")) {
				client.storage().setItem("file://" + currentSession.filename, contents);
			}
			shell.eval(contents, 0, Dict.create()).onSuccess(d -> { client.terminalClient.prompt();});
		}
	}

	protected void close(Event e) {
		Browser.consoleLog("close: ");
		Browser.consoleLog(e);
		if (currentSession != null) {
			if (!currentSession.filename.startsWith("*")) {
				String contents = currentSession.session.getValue();
				client.storage().setItem("autosave://" + currentSession.filename, contents);
			}
			sessionsByName.remove(currentSession.filename);
			sessionsById.remove(currentSession.id);
			if (sessionsByName.isEmpty()) {
				open("*scratch*", "", "Java");
			} else {
				currentSession = sessionsByName.entrySet().iterator().next().getValue();
				client.editor.setSession(currentSession.session);
			}
		}
	}

	@Override
	public Async<Dict> content(String content, String language) {
		Browser.consoleLog("Contents: " + content);
		currentSession.session.setValue(content);
		return null;
	}

	public String content() {
		return client.editor.getValue();
	}

	@Override
	public Async<Dict> mark(String message, int cursorPos, int start, int end) {
		Browser.consoleLog("Contents2: " + message + ", " + cursorPos + ", " + start + ", " + end);
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
		return Async.succeeded(Dict.create().put(EditorService.TEXT, client.editor.getValue()));
	}

	private void switchTo(EditSession sess) {
		if (currentSession != null)
			currentSession.tab.setClassName(TABCLASS_INACTIVE);
		sess.tab.setClassName(TABCLASS_ACTIVE);
		currentSession = sess;
		client.editor.setSession(currentSession.session);
	}

	@Override
	public Async<Dict> open(String filename, String text, String language) {
		Browser.consoleLog("Open: " + filename + " " + language + ":\n" + (text != null ? text : "null"));

		if (currentSession != null) {
			if (currentSession.filename.equals(filename)) {
				currentSession.session.setValue(text);
				return null;
			} else if(currentSession.filename.equals("*scratch*") && currentSession.session.getValue().equals("")) {
				Browser.consoleLog("replacing *scratch*");
				if(text == null)
					text = client.storage().getItem("file://" + filename);
				if(text == null)
					text = "";
				Browser.consoleLog("text: " + text);
				currentSession.session.setValue(text);
				currentSession.filename = filename;
				currentSession.tabLink.withText(filename);
				return null;
			}
		}
		EditSession sess = sessionsByName.get(filename);
		if (sess == null) {
			String id = "" + nextSessionId++;
			sess = new EditSession();
			sess.tab = Browser.document.createElement("span");
			sess.tabLink = Browser.document.createElement("a") //
					.withAttr("href", "#") //
					.withAttr("data-session", id)//
					.withAttr("data-filename", filename) //
					.withText(filename);
//					.withAttr("class", "nav-link") //
			sess.tab.appendChild(sess.tabLink);
			sess.closeLink = Browser.document.createElement("a") //
//					.withAttr("class", "ui-icon ui-icon-close") //
					.withAttr("href", "#") //
					.withAttr("data-session", id)//
					.withAttr("data-filename", filename) //
					.withAttr("role", "presentation")//
					.withText("×");
			sess.tab.appendChild(sess.closeLink);

			tabs.appendChild(sess.tab);
			Browser.consoleLog("editor tabs: ");
			Browser.consoleLog(tabs);
			sess.session = Ace.createEditSession(id, "ace/mode/java");
			sess.id = id;
			sess.filename = filename;

			EditSession theSession = sess;
			sess.tabLink.addEventListener("click", (e) -> {
				Browser.consoleLog("Switch to tab: " + filename);
				switchTo(theSession);
			});
			sess.closeLink.addEventListener("click", (e) -> {
				Browser.consoleLog("Close tab: " + filename);
			});
		}
		if(text == null)
			text = client.storage().getItem("file://" + currentSession.filename);
		sess.session.setValue(text);
		switchTo(sess);
		return null;
	}

	public class EditSession {
		String id;
		String filename;
		AceSession session;
		HTMLElement tab;
		HTMLElement tabLink;
		HTMLElement closeLink;
		}
}
