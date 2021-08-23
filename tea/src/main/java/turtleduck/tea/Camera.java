package turtleduck.tea;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSFunction;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLElement;

public interface Camera extends JSObject {
	void subscribe(String id, String dest, String mode, String text, String icon, String title);
	void unsubscribe(String id);
	void show();
	void hide();
	void showPreview();
	void hidePreview();
	void showVideo();
	void hideVideo();
	void attach(HTMLElement parent);
	void dispose();
	void pause();
	void play();
	boolean snapshot();
	Promise<Camera> initialize();
	Promise<JSString> qrScan();
	
	class Statics {
		@JSBody(params = { "id", "dest", "mode", "text", "icon", "title" }, script = "turtleduck.Camera.addSubscription(id, dest, mode, text, icon, title);")
		native static void addSubscription(String id, String dest, String mode, String text, String icon, String title);
		@JSBody(params = { "id"}, script = "turtleduck.Camera.removeSubscription(id);")
		native static void removeSubscription(String id);

	}
}
