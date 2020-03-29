package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface JSConsumer extends JSObject {
	void accept(JSObject obj);
}
