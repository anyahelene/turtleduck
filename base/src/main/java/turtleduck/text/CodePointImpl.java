package turtleduck.text;

import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class CodePointImpl implements CodePoint {
	protected static final CodePoint LATIN1[] = makeBlock(0);
	protected static final Map<Integer, CodePoint[]> BLOCKS = new HashMap<>();
	private final int value;
	private final String string;

	private CodePointImpl(int codePoint) {
		if (!Character.isValidCodePoint(codePoint))
			throw new IllegalArgumentException("Invalid code point: " + codePoint);
		value = codePoint;
		if (codePoint < 65536)
			string = Character.toString((char) value);
		else
			string = "";
	}

	@Override
	public int value() {
		return value;
	}

	public String stringValue() {
		return string;
	}

	public UnicodeBlock codeBlock() {
		return UnicodeBlock.of(value);
	}

	public UnicodeScript script() {
		return UnicodeScript.of(value);
	}

	public String toString() {
		return string;
	}

	protected static CodePoint[] makeBlock(int first) {
		CodePoint[] block = new CodePoint[256];
		for (int i = 0; i < 256; i++)
			block[i] = new CodePointImpl(i);
		return block;
//		return IntStream.range(first, first + 255)//
//				.mapToObj(i -> new CodePointImpl(i)).toArray(l -> new CodePoint[l]);
	}

	@Override
	public String toHtml() {
		if (value == '&')
			return "&amp;";
		else if (value == '<')
			return "&lt;";
		else if (value == '>')
			return "&gt;";
		else if (Character.isWhitespace(value) || !Character.isISOControl(value)) {
			return string;
		} else {
			return String.format("&#%x;", value);
		}
	}

}
