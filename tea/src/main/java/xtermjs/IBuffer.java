package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Represents a terminal buffer.
 */
public interface IBuffer extends JSObject {

	/**
	 * The y position of the cursor. This ranges between `0` (when the cursor is at
	 * baseY) and `Terminal.rows - 1` (when the cursor is on the last row).
	 */
	@JSProperty
	int getCursorY();

	/**
	 * The x position of the cursor. This ranges between `0` (left side) and
	 * `Terminal.cols - 1` (right side).
	 */
	@JSProperty
	int getCursorX();

	/**
	 * The line within the buffer where the top of the viewport is.
	 */
	@JSProperty
	int getViewportY();

	/**
	 * The line within the buffer where the top of the bottom page is (when fully
	 * scrolled down).
	 */
	@JSProperty
	int getBaseY();

	/**
	 * The amount of lines in the buffer.
	 */
	@JSProperty
	int getLength();

	/**
	 * Gets a line from the buffer, or undefined if the line index does not exist.
	 *
	 * Note that the result of this function should be used immediately after
	 * calling as when the terminal updates it could lead to unexpected behavior.
	 *
	 * @param y The line index to get.
	 */
	IBufferLine getLine(int y);

	/**
	 * Creates an empty cell object suitable as a cell reference in `line.getCell(x,
	 * cell)`. Use this to avoid costly recreation of cell objects when dealing with
	 * tons of cells.
	 */
	IBufferCell getNullCell();
}