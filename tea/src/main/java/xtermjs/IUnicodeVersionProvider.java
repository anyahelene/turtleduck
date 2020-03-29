package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * (EXPERIMENTAL) Unicode version provider.
 * Used to register custom Unicode versions with `Terminal.unicode.register`.
 */
public interface IUnicodeVersionProvider extends JSObject{

	/**
	 * String indicating the Unicode version provided.
	 */
	@JSProperty
	String getVersion();

	/**
	 * Unicode version dependent wcwidth implementation.
	 */
	int wcwidth(int codepoint);
}