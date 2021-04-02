package svg;

import org.teavm.jso.JSProperty;

public interface SVGGraphicsElement extends SVGElement {
	@JSProperty
	SVGAnimatedTransformList getTransform();

	DOMRect getBBox();

	DOMRect getBBox(SVGBoundingBoxOptions options);

	DOMMatrix getCTM();

	DOMMatrix getScreenCTM();
}
