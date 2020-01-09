package turtleduck.text;

import turtleduck.display.Layer;

public interface TextWindow extends Layer {
	/**
	 * Set page dimensions to a standard text mode.
	 * 
	 * <p>
	 * The {@link TextMode} controls the number of lines and columns on a full page.
	 * 
	 * @param mode A text mode constant
	 * @return
	 */
	TextWindow textMode(TextMode mode);

	/**
	 * Set page dimensions to a standard text mode.
	 * 
	 * <p>
	 * The {@link TextMode} controls the number of lines and columns on a full page.
	 * 
	 * @param mode                A text mode constant
	 * @param adjustDisplayAspect Whether to resize the graphics screen to match the
	 *                            aspect of the text mode
	 * @return
	 */
	TextWindow textMode(TextMode mode, boolean adjustDisplayAspect);

	TextMode textMode();

	int pageWidth();

	int pageHeight();

	TextCursor cursor();

	/**
	 * Redraw the part of the page that has changed since last redraw.
	 */
	void flush();

	/**
	 * With buffered printing, nothing is actually drawn until
	 * {@link #redrawDirty()} or {@link #redrawTextPage()} is called.
	 * 
	 * @param enabled Whether to use buffering (default is <code>true</code>)
	 */
	TextWindow buffering(boolean enabled);

	/**
	 * @return True if buffering is enabled
	 * @see #setBuffering(boolean)
	 */
	boolean buffering();

	/**
	 * @param x0 horizontal position (counting from 1)
	 * @param y0 vertical position (counting from 1)
	 * @param x1
	 * @param y1
	 * @return <code>this</code>, for further calls
	 */
	TextWindow clearRegion(int x0, int y0, int x1, int y1, CodePoint cp, Attributes attrs);

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

	void redraw();

	/**
	 * Scroll the page, adding empty lines at the top or bottom.
	 * 
	 * <p>
	 * A positive <code>lines</code> moves the page content <em>up</em>, inserting
	 * new lines at the bottom, while a negative argment moves the content
	 * <em>down</em>, adding new lines at the top.
	 * 
	 * 
	 * @param lines Number of lines to scroll
	 * @return
	 */
	TextWindow scroll(int lines);

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

	TextWindow set(int x, int y, CodePoint cp, Attributes attrs);

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	Attributes attributesAt(int x, int y);

}
