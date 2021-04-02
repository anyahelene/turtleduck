package turtleduck.tea;

import java.util.HashMap;
import java.util.Map;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSFunction;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.css.ElementCSSInlineStyle;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.events.WheelEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.dom.xml.NodeList;
import org.teavm.jso.dom.xml.Text;
import org.teavm.jso.typedarrays.ArrayBuffer;

import svg.DOMRect;
import svg.SVGSVGElement;
import svg.TSpanElement;
import svg.SVGTransform;
import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.messaging.CanvasService;
import turtleduck.util.Array;
import turtleduck.util.Dict;

@MessageDispatch("turtleduck.tea.generated.CanvasDispatch")
public class CanvasServer implements CanvasService {
	@JSBody(params = { "elt" }, script = "if(elt.transform.baseVal.numberOfItems == 0)\n"
			+ "  elt.transform.baseVal.appendItem(document.getElementById('svg0').createSVGTransform());\n"
			+ " return elt.transform.baseVal.getItem(0);\n")
	native static SVGTransform svgTransform(Element elt);

	private EventListener<KeyboardEvent> keyListener;
	private SVGSVGElement svg;
	private HTMLDocument document;
	private Map<String, Dict> styles = new HashMap<>();
	private HTMLElement figure;

	public CanvasServer() {
		document = Window.current().getDocument();
		svg = (SVGSVGElement) document.getElementById("svg0");
		figure = document.getElementById("figure0");
		if (svg != null) {
			svg.listenWheel(this::zoom);
		}
		HTMLElement gfxClear = document.getElementById("gfx-clear");
		if (gfxClear != null) {
			gfxClear.listenClick(this::clear);
		}
		HTMLElement gfxSave = document.getElementById("gfx-save");
		if (gfxSave != null) {
			gfxSave.listenClick(this::save);
		}

	}

	public Async<Dict> clear() {
		svg.setInnerHTML("");
		return null;
	}

	protected void zoom(WheelEvent e) {
		double deltaY = e.getDeltaY();
		if (e.getDeltaMode() == WheelEvent.DOM_DELTA_LINE)
			deltaY *= 10;
		if (e.getDeltaMode() == WheelEvent.DOM_DELTA_PAGE)
			deltaY *= 100;
		if (deltaY != 0) {
			DOMRect rect = svg.getViewBox().getBaseVal();
			deltaY = Math.max(deltaY, 5 - rect.getWidth());
			deltaY = Math.max(deltaY, 5 - rect.getHeight());
			rect.setWidth(rect.getWidth() + deltaY);
			rect.setHeight(rect.getHeight() + deltaY);
		}

	}

	protected void clear(Event e) {
		clear();
	}

	protected void save(Event e) {
		HTMLElement elt = document.createElement("a");
		String txt = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
				+ "<svg width=\"391\" height=\"391\" viewBox=\"-70.5 -70.5 391 391\" xmlns=\"http://www.w3.org/2000/svg\">";
		txt += svg.getInnerHTML();
		txt += "</svg>";
		elt.setAttribute("href", "data:image/svg+xml;charset=utf-8," + JSUtil.encodeURIComponent(txt));
		elt.setAttribute("download", "canvas.svg");
		Browser.consoleLog(elt);
		elt.click();

	}

	@Override
	public Async<Dict> drawPath(Array paths) {
		HTMLDocument document = Window.current().getDocument();
		svg.getStyle().setProperty("display", "block");
		Map<String, Element> groups = new HashMap<>();
		for (Dict path : paths.toListOf(Dict.class)) {
			Element elt = null;

			String d = path.getString("PATH");
			if (path.has("PATH")) {
				elt = path(path.getString("PATH"), path);
			} else if (path.has("TEXT")) {
				elt = text(path.getString("TEXT"), path);
			} else {
				Browser.consoleLog("Unknown operation: " + path.toJson());
			}

			if (elt != null) {
				String stroke = path.getString("STROKE");
				String fill = path.getString("FILL");
				Number w = path.get(WIDTH);
				if (w != null)
					elt.setAttribute("stroke-width", w.toString());
				elt.setAttribute("stroke", stroke != null ? stroke : "none");
				elt.setAttribute("fill", fill != null ? fill : "none");

				String objid = path.get(CanvasService.OBJ_ID);
				if (objid != null)
					elt.setAttribute("id", objid);

				String group = path.getString("GROUP");
				if (group != null) {
					String id = "svg0." + group;
					Element g = groups.get(group);
					if (g == null) {
						g = document.getElementById(id);
					}
					if (g == null) {
						g = document.createElementNS(svg.getNamespaceURI(), "g");
						g.setAttribute("id", id);
						svg.appendChild(g);
					}
					groups.put(group, g);
					if (styles.containsKey(group)) {
						Dict style = styles.remove(group);
						styleObject(g, style);
					}
					g.appendChild(elt);
				} else {
					svg.appendChild(elt);
				}
			}
		}
		return null;
	}

	private Element text(String t, Dict path) {
		Element elt = document.createElementNS(svg.getNamespaceURI(), "text");
		Number n = path.get(X);
		elt.setAttribute("x", n.toString());
		n = path.get(Y);
		elt.setAttribute("y", n.toString());
		// elt.appendChild(document.createTextNode(t));
		setText(elt, t);
		String style = "text-anchor: middle; dominant-baseline:middle; text-align: " + path.getString("ALIGN");
		n = path.get(FONT_SIZE);
		if (n != null)
			style = style + "; font-size: " + n.toString() + "px";

		elt.setAttribute("style", style);
		return elt;
	}

	protected void setText(Element elt, String text) {
		String[] split = text.split("\n");
		if (elt != null) {
			NodeList<Node> childNodes = elt.getChildNodes();
			int i = 0;
			for (; i < split.length; i++) {
				TSpanElement tspan;
				if (i < childNodes.getLength())
					tspan = ((TSpanElement) childNodes.item(i));
				else
					tspan = (TSpanElement) document.createElementNS(svg.getNamespaceURI(), "tspan");
				if (i > 0)
					tspan.withAttr("dy", "1.2em").withAttr("x", "0");
				tspan.setTextContent(split[i]);
				elt.appendChild(tspan);
			}
			for (int j = i; j < childNodes.getLength(); j++)
				elt.removeChild(childNodes.item(i));
		}
	}

	@Override
	public Async<Dict> setText(String id, String text) {
		Element elt = document.getElementById(id);
		setText(elt, text);
		return null;
	}

	private Element path(String d, Dict path) {
		Element elt = document.createElementNS(svg.getNamespaceURI(), "path");
		elt.setAttribute("d", d);
		return elt;
	}

	public void styleObject(Element elt, Dict style) {
		CSSStyleDeclaration css = ((ElementCSSInlineStyle) elt).getStyle();
		style.forEach(k -> {
			String key = k.key();
//			Browser.consoleLog(key + " → " + style.get(key, Object.class));

			if (key.equals("_transform")) {
				SVGTransform svgTransform = svgTransform(elt);
				// Browser.consoleLog(svgTransform);
				Array arr = style.getArray(key);
				svgTransform.getMatrix().setA(arr.get(0, Number.class).doubleValue());
				svgTransform.getMatrix().setB(arr.get(1, Number.class).doubleValue());
				svgTransform.getMatrix().setC(arr.get(2, Number.class).doubleValue());
				svgTransform.getMatrix().setD(arr.get(3, Number.class).doubleValue());
				svgTransform.getMatrix().setE(arr.get(4, Number.class).doubleValue());
				svgTransform.getMatrix().setF(arr.get(5, Number.class).doubleValue());
				// Browser.consoleLog(svgTransform);
			} else {
				css.setProperty(k.key(), style.getString(k.key()));
			}
		});
	}

	@Override
	public Async<Dict> styleObject(String id, Dict style) {
		Browser.consoleLog("Styling svg0." + id + " → " + style.toJson());
		Element obj = document.getElementById("svg0." + id);
		Browser.consoleLog(obj);
		if (obj != null) {
			styleObject(obj, style);
		} else {
			styles.put(id, style);
		}
		return null;
	}

	@Override
	public Async<Dict> onKeyPress(String javaScript) {
//		System.out.println("onkeypress: " + javaScript);
		if (keyListener != null)
			svg.neglectKeyDown(keyListener);
		keyListener = null;
		if (javaScript != null) {
			JSFunction function = JSUtil.function("kev", javaScript);
			// Browser.consoleLog(function);
			keyListener = kev -> {
				// System.out.println("key event!");
				function.call(null, kev);
			};
			// Browser.consoleLog(keyListener);

			svg.listenKeyDown(keyListener);
		}
		return null;
	}

	@Override
	public Async<Dict> evalScript(String javaScript) {
		if (javaScript != null) {
			JSFunction function = JSUtil.function("foo", javaScript);
			function.call(null, null);
		}
		return null;
	}

}
