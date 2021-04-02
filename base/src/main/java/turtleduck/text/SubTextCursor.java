package turtleduck.text;

import java.util.function.BiFunction;

import turtleduck.colors.Color;

public interface SubTextCursor extends TextCursor, AutoCloseable {

	/**
	 * Stop working with this sub-cursor, and continue printing with its parent.
	 * 
	 * <p>
	 * Position and attributes etc are restored to their values at the previous call
	 * to {@link #begin()}.
	 * 
	 * @return A restored cursor for continued printing
	 */
	TextCursor end();

	default SubTextCursor clearPage() {
		return at(1, 1).clearRegion(1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	default SubTextCursor clearAt(int x, int y) {
		return clearRegion(x, y, x, y);
	}

	default SubTextCursor clearLine(int y) {
		return clearRegion(0, y, Integer.MAX_VALUE, y);
	}

	default SubTextCursor clearToBeginningOfLine() {
		return clearRegion(0, y(), x(), y());
	}

	default SubTextCursor clearToEndOfLine() {
		return clearRegion(x(), y(), Integer.MAX_VALUE, y());
	}

	default Color backgroundAt(int x, int y) {
		return attributesAt(x, y).get(Attribute.ATTR_BACKGROUND);
	}

	default Color foregroundAt(int x, int y) {
		return attributesAt(x, y).get(Attribute.ATTR_FOREGROUND);
	}

	default Color background() {
		return attributes().get(Attribute.ATTR_BACKGROUND);
	}

	default Color foreground() {
		return attributes().get(Attribute.ATTR_FOREGROUND);
	}

	default SubTextCursor background(Color backColor) {
		return attributes(attributes().change().background(backColor).done());
	}

	default SubTextCursor foreground(Color foreColor) {
		return attributes(attributes().change().foreground(foreColor).done());
	}

	default SubTextCursor print(String s) {
		return print(s, attributes());
	}

	default SubTextCursor print(String s, Color foreColor) {
		return print(s, foreColor, null);
	}

	default SubTextCursor print(String s, Color foreColor, Color backColor) {
		return print(s, attributes().change().foreground(foreColor).background(backColor).done());
	}

	default SubTextCursor println() {
		return print("\n");
	}

	default SubTextCursor println(String s) {
		return print(s + "\n");
	}

	default SubTextCursor println(String s, Color foreColor) {
		return print(s + "\n", foreColor, null);
	}

	default SubTextCursor println(String s, Color foreColor, Color backColor) {
		return print(s + "\n", foreColor, backColor);
	}

	default SubTextCursor setFont(TextFont font) {
		return attributes(attributes().change().set(Attribute.ATTR_FONT, font).done());
	}

	default SubTextCursor write(CodePoint codePoint) {
		return write(codePoint, attributes());
	};

	@Override
	SubTextCursor beginningOfLine();

	@Override
	SubTextCursor beginningOfPage();

	@Override
	SubTextCursor clearRegion(int x0, int y0, int x1, int y1);

	@Override
	SubTextCursor attributes(Attributes attrs);

	@Override
	SubTextCursor move(int deltaX, int deltaY);

	@Override
	SubTextCursor moveHoriz(int dist);

	@Override
	SubTextCursor at(int newX, int newY);

	@Override
	SubTextCursor moveVert(int dist);

	@Override
	SubTextCursor plot(int x, int y);

	@Override
	SubTextCursor plot(int x, int y, BiFunction<Integer, Integer, Integer> op);

	@Override
	SubTextCursor print(String s, Attributes attrs);

	@Override
	SubTextCursor redrawTextPage();

	@Override
	SubTextCursor resetAttrs();

	@Override
	SubTextCursor resetFull();

	@Override
	SubTextCursor scroll(int lines);

	@Override
	SubTextCursor unplot(int x, int y);

	@Override
	SubTextCursor sendInput(String string);

	@Override
	SubTextCursor write(CodePoint codePoint, Attributes attrs);

	SubTextCursor flush();

}
