package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGAnimatedRect extends JSObject {
	@JSProperty
	DOMRect getBaseVal();

	@JSProperty
	void setBaseVal(DOMRect val);

	@JSProperty
	DOMRectReadOnly getAnimVal();
}
