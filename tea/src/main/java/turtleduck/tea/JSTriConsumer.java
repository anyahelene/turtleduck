package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface JSTriConsumer<T extends JSObject, U extends JSObject, V extends JSObject> extends JSObject {
	void accept(T obj1, U obj2, V obj3);
}
