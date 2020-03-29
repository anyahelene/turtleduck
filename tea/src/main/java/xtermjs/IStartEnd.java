package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface IStartEnd extends JSObject {

	@JSProperty
	int getStart();
	
	@JSProperty
	int getEnd();
}
