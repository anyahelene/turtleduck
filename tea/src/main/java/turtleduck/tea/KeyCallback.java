package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.Event;

@JSFunctor
public interface KeyCallback extends JSObject {
	void handle(String key, Event event);
}