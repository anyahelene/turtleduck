package turtleduck.text.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import turtleduck.objects.IdentifiedObject;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.PtyWriter;
import turtleduck.text.Attribute;
import turtleduck.text.Attributes;
import turtleduck.text.AttributesImpl;
import turtleduck.text.CodePoint;
import turtleduck.text.ControlSequences;
import turtleduck.text.SubTextCursor;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;

public class HtmlCursorImpl implements TextCursor, SubTextCursor {
	private static Attributes DEFAULT_ATTRS = new AttributesImpl<Attributes>(null);
	private Attributes currentAttrs = DEFAULT_ATTRS;
	private String currentStyle = null;
	private HTMLWriter terminal;
	private List<Attributes> stack = new ArrayList<>();
	private Consumer<String> host;
	private StringBuffer csiSeq = new StringBuffer();
	List<Attribute<?>> RELEVANT_ATTRS = Arrays.asList(Attribute.ATTR_FOREGROUND, Attribute.ATTR_BACKGROUND,
			Attribute.ATTR_BRIGHTNESS, Attribute.ATTR_STYLE, Attribute.ATTR_WEIGHT, Attribute.ATTR_UNDERLINE);
	private int csiMode = 0;
	private boolean csiEnabled = true;
	protected final String id;

	public HtmlCursorImpl(HTMLWriter writeToTerminal, Consumer<String> writeToHost) {
		id = IdentifiedObject.Registry.makeId(TextCursor.class, this);
		terminal = writeToTerminal;
		host = writeToHost;
		currentStyle = currentAttrs.toCss();
	}

	@Override
	public SubTextCursor begin() {
		if (!stack.isEmpty())
			throw new IllegalStateException("Only one subcursor nesting level supported");
		stack.add(currentAttrs);
		return this;
	}

	@Override
	public SubTextCursor beginningOfLine() {
		terminal.column(1);
		return this;
	}

	@Override
	public SubTextCursor beginningOfPage() {
		at(1, 1);
		return this;
	}

	@Override
	public SubTextCursor clearRegion(int x0, int y0, int x1, int y1) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Attributes attributesAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attributes attributes() {
		// TODO Auto-generated method stub
		return currentAttrs;
	}

	@Override
	public SubTextCursor attributes(Attributes attrs) {
		if (attrs != currentAttrs)
			setAttrs(attrs);
		return this;
	}

	private void setAttrs(Attributes attrs) {
		currentAttrs = attrs;
		currentStyle = attrs.toCss();
	}

	@Override
	public String charAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CodePoint codePointAt(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int x() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int y() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFilled(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SubTextCursor move(int deltaX, int deltaY) {
		moveHoriz(deltaX);
		moveVert(deltaY);
		return this;
	}

	@Override
	public SubTextCursor moveHoriz(int dist) {
		terminal.move(dist, 0);

		return this;
	}

	@Override
	public SubTextCursor at(int newX, int newY) {
		terminal.column(newX);
		terminal.row(newY);
		return this;
	}

	@Override
	public SubTextCursor moveVert(int dist) {
		terminal.move(0, dist);

		return this;
	}

	@Override
	public SubTextCursor plot(int x, int y) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public SubTextCursor plot(int x, int y, BiFunction<Integer, Integer, Integer> op) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public SubTextCursor print(String s, Attributes attrs) {
		Attributes old = currentAttrs;
		attributes(attrs);
		print(s);
		attributes(old);
		return this;
	}

	public SubTextCursor print(String s) {
		char first = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0xd800 && c <= 0xdbff) { // high surrogate
				first = c;
			} else if (c >= 0xdc00 && c <= 0xdfff) { // low surrogate
				if (first != 0) {
					write(Character.toCodePoint(first, c));
				}
			} else {
				write(c);
			}
		}
		return this;
	}

	@Override
	public SubTextCursor redrawTextPage() {
		// TODO
		return this;
	}

	@Override
	public SubTextCursor resetAttrs() {
		attributes(DEFAULT_ATTRS);
		return this;
	}

	@Override
	public SubTextCursor resetFull() {
		// TODO
		return this;
	}

	@Override
	public SubTextCursor scroll(int lines) {

		return this;
	}

	@Override
	public boolean autoScroll(boolean autoScroll) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SubTextCursor unplot(int x, int y) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public boolean hasInput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SubTextCursor sendInput(String string) {
		host.accept(string);
		return this;
	}

	@Override
	public SubTextCursor write(CodePoint codePoint, Attributes attrs) {
		Attributes old = currentAttrs;
		attributes(attrs);
		write(codePoint.intValue());
		attributes(old);
		return this;
	}

	@Override
	public SubTextCursor write(CodePoint codePoint) {
		return write(codePoint.intValue());
	}

	public SubTextCursor write(int cp) {
		if (csiMode != 0) {
//			System.out.println("" + csiMode + ": " + cp);
			cp = addToCsiBuffer(cp);
			if (cp == 0)
				return this;
		} else if (cp == 27 && csiEnabled) {
//			System.out.println("0→1: " + cp);
			csiSeq.append((char) cp);
			csiMode = 1;
			return this;
		} else {
//			System.out.println("" + 0 + ": " + cp);
		}
		terminal.writeGlyph(CodePoint.stringValue(cp), currentStyle);
		return this;
	}

	private int addToCsiBuffer(int cp) {
		if (csiMode == 1) {
			switch (cp) {
			case '[':
				csiMode = 2;
				csiSeq.append('[');
				break;
			case 'c':
				csiMode = 0;
				csiSeq = new StringBuffer();
				resetFull();
				break;
			default:
				csiReset();
				return cp;
			}
		} else if (csiMode == 2) {
			if (CodePoint.isInRange(cp, 0x30, 0x3f)) {
				csiSeq.append((char) cp);
			} else if (CodePoint.isInRange(cp, 0x20, 0x2f)) {
				csiMode = 3;
				csiSeq.append((char) cp);
			} else if (CodePoint.isInRange(cp, 0x40, 0x7e)) {
				csiSeq.append((char) cp);
				csiFinish();
			} else {
				csiReset();
				return cp;
			}

		} else if (csiMode == 3) {
			if (CodePoint.isInRange(cp, 0x20, 0x2f)) {
				csiSeq.append((char) cp);
			} else if (CodePoint.isInRange(cp, 0x40, 0x7e)) {
				csiSeq.append((char) cp);
				csiFinish();
			} else {
				csiReset();
				return cp;
			}
		}
		return 0;
	}

	private void csiFinish() {
		String s = csiSeq.toString();
		// String s = new String(csiSeq.stream().mapToInt((i) ->
		// i.stringValue()).toArray(), 0, csiSeq.size());
		ControlSequences.applyCsi(this, s);
		csiReset();
	}

	private void csiReset() {
		csiMode = 0;
		csiSeq = new StringBuffer();
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public TextCursor end() {
		currentAttrs = stack.remove(0);
		currentStyle = currentAttrs.toCss();
		return this;
	}

	@Override
	public SubTextCursor flush() {
		return this;
	}

	public interface HTMLWriter {
		void writeGlyph(String glyph, String attrs);

		void move(int dx, int dy);

		void column(int x);

		void row(int y);
	}
	
	@Override
	public String id() {
		return id;
	}

}
