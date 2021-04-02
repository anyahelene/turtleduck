package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGAnimatedString extends JSObject {
	@JSProperty
	String getBaseVal();

	@JSProperty
	void setBaseVal(String val);

	@JSProperty
	String getAnimVal();
}
