package svg;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;

public interface SVGElement extends HTMLElement {
//	@JSProperty
//	SVGAnimatedString getClassName();

	@JSProperty
	SVGSVGElement getOwnerSVGElement();

	@JSProperty
	SVGElement getViewportElement();
}
