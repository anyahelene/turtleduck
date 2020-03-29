package xtermjs;

import org.teavm.jso.JSObject;

/**
 * Represents a single cell in the terminal's buffer.
 */
public interface IBufferCell extends JSObject {

	/**
	 * The width of the character. Some examples:
	 *
	 * - `1` for most cells. - `2` for wide character like CJK glyphs. - `0` for
	 * cells immediately following cells with a width of `2`.
	 */
	int getWidth();

	/**
	 * The character(s) within the cell. Examples of what this can contain:
	 *
	 * - A normal width character - A wide character (eg. CJK) - An emoji
	 */
	String getChars();

	/**
	 * Gets the UTF32 codepoint of single characters, if content is a combined
	 * string it returns the codepoint of the last character in the string.
	 */
	int getCode();

	/**
	 * Gets the number representation of the foreground color mode, this can be used
	 * to perform quick comparisons of 2 cells to see if they're the same. Use
	 * `isFgRGB`, `isFgPalette` and `isFgDefault` to check what color mode a cell
	 * is.
	 */
	int getFgColorMode();

	/**
	 * Gets the number representation of the background color mode, this can be used
	 * to perform quick comparisons of 2 cells to see if they're the same. Use
	 * `isBgRGB`, `isBgPalette` and `isBgDefault` to check what color mode a cell
	 * is.
	 */
	int getBgColorMode();

	/**
	 * Gets a cell's foreground color number, this differs depending on what the
	 * color mode of the cell is:
	 *
	 * - Default: This should be 0, representing the default foreground color (CSI
	 * 39 m). - Palette: This is a number from 0 to 255 of ANSI colors (CSI 3(0-7)
	 * m, CSI 9(0-7) m, CSI 38 ; 5 ; 0-255 m). - RGB: A hex value representing a
	 * 'true color': 0xRRGGBB. (CSI 3 8 ; 2 ; Pi ; Pr ; Pg ; Pb)
	 */
	int getFgColor();

	/**
	 * Gets a cell's background color number, this differs depending on what the
	 * color mode of the cell is:
	 *
	 * - Default: This should be 0, representing the default background color (CSI
	 * 49 m). - Palette: This is a number from 0 to 255 of ANSI colors (CSI 4(0-7)
	 * m, CSI 10(0-7) m, CSI 48 ; 5 ; 0-255 m). - RGB: A hex value representing a
	 * 'true color': 0xRRGGBB (CSI 4 8 ; 2 ; Pi ; Pr ; Pg ; Pb)
	 */
	int getBgColor();

	/** Whether the cell has the bold attribute (CSI 1 m). */
	int isBold();

	/** Whether the cell has the inverse attribute (CSI 3 m). */
	int isItalic();

	/** Whether the cell has the inverse attribute (CSI 2 m). */
	int isDim();

	/** Whether the cell has the underline attribute (CSI 4 m). */
	int isUnderline();

	/** Whether the cell has the inverse attribute (CSI 5 m). */
	int isBlink();

	/** Whether the cell has the inverse attribute (CSI 7 m). */
	int isInverse();

	/** Whether the cell has the inverse attribute (CSI 8 m). */
	int isInvisible();

	/** Whether the cell is using the RGB foreground color mode. */
	boolean isFgRGB();

	/** Whether the cell is using the RGB background color mode. */
	boolean isBgRGB();

	/** Whether the cell is using the palette foreground color mode. */
	boolean isFgPalette();

	/** Whether the cell is using the palette background color mode. */
	boolean isBgPalette();

	/** Whether the cell is using the default foreground color mode. */
	boolean isFgDefault();

	/** Whether the cell is using the default background color mode. */
	boolean isBgDefault();

	/** Whether the cell has the default attribute (no color or style). */
	boolean isAttributeDefault();
}