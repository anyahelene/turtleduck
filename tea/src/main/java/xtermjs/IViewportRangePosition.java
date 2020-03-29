package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * An object representing a cell position within the viewport of the terminal.
 */
public interface IViewportRangePosition extends JSObject {

	/**
	 * The x position of the cell. This is a 0-based index that refers to the space
	 * in between columns, not the column itself. Index 0 refers to the left side of
	 * the viewport, index `Terminal.cols` refers to the right side of the viewport.
	 * This can be thought of as how a cursor is positioned in a text editor.
	 */
	@JSProperty
	int getX();

	/**
	 * The x position of the cell. This is a 0-based index that refers to the space
	 * in between columns, not the column itself. Index 0 refers to the left side of
	 * the viewport, index `Terminal.cols` refers to the right side of the viewport.
	 * This can be thought of as how a cursor is positioned in a text editor.
	 */
	@JSProperty
	void setX(int val);

	/**
	 * The y position of the cell. This is a 0-based index that refers to a specific
	 * row.
	 */
	@JSProperty
	int getY();

	/**
	 * The y position of the cell. This is a 0-based index that refers to a specific
	 * row.
	 */
	@JSProperty
	void setY(int val);
}