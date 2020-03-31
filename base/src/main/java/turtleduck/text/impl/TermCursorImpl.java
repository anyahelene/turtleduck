package turtleduck.text.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import turtleduck.terminal.PseudoTerminal;
import turtleduck.text.Attribute;
import turtleduck.text.Attributes;
import turtleduck.text.AttributesImpl;
import turtleduck.text.CodePoint;
import turtleduck.text.SubTextCursor;
import turtleduck.text.TextCursor;
import turtleduck.text.TextWindow;

public class TermCursorImpl implements TextCursor, SubTextCursor {
	private static Attributes DEFAULT_ATTRS = new AttributesImpl();
	private Attributes currentAttrs = DEFAULT_ATTRS;
	private PseudoTerminal terminal;
	private List<Attributes> stack = new ArrayList<>();

	public TermCursorImpl(PseudoTerminal term) {
		terminal = term;
	}

	@Override
	public SubTextCursor begin() {
		if (!stack.isEmpty())
			throw new IllegalStateException("Only one subcursor nesting level supported");
		stack.add(currentAttrs);
		terminal.writeToTerminal("\u001b[s");
		return this;
	}

	@Override
	public SubTextCursor beginningOfLine() {
		terminal.writeToTerminal("\r");
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

	List<Attribute<?>> RELEVANT_ATTRS = List.of(Attribute.ATTR_FOREGROUND, Attribute.ATTR_BACKGROUND,
			Attribute.ATTR_BRIGHTNESS, Attribute.ATTR_STYLE, Attribute.ATTR_WEIGHT, Attribute.ATTR_UNDERLINE);

	private void setAttrs(Attributes attrs) {
		StringBuilder b = new StringBuilder();
		for (Attribute<?> attr : RELEVANT_ATTRS) {
			if (!attrs.get(attr).equals(currentAttrs.get(attr)))
				b.append(attrs.toCSI(attr));
		}
		if (b.length() != 0)
			terminal.writeToTerminal(b.toString());
		currentAttrs = attrs;
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
		if (dist > 0)
			terminal.writeToTerminal("\u001b[" + (dist) + "C");
		else if (dist < 0)
			terminal.writeToTerminal("\u001b[" + (-dist) + "D");
		return this;
	}

	@Override
	public SubTextCursor at(int newX, int newY) {
		terminal.writeToTerminal(String.format("\u001b[%d;%dH", newY, newX));
		return this;
	}

	@Override
	public SubTextCursor moveVert(int dist) {
		if (dist < 0)
			terminal.writeToTerminal("\u001b[" + (dist) + "A");
		else if (dist > 0)
			terminal.writeToTerminal("\u001b[" + (-dist) + "B");
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
		terminal.writeToTerminal(s);
		attributes(old);
		return this;
	}

	@Override
	public SubTextCursor redrawTextPage() {
		terminal.writeToTerminal("\f");
		return this;
	}

	@Override
	public SubTextCursor resetAttrs() {
		attributes(DEFAULT_ATTRS);
		return this;
	}

	@Override
	public SubTextCursor resetFull() {
		terminal.writeToTerminal("\u001b[!P");
		return this;
	}

	@Override
	public SubTextCursor scroll(int lines) {
		if (lines < 0)
			terminal.writeToTerminal(String.format("\u001b[%dS", lines));
		else if (lines > 0)
			terminal.writeToTerminal(String.format("\u001b[%dT", -lines));

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
		terminal.writeToHost(string);
		return this;
	}

	@Override
	public SubTextCursor write(CodePoint codePoint, Attributes attrs) {
		print(codePoint.stringValue(), attrs);
		return this;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public TextCursor end() {
		terminal.writeToTerminal("\u001b[u");
		currentAttrs = stack.remove(0);
		return this;
	}

}
