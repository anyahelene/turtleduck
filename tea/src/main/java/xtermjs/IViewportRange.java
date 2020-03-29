package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * An object representing a range within the viewport of the terminal.
 */
public interface IViewportRange extends JSObject {

	/**
	 * The start of the range.
	 */
	@JSProperty
	IViewportRangePosition getStart();

	/**
	 * The start of the range.
	 */
	@JSProperty
	void setStart(IViewportRangePosition val);

	/**
	 * The end of the range.
	 */
	@JSProperty
	IViewportRangePosition getEnd();

	/**
	 * The end of the range.
	 */
	@JSProperty
	void setEnd(IViewportRangePosition val);
}