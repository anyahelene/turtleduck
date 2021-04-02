package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGAnimatedTransformList extends JSObject {
	@JSProperty
	SVGTransformList getBaseVal();

	@JSProperty
	SVGTransformList getAnimVal();
}
