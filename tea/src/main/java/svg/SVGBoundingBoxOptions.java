package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGBoundingBoxOptions extends JSObject {
	@JSProperty
	boolean getFill();

	@JSProperty
	boolean getStroke();

	@JSProperty
	boolean getMarkers();

	@JSProperty
	boolean getClipped();

	@JSProperty
	void setFill(boolean fill);

	@JSProperty
	void setStroke(boolean stroke);

	@JSProperty
	void setMarkers(boolean markers);

	@JSProperty
	void setClipped(boolean clipped);
}
