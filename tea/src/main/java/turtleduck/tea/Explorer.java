package turtleduck.tea;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Text;

import turtleduck.comms.AbstractChannel;
import turtleduck.comms.Message;
import turtleduck.comms.Message.StringDataMessage;

public class Explorer extends AbstractChannel {
//	private HTMLElement element;

	public Explorer(String name, String elementId) {
		super(name, elementId, null);
	}

	@Override
	public void receive(Message msg) {
		if (msg.type().equals("Data")) {
			Message.StringDataMessage dmsg = (StringDataMessage) msg;
			String[] split = dmsg.data().split(":", 4);
			if (split.length == 4) {
				String cmd = split[0];
				String id = service + "-" + split[1];
				String kind = split[2];
				String arg = split[3];
				HTMLElement elt = Window.current().getDocument().getElementById(id);
				HTMLElement parent = Window.current().getDocument().getElementById(service + "-" + kind + "s");
				switch (cmd) {
				case "add":
				case "upd":
					if (parent != null) {
						if (elt == null) {
							elt = Window.current().getDocument().createElement("li");
							elt.setAttribute("id", id);
							Text text = Window.current().getDocument().createTextNode(arg);
							elt.appendChild(text);
						} else {
							elt.getFirstChild().setNodeValue(arg);
							elt.getParentNode().removeChild(elt);
						}
						parent.appendChild(elt);
					}
					break;
				case "del":
					if (elt != null)
						elt.getParentNode().removeChild(elt);
					break;
				}
			}
		}
	}

	@Override
	public void initialize() {
//		element = Window.current().getDocument().getElementById(service);
	}
}
