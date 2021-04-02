package svg;

import org.teavm.jso.JSProperty;

public interface DOMRect extends DOMRectReadOnly {
	@JSProperty
	void setX(double x);

	@JSProperty
	void setY(double y);

	@JSProperty
	void setWidth(double width);

	@JSProperty
	void setHeight(double height);

}
