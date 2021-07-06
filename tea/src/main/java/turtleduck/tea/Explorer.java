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

import java.util.HashMap;
import java.util.Map;

@MessageDispatch("turtleduck.tea.generated.ExplorerDispatch")
public class Explorer implements ExplorerService {
	public static final Logger logger = Logging.getLogger(Explorer.class);
	private HTMLElement completion;
	private HTMLElement explorerElement;
	private Map<String, ExplorerData> snippets = new HashMap<>();
	private HTMLElement scroller;
	private int scrollLeft = 0;
	protected ShellService shellService;

	public Explorer(HTMLElement element, ShellService service) {
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
		JSUtil.createComponent(element);
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
			if (evData.category.equals("var")) {
				evData.lastFocus = JSUtil.activeComponent();
				handleDotSuggestion(evData);
				ev.preventDefault();
			}
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
							"java " + data.category + (data.sid.startsWith("s") ? " builtin" : ""));
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

	protected void handleDotSuggestion(ExplorerData data) {
		String varName = data.signature;

		if (data.completionData != null) {
			logger.info("recompletion: activeelement: {}", data.lastFocus);
			completion.clear();
			completion.appendChild(element("h3", varName));
			completion.appendChild(data.completionData);
			logger.info("completion height: {}", completion.getOffsetHeight());
			completion.getStyle().setProperty("display", "flex");
			completion.getStyle().setProperty("top", data.element.getOffsetTop() + "px");
			// completion.getStyle().setProperty("left", data.element.getOffsetLeft() +
			// "px");
			completion.focus();
			return;
		}
		// HTMLElement element = data.element;
		String generics = "<(?:[^>]|<(?:[^>]|<[^<>]*>)*>)*>";
		Async<Dict> complete = shellService.complete(varName + ".", varName.length() + 1, 0);
		complete.onSuccess(msg -> {
			logger.info("complete reply: {}", msg);
			int anchor = msg.get(CodeService.ANCHOR);
			boolean matches = msg.get(CodeService.MATCHES);
			Array comps = msg.get(CodeService.COMPLETES);
			HTMLElement list = element("ul");
			completion.clear();

			logger.info("completion: activeelement: {}", data.lastFocus);
			for (String comp : comps.toListOf(String.class)) {
				String continuation = comp;
				String signature = varName;
				int i = comp.indexOf('–');
				if (i >= 0) {
					continuation = comp.substring(0, i);
					signature = comp.substring(i + 1)
							.replaceAll("([a-zA-Z_]([a-zA-Z0-9_]|" + generics + ")*\\.)(?!\\.)", "")
							.replaceAll("^(\\S+)\\s+(?:" + generics + ")?(.*)$", ".$2 → $1");
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
					if (data.lastFocus != null) {
						data.lastFocus.focus();
						logger.info("completion: focusing: {}", data.lastFocus);

					}
				});
				list.appendChild(element("li", link));
			}
			data.completionData = list;
			completion.appendChild(element("h3", varName));
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

	class ExplorerData {
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
}
