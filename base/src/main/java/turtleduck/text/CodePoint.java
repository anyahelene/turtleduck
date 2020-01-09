package turtleduck.text;

import java.util.stream.Stream;

public interface CodePoint {
	public static class CodePoints {
		public static final CodePoint NUL = codePoint(0x00);
		public static final CodePoint SPACE = codePoint(0x20);
		public static final CodePoint ESC = codePoint(0x1b);
		public static final CodePoint REPLACEMENT_CHARACTER = codePoint(0xfffd);
		public static CodePoint combine(CodePoint cp, CodePoint cp2) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static CodePoint codePoint(int codePoint) {
		if (codePoint < 0)
			throw new IllegalArgumentException("Invalid negative code point: " + codePoint);
		else if (codePoint < 256)
			return CodePointImpl.LATIN1[codePoint];
		else if (Character.isValidCodePoint(codePoint)) {
			int block = codePoint & 0x10ff00;
			int c = codePoint & 0xff;
			return CodePointImpl.BLOCKS.computeIfAbsent(block, i -> CodePointImpl.makeBlock(i))[c];
		} else {
			throw new IllegalArgumentException("Invalid code point: " + codePoint);
		}
	}

	public static Stream<CodePoint> stream(String string) {
		return string.codePoints().mapToObj(i -> codePoint(i));
	}

	int value();

	default int intValue() {
		return Character.getNumericValue(value());
	}

	default int digitValue(int radix) {
		return Character.digit(value(), radix);
	}

	default String stringValue() {
		return Character.toString(value());
	}

	default boolean isAscii() {
		return value() >= 0 && value() < 128;
	}

	default boolean isValidCodePoint() {
		return Character.isValidCodePoint(value());
	}

	default boolean isBmpCodePoint() {
		return Character.isBmpCodePoint(value());
	}

	default boolean isSupplementaryCodePoint() {
		return Character.isSupplementaryCodePoint(value());
	}

	default boolean isLowerCase() {
		return Character.isLowerCase(value());
	}

	default boolean isUpperCase() {
		return Character.isUpperCase(value());
	}

	default boolean isTitleCase() {
		return Character.isTitleCase(value());
	}

	default boolean isDigit() {
		return Character.isDigit(value());
	}

	default boolean isDefined() {
		return Character.isDefined(value());
	}

	default boolean isLetter() {
		return Character.isLetter(value());
	}

	default boolean isLetterOrDigit() {
		return Character.isLetterOrDigit(value());
	}

	default boolean isAlphabetic() {
		return Character.isAlphabetic(value());
	}

	default boolean isIdeographic() {
		return Character.isIdeographic(value());
	}

	default boolean isJavaIdentifierStart() {
		return Character.isJavaIdentifierStart(value());
	}

	default boolean isJavaIdentifierPart() {
		return Character.isJavaIdentifierPart(value());
	}

	default boolean isUnicodeIdentifierStart() {
		return Character.isUnicodeIdentifierStart(value());
	}

	default boolean isUnicodeIdentifierPart() {
		return Character.isUnicodeIdentifierPart(value());
	}

	default boolean isIdentifierIgnorable() {
		return Character.isIdentifierIgnorable(value());
	}

	default boolean isSpaceChar() {
		return Character.isSpaceChar(value());
	}

	default boolean isWhitespace() {
		return Character.isWhitespace(value());
	}

	default boolean isISOControl() {
		return Character.isISOControl(value());
	}

	default boolean isMirrored() {
		return Character.isMirrored(value());
	}

	/**
	 * Check if the code point is in the given range (inclusive)
	 * 
	 * @param low  Low end of range (inclusive)
	 * @param high High end of range (inclusive)
	 * @return true iff low <= codepoint <= high
	 */
	default boolean isInRange(int low, int high) {
		return value() >= low && value() <= high;
	}

	String toHtml();
}
