package turtleduck.jfx;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.text.BlocksAndBoxes;
import turtleduck.text.TextFont;
import turtleduck.turtle.Canvas;

/**
 * TextFont – for grid-based text / character graphics
 * <p>
 * TextFonts are used for drawing text and character graphics. Each character is
 * assumed to be uniform in size, fitting within a square with sides of length
 * {@link #getSquareSize()}. The given font file should contain a monospaced
 * font of a type suitable for JavaFX, such as OpenType or TrueType.
 *
 * <p>
 * Additional horizontal and vertical positioning and scaling can be used to
 * make the font fit with the square-shaped concept.
 *
 * <p>
 * See {@link #setGraphicsContext(GraphicsContext)} for setting up the graphics
 * context for writing with the font, or
 * {@link #setGraphicsContext(GraphicsContext, double)} for extra on-the-fly
 * horizontal scaling, e.g. to make half-width letters ("hires" mode).
 *
 */
public class JfxTextFont implements TextFont {
	private static final String[] searchPath = { "/fonts/", "", "../", "../fonts/", "../../fonts/", "../../../fonts/" };
	private static final Map<String, String> loadedFonts = new HashMap<>();
	private static final double thin = 2.0, thick = 4.0;
	private static final String[] boxDrawingShapes = { // lines
			"--..", "**..", "..--", "..**",
			// dashed lines
			"3--..", "3**..", "3..--", "3..**", "4--..", "4**..", "4..--", "4..**",
			// corners
			".-.-", ".*.-", ".-.*", ".*.*", "-..-", "*..-", "-..*", "*..*", ".--.", ".*-.", ".-*.", ".**.", "-.-.",
			"*.-.", "-.*.", "*.*.",
			// |-
			".---", ".*--", ".-*- ", ".--* ", ".-**", ".**-", ".*-*", ".***", "-.--", "*.--", "-.*-", "-.-*", "-.**",
			"*.*-", "*.-*", "*.**",
			// T
			"--.-", "*-.-", "-*.-", "**.-", "--.*", "*-.*", "-*.*", "**.*", "---.", "*--. ", "-*-.", "**-.", "--*.",
			"*-*.", "-**.", "***.",
			// +
			"----", "*---", "-*--", "**--", "--*-", "---*", "--**", "*-*-", "-**-", "*--*", "-*-*", "***-", "**-*",
			"*-**", "-***", "****",
			// dashes
			"2--..", "2**..", "2..--", "2..**",
			// double lines
			"==..", "..==", ".=.-", ".-.=", ".N.N", "=..-", "-..=", "M..M", ".=-.", ".-=.", ".MM.", "=.-.", "-.=.",
			"N.N.", ".=--", ".s==", ".zMN", "=.--", "s.==", "z.NM", "==.s", "--.=", "MN.z", "==s.", "--=.", "NMz.",
			"==--", "--==", "zzzz",
			// round corners
			"0.-.-", "0-..-", "0-.-.", "0.--.",
			// diagonals
			"////", "\\\\\\\\", "//\\\\\\",
			// short lines
			"-...", "..-.", ".-..", "...-", "*...", "..*.", ".*..", "...*",
			// thin/thick lines
			"-*..", "..-*", "*-..", "..*-" };

	protected static void drawMasked(GraphicsContext ctx, Image src, Image mask, double x, double y, boolean invert,
			boolean blend) {
		int w = (int) src.getWidth();
		int h = (int) src.getHeight();

		PixelReader pixelSrc = src.getPixelReader();
		PixelReader pixelMask = mask.getPixelReader();
		PixelWriter pixelWriter = ctx.getPixelWriter();
		Affine transform = ctx.getTransform();
		Point2D point = transform.transform(x, y);
		int dx = (int) point.getX(), dy = (int) point.getY();

		for (int px = 0; px < w; px++) {
			for (int py = 0; py < h; py++) {
				int a = pixelMask.getArgb(px, py) >>> 24;
				int rgb = pixelSrc.getArgb(px, py);
				if (invert) {
					a = ~a & 0xff;
				}
				if (blend) {
					a = ((rgb >>> 24) * a) >>> 8;
				}
				pixelWriter.setArgb(px + dx, py + dy, (a << 24) | rgb);

			}
		}
	}

	protected static void fillInverse(GraphicsContext ctx, Image img, double x, double y) {
		int w = (int) img.getWidth();
		int h = (int) img.getHeight();
		PixelReader pixelReader = img.getPixelReader();
		PixelWriter pixelWriter = ctx.getPixelWriter();
		Affine transform = ctx.getTransform();
		Point2D point = transform.transform(x, y);
		int dx = (int) point.getX(), dy = (int) point.getY();
		Color c = ctx.getFill() instanceof Color ? (Color) ctx.getFill() : Color.BLACK;
		int rgb = ((int) (c.getRed() * 255)) << 16 | ((int) (c.getGreen() * 255)) << 8 | ((int) (c.getBlue() * 255));

		for (int px = 0; px < w; px++) {
			for (int py = 0; py < h; py++) {
				int a = (~pixelReader.getArgb(px, py) & 0xff000000);
				// if(a != 0)
				pixelWriter.setArgb(px + dx, py + dy, a | rgb);

			}
		}
	}

	/**
	 * Load a font.
	 *
	 * Will first try to use the provided string as a file name, and load the font
	 * from a font file (should be in a format supported by JavaFX). If loading from
	 * file fails, we assume we have been given the name of a font which is passed
	 * directly to JavaFX for loading. A fallback font is used if this fails.
	 *
	 * When looking for files, his method will search for the file in a search path
	 * starting with the folder containing the {@link JfxTextFont} class file (using
	 * Java's standard {@link Class#getResourceAsStream(String)} system). The search
	 * path also includes ".." (parent directory) and "../fonts".
	 *
	 * The loaded font will be cached, so that additional calls with the same file
	 * name will not cause the file to be loaded again.
	 *
	 * If the font cannot be loaded, a default font will be substituted. You may
	 * check which font you got using {@link Font#getName()} or
	 * {@link Font#getFamily()}.
	 *
	 * @param name Font name, or relative path to the file
	 * @param size Desired point size of the font
	 * @return A JavaFX font
	 */
	public static final Font findFont(String name, double size) {
		return findFont(name, size, JfxTextFont.class);
	}

	/**
	 * Load a font.
	 *
	 * Will first try to use the provided string as a file name, and load the font
	 * from a font file (should be in a format supported by JavaFX). If loading from
	 * file fails, we assume we have been given the name of a font which is passed
	 * directly to JavaFX for loading. A fallback font is used if this fails.
	 *
	 * When looking for files, this method will search for the file in a search path
	 * starting with the folder containing the provided Java class (using Java's
	 * standard {@link Class#getResourceAsStream(String)} system). The search path
	 * also includes ".." (parent directory) and "../fonts".
	 *
	 * The loaded font will be cached, so that additional calls with the same file
	 * name will not cause the file to be loaded again.
	 *
	 * If the font cannot be loaded, a default font will be substituted. You may
	 * check which font you got using {@link Font#getName()} or
	 * {@link Font#getFamily()}.
	 *
	 * @param name  Name of a font, or relative path to the font file
	 * @param size  Desired point size of the font
	 * @param clazz A class for finding files relative to
	 * @return A JavaFX font
	 */
	protected static final Font findFont(String name, double size, Class<?> clazz) {
		if (name == null || size < 0) {
			throw new IllegalArgumentException();
		}

		if (loadedFonts.containsKey(name)) {
			return Font.font(loadedFonts.get(name), size);
		}

		for (String path : searchPath) {
			System.err.println("Trying: " + path + name + ": " + clazz.getResource(path + name));
			try (InputStream stream = clazz.getResourceAsStream(path + name)) {
				Font font = Font.loadFont(stream, size);
				if (font != null) {
					loadedFonts.put(name, font.getName());
					// System.err.println("Found: " + font.getName());
					return font;
				}
			} catch (FileNotFoundException e) {
				// we'll just try the next alternative in the search path
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Font font = Font.font(name, size);
		if (font == null) {
			font = Font.font(size);
		}
		if (font == null) {
			throw new RuntimeException("Even the default font seems to be unavailable – this shouldn't happen! :(");
		}
		if (font.getName().equals(Font.getDefault().getName())) {
			System.err.println("TextFont: Default font '" + font.getName() + "' substituted for '" + name + "'");
		}
		// System.err.println("Found: " + name + "=" + font.getName());
		loadedFonts.put(name, name);
		return font;
	}

	private javafx.scene.canvas.Canvas tmpCanvas;
	private final WritableImage img;

	/**
	 * The JavaFX font
	 */
	private Font font;
	/** Font size */
	private final double size;

	/**
	 * Horizontal positioning of letters.
	 *
	 * Each letter should be approximately centered within its available
	 * square-shaped space.
	 */
	private final double xTranslate;

	/**
	 * Vertical positioning of letters.
	 *
	 * Each letter should be positioned on the baseline so that ascenders and
	 * descenders fall within its available square-shaped space.
	 */
	private final double yTranslate;

	/**
	 * Horizontal scaling factor (1.0 means no scaling)
	 *
	 * Most fonts are relatively tall and narrow, and need horizontal scaling to fit
	 * a square shape.
	 */
	private final double xScale;

	/**
	 * Vertical scaling factor (1.0 means no scaling)
	 */
	private final double yScale;

	/**
	 * Width and height of the square-shaped space each letter should fit within
	 */
	private double squareSize;

	private String fileName;

	SnapshotParameters snapshotParameters = new SnapshotParameters();

	{
		snapshotParameters.setFill(Color.TRANSPARENT);
	}

	/**
	 * Create a new TextFont.
	 *
	 * <p>
	 * TextFonts are used for drawing text and character graphics. Each character is
	 * assumed to be uniform in size, fitting within a square with sides of length
	 * {@link #getSquareSize()}. The given font file should contain a monospaced
	 * font of a type suitable for JavaFX, such as OpenType or TrueType.
	 *
	 * <p>
	 * Additional horizontal and vertical positioning and scaling can be used to
	 * make the font fit with the square-shaped concept.
	 *
	 * <p>
	 * See {@link #setGraphicsContext(GraphicsContext)} for setting up the graphics
	 * context for writing with the font, or
	 * {@link #setGraphicsContext(GraphicsContext, double)} for extra on-the-fly
	 * horizontal scaling, e.g. to make half-width letters ("hires" mode).
	 *
	 *
	 * @param font       Name of the font file. Will search for the file in the same
	 *                   folder as the TextFont class, as well as ".." and
	 *                   "../fonts".
	 * @param size       Point size of the font.
	 * @param squareSize The width and height of a square defining the bounds of
	 *                   letters
	 * @param xTranslate Horizontal positioning of letters
	 * @param yTranslate Vertical positioning of letters
	 * @param xScale     Horizontal scaling factor
	 * @param yScale     Vertical scaling factor
	 */
	public JfxTextFont(Font font, double squareSize, double xTranslate, double yTranslate, double xScale,
			double yScale) {
		super();
		this.fileName = font.getName();
		this.font = font;
		this.size = font.getSize();
		this.squareSize = squareSize;
		this.xTranslate = xTranslate;
		this.yTranslate = yTranslate;
		this.xScale = xScale;
		this.yScale = yScale;
		this.img = new WritableImage((int) squareSize, (int) squareSize);
	}

	/**
	 * Create a new TextFont.
	 *
	 * <p>
	 * TextFonts are used for drawing text and character graphics. Each character is
	 * assumed to be uniform in size, fitting within a square with sides of length
	 * {@link #getSquareSize()}. The given font file should contain a monospaced
	 * font of a type suitable for JavaFX, such as OpenType or TrueType.
	 *
	 * <p>
	 * Additional horizontal and vertical positioning and scaling can be used to
	 * make the font fit with the square-shaped concept.
	 *
	 * <p>
	 * See {@link #setGraphicsContext(GraphicsContext)} for setting up the graphics
	 * context for writing with the font, or
	 * {@link #setGraphicsContext(GraphicsContext, double)} for extra on-the-fly
	 * horizontal scaling, e.g. to make half-width letters ("hires" mode).
	 *
	 *
	 * @param font       Name of the font file. Will search for the file in the same
	 *                   folder as the TextFont class, as well as ".." and
	 *                   "../fonts".
	 * @param size       Point size of the font.
	 * @param squareSize The width and height of a square defining the bounds of
	 *                   letters
	 * @param xTranslate Horizontal positioning of letters
	 * @param yTranslate Vertical positioning of letters
	 * @param xScale     Horizontal scaling factor
	 * @param yScale     Vertical scaling factor
	 */
	public JfxTextFont(String fileName, double size, double squareSize, double xTranslate, double yTranslate,
			double xScale, double yScale) {
		super();
		this.fileName = fileName;
		this.font = findFont(fileName, size);
		this.size = size;
		this.squareSize = squareSize;
		this.xTranslate = xTranslate;
		this.yTranslate = yTranslate;
		this.xScale = xScale;
		this.yScale = yScale;
		this.img = new WritableImage((int) squareSize, (int) squareSize);
	}

	/**
	 * Create a new TextFont.
	 *
	 * <p>
	 * TextFonts are used for drawing text and character graphics. Each character is
	 * assumed to be uniform in size, fitting within a square with sides of length
	 * {@link #getSquareSize()}. The given font file should contain a monospaced
	 * font of a type suitable for JavaFX, such as OpenType or TrueType.
	 *
	 * <p>
	 * Additional horizontal and vertical positioning and scaling can be used to
	 * make the font fit with the square-shaped concept.
	 *
	 * <p>
	 * See {@link #setGraphicsContext(GraphicsContext)} for setting up the graphics
	 * context for writing with the font, or
	 * {@link #setGraphicsContext(GraphicsContext, double)} for extra on-the-fly
	 * horizontal scaling, e.g. to make half-width letters ("hires" mode).
	 *
	 *
	 * @param font         Name of the font file. Will search for the file in the
	 *                     same folder as the TextFont class, as well as ".." and
	 *                     "../fonts".
	 * @param size         Point size of the font.
	 * @param squareSize   The width and height of a square defining the bounds of
	 *                     letters
	 * @param xTranslate   Horizontal positioning of letters
	 * @param yTranslate   Vertical positioning of letters
	 * @param xScale       Horizontal scaling factor
	 * @param yScale       Vertical scaling factor
	 * @param deferLoading True if the font file shouldn't be loaded before the font
	 *                     is actually used
	 */
	public JfxTextFont(String fileName, double size, double squareSize, double xTranslate, double yTranslate,
			double xScale, double yScale, boolean deferLoading) {
		super();
		this.fileName = fileName;
		this.font = deferLoading ? null : findFont(fileName, size);
		this.size = size;
		this.squareSize = squareSize;
		this.xTranslate = xTranslate;
		this.yTranslate = yTranslate;
		this.xScale = xScale;
		this.yScale = yScale;
		this.img = new WritableImage((int) squareSize, (int) squareSize);
	}

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
	@Override
	public TextFont adjust(double size, double deltaXTranslate, double deltaYTranslate, double deltaXScale,
			double deltaYScale) {
		if (size == 0.0) {
			return new JfxTextFont(fileName, this.size, squareSize, xTranslate + deltaXTranslate,
					yTranslate + deltaYTranslate, xScale + deltaXScale, yScale + deltaYScale);
		} else {
			return new JfxTextFont(fileName, this.size + size, squareSize, xTranslate + deltaXTranslate,
					yTranslate + deltaYTranslate, xScale + deltaXScale, yScale + deltaYScale);
		}

	}

	protected GraphicsContext context(Canvas canvas) {
		if (canvas instanceof JfxCanvas) {
			return ((JfxCanvas) canvas).context;
		} else {
			throw new IllegalArgumentException("Expected a JfxCanvas: " + canvas);
		}
	}

	private void doDraw(String text, double xScaleFactor, Paint fill, Paint stroke, int mode, GraphicsContext target,
			int width) {
		if ((mode & ATTR_BRIGHT) != 0) {
			fill = fill.brighter();
			stroke = stroke.brighter();
		}
		if ((mode & ATTR_FAINT) != 0) {
			fill = fill.opacity(0.5);
			stroke = stroke.opacity(0.5);
		}
		target.setFill(JfxColor.toJfxPaint(fill));
		target.setStroke(JfxColor.toJfxPaint(stroke));
		if (text.length() > 0 && text.charAt(0) >= 0x2500 && text.charAt(0) <= 0x259f
				&& (mode & ATTR_NO_FAKE_CHARS) == 0) {
			target.save(); // save 3
			target.setStroke(target.getFill());
			fakeBlockElement(target, text, xScaleFactor);
			target.restore(); // restore 3
		} else {
			target.save(); // save 3
			if ((mode & ATTR_ITALIC) != 0) {
				target.translate(-0.2, 0);
				target.transform(new Affine(Transform.shear(-0.2, 0)));
			}
			setGraphicsContext(target, xScaleFactor);
			if (fill != null) {
				target.fillText(text, 0.0, 0.0);
			}
			if ((mode & ATTR_BOLD) != 0) {
				// System.err.println("stroke2");
				target.save(); // save 4
				target.setLineWidth(thin);
				target.setStroke(target.getFill());
				target.strokeText(text, 0.0, 0.0);
				target.restore(); // restore 4
			}
			if (stroke != null || (mode & ATTR_OUTLINE) != 0) {
				// System.err.println("stroke2");
				target.setLineWidth(((mode & ATTR_BOLD) != 0) ? thin : thin / 2);
				target.strokeText(text, 0.0, 0.0);
			}
			target.restore(); // restore 3
		}

		if ((mode & ATTR_UNDERLINE) != 0) {
			target.fillRect(0, yTranslate - 2, width * squareSize * xScaleFactor, thin);
		}
		if ((mode & ATTR_OVERLINE) != 0) {
			target.fillRect(0, -squareSize + 2, width * squareSize * xScaleFactor, thin);
		}
		if ((mode & ATTR_LINE_THROUGH) != 0) {
			target.fillRect(0, -squareSize / 2 + 2, width * squareSize * xScaleFactor, thin);
		}
	}

	/**
	 * Draw a single character to an offscreen buffer, and return it as an
	 * {@link Image}.
	 *
	 * The contents of the returned {@link Image} is valid until then next call to
	 * {@link #drawCharacter(String, int, Paint, Paint)} or one of the other text
	 * drawing commands.
	 *
	 * @param c      A string containing the single character to be drawn.
	 * @param mode   Text attributes (zero or more <code>ATTR_*</code> flags ORed
	 *               together)
	 * @param fill   Fill paint, or null for no fill.
	 * @param stroke Stroke paint, normally null for no stroke.
	 * @return An image, where pixels can be read by {@link Image#getPixelReader()}
	 */
	protected Image drawCharacter(String c, int mode, Paint fill, Paint stroke) {
		if (c.codePointCount(0, c.length()) != 1) {
			throw new IllegalArgumentException("String argument should have exactly 1 codepoint: '" + c + "'");
		}
		javafx.scene.canvas.Canvas tmpCanvas = getTmpCanvas();
		GraphicsContext target = tmpCanvas.getGraphicsContext2D();
		target.save(); // save 2 if drawIndirect
		target.translate(0, squareSize);
		if (fill == null) {
			fill = Colors.TRANSPARENT;
		}
		if (stroke == null) {
			stroke = Colors.TRANSPARENT;
		}
		doDraw(c, 1, fill, stroke, mode, target, 1);
		tmpCanvas.snapshot(snapshotParameters, img);
		return img;
	}

	/**
	 * Draw the given text at position (0,0).
	 *
	 * The <code>ctx</code> should normally be translated to the appropriate text
	 * position before calling this method.
	 *
	 * Text will be clipped so each character fits its expected square-shaped area,
	 * and the area will be cleared to transparency before drwaing.
	 *
	 * The graphics context's current path will be overwritten.
	 *
	 * @param ctx  a grapics context
	 * @param text string to be printed
	 */
	@Override
	public void drawText(Canvas canvas, String text) {
		GraphicsContext ctx = context(canvas);
		textAt(ctx, 0.0, 0.0, text, 1.0, true, true, JfxColor.fromJfxColor(ctx.getFill()), null, 0, null);
	}

	/**
	 * Draw the given text at position (0,0), with horizontal scaling.
	 *
	 * The <code>ctx</code> should normally be translated to the appropriate text
	 * position before calling this method.
	 *
	 * Text will be clipped so each character fits its expected square-shaped area,
	 * and the area will be cleared to transparency before drwaing.
	 *
	 * The graphics context's current path will be overwritten.
	 *
	 * @param ctx          a grapics context
	 * @param text         string to be printed
	 * @param xScaleFactor a horizontal scaling factor
	 */
	@Override
	public void drawText(Canvas canvas, String text, double xScaleFactor) {
		GraphicsContext ctx = context(canvas);
		textAt(ctx, 0.0, 0.0, text, xScaleFactor, true, false, JfxColor.fromJfxColor(ctx.getFill()), null, 0, null);
	}

	/**
	 * Draw the given text at position (x,y).
	 *
	 * Text will be clipped so each character fits its expected square-shaped area,
	 * and the area will be cleared to transparency before drwaing.
	 *
	 * The graphics context's current path will be overwritten.
	 *
	 * @param ctx  a grapics context
	 * @param x    X-position of the lower left corner of the text
	 * @param y    Y-position of the lower left corner of the text
	 * @param text string to be printed
	 */
	@Override
	public void drawTextAt(Canvas canvas, double x, double y, String text) {
		GraphicsContext ctx = context(canvas);
		textAt(ctx, x, y, text, 1.0, true, false, JfxColor.fromJfxColor(ctx.getFill()), null, 0, null);
	}

	/**
	 * Draw the given text at position (x, y), with horizontal scaling.
	 *
	 * The area will be cleared to transparency before drawing.
	 *
	 * The graphics context's current path will be overwritten.
	 *
	 * @param ctx          a graphics context
	 * @param x            X-position of the lower left corner of the text
	 * @param y            Y-position of the lower left corner of the text
	 * @param text         string to be printed
	 * @param xScaleFactor a horizontal scaling factor
	 */
	@Override
	public void drawTextAt(Canvas canvas, double x, double y, String text, double xScaleFactor, int mode, Paint bg) {
		GraphicsContext ctx = context(canvas);
		textAt(ctx, x, y, text, xScaleFactor, true, false, JfxColor.fromJfxColor(ctx.getFill()), null, mode, bg);
	}

	/**
	 * Draw the given text at position (x, y), with horizontal scaling.
	 *
	 * The area will not be cleared to transparency before drawing.
	 *
	 * The graphics context's current path will be overwritten.
	 *
	 * @param ctx          a graphics context
	 * @param x            X-position of the lower left corner of the text
	 * @param y            Y-position of the lower left corner of the text
	 * @param text         string to be printed
	 * @param xScaleFactor a horizontal scaling factor
	 */
	@Override
	public void drawTextNoClearAt(Canvas canvas, double x, double y, String text, double xScaleFactor, int mode,
			Paint bg) {
		GraphicsContext ctx = context(canvas);
		textAt(ctx, x, y, text, xScaleFactor, false, false, JfxColor.fromJfxColor(ctx.getFill()), null, mode, bg);
	}

	private void fakeBlockElement(GraphicsContext ctx, String text, double xScaleFactor) {
		text.codePoints().forEach((int c) -> {
			// System.out.printf("%s %x%n", text, c);
			if (c >= 0x2596 && c <= 0x259f) { // quadrants
				int bits = BlocksAndBoxes.unicodeBlocksString.indexOf(c);
				if ((bits & 1) > 0) { // lower right
					ctx.fillRect(squareSize * xScaleFactor / 2, -squareSize / 2, (squareSize / 2) * xScaleFactor,
							squareSize / 2);
				}
				if ((bits & 2) > 0) { // lower left
					ctx.fillRect(0, -squareSize / 2, (squareSize / 2) * xScaleFactor, squareSize / 2);
				}
				if ((bits & 4) > 0) { // upper right
					ctx.fillRect(squareSize * xScaleFactor / 2, -squareSize, (squareSize / 2) * xScaleFactor,
							squareSize / 2);
				}
				if ((bits & 8) > 0) { // upper left
					ctx.fillRect(0, -squareSize, (squareSize / 2) * xScaleFactor, squareSize / 2);
				}
			} else if (c == 0x2580) { // upper half
				ctx.fillRect(0, -squareSize, (squareSize) * xScaleFactor, squareSize / 2);
			} else if (c > 0x2580 && c <= 0x2588) { // x/8 block
				int height = c - 0x2580;
				ctx.fillRect(0, -(height * squareSize) / 8, (squareSize) * xScaleFactor, (height * squareSize) / 8);
			} else if (c >= 0x2589 && c <= 0x258f) { // x/8 block
				int width = 8 - (c - 0x2588);
				ctx.fillRect(0, -squareSize, ((width * squareSize) / 8) * xScaleFactor, squareSize);
			} else if (c == 0x2590) { // right half
				ctx.fillRect(squareSize * xScaleFactor / 2, -squareSize, (squareSize / 2) * xScaleFactor, squareSize);
			} else if (c == 0x2591) { // light shade
				ctx.save();
				ctx.setGlobalAlpha(0.25);
				ctx.fillRect(0, -squareSize, (squareSize) * xScaleFactor, squareSize);
				ctx.restore();
			} else if (c == 0x2592) { // medium shade
				ctx.save();
				ctx.setGlobalAlpha(0.5);
				ctx.fillRect(0, -squareSize, (squareSize) * xScaleFactor, squareSize);
				ctx.restore();
			} else if (c == 0x2593) { // dark shade
				ctx.save();
				ctx.setGlobalAlpha(0.75);
				ctx.fillRect(0, -squareSize, (squareSize) * xScaleFactor, squareSize);
				ctx.restore();
			} else if (c == 0x2594) { // upper eighth
				ctx.fillRect(0, -squareSize, (squareSize) * xScaleFactor, squareSize / 8);
			} else if (c == 0x2595) { // right eighth
				ctx.fillRect(((7 * squareSize) / 8) * xScaleFactor, -squareSize, (squareSize / 8) * xScaleFactor,
						squareSize);
			} else if (c == 0x2571) {
				ctx.save();
				ctx.setLineWidth(2.0);
				ctx.strokeLine(0, 0, squareSize * xScaleFactor, -squareSize);
				ctx.restore();
			} else if (c == 0x2572) {
				ctx.save();
				ctx.setLineWidth(2.0);
				ctx.strokeLine(0, -squareSize, squareSize * xScaleFactor, 0);
				ctx.restore();
			} else if (c == 0x2573) {
				ctx.save();
				ctx.setLineWidth(2.0);
				ctx.strokeLine(0, 0, squareSize * xScaleFactor, -squareSize);
				ctx.strokeLine(0, -squareSize, squareSize * xScaleFactor, 0);
				ctx.restore();
			} else if (c >= 0x2500 && c <= 0x257f) {
				ctx.save();
				ctx.setLineCap(StrokeLineCap.BUTT);
				String spec = boxDrawingShapes[c - 0x2500];
				int i = 0;
				double extraThickness = 0.0;
				if (Character.isDigit(spec.charAt(i))) {
					extraThickness = setContextFromChar(ctx, spec.charAt(i++));
				}
				char s;
				double hThickness = Math.max(setContextFromChar(ctx, spec.charAt(i)),
						setContextFromChar(ctx, spec.charAt(i + 1))) + extraThickness;
				double vThickness = Math.max(setContextFromChar(ctx, spec.charAt(i + 2)),
						setContextFromChar(ctx, spec.charAt(i + 3))) + extraThickness;
				if (c >= 0x2550 || spec.charAt(i) != spec.charAt(i + 1)) {
					s = spec.charAt(i++);
					setContextFromChar(ctx, s);
					strokeHalfLine(ctx, xScaleFactor, -1, 0, s, vThickness);
					s = spec.charAt(i++);
					setContextFromChar(ctx, s);
					strokeHalfLine(ctx, xScaleFactor, 1, 0, s, vThickness);
				} else {
					s = spec.charAt(i++);
					s = spec.charAt(i++);
					setContextFromChar(ctx, s);
					strokeFullLine(ctx, xScaleFactor, 1, 0, s);
				}
				if (c >= 0x2550 || spec.charAt(i) != spec.charAt(i + 1)) {
					s = spec.charAt(i++);
					setContextFromChar(ctx, s);
					strokeHalfLine(ctx, xScaleFactor, 0, -1, s, hThickness);
					s = spec.charAt(i++);
					setContextFromChar(ctx, s);
					strokeHalfLine(ctx, xScaleFactor, 0, 1, s, hThickness);
				} else {
					s = spec.charAt(i++);
					s = spec.charAt(i++);
					setContextFromChar(ctx, s);
					strokeFullLine(ctx, xScaleFactor, 0, 1, s);

				}
				ctx.restore();
			}
		});

	}

	@Override
	public String fontName() {
		return getFont().getName();
	}

	@Override
	public double fontSize() {
		return getFont().getSize();
	}

	/**
	 * @return the font
	 */
	protected Font getFont() {
		if (font == null) {
			font = findFont(fileName, size);
		}
		return font;
	}

	@Override
	public <T> T getFont(Class<T> fontClass) {
		if (fontClass == Font.class)
			return (T) getFont();
		else
			return null;
	}

	/**
	 * @return the size
	 */
	@Override
	public double getSize() {
		return size;
	}

	/**
	 * Width and height of the square-shaped space each letter should fit within
	 *
	 * @return the squareSize
	 */
	@Override
	public double getSquareSize() {
		return squareSize;
	}

	private javafx.scene.canvas.Canvas getTmpCanvas() {
		if (tmpCanvas == null) {
			tmpCanvas = new javafx.scene.canvas.Canvas(squareSize, squareSize);
		} else {
			tmpCanvas.getGraphicsContext2D().clearRect(0, 0, squareSize, squareSize);
		}
		return tmpCanvas;
	}

	/**
	 * Horizontal scaling factor (1.0 means no scaling)
	 *
	 * Most fonts are relatively tall and narrow, and need horizontal scaling to fit
	 * a square shape.
	 *
	 * @return the xScale
	 */
	@Override
	public double getxScale() {
		return xScale;
	}

	/**
	 * Horizontal positioning of letters.
	 *
	 * Each letter should be approximately centered within its available
	 * square-shaped space.
	 *
	 * @return the xTranslate
	 */
	@Override
	public double getxTranslate() {
		return xTranslate;
	}

	/**
	 * Vertical scaling factor (1.0 means no scaling)
	 *
	 * @return the yScale
	 */
	@Override
	public double getyScale() {
		return yScale;
	}

	/**
	 * /** Vertical positioning of letters.
	 *
	 * Each letter should be positioned on the baseline so that ascenders and
	 * descenders fall within its available square-shaped space.
	 *
	 * @return the yTranslate
	 */
	@Override
	public double getyTranslate() {
		return yTranslate;
	}

	private double setContextFromChar(GraphicsContext ctx, char c) {
		switch (c) {
		case '0':
			return -2 * thin;
		case '2':
			ctx.setLineDashes(new double[] { 14.75, 2.5 });
			break;
		case '3':
			ctx.setLineDashes(new double[] { 9, 2.5 });
			break;
		case '4':
			ctx.setLineDashes(new double[] { 6.125, 2.5 });
			break;
		case '.':
			return 0.0;
		case '-':
		case 's':
			ctx.setLineWidth(thin);
			return thin;
		case '*':
			ctx.setLineWidth(thick);
			return thick;
		case '=':
		case 'N':
		case 'M':
		case 'z':
			ctx.setLineWidth(thin);
			return thin;
		}
		return 0.0;
	}

	/**
	 * Set up a graphics context for drawing with this font.
	 *
	 * Caller should call {@link GraphicsContext#save()} first, and then
	 * {@link GraphicsContext#restore()} afterwards, to clean up adjustments to the
	 * transformation matrix (i.e., translation, scaling).
	 *
	 * The GraphicsContext should be translated to the coordinates where the text
	 * should appear <em>before</em> calling this method, since this method will
	 * modify the coordinate system.
	 *
	 * @param ctx A GraphicsContext
	 */
	@Override
	public void setGraphicsContext(Canvas canvas) {
		GraphicsContext ctx = context(canvas);
		ctx.setFont(getFont());
		ctx.translate(xTranslate, yTranslate);
		ctx.scale(xScale, yScale);
	}

	/**
	 * Set up a graphics context for drawing with this font.
	 *
	 * Caller should call {@link GraphicsContext#save()} first, and then
	 * {@link GraphicsContext#restore()} afterwards, to clean up adjustments to the
	 * transformation matrix (i.e., translation, scaling).
	 *
	 * The GraphicsContext should be translated to the coordinates where the text
	 * should appear <em>before</em> calling this method, since this method will
	 * modify the coordinate system.
	 *
	 * @param ctx          A GraphicsContext
	 * @param xScaleFactor Additional horizontal scaling, normally 0.5 (for
	 *                     half-width characters)
	 */
	@Override
	public void setGraphicsContext(Canvas canvas, double xScaleFactor) {
		setGraphicsContext(context(canvas), xScaleFactor);
	}

	private void setGraphicsContext(GraphicsContext ctx, double xScaleFactor) {
		ctx.setFont(getFont());
		ctx.translate(xTranslate * xScaleFactor, yTranslate);
		ctx.scale(xScale * xScaleFactor, yScale);
	}

	private void strokeFullLine(GraphicsContext ctx, double xScaleFactor, double xDir, double yDir, char c) {
		double x = squareSize * xScaleFactor / 2, y = -squareSize / 2;
		if (c != '.') {
			// System.out.printf("(%4.1f %4.1f %4.1f %4.1f) w=%1.4f%n", x * yDir, y * xDir,
			// x * yDir + xDir * squareSize,
			// y * xDir + yDir * squareSize, ctx.getLineWidth());
			if (c == '=') {
				ctx.setLineWidth(2.0);
				ctx.strokeLine((x - 2) * yDir, (y - 2) * xDir, (x - 2) * yDir + xDir * squareSize * xScaleFactor,
						(y - 2) * xDir + yDir * -squareSize);
				ctx.strokeLine((x + 2) * yDir, (y + 2) * xDir, (x + 2) * yDir + xDir * squareSize * xScaleFactor,
						(y + 2) * xDir + yDir * -squareSize);
			} else {
				ctx.strokeLine(x * yDir, y * xDir, x * yDir + xDir * squareSize * xScaleFactor,
						y * xDir + yDir * -squareSize);
			}
		}
	}

	private void strokeHalfLine(GraphicsContext ctx, double xScaleFactor, double xDir, double yDir, char c,
			double otherWidth) {
		if (c != '.') {
			double factor = 1.0, dblFactor = 0.0;
			if (c == 's' || c == 'z') {
				factor = -1;
			}
			if (c == 'N') {
				dblFactor = -2;
			}
			if (c == 'M') {
				dblFactor = 2;
			}
			double x = squareSize * xScaleFactor / 2, y = -squareSize / 2;
			x -= xDir * factor * (otherWidth / 2);
			y -= yDir * factor * (otherWidth / 2);

			if (c == '=' || c == 'z' || c == 'N' || c == 'M') {
				ctx.setLineWidth(2.0);
				double x0 = x - 2 * yDir;
				double y0 = y - 2 * xDir;
				x0 += dblFactor * xDir * (otherWidth / 2);
				y0 += dblFactor * yDir * (otherWidth / 2);
				ctx.strokeLine(x0, y0, x0 + xDir * squareSize * xScaleFactor, y0 + yDir * squareSize);
				double x1 = x + 2 * yDir;
				double y1 = y + 2 * xDir;
				x1 -= dblFactor * xDir * (otherWidth / 2);
				y1 -= dblFactor * yDir * (otherWidth / 2);
				ctx.strokeLine(x1, y1, x1 + xDir * squareSize * xScaleFactor, y1 + yDir * squareSize);
			} else {
				ctx.strokeLine(x, y, x + xDir * squareSize * xScaleFactor, y + yDir * squareSize);
			}
		}
	}

	/**
	 * Draw text at the given position.
	 *
	 * For most cases, the simpler {@link #drawText(GraphicsContext, String)} or
	 * {@link #drawText(GraphicsContext, String, double)} will be easier to use.
	 *
	 * If <code>clip</code> is true, the graphics context's current path will be
	 * overwritten.
	 *
	 * @param ctx          A GraphicsContext
	 * @param x            X-position of the lower left corner of the text
	 * @param y            Y-position of the lower left corner of the text
	 * @param text         The text to be printed
	 * @param xScaleFactor Horizontal scaling factor, normally 1.0 (full width) or
	 *                     0.5 (half width)
	 * @param clear        True if the area should be cleared (to transparency)
	 *                     before drawing; normally true.
	 * @param clip         True if the text drawing should be clipped to fit the
	 *                     expected printing area; normally true.
	 * @param fill         Paint to use if the letter shapes should be filled;
	 *                     otherwise null. See ATTR_*.
	 * @param stroke       Paint to use if the letter shapes should be stroked
	 *                     (outlined); normally null.
	 * @param mode         Mode attributes
	 * @param bg           Paint to use for the background (normally null or
	 *                     {@link Color#TRANSPARENT})
	 *
	 */

	protected void textAt(GraphicsContext ctx, double x, double y, String text, double xScaleFactor, boolean clear,
			boolean clip, Paint fill, Paint stroke, int mode, Paint bg) {
//		GraphicsContext ctx = context(canvas);
		GraphicsContext target = ctx;
		int width = text.codePointCount(0, text.length());
		if (width == 0) {
			return;
		}
		ctx.save(); // save 1
		ctx.translate(x, y);
		if (clear && (mode & ATTR_INVERSE) == 0 && (mode & ATTR_OVERSTRIKE) == 0) {
			ctx.clearRect(0 + 0.5, -squareSize + 0.5, squareSize * width * xScaleFactor - 1, squareSize - 1);
		}
		if (bg != null && bg != Colors.TRANSPARENT) {
			ctx.save(); // save 2
			ctx.setFill(JfxColor.toJfxPaint(bg));
			ctx.fillRect(0, -squareSize, squareSize * width * xScaleFactor, squareSize);
			ctx.restore(); // restore 2
		}
		boolean drawIndirect = clip || (mode & (ATTR_INVERSE | ATTR_CLIP)) != 0;
		if (drawIndirect) {
			javafx.scene.canvas.Canvas tmpCanvas = getTmpCanvas();
			target = tmpCanvas.getGraphicsContext2D();
			target.save(); // save 2 if drawIndirect
			target.translate(0, squareSize);
			// target.setFill((mode & ATTR_INVERSE) != 0 ? Color.BLACK : fill);
			// target.setStroke((mode & ATTR_INVERSE) != 0 ? Color.BLACK : stroke);
			fill = (mode & ATTR_INVERSE) != 0 ? Colors.BLACK : fill;
			stroke = (mode & ATTR_INVERSE) != 0 ? Colors.BLACK : stroke;
		}
		doDraw(text, xScaleFactor, fill, stroke, mode, target, width);

		if (drawIndirect) {
			target.restore(); // restore 2 if drawIndirect
			tmpCanvas.snapshot(snapshotParameters, img);
			if ((mode & ATTR_INVERSE) != 0) {
				fillInverse(ctx, img, 0, -squareSize);
			} else {
				ctx.drawImage(img, 0, -squareSize);
			}
		}

		ctx.restore(); // restore 1
	}
}
