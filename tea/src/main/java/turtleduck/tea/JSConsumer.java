package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface JSConsumer<T extends JSObject> extends JSObject {
	void accept(T obj);
}
