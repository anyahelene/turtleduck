package turtleduck.text;

import turtleduck.colors.Color;
import turtleduck.display.Layer;

public interface TextFont {

	/**
	 * Inverse video, switch background and foreground.
	 */
	int ATTR_INVERSE = 0x01;
	/**
	 * Italic or slanted font style.
	 */
	int ATTR_ITALIC = 0x02;
	/**
	 * Bold font weight (or bright color)
	 */
	int ATTR_BOLD = 0x04;
	/**
	 * Draw character outline.
	 */
	int ATTR_OUTLINE = 0x08;
	/**
	 * Draw line under text / at base line.
	 */
	int ATTR_UNDERLINE = 0x10;
	/**
	 * Draw line over text.
	 */
	int ATTR_OVERLINE = 0x20;
	/**
	 * Draw line through text.
	 */
	int ATTR_LINE_THROUGH = 0x40;
	/**
	 * Write on top of existing text (i.e., don't clear area before writing)
	 */
	int ATTR_OVERSTRIKE = 0x80;
	/**
	 * Clip to character box.
	 */
	int ATTR_CLIP = 0x80;
	/**
	 * Don't use built-in draw commands to emulate block charcters.
	 */
	int ATTR_NO_FAKE_CHARS = 0x100;
	/**
	 * Text should blink (not implemented).
	 */
	int ATTR_BLINK = 0x200; // NOT IMPLEMENTED
	/**
	 * Fainter colour (50% opacity).
	 */
	int ATTR_FAINT = 0x400; // NOT IMPLEMENTED
	/**
	 * Brighter colour.
	 */
	int ATTR_BRIGHT = 0x800;

	/**
	 * Create a copy of this font, with the given adjustments to the translation and
	 * scaling.
	 *
	 * @param deltaXTranslate
	 * @param deltaYTranslate
	 * @param deltaXScale
	 * @param deltaYScale
	 * @return
	 */
	TextFont adjust(double size, double deltaXTranslate, double deltaYTranslate, double deltaXScale,
			double deltaYScale);

	/**
	 * Draw the given text at position (0,0).
	 *
	 * The <code>canvas</code> should normally be translated to the appropriate text
	 * position before calling this method.
	 *
	 * Text will be clipped so each character fits its expected square-shaped area,
	 * and the area will be cleared to transparency before drwaing.
	 *
	 * The canvas's current path will be overwritten.
	 *
	 * @param canvas
	 *            a grapics context
	 * @param text
	 *            string to be printed
	 */
	void drawText(Layer canvas, String text);

	/**
	 * Draw the given text at position (0,0), with horizontal scaling.
	 *
	 * The <code>canvas</code> should normally be translated to the appropriate text
	 * position before calling this method.
	 *
	 * Text will be clipped so each character fits its expected square-shaped area,
	 * and the area will be cleared to transparency before drwaing.
	 *
	 * The canvas's current path will be overwritten.
	 *
	 * @param canvas
	 *            a grapics context
	 * @param text
	 *            string to be printed
	 * @param xScaleFactor
	 *            a horizontal scaling factor
	 */
	void drawText(Layer canvas, String text, double xScaleFactor);

	/**
	 * Draw the given text at position (x,y).
	 *
	 * Text will be clipped so each character fits its expected square-shaped area,
	 * and the area will be cleared to transparency before drwaing.
	 *
	 * The canvas's current path will be overwritten.
	 *
	 * @param canvas
	 *            a grapics context
	 * @param x
	 *            X-position of the lower left corner of the text
	 * @param y
	 *            Y-position of the lower left corner of the text
	 * @param text
	 *            string to be printed
	 */
	void drawTextAt(Layer canvas, double x, double y, String text);

	/**
	 * Draw the given text at position (x, y), with horizontal scaling.
	 *
	 * The area will be cleared to transparency before drawing.
	 *
	 * The canvas's current path will be overwritten.
	 *
	 * @param canvas
	 *            a canvas
	 * @param x
	 *            X-position of the lower left corner of the text
	 * @param y
	 *            Y-position of the lower left corner of the text
	 * @param text
	 *            string to be printed
	 * @param xScaleFactor
	 *            a horizontal scaling factor
	 */
	void drawTextAt(Layer canvas, double x, double y, String text, double xScaleFactor, int mode, Color bg);

	/**
	 * Draw the given text at position (x, y), with horizontal scaling.
	 *
	 * The area will not be cleared to transparency before drawing.
	 *
	 * The canvas's current path will be overwritten.
	 *
	 * @param canvas
	 *            a canvas
	 * @param x
	 *            X-position of the lower left corner of the text
	 * @param y
	 *            Y-position of the lower left corner of the text
	 * @param text
	 *            string to be printed
	 * @param xScaleFactor
	 *            a horizontal scaling factor
	 */
	void drawTextNoClearAt(Layer canvas, double x, double y, String text, double xScaleFactor, int mode,
			Color bg);

	/**
	 * @return the font
	 */
	<T> T getFont(Class<T> fontClass);

	String fontName();
	
	double fontSize();
	/**
	 * @return the size
	 */
	double getSize();

	/**
	 * Width and height of the square-shaped space each letter should fit within
	 *
	 * @return the squareSize
	 */
	double getSquareSize();

	/**
	 * Horizontal scaling factor (1.0 means no scaling)
	 *
	 * Most fonts are relatively tall and narrow, and need horizontal scaling to fit
	 * a square shape.
	 *
	 * @return the xScale
	 */
	double getxScale();

	/**
	 * Horizontal positioning of letters.
	 *
	 * Each letter should be approximately centered within its available
	 * square-shaped space.
	 *
	 * @return the xTranslate
	 */
	double getxTranslate();

	/**
	 * Vertical scaling factor (1.0 means no scaling)
	 *
	 * @return the yScale
	 */
	double getyScale();

	/**
	 * /** Vertical positioning of letters.
	 *
	 * Each letter should be positioned on the baseline so that ascenders and
	 * descenders fall within its available square-shaped space.
	 *
	 * @return the yTranslate
	 */
	double getyTranslate();

	/**
	 * Set up a canvas for drawing with this font.
	 *
	 * Caller should call {@link Canvas#save()} first, and then
	 * {@link Canvas#restore()} afterwards, to clean up adjustments to the
	 * transformation matrix (i.e., translation, scaling).
	 *
	 * The Canvas should be translated to the coordinates where the text
	 * should appear <em>before</em> calling this method, since this method will
	 * modify the coordinate system.
	 *
	 * @param canvas
	 *            A Canvas
	 */
	void setGraphicsContext(Layer canvas);

	/**
	 * Set up a canvas for drawing with this font.
	 *
	 * Caller should call {@link Canvas#save()} first, and then
	 * {@link Canvas#restore()} afterwards, to clean up adjustments to the
	 * transformation matrix (i.e., translation, scaling).
	 *
	 * The Canvas should be translated to the coordinates where the text
	 * should appear <em>before</em> calling this method, since this method will
	 * modify the coordinate system.
	 *
	 * @param canvas
	 *            A Canvas
	 * @param xScaleFactor
	 *            Additional horizontal scaling, normally 0.5 (for half-width
	 *            characters)
	 */
	void setGraphicsContext(Layer canvas, double xScaleFactor);

	/**
	 * Draw text at the given position.
	 *
	 * For most cases, the simpler {@link #drawText(Canvas, String)} or
	 * {@link #drawText(Canvas, String, double)} will be easier to use.
	 *
	 * If <code>clip</code> is true, the canvas's current path will be
	 * overwritten.
	 *
	 * @param canvas
	 *            A Canvas
	 * @param x
	 *            X-position of the lower left corner of the text
	 * @param y
	 *            Y-position of the lower left corner of the text
	 * @param text
	 *            The text to be printed
	 * @param xScaleFactor
	 *            Horizontal scaling factor, normally 1.0 (full width) or 0.5 (half
	 *            width)
	 * @param clear
	 *            True if the area should be cleared (to transparency) before
	 *            drawing; normally true.
	 * @param clip
	 *            True if the text drawing should be clipped to fit the expected
	 *            printing area; normally true.
	 * @param fill
	 *            Paint to use if the letter shapes should be filled; otherwise null. See ATTR_*.
	 * @param stroke
	 *            Paint to use if the letter shapes should be stroked (outlined); normally
	 *            null.
	 * @param mode
	 * 			  Mode attributes
	 * @param bg
	 *            Paint to use for the background (normally null or {@link Color#TRANSPARENT})
	 *
	 */
//	void textAt(Canvas canvas, double x, double y, String text, double xScaleFactor, boolean clear, boolean clip,
	//		Paint fill, Paint stroke, int mode, Paint bg);

	/**
	 * Draw a single character to an offscreen buffer, and return it as an
	 * {@link Image}.
	 *
	 * The contents of the returned {@link Image} is valid until then next call
	 * to {@link #drawCharacter(String, int, Paint, Paint)} or one of the
	 * other text drawing commands.
	 *
	 * @param c A string containing the single character to be drawn.
	 * @param mode Text attributes (zero or more <code>ATTR_*</code> flags ORed together)
	 * @param fill Fill paint, or null for no fill.
	 * @param stroke Stroke paint, normally null for no stroke.
	 * @return An image, where pixels can be read by {@link Image#getPixelReader()}
	 */
	//Image drawCharacter(String c, int mode, Paint fill, Paint stroke);

}