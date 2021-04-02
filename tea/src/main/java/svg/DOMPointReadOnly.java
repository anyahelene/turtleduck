package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface DOMPointReadOnly extends JSObject {
	@JSProperty
	double getX();

	@JSProperty
	double getY();

	@JSProperty
	double getZ();

	@JSProperty
	double getW();
	
	DOMPoint matrixTransform(DOMMatrix m);

}
