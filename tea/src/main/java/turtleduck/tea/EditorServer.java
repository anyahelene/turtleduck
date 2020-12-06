package turtleduck.tea;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import ace.Ace;
import ace.AceSession;
import turtleduck.comms.AbstractChannel;
import turtleduck.comms.Message;
import turtleduck.comms.Message.StringDataMessage;

public class EditorServer extends AbstractChannel {
	private HTMLElement element;
	private Client client;
	private AceSession session;
	private HTMLElement saveButton;
	private HTMLElement closeButton;
	private HTMLElement tabs;
	private HTMLElement wrapper;
	private HTMLElement tabItem;

	public EditorServer(HTMLElement element, HTMLElement wrapper, HTMLElement tabs, String service, Client client) {
		super(element.getAttribute("id"), service, null);
		this.client = client;
		this.tabs = tabs;
		this.wrapper = wrapper;
	}

	@Override
	public void receive(Message msg) {
		HTMLDocument document = Window.current().getDocument();
		if (msg.type().equals("Data")) {
			Message.StringDataMessage dmsg = (StringDataMessage) msg;
			NativeTScreen.consoleLog("Contents: " + dmsg.data());
			session.setValue(dmsg.data());
		} else if (msg.type().equals("Dict")) {
			Message.DictDataMessage dmsg = (Message.DictDataMessage) msg;
			NativeTScreen.consoleLog("Contents1: " + dmsg);
			String kind = dmsg.get("kind");
			if ("mark".equals(kind)) {
				String err = dmsg.get("msg");
				int pos = Integer.parseInt(dmsg.get("pos"));
				int start = Integer.parseInt(dmsg.get("start"));
				int end = Integer.parseInt(dmsg.get("end"));
				NativeTScreen.consoleLog("Contents2: " + err + ", " + pos + ", " + start + ", " + end);
			} else if ("code".equals(kind)) {
				document.getElementById("instructions").getStyle().setProperty("display", "block");
				HTMLElement disp = document.getElementById("bytecode-display");
				disp.setInnerHTML(dmsg.get("text"));
			} else if ("methods".equals(kind)) {
				HTMLElement ul = document.getElementById("explorer-methods");
				if (ul != null) {
					ul.clear();
					for (String s : dmsg.get("text").split("<li>")) {
						ul.appendChild(document.createElement("li").withText(s));
					}
				}
			} else if ("vars".equals(kind)) {
				HTMLElement ul = document.getElementById("explorer-variables");
				if (ul != null) {
					ul.clear();
					for (String s : dmsg.get("text").split("<li>")) {
						ul.appendChild(document.createElement("li").withText(s));
					}
				}
			} else if ("types".equals(kind)) {
				HTMLElement ul = document.getElementById("explorer-types");
				if (ul != null) {
					ul.clear();
					for (String s : dmsg.get("text").split("<li>")) {
						ul.appendChild(document.createElement("li").withText(s));
					}
				}
			}

		}
	}

	@Override
	public void closed(String reason) {
		super.closed(reason);
		if (saveButton != null)
			saveButton.removeEventListener("click", this::save);
		if (closeButton != null)
			closeButton.removeEventListener("click", this::close);
	}

	@Override
	public void initialize() {
		HTMLDocument document = Window.current().getDocument();
		wrapper.getStyle().setProperty("display", "flex");
		saveButton = document.getElementById("tb-save");
		closeButton = document.getElementById("tb-close");
		if (tabs != null) {
			tabItem = document.createElement("li");
			tabItem.setClassName("nav-item");
			HTMLElement link = document.createElement("a") //
					.withAttr("href", "#") //
					.withAttr("class", "nav-link") //
					.withText(name);
			tabItem.appendChild(link);
			link = document.createElement("span") //
					.withAttr("class", "ui-icon ui-icon-close") //
					.withAttr("role", "presentation")//
					.withText("Remove Tab");
			tabItem.appendChild(link);
			NativeTScreen.consoleLog(saveButton);
			NativeTScreen.consoleLog(tabs);

			tabs.appendChild(tabItem);

//			link.addEventListener("click", (e) -> {
//				link2.setClassName(link2.getClassName().replace(" active", ""));
//				link.setClassName(link.getClassName() + " active");
//			});
//			link2.addEventListener("click", (e) -> {
//				link.setClassName(link.getClassName().replace(" active", ""));
//				link2.setClassName(link2.getClassName() + " active");
//			});
		}

		element = document.getElementById(service + "-embed");
		NativeTScreen.consoleLog("save: " + service + "-save");
		NativeTScreen.consoleLog(saveButton);
		if (saveButton != null)
			saveButton.addEventListener("click", this::save);
		if (closeButton != null)
			closeButton.addEventListener("click", this::close);

		if (client.editor == null) {
			client.editor = Ace.edit(element);
			client.editor.setTheme("ace/theme/dawn");
			session = client.editor.session();
			session.setMode("ace/mode/java");
			client.map.set("editor", client.editor);
		} else {
			session = Ace.createEditSession(name, "ace/mode/java");
			client.editor.setSession(session);
		}
	}

	public void dispose() {
		if (tabs != null) {
			tabs.removeChild(tabItem);
		}
		if (saveButton != null)
			saveButton.removeEventListener("click", this::save);
		if (closeButton != null)
			closeButton.removeEventListener("click", this::close);
	}

	protected void save(Event e) {
		NativeTScreen.consoleLog("save: ");
		NativeTScreen.consoleLog(e);
		if (client.editor != null) {
			AceSession sess = client.editor.getSession();
			if (sess == session) {
				String contents = client.editor.getValue();
				StringDataMessage msg = Message.createStringData(0, contents);
				send(msg);
			}
		}
	}

	protected void close(Event e) {
		NativeTScreen.consoleLog("close: ");
		NativeTScreen.consoleLog(e);
	}
}
