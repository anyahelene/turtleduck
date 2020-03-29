package xtermjs;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface IObjectHandler<T extends JSObject> extends JSObject {

	void call(T value);
}
