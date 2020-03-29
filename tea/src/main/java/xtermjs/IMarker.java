package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Represents a specific line in the terminal that is tracked when scrollback
 * is trimmed and lines are added or removed. This is a single line that may
 * be part of a larger wrapped line.
 */
public interface IMarker extends IDisposable, JSObject {

	/**
	 * A unique identifier for this marker.
	 */
	@JSProperty
	int getId();

	/**
	 * Whether this marker is disposed.
	 */
	@JSProperty
	boolean getIsDisposed();

	/**
	 * The actual line index in the buffer at this point in time. This is set to -1
	 * if the marker has been disposed.
	 */
	@JSProperty
	int getLine();
}