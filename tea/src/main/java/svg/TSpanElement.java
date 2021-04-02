package svg;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;

public interface TSpanElement extends HTMLElement {
	@JSProperty
	String getTextContent();
	@JSProperty
	void setTextContent(String text);

}
