package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLElement;

public interface MDRender extends JSObject {

	void render_unsafe(HTMLElement element, String code);
}
