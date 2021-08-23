package turtleduck.tea;

import org.slf4j.Logger;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.NodeList;
import org.teavm.jso.dom.xml.Text;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.messaging.CodeService;
import turtleduck.messaging.ExplorerService;
import turtleduck.messaging.ShellService;
import turtleduck.tea.teavm.DataTransfer;
import turtleduck.tea.teavm.DragEvent;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

import static turtleduck.tea.HTMLUtil.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@MessageDispatch("turtleduck.tea.generated.ExplorerDispatch")
public class Explorer implements ExplorerService {

	// things that should use "help" to get more info
	private static final Set<String> helpCategories = new HashSet<>(
			Arrays.asList("class", "enum", "interface", "anno", "type"));
	// things that should use "dot suggestion" to get more info
	private static final Set<String> dotCategories = new HashSet<>(Arrays.asList("var", "import"));
	public static final Logger logger = Logging.getLogger(Explorer.class);
	private HTMLElement completion;
	private HTMLElement explorerElement;
	private Map<String, ExplorerData> snippets = new HashMap<>();
	private HTMLElement scroller;
	private int scrollLeft = 0;
	protected ShellService shellService;
	private Component component;

	public Explorer(HTMLElement element, Component parent, ShellService service) {
		this.explorerElement = element;
		this.shellService = service;
		NodeList<? extends HTMLElement> nodeList = element.getElementsByTagName("section");
		if (nodeList.getLength() != 1) {
			logger.error("expected exactly *one* <section> element under the main element: {}", nodeList);
		} else {
			scroller = nodeList.get(0);
		}
		this.completion = div(clazz("completion"), style("display", "none"), attr("tabindex", "-1"));
		explorerElement.appendChild(completion);
		completion.addEventListener("focusout", e -> {
			if (!JSUtil.relatedIsContained(e))
				completion.getStyle().setProperty("display", "none");
		});
		component = JSUtil.createComponent(element);
		component.addWindowTools();
		component.setParent(parent);
		component.register();
	}

	protected void dragstartHandler(Event ev) {
		scrollLeft = scroller.getScrollLeft();
		logger.info("dragstart {} scrollLeft={}", ev, scrollLeft);
		DataTransfer dataTransfer = ((DragEvent) ev).getDataTransfer();
		logger.info("transfer {}", dataTransfer);
		HTMLElement element = (HTMLElement) ev.getCurrentTarget();
		ExplorerData data = snippets.get(element.getAttribute("id"));
		logger.info("data {}", data);
		if (data != null && dataTransfer != null) {
			dataTransfer.setDropEffect("copy");
			dataTransfer.setStringData("text/plain", data.pasteData);
			logger.info("setdata {}: {}", data.pasteData, dataTransfer);
//			ev.preventDefault();
		}
	}

	protected void dragendHandler(Event ev) {
		scroller.setScrollLeft(scrollLeft);
	}

	protected void clickHandler(Event ev) {
		logger.info("click event: {}", ev);
		HTMLElement evElt = (HTMLElement) ev.getCurrentTarget();
		ExplorerData evData = snippets.get(evElt.getAttribute("id"));
		if (evData != null) {
			evData.lastFocus = JSUtil.activeComponent();
			displayHelp(evData);
			ev.preventDefault();
		}
	}

	@Override
	public Async<Dict> update(Dict info) {
		try {
			HTMLDocument document = Window.current().getDocument();
			String snipkind = info.get(ShellService.SNIP_KIND);
			String kind = snipkind.replaceAll("\\..*$", "");
			HTMLElement ul = document.getElementById("explorer-" + kind);
			HTMLElement head = document.getElementById("explorer-head-" + kind);

			if (ul != null && !snipkind.equals("var.temp")) {
				String eltId = "snippet-" + info.get(ShellService.SNIP_ID);
				String event = info.getString("event");
				ExplorerData data = snippets.get(eltId);

				if (data != null && event.endsWith("del")) {
					snippets.remove(eltId);
					data.element.delete();
					if (!ul.hasChildNodes()) {
						ul.setClassName(ul.getClassName().replaceAll("\\s*empty", "") + " empty");
						head.setClassName(head.getClassName().replaceAll("\\s*empty", "") + " empty");
					}
				} else {
					if (data == null) {
						data = new ExplorerData();
						data.element = element("li", attr("id", eltId));
						ul.appendChild(data.element);
						ul.setClassName(ul.getClassName().replaceAll("\\s*empty", ""));
						head.setClassName(head.getClassName().replaceAll("\\s*empty", ""));

						data.element.setAttribute("draggable", "true");
						data.element.addEventListener("dragstart", this::dragstartHandler);
						data.element.addEventListener("dragend", this::dragendHandler);
						data.element.addEventListener("click", this::clickHandler);
						data.sid = info.get(ShellService.SNIP_ID);
						data.eltId = eltId;
						snippets.put(eltId, data);
					}
					data.snipkind = snipkind;
					data.sym = info.getString("sym");
					data.fullname = info.get(ShellService.FULL_NAME);
					data.category = info.getString("category");
					data.signature = info.getString("signature");
					data.icon = info.get(ShellService.ICON);
					data.pasteData = data.signature;
					data.completionData = null;

					if (data.sym.equals("✅"))
						data.sym = "";
					if (data.fullname != null) {
						data.element.setAttribute("title", data.fullname);
					}
					data.element.setAttribute("class",
							"clickable " + data.category + (data.sid.startsWith("s") ? " builtin" : ""));
					if (data.icon != null && !data.icon.isEmpty())
						data.element.setAttribute("style", "list-style-type: \"" + data.icon + "\"");
					data.element.withText(data.signature + data.sym);
				}

			}
			return null;
		} catch (Throwable t) {
			logger.error("update trouble: {}", t);
			throw t;
		}
	}

	protected void displayHelp(ExplorerData data) {
		String varName = data.signature;

		if (data.completionData != null) {
			logger.info("recompletion: activeelement: {}", data.lastFocus);
			String focusTitle = data.lastFocus != null ? data.lastFocus.title() : "";
			String pasteUrl = "insert://" + focusTitle + "/";
			completion.clear();
			completion.appendChild(element("h3", data.completionTitle));
			NodeList<? extends HTMLElement> allDetails = data.completionData.querySelectorAll("details");
			for (int i = 0; i < allDetails.getLength(); i++) {
				allDetails.get(i).removeAttribute("open");
			}
			NodeList<? extends HTMLElement> targets = data.completionData.querySelectorAll("a.paste");
			for (int i = 0; i < targets.getLength(); i++) {
				HTMLElement elt = targets.get(i);
				elt.setAttribute("href", pasteUrl + elt.getAttribute("data-paste"));
			}
			completion.appendChild(data.completionData);
			logger.info("completion height: {}", completion.getOffsetHeight());
			completion.getStyle().setProperty("display", "flex");
			completion.getStyle().setProperty("top", data.element.getOffsetTop() + "px");
			// completion.getStyle().setProperty("left", data.element.getOffsetLeft() +
			// "px");
			completion.focus();
			return;
		}

		if (dotCategories.contains(data.category)) {
			doDotHelp(varName, data);

		} else if (helpCategories.contains(data.category)) {
			doPlainHelp(varName, data);
		}
	}

	protected void doDotHelp(String varName, ExplorerData data) {
		// HTMLElement element = data.element;
		String code = varName + ".";
		Async<Dict> complete = shellService.complete(code, varName.length() + 1, 0);
		complete.onSuccess(msg -> {
			logger.info("inspect reply: {}", msg);
			int anchor = msg.get(CodeService.ANCHOR);
			boolean matches = msg.get(CodeService.MATCHES, false);
			Array comps = msg.get(CodeService.COMPLETES);
			String typename = msg.get(ShellService.TYPE, null);
			String title = varName;
			if (typename != null) {
				title = title + " : " + typename;
			}
			HTMLElement list = element("ul");
			completion.clear();

			logger.info("inspection: activeelement: {}", data.lastFocus);
			for (String comp : comps.toListOf(String.class)) {
				String[] result = comp.split("–");
				String continuation = result[0];
				String text = code.substring(0, anchor) + continuation;
				String signature = result.length > 1 ? result[1] : "";
				String doc = result.length > 2 ? result[2].trim() : ""; // .replaceAll("\n+$", "") : "";
				int cursorAdj = 0;
				if (text.endsWith("(")) {
					text += ")";
					cursorAdj = -1;
				}
				HTMLElement docElt = null;
				if (!doc.isEmpty()) {
					int i = doc.indexOf('\n');
					if (i > 0) {
						String synopsis = doc.substring(0, i);
						doc = doc.substring(i);
						docElt = element("details", clazz("doc"), element("summary", synopsis), doc);
					} else {
						docElt = element("details", clazz("doc summary-only"), element("summary", doc));
						// docElt = element("span", clazz("doc"), doc);
					}
				}
				if (!signature.isEmpty())
					signature = prettySignature(signature);
				else
					signature = text;
				String focusTitle = data.lastFocus != null ?  data.lastFocus.title() : "";
				HTMLElement link = element("a", clazz("paste"),//
						attr("href", "insert://" + focusTitle + "/" + text), //
						attr("data-paste", text), signature);
				JSUtil.activatePaste(link, "currentTarget", text, cursorAdj, e -> {
					// completion.clear();
					completion.getStyle().setProperty("display", "none");
					if (data.lastFocus != null) {
						data.lastFocus.focus();
						logger.info("completion: focusing: {}", data.lastFocus);

					}
				});
				list.appendChild(element("li", link, docElt));
			}
			data.completionTitle = title;
			data.completionData = list;
			completion.appendChild(element("h3", title));
			completion.appendChild(list);
			logger.info("completion height: {}", completion.getOffsetHeight());
			completion.getStyle().setProperty("display", "flex");
			completion.getStyle().setProperty("top", data.element.getOffsetTop() + "px");
//			completion.getStyle().setProperty("left", data.element.getOffsetLeft() + "px");
			completion.focus();
		}).onFailure(msg -> {
			logger.error("complete error: {}", msg);
		});
	}

	protected void doPlainHelp(String varName, ExplorerData data) {
		Async<Dict> inspect = shellService.inspect(varName, 0, 0);
		inspect.onSuccess(msg -> {
			logger.info("inspect reply: {}", msg);
			// Dict inspectData = msg.get(CodeService.DATA);
			String signature = msg.get(ShellService.SIGNATURE, null);
			String doc = msg.get(ShellService.TEXT, null);
			String typename = msg.get(ShellService.TYPE, null);

			String title = varName;
			if (typename != null) {
				title = title + " : " + typename;
			}
			completion.clear();

			HTMLElement docElt = null;
			if (!doc.isEmpty()) {
				int i = doc.indexOf('\n');
				if (i > 0) {
					String synopsis = doc.substring(0, i);
					doc = doc.substring(i);
					docElt = element("details", clazz("doc"), element("summary", synopsis), doc);
				} else {
					docElt = element("details", clazz("doc summary-only"), element("summary", doc));
					// docElt = element("span", clazz("doc"), doc);
				}
			}
			if (!signature.isEmpty())
				signature = prettySignature(signature);
			else
				signature = varName;
			String focusTitle = data.lastFocus != null ?  data.lastFocus.title() : "";
			HTMLElement link = element("a", clazz("paste"), 
					attr("href", "insert://" + focusTitle + "/" + varName), 
					attr("data-paste", varName), signature);
			JSUtil.activatePaste(link, "currentTarget", varName, 0, e -> {
				// completion.clear();
				completion.getStyle().setProperty("display", "none");
				if (data.lastFocus != null) {
					data.lastFocus.focus();
					logger.info("completion: focusing: {}", data.lastFocus);

				}
			});

			data.completionTitle = title;
			data.completionData = docElt;
			completion.appendChild(element("h3", title));
			completion.appendChild(docElt);
			logger.info("completion height: {}", completion.getOffsetHeight());
			completion.getStyle().setProperty("display", "flex");
			completion.getStyle().setProperty("top", data.element.getOffsetTop() + "px");
//			completion.getStyle().setProperty("left", data.element.getOffsetLeft() + "px");
			completion.focus();
		}).onFailure(msg -> {
			logger.error("complete error: {}", msg);
		});
	}

	class ExplorerData {
		public String completionTitle;
		HTMLElement element;
		String eltId;
		String snipkind;
		String event;
		String sid;
		String sym;
		String fullname;
		String category;
		String signature;
		String icon;
		String pasteData;
		HTMLElement completionData;
		Component lastFocus;
	}

	private static String generics = "<(?:[^>]|<(?:[^>]|<[^<>]*>)*>)*>";
	private static String genericsPattern1 = "([a-zA-Z_]([a-zA-Z0-9_]|" + generics + ")*\\.)(?!\\.)";
	private static String genericsPattern2 = "^(\\S+)\\s+(?:" + generics + ")?(.*)$";

	private static String prettySignature(String s) {
		return s.replaceAll(genericsPattern1, "").replaceAll(genericsPattern2, ".$2 → $1");
	}

}
