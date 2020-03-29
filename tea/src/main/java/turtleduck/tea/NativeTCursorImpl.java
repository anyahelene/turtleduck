package turtleduck.tea;

import turtleduck.text.Attributes;
import turtleduck.text.CodePoint;
import turtleduck.text.TextWindow;
import turtleduck.text.impl.CursorImpl;

public class NativeTCursorImpl extends CursorImpl {
	public NativeTCursorImpl(CursorImpl cursor) {
		super(cursor);
	}

	public NativeTCursorImpl(TextWindow page) {
		super(page);
	}
	

	@Override
	public CursorImpl print(String s, Attributes attrs) {
		for(char c : s.toCharArray()) {
			write(CodePoint.codePoint(c), attrs);
		}
		return this;
	}

}
