package turtleduck.text.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseLayer;
import turtleduck.text.Attribute;
import turtleduck.text.Attributed;
import turtleduck.text.Attributes;
import turtleduck.text.AttributesImpl;
import turtleduck.text.CodePoint;
import turtleduck.text.TextCursor;
import turtleduck.text.TextFont;
import turtleduck.text.TextMode;
import turtleduck.text.TextWindow;
import turtleduck.text.CodePoint.CodePoints;
import turtleduck.text.Region;
import turtleduck.turtle.Canvas;

public abstract class WindowImpl<S extends Screen> extends BaseLayer<S> implements TextWindow {
	protected static class Cell {
		CodePoint cp;
		Attributes attrs;

		public Cell(CodePoint cp, Attributes attrs) {
			this.cp = cp;
			this.attrs = attrs;
		}

		public String toHtml(int col, int line) {
			if (cp == CodePoints.NUL)
				return "&nbsp;";
			else {
				String css = attrs.toCss();
				if (!css.equals(""))
					css = String.format(" style=\"%s\"", css);
				return String.format("<span id=\"L%dC%d\"%s>%s</span>", line, col, css, cp.toHtml());
			}
		}

		public String toString() {
			if (cp == CodePoints.NUL)
				return "";
			else
				return cp.toString();
		}

		public CodePoint codePoint() {
			return cp;
		}

		public void clear() {
			cp = CodePoints.NUL;
			attrs = DEFAULT_ATTRS;
		}

		public boolean notNull() {
			return cp != CodePoints.NUL;
		}

		public Attributes attrs() {
			return attrs;
		}

		public TextFont font() {
			return attrs.get(Attribute.ATTR_FONT);
		}

		public Paint background() {
			return attrs.get(Attribute.ATTR_BACKGROUND);
		}

		public Paint foreground() {
			return attrs.get(Attribute.ATTR_FOREGROUND);
		}
	}

	protected static class Line {
		List<Cell> cells;

		public Line(int cols) {
			cells = new ArrayList<>(cols);
			for (int i = 0; i < cols; i++)
				cells.add(new Cell(CodePoints.NUL, DEFAULT_ATTRS));
		}

		public Cell col(int x) {
			return cells.get(x - 1);
		}

		public String toHtml(int l) {
			return IntStream.range(0, cells.size()).mapToObj(i -> cells.get(i).toHtml(i + 1, l))//
					.collect(Collectors.joining("", String.format("<p id=\"L%d\">", l), "</p>"));
		}

		public void clear() {
			cells.stream().forEach(c -> c.clear());
		}

		public void forEachElement(int x0, int x1, int y, Consumer<TextElement> action) {
			Attributes attrs = null;
			for (int x = x0; x <= x1 + 1; x++) {
				if (attrs == null && x <= x1) {
					attrs = cells.get(x - 1).attrs;
				}
				if (x == x1 + 1) {
					action.accept(new TextElement(x0, y, x - x0, 1, cells.subList(x0 - 1, x - 1), attrs));
				} else if (attrs != cells.get(x - 1).attrs) {
					action.accept(new TextElement(x0, y, x - x0 , 1, cells.subList(x0 - 1, x - 1), attrs));
					x0 = x;
					attrs = cells.get(x - 1).attrs;
				}
			}
		}
	}

	protected static class Cells {
		int cols, rows;
		Region region;
		List<Line> lines;

		public Cells(int cols, int rows) {
			lines = new ArrayList<>(cols * rows);
			for (int i = 0; i < rows; i++)
				lines.add(new Line(cols));
			this.cols = cols;
			this.rows = rows;
			region = Region.rectangular(1, 1, cols, rows);
		}

		public Line line(int y) {
			return lines.get(y - 1);
		}

		public String toHtml() {
			return IntStream.range(0, lines.size()).mapToObj(i -> lines.get(i).toHtml(i + 1))//
					.collect(Collectors.joining("", "<code id=\"CELLS\">", "</code>"));
		}

		public Stream<Cell> streamRegion(int x0, int y0, int x1, int y1) {
			return Region.flow(x0, y0, x1, y1).posStream(region).map(pos -> line(pos.line()).col(pos.column()));
		}

		public void forEachElement(Region region, Consumer<TextElement> action) {
			for (int y = region.line(); y <= region.endLine(); y++) {
				Line line = lines.get(y - 1);
				int x0 = y == region.line() ? region.column() : 1;
				int x1 = y == region.endLine() ? region.endColumn() : cols;
				line.forEachElement(x0, x1, y, action);
			}
		}
	}

	protected static class TextElement {
		protected List<Cell> data;
		protected Attributes attrs;
		private int x, y, w, h;

		public int x() {
			return x;
		}

		public int y() {
			return y;
		}

		public TextElement(int x, int y, int w, int h, List<Cell> data, Attributes attrs) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.data = data;
			this.attrs = attrs;
		}

		public int width() {
			return w;
		}

		public int height() {
			return h;
		}

		public List<Cell> cells() {
			return data;
		}

		public Attributes attributes() {
			return attrs;
		}

		public String toHtml() {
			StringBuilder sb = new StringBuilder();
			String css = attrs.toCss();
			if (!css.equals(""))
				css = String.format(" style=\"%s\"", css);
			sb.append(String.format("<span id=\"L%dC%d\"%s>", y, x, css));
			for (Cell c : data) {
				if (c.cp != CodePoints.NUL)
					sb.append(c.cp.toHtml());
			}
			sb.append("</span>");
			return sb.toString();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Cell c : data)
				sb.append(c.cp.toString());
			return sb.toString();
		}

	}

	protected static final Attributes DEFAULT_ATTRS = new AttributesImpl();

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

	protected TextMode textMode;
	// private int pageWidth = LINE_WIDTHS[resMode], pageHeight =
	// PAGE_HEIGHTS[resMode];
	private int leftMargin = 1, topMargin = 1;
	private int videoAttrs = 0;

	private String csiSeq = null;
	private int csiMode = 0;

	private boolean csiEnabled = true;
	private boolean autoscroll = true;
	private boolean autowrap = true;

	private int dirtyX0 = Integer.MAX_VALUE;
	private int dirtyX1 = Integer.MIN_VALUE;
	private int dirtyY0 = Integer.MAX_VALUE;
	private int dirtyY1 = Integer.MIN_VALUE;
	private boolean useBuffer = true;
	protected Cells cells;

	public WindowImpl(String id, TextMode mode, S screen, double width, double height) {
		super(id, screen, width, height);
		textMode = mode;
		cells = new Cells(TextMode.LINE_WIDTH_MAX, TextMode.PAGE_HEIGHT_MAX);
	}

	@Override
	public TextWindow set(int x, int y, CodePoint cp, Attributes attrs) {
		if (y < 0) {
			y = textMode.getPageHeight() + y + 1;
		} else if (y == textMode.getPageHeight() + 1 && autoscroll) {
			scroll(1);
			y--;
		}
		if (y == 0 || y > textMode.getPageHeight()) {
			throw new IndexOutOfBoundsException("y=" + y);
		}
		if (x < 0) {
			x = textMode.getLineWidth() + x + 1;
		} else if (x == textMode.getLineWidth() + 1 && autowrap) {
			x = 1;
			y++;
			if (y == textMode.getPageHeight() + 1 && autoscroll) {
				scroll(1);
				y--;
			}
		}
		Cell col = cells.line(y).col(x);
		if (attrs.isSet(Attribute.ATTR_OVERSTRIKE)) {
			if (cp == col.cp) {
				attrs = attrs.change().bold(true).overstrike(false).done();
			} else {
				attrs = attrs.change().overstrike(false).done();
				cp = CodePoints.combine(col.cp, cp);
			}
		}
		col.cp = cp;
		col.attrs = attrs;
		dirty(x, y);
		return this;
	}

	@Override
	public TextWindow clearRegion(int x0, int y0, int x1, int y1, CodePoint cp, Attributes attrs) {
		cells.streamRegion(x0, y0, x1, y1).forEach(c -> {
			c.cp = cp;
			c.attrs = attrs;
		});
		dirty(x0, y0);
		dirty(x1, y1);
		if (!useBuffer)
			flush();
		return this;
	}

	@Override
	public TextWindow cycleMode(boolean adjustDisplayAspect) {
		textMode(textMode.nextMode(), true);
		System.out.println(textMode);
		return this;
	}

	/**
	 * @param x horizontal position (counting from 1)
	 * @param y vertical position (counting from 1)
	 * @return <code>this</code>, for further calls
	 */
	@Override
	public Attributes attributesAt(int x, int y) {
		// TODO: bounds check
		return cells.line(y).col(x).attrs;
	}

	@Override
	public CodePoint codePointAt(int x, int y) {
		// TODO: bounds check
		return cells.line(y).col(x).cp;
	}

	@Override
	public String charAt(int x, int y) {
		// TODO: bounds check
		CodePoint cp = cells.line(y).col(x).cp;
		if (cp == CodePoints.NUL)
			return "";
		else
			return cp.stringValue();
	}

	@Override
	public void redraw() {
		redraw(1, 1, pageWidth(), pageHeight(), DEFAULT_ATTRS);
		clean();
	}

	// protected abstract void redrawTextPage(int x0, int y0, int x1, int y1);
	protected abstract void redraw(int x0, int y0, int x1, int y1, Attributes attrs);

	@Override
	public TextWindow scroll(int i) {
		while (i < 0) {
			scrollDown();
			i++;
		}
		while (i > 0) {
			scrollUp();
			i--;
		}
		return this;
	}

	public void scrollDown() {
		Line remove = cells.lines.remove(cells.lines.size() - 1);
		remove.clear();
		cells.lines.add(0, remove);
		dirty(1, 1);
		dirty(pageWidth(), pageHeight());
		if (!useBuffer)
			flush();
	}

	public void scrollUp() {
		Line remove = cells.lines.remove(0);
		remove.clear();
		cells.lines.add(remove);
		dirty(1, 1);
		dirty(pageWidth(), pageHeight());
		if (!useBuffer)
			flush();
	}

	@Override
	public boolean autoScroll(boolean autoScroll) {
		boolean old = autoscroll;
		autoscroll = autoScroll;
		return old;
	}

	@Override
	public TextWindow textMode(TextMode mode) {
		return textMode(mode, false);
	}

	@Override
	public TextWindow textMode(TextMode mode, boolean adjustDisplayAspect) {
		if (mode == null)
			throw new IllegalArgumentException();
		textMode = mode;
		if (adjustDisplayAspect && screen != null)
			screen.setAspect(textMode.getAspect());
		dirty(1, 1);
		dirty(pageWidth(), pageHeight());
		if (!useBuffer)
			flush();
		return this;
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
		if (DEBUG_REDRAW)
			System.out.printf("dirty(%d,%d): Dirty region is (%d,%d)–(%d,%d)%n", x, y, dirtyX0, dirtyY0, dirtyX1,
					dirtyY1);
		dirtyX0 = Math.max(Math.min(x, dirtyX0), 1);
		dirtyX1 = Math.min(Math.max(x, dirtyX1), pageWidth());
		dirtyY0 = Math.max(Math.min(y, dirtyY0), 1);
		dirtyY1 = Math.min(Math.max(y, dirtyY1), pageHeight());
		if (DEBUG_REDRAW)
			System.out.printf("            → Dirty region is (%d,%d)–(%d,%d)%n", dirtyX0, dirtyY0, dirtyX1, dirtyY1);
	}

	/**
	 * Redraw the part of the page that has changed since last redraw.
	 */
	@Override
	public void flush() {
		if (isDirty()) {
			if (DEBUG_REDRAW)
				System.out.printf("flush(): Dirty region is (%d,%d)–(%d,%d)%n", dirtyX0, dirtyY0, dirtyX1, dirtyY1);
			redraw(dirtyX0, dirtyY0, dirtyX1, dirtyY1, DEFAULT_ATTRS);
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
	 * @param buffering Whether to use buffering
	 */
	@Override
	public TextWindow buffering(boolean buffering) {
		useBuffer = buffering;
		return this;
	}

	/**
	 * @return True if buffering is enabled
	 * @see #setBuffering(boolean)
	 */
	@Override
	public boolean buffering() {
		return useBuffer;
	}

	@Override
	public TextMode textMode() {
		return textMode;
	}

	@Override
	public int pageWidth() {
		return textMode.getLineWidth();
	}

	@Override
	public int pageHeight() {
		return textMode.getPageHeight();
	}

	@Override
	public TextCursor cursor() {
		return new CursorImpl(this);
	}

	@Override
	public void clear() {
		clearRegion(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, CodePoints.NUL, DEFAULT_ATTRS);
	}

	@Override
	public abstract Canvas canvas();

	@Override
	public abstract void show();

	@Override
	public abstract void hide();
}
