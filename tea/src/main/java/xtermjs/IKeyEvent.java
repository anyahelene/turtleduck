package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.KeyboardEvent;

public interface IKeyEvent extends JSObject {

	@JSProperty
	String getKey();
	
	@JSProperty
	KeyboardEvent getDomEvent();
}
