package turtleduck.text;

import java.util.function.BiFunction;

import turtleduck.colors.Paint;
import turtleduck.text.Attributes.AttributeBuilder;

/**
 * @author anya
 *
 */
/**
 * @author anya
 *
 */
public interface TextCursor {

	/**
	 * Save the current state, and continue printing with a sub-cursor.
	 * 
	 * <p>
	 * The state includes the current position and the attributes (colours, style)
	 * that will be applied to printed text. Once {@link SubTextCursor#end()} is
	 * called, further printing happens from the position and attributes in effect
	 * at the time of the call to #{@link begin()}.
	 * 
	 * @return
	 * @see SubTextCursor#end()
	 */
	SubTextCursor begin();

	/**
	 * Move cursor to beginning of current line.
	 * 
	 * <p>
	 * Equivalent to printing <code>"\r"</code>
	 * 
	 * @return <code>this</code>, for further calls
	 */
	TextCursor beginningOfLine();

	/**
	 * Move cursor to top left of the page
	 * 
	 * @return <code>this</code>, for further calls
	 */
	TextCursor beginningOfPage();

	/**
	 * Clear the page and position cursor at top left.
	 * 
	 * <p>
	 * Equivalent to printing <code>"\f"</code>
	 * 
	 * @return <code>this</code>, for further calls
	 */
	default TextCursor clearPage() {
		return at(1, 1).clearRegion(1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Clear the character cell at position (x,y)
	 * 
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	default TextCursor clearAt(int x, int y) {
		return clearRegion(x, y, x, y);
	}

	/**
	 * Clear line number <code>y</code>.
	 * 
	 * <p>
	 * Equivalent to <code>clearRegion(0, y, Integer.MAX_VALUE, y);</code>
	 *
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	default TextCursor clearLine(int y) {
		return clearRegion(0, y, Integer.MAX_VALUE, y);
	}

	/**
	 * Clear from (and including) current cursor position to the beginning of the
	 * current line.
	 * 
	 * <p>
	 * Cursor position does not change.
	 * 
	 * <p>
	 * Equivalent to <code>clearRegion(0, y(), x(), y());</code>
	 * 
	 * @return
	 */
	default TextCursor clearToBeginningOfLine() {
		return clearRegion(0, y(), x(), y());
	}

	/**
	 * Clear from (and including) current cursor position to the end of the current
	 * line.
	 * 
	 * <p>
	 * Cursor position does not change.
	 * 
	 * <p>
	 * Equivalent to <code>clearRegion(x(), y(), Integer.MAX_VALUE, y());</code>
	 * 
	 * @return
	 */
	default TextCursor clearToEndOfLine() {
		return clearRegion(x(), y(), Integer.MAX_VALUE, y());
	}

	/**
	 * @param x0 horizontal position (counting from 1)
	 * @param y0 vertical position (counting from 1)
	 * @param x1
	 * @param y1
	 * @return <code>this</code>, for further calls
	 */
	TextCursor clearRegion(int x0, int y0, int x1, int y1);

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	default Paint backgroundAt(int x, int y) {
		return attributesAt(x, y).get(Attribute.ATTR_BACKGROUND);
	}

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	default Paint foregroundAt(int x, int y) {
		return attributesAt(x, y).get(Attribute.ATTR_FOREGROUND);
	}

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	Attributes attributesAt(int x, int y);

	default Paint background() {
		return attributes().get(Attribute.ATTR_BACKGROUND);
	}

	default Paint foreground() {
		return attributes().get(Attribute.ATTR_FOREGROUND);
	}

	Attributes attributes();

	default TextCursor background(Paint backColor) {
		return attributes(attributes().change().background(backColor).done());
	}

	default TextCursor foreground(Paint foreColor) {
		return attributes(attributes().change().foreground(foreColor).done());
	}

	/**
	 * Set the current style attributes.
	 * 
	 * <p>
	 * The previous attributes are discarded. To modify attributes, do something
	 * like
	 * <code>cursor.attributes(cursor.attributes().change().style(TextStyle.ITALICS).done())</code>.
	 * 
	 * @param attrs The new attributes
	 * @return
	 */
	TextCursor attributes(Attributes attrs);

	/**
	 * Retrieve the character at the given position.
	 * 
	 * <p>
	 * Returns <code>""</code> if the position has not been printed to (visually
	 * equivalent to, but distinct from <code>" "</code> (a space)).
	 * 
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return A string representing the character at the given position
	 */
	String charAt(int x, int y);

	/**
	 * Retrieve the character at the given position.
	 * 
	 * <p>
	 * Returns <code>CodePoints.NUL</code> if the position has not been printed to
	 * (visually equivalent to, but distinct from <code>CodePoints.SPACE</code> (a
	 * space)).
	 * 
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return A string representing the character at the given position
	 */
	CodePoint codePointAt(int x, int y);

	int x();

	int y();

	boolean isFilled(int x, int y);

	TextCursor move(int deltaX, int deltaY);

	TextCursor moveHoriz(int dist);

	/**
	 * @param newX new horizontal position (counting from 1)
	 * @param newY new vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	TextCursor at(int newX, int newY);

	TextCursor moveVert(int dist);

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	TextCursor plot(int x, int y);

	/**
	 * @param x  horizontal position (counting from 1)
	 * @param y  vertical position (counting from 1)
	 * @param op
	 * @return <code>this</code>, for further calls
	 */
	TextCursor plot(int x, int y, BiFunction<Integer, Integer, Integer> op);

	/**
	 * Print the string, starting at current position.
	 * 
	 * <p>
	 * Cursor is left at the next position after the printed string. Newlines and
	 * control sequences in the string are interpreted, and may have additional
	 * effects.
	 * 
	 * @param s A string
	 * @return <code>this</code>, for further calls
	 */
	default TextCursor print(String s) {
		return print(s, attributes());
	}

	/**
	 * Print the string with the given style attributes, starting at current
	 * position.
	 * 
	 * <p>
	 * Cursor is left at the next position after the printed string. Newlines and
	 * control sequences in the string are interpreted, and may have additional
	 * effects.
	 * <p>
	 * The previous style is restored after printing.
	 * 
	 * @param s     A string
	 * @param attrs A set of attributes (e.g., colours, font styles)
	 * @return <code>this</code>, for further calls
	 */
	TextCursor print(String s, Attributes attrs);

	/**
	 * Print the string in the given colour, starting at current position.
	 * 
	 * <p>
	 * Cursor is left at the next position after the printed string. Newlines and
	 * control sequences in the string are interpreted, and may have additional
	 * effects.
	 * <p>
	 * The previous style is restored after printing.
	 * 
	 * @param s         A string
	 * @param foreColor A colour to use for the characters (null for current
	 *                  foreground)
	 * @return <code>this</code>, for further calls
	 */
	default TextCursor print(String s, Paint foreColor) {
		return print(s, foreColor, null);
	}

	/**
	 * Print the string in the given colour, starting at current position.
	 * 
	 * <p>
	 * Cursor is left at the next position after the printed string. Newlines and
	 * control sequences in the string are interpreted, and may have additional
	 * effects.
	 * <p>
	 * The previous style is restored after printing.
	 * 
	 * @param s         A string
	 * @param foreColor A colour to use for the characters (null for current
	 *                  foreground)
	 * @param backColor A colour to use for the background (null for current
	 *                  background)
	 * @return <code>this</code>, for further calls
	 */
	default TextCursor print(String s, Paint foreColor, Paint backColor) {
		if (foreColor == null && backColor == null)
			return print(s);
		return print(s, attributes().change().foreground(foreColor).background(backColor).done());
	}

	/**
	 * Equivalent to <code>print("\n")</code>
	 * 
	 * @return <code>this</code>, for further calls
	 * @see #print(String)
	 */
	default TextCursor println() {
		return print("\n");
	}

	/**
	 * Equivalent to <code>print(s+"\n")</code>
	 * 
	 * @param s A string
	 * @return <code>this</code>, for further calls
	 * @see #print(String, Paint, Paint)
	 */
	default TextCursor println(String s) {
		return print(s + "\n");
	}

	/**
	 * Equivalent to <code>print(s+"\n", foreColor)</code>
	 * 
	 * @param s         A string
	 * @param foreColor A colour to use for the characters (null for current
	 *                  foreground)
	 * @return <code>this</code>, for further calls
	 * @see #print(String, Paint, Paint)
	 */
	default TextCursor println(String s, Paint foreColor) {
		return print(s + "\n", foreColor, null);
	}

	/**
	 * Equivalent to <code>print(s+"\n", foreColor, backColor)</code>
	 * 
	 * @param s         A string
	 * @param foreColor A colour to use for the characters (null for current
	 *                  foreground)
	 * @param backColor A colour to use for the background (null for current
	 *                  background)
	 * @return <code>this</code>, for further calls
	 * @see #print(String, Paint, Paint)
	 */
	default TextCursor println(String s, Paint foreColor, Paint backColor) {
		return print(s + "\n", foreColor, backColor);
	}

	TextCursor redrawTextPage();

	TextCursor resetAttrs();

	TextCursor resetFull();

	/**
	 * Scroll the page, adding empty lines at the top or bottom.
	 * 
	 * <p>
	 * A positive <code>lines</code> moves the page content <em>up</em>, inserting
	 * new lines at the bottom, while a negative argment moves the content
	 * <em>down</em>, adding new lines at the top.
	 * 
	 * <p>
	 * The current position is unchanged, although the text underneath will have
	 * changed.
	 * 
	 * @param lines Number of lines to scroll
	 * @return
	 */
	TextCursor scroll(int lines);

	/**
	 * Set autoscroll mode.
	 * 
	 * If autoscroll is on, the page will scroll up if the current position moves
	 * past the bottom of the page.
	 * 
	 * @param autoScroll
	 * @return
	 */
	boolean autoScroll(boolean autoScroll);

	default TextCursor setFont(TextFont font) {
		return attributes(attributes().change().set(Attribute.ATTR_FONT, font).done());
	}

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	TextCursor unplot(int x, int y);

	boolean hasInput();

	TextCursor sendInput(String string);

	default TextCursor write(CodePoint codePoint) {
		return write(codePoint, attributes());
	};

	TextCursor write(CodePoint codePoint, Attributes attrs);
	
}
