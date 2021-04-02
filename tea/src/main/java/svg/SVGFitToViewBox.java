package svg;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface SVGFitToViewBox extends JSObject {
	@JSProperty
	SVGAnimatedRect getViewBox();
	@JSProperty
	SVGAnimatedPreserveAspectRatio getPreserveAspectRatio();
}
