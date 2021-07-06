package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSBoolean;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLElement;

public interface Component  extends JSMapLike<JSObject> {
	void focus();

	HTMLElement element();
	
	void select();
	
	Component current();
	
	Component parent();
	
	void setParent(Component parent);
	
	void register();
	
	void addDependent(HTMLElement dep);

	void setTitle(String string);

	void onclose(JSBiFunction<Component, Event, JSBoolean> handler);
	
	void addWindowTools();
}
