package turtleduck.tea;

import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;
import org.teavm.jso.dom.xml.Text;

class HTMLUtil {

	public static class Attr {
		String name, value;

		Attr(String a, String v) {
			name = a;
			value = v;
		}

	}

	public static class Style {
		String name, value;

		Style(String a, String v) {
			name = a;
			value = v;
		}

	}

	public static Attr attr(String name, String value) {
		return new Attr(name, value);
	}

	public static Attr style(String style) {
		return new Attr("style", style);
	}

	public static Style style(String property, String value) {
		return new Style(property, value);
	}

	public static Attr clazz(String clazz) {
		return new Attr("class", clazz);
	}

	public static Attr id(String id) {
		return new Attr("id", id);
	}

	static HTMLElement div(Object... attrsAndContent) {
		return element("div", attrsAndContent);
	}

	static HTMLElement span(Object... attrsAndContent) {
		return element("span", attrsAndContent);
	}
	static Text text(String s) {
		return Browser.document.createTextNode(s);
	}
	static HTMLElement element(String tag, Object... attrsAndContent) {
		HTMLElement elt = Browser.document.createElement(tag);
		CSSStyleDeclaration style = null;
		if (elt != null) {
			for (Object obj : attrsAndContent) {
//				System.out.println(obj);
				if (obj instanceof Attr) {
					Attr a = (Attr) obj;
					elt.setAttribute(a.name, a.value);
				} else if (obj instanceof Style) {
					Style s = (Style) obj;
					if (style == null)
						style = elt.getStyle();
					style.setProperty(s.name, s.value);
				} else if (obj instanceof String) {
					elt.appendChild(Browser.document.createTextNode((String) obj));
				} else if (obj != null) {
					try {
						elt.appendChild((Node) obj);
					} catch (RuntimeException e) {
						Client.logger.error("don't know what to do with {}", obj);
					}
				}
			}
			return elt;
		} else {
			throw new RuntimeException("Didn't get element");
		}
	}
}
