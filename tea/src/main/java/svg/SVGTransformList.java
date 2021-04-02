package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGTransformList extends JSObject {
	@JSProperty
	long getLength();

	@JSProperty
	long getNumberOfItems();

	void clear();

	SVGTransform initialize(SVGTransform newItem);

	SVGTransform getItem(long index);

	SVGTransform insertItemBefore(SVGTransform newItem, long index);

	SVGTransform replaceItem(SVGTransform newItem, long index);

	SVGTransform removeItem(long index);

	SVGTransform appendItem(SVGTransform newItem);

	void setItem(long index, SVGTransform newItem);

	// Additional methods not common to other list interfaces.
	SVGTransform createSVGTransformFromMatrix(DOMMatrix matrix);

//	  SVGTransform createSVGTransformFromMatrix(DOMMatrix2DInit matrix);
	SVGTransform consolidate();
}
