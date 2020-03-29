package xtermjs;

import org.teavm.jso.JSObject;

/**
 * An addon that can provide additional functionality to the terminal.
 */
public interface ITerminalAddon extends JSObject, IDisposable {

	/**
	 * This is called when the addon is activated.
	 */
	void activate(Terminal terminal);
}