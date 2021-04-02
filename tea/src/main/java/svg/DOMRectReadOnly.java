package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface DOMRectReadOnly extends JSObject {
//	static DOMRectReadOnly fromRect(DOMRectInit other);

	@JSProperty
	double getX();

	@JSProperty
	double getY();

	@JSProperty
	double getWidth();

	@JSProperty
	double getHeight();

	@JSProperty
	double getTop();

	@JSProperty
	double getRight();

	@JSProperty
	double getBottom();

	@JSProperty
	double getLeft();

	String toJSON();
}
