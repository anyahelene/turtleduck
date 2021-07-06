package turtleduck.tea;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface JSBiFunction<T extends JSObject, U extends JSObject, V extends JSObject> extends JSObject {
	V apply(T obj1, U obj2);
}
