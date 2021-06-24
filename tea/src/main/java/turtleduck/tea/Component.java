package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.dom.html.HTMLElement;

public interface Component  extends JSMapLike<JSObject> {
	void focus();

	HTMLElement element();
}
