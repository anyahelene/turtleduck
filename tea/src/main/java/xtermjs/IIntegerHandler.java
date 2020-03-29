package xtermjs;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface IIntegerHandler extends JSObject {

	void call(int value);
}
