package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGNumber extends JSObject {
	@JSProperty
	double getValue();
	
	@JSProperty
	void setValue(double value);
}
