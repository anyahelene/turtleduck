package svg;

import org.teavm.jso.JSProperty;

public interface DOMPoint extends DOMPointReadOnly {
	@JSProperty
	void setX(double x);

	@JSProperty
	void setY(double y);

	@JSProperty
	void setZ(double z);

	@JSProperty
	void setW(double w);
}
