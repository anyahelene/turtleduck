package xtermjs;

import org.teavm.jso.JSObject;

/**
 * An object that can be disposed via a dispose function.
 */
public interface IDisposable extends JSObject {

	
	void dispose();
}