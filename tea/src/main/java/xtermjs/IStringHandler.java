package xtermjs;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface IStringHandler extends JSObject {

	void call(String value);
}
