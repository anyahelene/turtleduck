package turtleduck.tea;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Text;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.ShellService;
import turtleduck.util.Dict;

@MessageDispatch("turtleduck.tea.generated.ExplorerDispatch")
public class Explorer implements ExplorerService {
	public void receive(String data) {
		String service = "explorer";
		String[] split = data.split(":", 4);
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

	@Override
	public Async<Dict> update(Dict info) {
		String snipkind = info.get(ShellService.SNIP_KIND);
		String event = info.getString("event");
		String sid = info.get(ShellService.SNIP_ID);
		String sym = info.getString("sym");
		String fullname = info.get(ShellService.FULL_NAME);
		String category = info.getString("category");
		String signature = info.getString("signature");
		String icon = info.get(ShellService.ICON);
		HTMLDocument document = Window.current().getDocument();
		String kind = snipkind;
		int i = kind.indexOf('.');
		if (i >= 0)
			kind = kind.substring(0, i);
		HTMLElement ul = document.getElementById("explorer-" + kind);
		HTMLElement head = document.getElementById("explorer-head-" + kind);
		if (ul != null && !snipkind.equals("var.temp")) {
			String eltId = "snippet-" + sid;
			HTMLElement elt;
			elt = document.getElementById(eltId);

			if (event.endsWith("del")) {
				elt.delete();
				if (!ul.hasChildNodes()) {
					ul.setClassName(ul.getClassName().replaceAll("\\s*empty", "") + " empty");
					head.setClassName(head.getClassName().replaceAll("\\s*empty", "") + " empty");
				}
			} else {
				if (elt == null) {
					elt = document.createElement("li");
					elt.setAttribute("id", eltId);
					ul.appendChild(elt);
					ul.setClassName(ul.getClassName().replaceAll("\\s*empty", ""));
					head.setClassName(head.getClassName().replaceAll("\\s*empty", ""));
				}
				if (sym.equals("✅"))
					sym = "";
				if (fullname != null) {
					elt.setAttribute("title", fullname);
				}
				elt.setAttribute("class", "java " + category + (sid.startsWith("s") ? " builtin" : ""));
				if (icon != null && !icon.isEmpty())
					elt.setAttribute("style", "list-style-type: \"" + icon + "\"");
				elt.withText(signature + sym);
			}
		}
		return null;
	}

}
