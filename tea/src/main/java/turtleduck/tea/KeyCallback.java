package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.dom.events.Event;

@JSFunctor
public interface KeyCallback extends JSObject {
	Promise<JSObject> handle(String key, JSMapLike<JSObject> data, Event event);
}