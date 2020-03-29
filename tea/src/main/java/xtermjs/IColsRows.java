package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public interface IColsRows extends JSObject {

	@JSProperty
	int getCols();
	
	@JSProperty
	int getRows();
}
