package xtermjs;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface IVoidHandler extends JSObject {

	void call();
}
