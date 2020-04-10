package turtleduck.tea;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
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

	public EditorServer(String name, String elementId, Client client) {
		super(name, elementId, null);
		this.client = client;
	}

	@Override
	public void receive(Message msg) {
		if (msg.type().equals("Data")) {
			Message.StringDataMessage dmsg = (StringDataMessage) msg;
			NativeTScreen.consoleLog("Contents: " + dmsg.data());
			session.setValue(dmsg.data());
		}
	}

	@Override
	public void closed(String reason) {
		super.closed(reason);
		HTMLElement save = Window.current().getDocument().getElementById(name + "-save");
		if (save != null)
			save.removeEventListener("click", this::save);
		HTMLElement close = Window.current().getDocument().getElementById(name + "-close");
		if (close != null)
			close.removeEventListener("click", this::close);
	}


	@Override
	public void initialize() {
		element = Window.current().getDocument().getElementById(service);
		NativeTScreen.consoleLog("save: " + service + "-save");
		HTMLElement save = Window.current().getDocument().getElementById(service + "-save");
		NativeTScreen.consoleLog(save);
		if (save != null)
			save.addEventListener("click", this::save);
		HTMLElement close = Window.current().getDocument().getElementById(service + "-close");
		if (close != null)
			close.addEventListener("click", this::close);

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

	protected void save(Event e) {
		NativeTScreen.consoleLog("save: ");
		NativeTScreen.consoleLog(e);
		if(client.editor != null) {
			AceSession sess = client.editor.getSession();
			if(sess == session) {
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
