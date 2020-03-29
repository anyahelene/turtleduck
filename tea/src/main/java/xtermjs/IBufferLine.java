package xtermjs;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 * Represents a line in the terminal's buffer.
 */
public interface IBufferLine extends JSObject {

		/**
		 * Whether the line is wrapped from the previous line.
		 */
		@JSProperty
		boolean getIsWrapped();

		/**
		 * The length of the line, all call to getCell beyond the length will result in
		 * `undefined`.
		 */
		@JSProperty
		int getLength();

	/**
     * Gets a cell from the line, or undefined if the line index does not exist.
     *
     * Note that the result of this function should be used immediately after
     * calling as when the terminal updates it could lead to unexpected
     * behavior.
     *
     * @param x The character index to get.
     * @param cell Optional cell object to load data into for performance
     * reasons. This is mainly useful when every cell in the buffer is being
     * looped over to avoid creating new objects for every cell.
     */
IBufferCell getCell(int x, IBufferCell cell);

		/**
		 * Gets the line as a string. Note that this is gets only the string for the
		 * line, not taking isWrapped into account.
		 *
		 * @param trimRight   Whether to trim any whitespace at the right of the line.
		 * @param startColumn The column to start from (inclusive).
		 * @param endColumn   The column to end at (exclusive).
		 */
		String translateToString(boolean trimRight, int startColumn, int endColumn);
	}