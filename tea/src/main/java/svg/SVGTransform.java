package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGTransform extends JSObject {
	@JSProperty
	DOMMatrix getMatrix();

	// a method, not a @JSProperty
	void setMatrix(DOMMatrix matrix);

}
