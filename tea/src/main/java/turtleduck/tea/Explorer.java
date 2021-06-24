package turtleduck.tea;

import org.slf4j.Logger;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Text;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.messaging.CodeService;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.ShellService;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

import static turtleduck.tea.HTMLUtil.*;

@MessageDispatch("turtleduck.tea.generated.ExplorerDispatch")
public class Explorer implements ExplorerService {
	public static final Logger logger = Logging.getLogger(Explorer.class);
	private HTMLElement completion;
	private HTMLElement explorerElement;

	public Explorer(HTMLElement element) {
		this.explorerElement = element;
		this.completion = div(clazz("completion"), style("display", "none"), attr("tabindex", "-1"));
		explorerElement.appendChild(completion);
		// completion.addEventListener("blur", e -> {
		// completion.getStyle().setProperty("display", "none"); });
		JSUtil.createComponent(element);
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
			HTMLElement elt = document.getElementById(eltId);

			if (event.endsWith("del")) {
				elt.delete();
				if (!ul.hasChildNodes()) {
					ul.setClassName(ul.getClassName().replaceAll("\\s*empty", "") + " empty");
					head.setClassName(head.getClassName().replaceAll("\\s*empty", "") + " empty");
				}
			} else {
				if (elt == null) {
					HTMLElement newelt = document.createElement("li");
					elt = newelt;
					elt.setAttribute("id", eltId);
					ul.appendChild(elt);
					ul.setClassName(ul.getClassName().replaceAll("\\s*empty", ""));
					head.setClassName(head.getClassName().replaceAll("\\s*empty", ""));
					JSUtil.activateDrag(elt, "text/plain", signature);
					if (category.equals("var")) {
						elt.addEventListener("click", ev -> {
							logger.info("completion event: {}", ev);
							handleDotSuggestion(newelt, signature, JSUtil.activeComponent());
							ev.preventDefault();
						});
					}
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

	protected void handleDotSuggestion(HTMLElement element, String varName, Component lastFocus) {
		String generics = "<(?:[^>]|<(?:[^>]|<[^<>]*>)*>)*>";
		Async<Dict> complete = Client.client.shellService.complete(varName + ".", varName.length() + 1, 0);
		complete.onSuccess(msg -> {
			logger.info("complete reply: {}", msg);
			int anchor = msg.get(CodeService.ANCHOR);
			boolean matches = msg.get(CodeService.MATCHES);
			Array comps = msg.get(CodeService.COMPLETES);
			HTMLElement list = element("ul");
			completion.clear();

			logger.info("completion: activeelement: {}", lastFocus);
			for (String comp : comps.toListOf(String.class)) {
				String continuation = comp;
				String signature = varName;
				int i = comp.indexOf('–');
				if (i >= 0) {
					continuation = comp.substring(0, i);
					signature = comp.substring(i + 1)
							.replaceAll("([a-zA-Z_]([a-zA-Z0-9_]|" + generics + ")*\\.)(?!\\.)", "")
							.replaceAll("^(\\S+)\\s+(?:"+generics+")?(.*)$", ".$2 → $1");
				}
				String text = varName + "." + continuation;
				int cursorAdj = 0;
				if (text.endsWith("(")) {
					text += ")";
					cursorAdj = -1;
				}
				HTMLElement link = element("a", attr("href", "#paste(" + text + ")"), signature);
				JSUtil.activatePaste(link, "currentTarget", text, cursorAdj, e -> {
					// completion.clear();
					completion.getStyle().setProperty("display", "none");
					if (lastFocus != null) {
						lastFocus.focus();
						logger.info("completion: focusing: {}", lastFocus);

					}
				});
				list.appendChild(element("li", link));
			}
			completion.appendChild(element("h3", varName));
			completion.appendChild(list);
			completion.getStyle().setProperty("display", "flex");
			completion.focus();
		}).onFailure(msg -> {
			logger.error("complete error: {}", msg);
		});
	}
}
