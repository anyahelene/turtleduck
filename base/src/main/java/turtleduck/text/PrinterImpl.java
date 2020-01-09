package turtleduck.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseLayer;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;

public abstract class PrinterImpl<S extends Screen> extends BaseLayer<S> implements Layer, Printer {
	protected static class Char {
		public int mode;
		public String s;
		public Paint fill;
		public Paint stroke;
		public Paint bg;

		public Char(String s, Paint fill, Paint stroke, Paint bg, int mode) {
			this.s = s;
			this.fill = fill;
			this.stroke = stroke;
			this.bg = bg;
			this.mode = mode;
		}
	}

	protected static final Paint DEFAULT_BACKGROUND = null;
	protected static final TextMode DEFAULT_MODE = TextMode.MODE_40X22;
	protected static final boolean DEBUG_REDRAW = false;

	public static String center(String s, int width) {
		for (; s.length() < width; s = " " + s + " ")
			;
		return s;
	}

	public static String repeat(String s, int width) {
		String r = s;
		for (; r.length() < width; r += s)
			;
		return r;
	}

	Paint DEFAULT_FILL = Colors.BLACK;

	Paint DEFAULT_STROKE = Colors.TRANSPARENT;

	protected TextMode textMode;
	protected Paint fill;
	protected Paint stroke;
	protected Paint background;
	protected List<Char[]> lineBuffer = new ArrayList<>();
	private boolean autoscroll = true;
	protected final Canvas textPage;
	protected int x = 1, y = 1, savedX = 1, savedY = 1;
	// private int pageWidth = LINE_WIDTHS[resMode], pageHeight =
	// PAGE_HEIGHTS[resMode];
	private int leftMargin = 1, topMargin = 1;
	protected TextFont font;
	private int videoAttrs = 0;

	private String csiSeq = null;
	private int csiMode = 0;

	private boolean csiEnabled = true;

	private int dirtyX0 = Integer.MAX_VALUE;
	private int dirtyX1 = Integer.MIN_VALUE;
	private int dirtyY0 = Integer.MAX_VALUE;
	private int dirtyY1 = Integer.MIN_VALUE;
	private boolean useBuffer = true;

	public PrinterImpl(String id, double width, double height) {
		super(id, null, width, height);
		this.textPage = null;
		for (int i = 0; i < TextMode.PAGE_HEIGHT_MAX; i++) {
			lineBuffer.add(new Char[TextMode.LINE_WIDTH_MAX]);
		}
		resetFull();
	}

	public PrinterImpl(String id, S screen, Canvas page) {
		super(id, screen);
		this.textPage = page;
		for (int i = 0; i < TextMode.PAGE_HEIGHT_MAX; i++) {
			lineBuffer.add(new Char[TextMode.LINE_WIDTH_MAX]);
		}
	}

	@Override
	public void write(CodePoint codePoint) {
		addToCharBuffer(codePoint.stringValue());
	}

	@Override
	public void addToCharBuffer(String string) {
		string.codePoints().mapToObj((int i) -> String.valueOf(Character.toChars(i))).forEach((String s) -> {
			if (csiMode != 0) {
				s = addToCsiBuffer(s);
			}
			switch (s) {
			case "\r":
				moveTo(leftMargin, y);
				break;
			case "\n":
				moveTo(leftMargin, y + 1);
				break;
			case "\f":
				moveTo(leftMargin, topMargin);
				for (Char[] line : lineBuffer)
					Arrays.fill(line, null);
				if (textPage != null) {
					if (background != null && background != Colors.TRANSPARENT) {
						textPage.clear(new Fill() {

							@Override
							public Paint fillPaint() {
								return background;
							}
						});
					} else {
						textPage.clear();
					}
					}
				break;
			case "\b":
				moveHoriz(-1);
				break;
			case "\t":
				moveTo((x + 8) % 8, y);
				break;
			case "\u001b":
				if (csiEnabled) {
					csiSeq = s;
					csiMode = 1;
				}
				break;
			default:
				if (s.length() > 0 && s.codePointAt(0) >= 0x20) {
					drawChar(x, y, setChar(x, y, s));
					moveHoriz(1);
				}
				break;
			}
		});
	}

	private String addToCsiBuffer(String s) {
		if (csiMode == 1) {
			switch (s) {
			case "[":
				csiMode = 2;
				csiSeq += s;
				break;
			case "c":
				csiMode = 0;
				resetFull();
				break;
			default:
				csiReset();
				return s;
			}
		} else if (csiMode == 2) {
			int c = s.codePointAt(0);
			if (c >= 0x30 && c <= 0x3f) {
				csiSeq += s;
			} else if (c >= 0x20 && c <= 0x2f) {
				csiMode = 3;
				csiSeq += s;
			} else if (c >= 0x40 && c <= 0x7e) {
				csiSeq += s;
				csiFinish();
			} else {
				csiReset();
				return s;
			}

		} else if (csiMode == 3) {
			int c = s.codePointAt(0);
			if (c >= 0x20 && c <= 0x2f) {
				csiSeq += s;
			} else if (c >= 0x40 && c <= 0x7e) {
				csiSeq += s;
				csiFinish();
			} else {
				csiReset();
				return s;
			}
		}
		return "";
	}

	@Override
	public void beginningOfLine() {
		x = leftMargin;
	}

	@Override
	public void beginningOfPage() {
		x = leftMargin;
		y = topMargin;
	}

	@Override
	public void clear() {
		print("\f");
	}
	

	@Override
	public void clearAt(int x, int y) {
		printAt(x, y, " ");
	}

	@Override
	public void clearLine(int y) {
		y = constrainY(y);
		if (y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			Arrays.fill(lineBuffer.get(y - 1), null);
			dirty(1, y);
			dirty(getLineWidth(), y);
			if (!useBuffer)
				redrawDirty();
		}
	}

	@Override
	public void clearRegion(int x, int y, int width, int height) {
		if (x > getLineWidth() || y > getPageHeight())
			return;
		int x2 = Math.min(x + width - 1, getLineWidth());
		int y2 = Math.min(y + height - 1, getPageHeight());
		if (x2 < 1 || y2 < 1)
			return;
		int x1 = Math.max(1, x);
		int y1 = Math.max(1, y);
		// Char fillWith = new Char("*", Paint.BLACK, Paint.GREEN, Paint.TRANSPARENT,
		// 0);
		for (int i = y1; i <= y2; i++) {
			Arrays.fill(lineBuffer.get(i - 1), x1 - 1, x2, null);
		}
		dirty(x1, y1);
		dirty(x2, y2);
		if (!useBuffer)
			redrawDirty();
	}

	private int constrainX(int x) {
		return x; // Math.min(LINE_WIDTH_HIRES, Math.max(1, x));
	}

	@Override
	public int constrainY(int y) {
		return y; // Math.min(pageHeight, Math.max(1, y));
	}

	@Override
	public int constrainYOrScroll(int y) {
		if (autoscroll) {
			if (y < 1) {
				scroll(y - 1);
				return 1;
			} else if (y > getPageHeight()) {
				scroll(y - getPageHeight());
				return getPageHeight();
			}
		}

		return y;// Math.min(pageHeight, Math.max(1, y));
	}

	private void csiFinish() {
		ControlSequences.applyCsi(this, csiSeq);
		csiReset();
	}

	private void csiReset() {
		csiMode = 0;
		csiSeq = null;
	}

	@Override
	public void cycleMode(boolean adjustDisplayAspect) {
		textMode = textMode.nextMode();
		if (adjustDisplayAspect && screen != null)
			screen.setAspect(textMode.getAspect());
		dirty(1, 1);
		dirty(getLineWidth(), getPageHeight());
		if (!useBuffer)
			redrawDirty();
	}

	protected void drawChar(int x, int y, Char c) {
		if (useBuffer) {
			dirty(x, y);
		} else if (c != null && textPage != null) {
			doDrawChar(x, y, c);
		}
	}
protected abstract void doDrawChar(int x, int y, Char c);
	@Override
	public void drawCharCells() {
// TODO: port from JavaFX
	}

	@Override
	public Paint getBackground(int x, int y) {
		Char c = null;
		if (x > 0 && x <= TextMode.LINE_WIDTH_MAX && y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			c = lineBuffer.get(y - 1)[x - 1];
		}
		Paint bg = Colors.TRANSPARENT;
		if (c != null && c.bg instanceof Paint)
			bg = (Paint) c.bg;
		else if (background instanceof Paint)
			bg = (Paint) background;
		return bg;
	}

	@Override
	public boolean getBold() {
		return (videoAttrs & TextFont.ATTR_BRIGHT) != 0;
	}

	@Override
	public String getChar(int x, int y) {
		Char c = null;
		if (x > 0 && x <= TextMode.LINE_WIDTH_MAX && y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			c = lineBuffer.get(y - 1)[x - 1];
		}
		if (c != null)
			return c.s;
		else
			return " ";
	}

	@Override
	public double getCharHeight() {
		return textMode.getCharHeight();
	}

	@Override
	public double getCharWidth() {
		return textMode.getCharWidth();
	}

	@Override
	public Paint getColor(int x, int y) {
		Char c = null;
		if (x > 0 && x <= TextMode.LINE_WIDTH_MAX && y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			c = lineBuffer.get(y - 1)[x - 1];
		}
		if (c != null)
			return c.fill;
		else
			return fill;
	}

	@Override
	public TextFont getFont() {
		return font;
	}

	@Override
	public boolean getItalics() {
		return (videoAttrs & TextFont.ATTR_ITALIC) != 0;
	}

	/**
	 * @return the leftMargin
	 */
	@Override
	public int getLeftMargin() {
		return leftMargin;
	}

	@Override
	public int getLineWidth() {
		return textMode.getLineWidth();
	}

	@Override
	public int getPageHeight() {
		return textMode.getPageHeight();
	}

	@Override
	public boolean getReverseVideo() {
		return (videoAttrs & TextFont.ATTR_INVERSE) != 0;
	}

	@Override
	public TextMode getTextMode() {
		return textMode;
	}

	/**
	 * @return the topMargin
	 */
	@Override
	public int getTopMargin() {
		return topMargin;
	}

	@Override
	public int getVideoMode() {
		return videoAttrs;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public boolean isFilled(int x, int y) {
		return !getChar(x, y).equals(" ");
	}

	@Override
	public void layerToBack() {
		if (screen != null) {
			screen.moveToBack(this);
		}
	}

	@Override
	public void layerToFront() {
		if (screen != null) {
			screen.moveToFront(this);
		}
	}

	@Override
	public void move(int deltaX, int deltaY) {
		x = constrainX(x + deltaX);
		y = constrainYOrScroll(y + deltaY);
	}

	@Override
	public void moveHoriz(int dist) {
		x = constrainX(x + dist);
	}

	@Override
	public void moveTo(int newX, int newY) {
		x = constrainX(newX);
		y = constrainYOrScroll(newY);
	}

	@Override
	public void moveVert(int dist) {
		y = constrainYOrScroll(y + dist);
	}

	@Override
	public void plot(int x, int y) {
		plot(x, y, (a, b) -> a | b);
	}

	@Override
	public void plot(int x, int y, BiFunction<Integer, Integer, Integer> op) {
		int textX = (x) / 2 + 1;
		int textY = (y) / 2 + 1;
//		System.out.println(textX + "," + textY);
		int bitPos = (x + 1) % 2 + ((y + 1) % 2) * 2;
		String blockChar = BlocksAndBoxes.unicodeBlocks[1 << bitPos];
		// System.out.println(blockChar + ", " + bitPos + ", ("+ (x) + ", " + (y) + ")"+
		// ", (" + (textX) + ", " + (textY) + ")");
		String s = BlocksAndBoxes.blockComposeOrOverwrite(getChar(textX, textY), blockChar, op);
		// System.out.println("Merge '" + getChar(textX, textY) + "' + '" + blockChar +
		// "' = '" + s + "'");
		printAt(textX, textY, s);
	}

	@Override
	public void print(String s) {
		addToCharBuffer(s);
	}

	@Override
	public void print(String s, Paint paint) {
		Paint tmp = fill;
		fill = paint;
		addToCharBuffer(s);
		fill = tmp;
	}

	@Override
	public void printAt(int atX, int atY, String s) {
		moveTo(atX, atY);
		print(s);
	}

	@Override
	public void printAt(int atX, int atY, String s, Paint ink) {
		moveTo(atX, atY);
		print(s, ink);
	}

	@Override
	public void println() {
		print("\n");
	}

	@Override
	public void println(String s) {
		print(s);
		print("\n");
	}

	@Override
	public void redrawTextPage() {
		redrawTextPage(1, 1, getLineWidth(), getPageHeight());
		clean();
	}
	protected abstract void redrawTextPage(int x0, int y0, int x1, int y1);


	@Override
	public void resetAttrs() {
		this.fill = DEFAULT_FILL;
		this.stroke = DEFAULT_STROKE;
		this.background = DEFAULT_BACKGROUND;
		this.videoAttrs = 0;
		this.csiSeq = null;
		this.csiMode = 0;
	}

	@Override
	public void resetFull() {
		resetAttrs();
		beginningOfPage();
		this.autoscroll = true;
		this.textMode = DEFAULT_MODE;
		redrawTextPage();
	}

	@Override
	public void restoreCursor() {
		x = savedX;
		y = savedY;
	}

	@Override
	public void saveCursor() {
		savedX = x;
		savedY = y;
	}

	@Override
	public void scroll(int i) {
		while (i < 0) {
			scrollDown();
			i++;
		}
		while (i > 0) {
			scrollUp();
			i--;
		}
	}

	@Override
	public void scrollDown() {
		Char[] remove = lineBuffer.remove(lineBuffer.size() - 1);
		Arrays.fill(remove, null);
		lineBuffer.add(0, remove);
		dirty(1, 1);
		dirty(getLineWidth(), getPageHeight());
		if (!useBuffer)
			redrawDirty();
	}

	@Override
	public void scrollUp() {
		Char[] remove = lineBuffer.remove(0);
		Arrays.fill(remove, null);
		lineBuffer.add(remove);
		dirty(1, 1);
		dirty(getLineWidth(), getPageHeight());
		if (!useBuffer)
			redrawDirty();
	}

	@Override
	public boolean setAutoScroll(boolean autoScroll) {
		boolean old = autoscroll;
		autoscroll = autoScroll;
		return old;
	}

	@Override
	public void setBackground(int x, int y, Paint bg) {
		Char c = null;
		if (x > 0 && x <= TextMode.LINE_WIDTH_MAX && y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			c = lineBuffer.get(y - 1)[x - 1];
		}
		if (c != null) {
			c.bg = bg;
			drawChar(x, y, c);
		}
	}

	@Override
	public void setBackground(Paint bgColor) {
		this.background = bgColor;
	}

	@Override
	public void setBold(boolean enabled) {
		if (enabled)
			videoAttrs |= TextFont.ATTR_BRIGHT;
		else
			videoAttrs &= ~TextFont.ATTR_BRIGHT;
	}

	
	protected Char setChar(int x, int y, String s) {
		if (x > 0 && x <= TextMode.LINE_WIDTH_MAX && y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			Char oldC = lineBuffer.get(y - 1)[x - 1];
			Char c = new Char(s, fill, stroke, background, videoAttrs);
			if(c.bg == null && oldC != null)
				c.bg = oldC.bg;
			lineBuffer.get(y - 1)[x - 1] = c;
			return c;
		}
		return null;
	}

	@Override
	public void setColor(int x, int y, Paint fill) {
		Char c = null;
		if (x > 0 && x <= TextMode.LINE_WIDTH_MAX && y > 0 && y <= TextMode.PAGE_HEIGHT_MAX) {
			c = lineBuffer.get(y - 1)[x - 1];
		}
		if (c != null) {
			c.fill = fill;
			drawChar(x, y, c);
		}
	}

	@Override
	public void setFill(Paint fill) {
		this.fill = fill != null ? fill : DEFAULT_FILL;
	}

	@Override
	public void setFont(TextFont font) {
		this.font = font;
	}

	@Override
	public void setInk(Paint ink) {
		fill = ink != null ? ink : DEFAULT_FILL;
		stroke = ink != null ? ink : DEFAULT_STROKE;
	}

	@Override
	public void setItalics(boolean enabled) {
		if (enabled)
			videoAttrs |= TextFont.ATTR_ITALIC;
		else
			videoAttrs &= ~TextFont.ATTR_ITALIC;
	}

	/**
	 */
	@Override
	public void setLeftMargin() {
		this.leftMargin = x;
	}

	/**
	 * @param leftMargin
	 *            the leftMargin to set
	 */
	@Override
	public void setLeftMargin(int leftMargin) {
		this.leftMargin = constrainX(leftMargin);
	}

	@Override
	public void setReverseVideo(boolean enabled) {
		if (enabled)
			videoAttrs |= TextFont.ATTR_INVERSE;
		else
			videoAttrs &= ~TextFont.ATTR_INVERSE;
	}

	@Override
	public void setStroke(Paint stroke) {
		this.stroke = stroke != null ? stroke : DEFAULT_STROKE;
	}

	@Override
	public void setTextMode(TextMode mode) {
		setTextMode(mode, false);
	}

	@Override
	public void setTextMode(TextMode mode, boolean adjustDisplayAspect) {
		if (mode == null)
			throw new IllegalArgumentException();
		textMode = mode;
		if (adjustDisplayAspect && screen != null)
			screen.setAspect(textMode.getAspect());
		dirty(1, 1);
		dirty(getLineWidth(), getPageHeight());
		if (!useBuffer)
			redrawDirty();
	}

	@Override
	public void setTopMargin() {
		this.topMargin = y;
	}

	/**
	 * @param topMargin
	 *            the topMargin to set
	 */
	@Override
	public void setTopMargin(int topMargin) {
		this.topMargin = constrainY(topMargin);
	}

	@Override
	public void setVideoAttrDisabled(int attr) {
		videoAttrs &= ~attr;
	}

	@Override
	public void setVideoAttrEnabled(int attr) {
		videoAttrs |= attr;
	}

	@Override
	public void setVideoAttrs(int attr) {
		videoAttrs = attr;
	}

	@Override
	public void unplot(int x, int y) {
		plot(x, y, (a, b) -> a & ~b);
	}

	@Override
	public double width() {
		return width;
	}

	@Override
	public double height() {
		return height;
	}

	private boolean isDirty() {
		return dirtyX0 <= dirtyX1 || dirtyY0 <= dirtyY1;
	}

	/**
	 * Expand the dirty region (area that should be redrawn) to include the given
	 * position
	 * 
	 * @param x
	 * @param y
	 */
	private void dirty(int x, int y) {
		dirtyX0 = Math.max(Math.min(x, dirtyX0), 1);
		dirtyX1 = Math.min(Math.max(x, dirtyX1), getLineWidth());
		dirtyY0 = Math.max(Math.min(y, dirtyY0), 1);
		dirtyY1 = Math.min(Math.max(y, dirtyY1), getPageHeight());
	}

	/**
	 * Redraw the part of the page that has changed since last redraw.
	 */
	@Override
	public void redrawDirty() {
		if (isDirty()) {
			if (DEBUG_REDRAW)
				System.out.printf("redrawDirty(): Dirty region is (%d,%d)–(%d,%d)%n", dirtyX0, dirtyY0, dirtyX1,
						dirtyY1);
			redrawTextPage(dirtyX0, dirtyY0, dirtyX1, dirtyY1);
			clean();
		}
	}

	/**
	 * Mark the entire page as clean
	 */
	private void clean() {
		dirtyX0 = Integer.MAX_VALUE;
		dirtyX1 = Integer.MIN_VALUE;
		dirtyY0 = Integer.MAX_VALUE;
		dirtyY1 = Integer.MIN_VALUE;
	}

	/**
	 * With buffered printing, nothing is actually drawn until
	 * {@link #redrawDirty()} or {@link #redrawTextPage()} is called.
	 * 
	 * @param buffering
	 *            Whether to use buffering
	 */
	@Override
	public void setBuffering(boolean buffering) {
		useBuffer = buffering;
	}

	/**
	 * @return True if buffering is enabled
	 * @see #setBuffering(boolean)
	 */
	@Override
	public boolean getBuffering() {
		return useBuffer;
	}
}
