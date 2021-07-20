package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface JSStringConsumer extends JSObject {
	void accept(String obj);
}
