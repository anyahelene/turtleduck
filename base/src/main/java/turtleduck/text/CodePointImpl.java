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
		string = Character.toString(value());
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
		return IntStream.range(first, first + 255)//
				.mapToObj(i -> new CodePointImpl(i)).toArray(l -> new CodePoint[l]);
	}

}
