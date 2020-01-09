package turtleduck.text.impl;

import java.util.function.BiFunction;

import turtleduck.text.Attributes;
import turtleduck.text.AttributesImpl;
import turtleduck.text.BlocksAndBoxes;
import turtleduck.text.CodePoint;
import turtleduck.text.CodePoint.CodePoints;
import turtleduck.text.SubTextCursor;
import turtleduck.text.TextCursor;
import turtleduck.text.TextFont;
import turtleduck.text.TextWindow;

public class CursorImpl implements TextCursor, SubTextCursor {
	protected Attributes attrs = new AttributesImpl();
	protected int x = 1, y = 1;
	protected TextFont font;
	protected final TextWindow page;
	protected final CursorImpl parent;
	protected boolean isActive = true;
	private boolean autoScroll;

	public CursorImpl(TextWindow page) {
		this.page = page;
		this.parent = null;
	}

	public CursorImpl(CursorImpl parent) {
		this.x = parent.x;
		this.y = parent.y;
		this.font = parent.font;
		this.page = parent.page;
		this.attrs = parent.attrs;
		this.parent = parent;
	}

	@Override
	public SubTextCursor begin() {
		return new CursorImpl(this);
	}

	@Override
	public TextCursor beginningOfLine() {
		x = 1;
		return this;
	}

	@Override
	public TextCursor beginningOfPage() {
		x = 1;
		y = 1;
		return this;
	}

	@Override
	public TextCursor clearRegion(int x0, int y0, int x1, int y1) {
		page.clearRegion(x0, y0, x1, y1, CodePoints.NUL, attrs);
		return this;
	}

	@Override
	public Attributes attributesAt(int x, int y) {
		return page.attributesAt(x, y);
	}

	@Override
	public Attributes attributes() {
		return attrs;
	}

	@Override
	public TextCursor attributes(Attributes attrs) {
		if (attrs != null)
			this.attrs = attrs;
		return this;
	}

	@Override
	public String charAt(int x, int y) {
		return page.charAt(x, y);
	}

	@Override
	public CodePoint codePointAt(int x, int y) {
		return page.codePointAt(x, y);
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return x;
	}

	@Override
	public boolean isFilled(int x, int y) {
		return codePointAt(x, y) != CodePoints.NUL;
	}

	@Override
	public TextCursor move(int deltaX, int deltaY) {
		x += deltaX;
		y += deltaY;
		if (x > page.pageWidth() + 1) {
			x -= page.pageWidth();
			y++;
		}
		if (y < 1) {
			page.scroll(y - 1);
			y = 1;
		} else if (y > page.pageHeight()) {
			page.scroll(y - page.pageHeight());
			y = page.pageHeight();
		}
		return this;
	}

	@Override
	public TextCursor moveHoriz(int dist) {
		move(dist, 0);
		return this;
	}

	@Override
	public TextCursor moveVert(int dist) {
		move(0, dist);
		return this;
	}

	@Override
	public TextCursor print(String s, Attributes attrs) {
		s.codePoints().forEach(cp -> write(CodePoint.codePoint(cp), attrs));
		return this;
	}

	@Override
	public TextCursor redrawTextPage() {
		page.redraw();
		return this;
	}

	@Override
	public TextCursor resetAttrs() {
		attrs = WindowImpl.DEFAULT_ATTRS;
		return this;
	}

	@Override
	public TextCursor scroll(int lines) {
		page.scroll(lines);
		return this;
	}

	@Override
	public boolean autoScroll(boolean autoScroll) {
		this.autoScroll = autoScroll;
		return false;
	}

	@Override
	public boolean hasInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TextCursor sendInput(String string) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TextCursor write(CodePoint cp, Attributes attrs) {
		page.set(x, y, cp, attrs);
		switch (cp.value()) {
		case '\r':
			page.set(x, y, cp, attrs);
			at(1, y);
			break;
		case '\n':
			page.set(x, y, cp, attrs);
			at(1, y + 1);
			break;
		case '\f':
			clearPage();
			break;
		case '\b':
			move(-1, 0);
			break;
		case '\t':
			at((x + 8) % 8, y);
			break;
		case 0x1b:
			// CSI
			break;
		default:
			if (x > page.pageWidth()) {
				at(1, y + 1);
			}
			page.set(x, y, cp, attrs);
			move(1, 0);
			break;
		}
		return this;
	}

	@Override
	public TextCursor clearAt(int x, int y) {
		at(x, y);
		page.set(x, y, CodePoints.NUL, attrs);
		return this;
	}

	@Override
	public TextCursor at(int newX, int newY) {
		if (newX < 1 || newX > page.pageWidth() || newY < 1 || newY > page.pageHeight() + 1)
			throw new IndexOutOfBoundsException("(" + newX + "," + newY + ")");
		if (newY > page.pageHeight()) {
			scroll(1);
			newY--;
		}
		x = newX;
		y = newY;
		return this;
	}

	@Override
	public TextCursor plot(int x, int y) {
		plot(x, y, (a, b) -> a | b);
		return this;
	}

	@Override
	public TextCursor plot(int x, int y, BiFunction<Integer, Integer, Integer> op) {
		int textX = (x) / 2 + 1;
		int textY = (y) / 2 + 1;
//		System.out.println(textX + "," + textY);
		int bitPos = (x + 1) % 2 + ((y + 1) % 2) * 2;
		String blockChar = BlocksAndBoxes.unicodeBlocks[1 << bitPos];
		// System.out.println(blockChar + ", " + bitPos + ", ("+ (x) + ", " + (y) + ")"+
		// ", (" + (textX) + ", " + (textY) + ")");
		String s = BlocksAndBoxes.blockComposeOrOverwrite(charAt(textX, textY), blockChar, op);
		// System.out.println("Merge '" + getChar(textX, textY) + "' + '" + blockChar +
		// "' = '" + s + "'");
		at(textX, textY).print(s);
		return this;
	}

	@Override
	public TextCursor unplot(int x, int y) {
		plot(x, y, (a, b) -> a & ~b);
		return this;
	}

	@Override
	public TextCursor resetFull() {
		resetAttrs();
		beginningOfPage();
		page.redraw();
		return this;
	}

	@Override
	public void close() {
		isActive = false;
	}

	@Override
	public TextCursor end() {
		close();
		return parent;
	}

}
