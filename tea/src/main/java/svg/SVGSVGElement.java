package svg;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.dom.xml.Element;
import org.teavm.jso.dom.xml.NodeList;

public interface SVGSVGElement extends SVGGraphicsElement, SVGFitToViewBox, EventTarget /* WindowEventTarget */ {
	@JSProperty
	SVGAnimatedLength getX();

	@JSProperty
	SVGAnimatedLength getY();

	@JSProperty
	SVGAnimatedLength getWidth();

	@JSProperty
	SVGAnimatedLength getHeight();

	@JSProperty
	double getCurrentScale();

	@JSProperty
	DOMPointReadOnly getCurrentTranslate();

	NodeList<SVGElement> getIntersectionList(DOMRectReadOnly rect, SVGElement referenceElement);

	NodeList<SVGElement> getEnclosureList(DOMRectReadOnly rect, SVGElement referenceElement);

	boolean checkIntersection(SVGElement element, DOMRectReadOnly rect);

	boolean checkEnclosure(SVGElement element, DOMRectReadOnly rect);

	void deselectAll();

	SVGNumber createSVGNumber();

	SVGLength createSVGLength();

	SVGAngle createSVGAngle();

	DOMPoint createSVGPoint();

	DOMMatrix createSVGMatrix();

	DOMRect createSVGRect();

	SVGTransform createSVGTransform();

	SVGTransform createSVGTransformFromMatrix(DOMMatrix matrix);
//	SVGTransform createSVGTransformFromMatrix(DOMMatrix2DInit matrix);

	Element getElementById(String elementId);

}
