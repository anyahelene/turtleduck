package turtleduck.terminal;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import turtleduck.text.CodePoint;
import turtleduck.text.ControlSequences;
import turtleduck.text.TextCursor;

public class TerminalPrintStream extends PrintStreamWrapper {
	private final TextCursor dest;
	private final ByteBuffer bytes = ByteBuffer.allocate(4);
	private int bytesExpected = 0;
	private int utf8cp = 0;
	private List<Integer> csiSeq = new ArrayList<>();
	private int csiMode = 0;
	private boolean csiEnabled;

	public TerminalPrintStream(TextCursor destination, boolean csiEnabled) {
		this.dest = destination;
		this.csiEnabled = csiEnabled;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	protected synchronized void writeByte(int b) {
		if (bytesExpected == 0) { // start of new char
			if (b < 0b10000000) { // single byte
				writeString(String.valueOf((char) b));
				return;
			} else if (b < 0b11000000) { // continuation byte
				utf8Error(b);
				return;
			} else if (b < 0b11100000) { // two-byte sequence
				if (b == 0xc0 || b == 0xc1) { // 0xC0 and 0xC1 are illegal
					utf8Error(b);
					return;
				}
				utf8cp = b & 0x1f;
				bytesExpected = 1;
			} else if (b < 0b11110000) { // three-byte sequence
				utf8cp = b & 0x0f;
				bytesExpected = 2;
			} else if (b < 0xf5) { // four-byte sequence
				utf8cp = b & 0x07;
				bytesExpected = 3;
			} else {
				utf8Error(b);
				return;
			}
		} else if ((b & 0b11000000) != 0b10000000) { // not a continuation byte
			utf8Error();
			return;
		} else if (bytesExpected == 1) { // last byte in sequence
			utf8cp = (utf8cp << 6) | (b & 0x3f);
			bytes.put((byte) b);
			flushUtf8();
			bytesExpected = 0;
			return;
		} else {
			utf8cp = (utf8cp << 6) | (b & 0x3f);
			bytesExpected--;
		}
		System.err.println(bytes.toString() + " " + bytesExpected);
		bytes.put((byte) b);
	}

	private void flushUtf8() {
		bytes.flip();
		writeCodepoint(utf8cp);
//		String s = Charset.forName("UTF-8").decode(bytes).toString();
//		String t = Character.toString(utf8cp);
//		assert s.equals(t);
//		System.out.printf("'%s' = '%s': %s\n", s, t, s.equals(t));
//		writeCodepoint(CodePoint.codePoint(utf8cp));
		bytes.clear();
	}

	private void utf8Error() {
		bytesExpected = 0;
		bytes.clear();
		writeCodepoint(CodePoint.CodePoints.REPLACEMENT_CHARACTER.intValue());
	}

	private void writeCodepoint(int cp) {
		if (csiMode != 0) {
			System.out.println("" + csiMode + ": " + cp);
			cp = addToCsiBuffer(cp);
			if (cp != 0)
				dest.write(CodePoint.codePoint(cp));
		} else if (cp == 27 && csiEnabled) {
			System.out.println("0→1: " + cp);
			csiSeq.add(cp);
			csiMode = 1;
		} else {
			System.out.println("" + 0 + ": " + cp);
			dest.write(CodePoint.codePoint(cp));
		}
	}

	private void utf8Error(int b) {
		utf8Error();
	}

	@Override
	protected void writeString(String s) {
		// avoid String::codepoints() in order to be compatible with TeaVM
		char first = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= 0xd800 && c <= 0xdbff) { // high surrogate
				first = c;
			} else if (c >= 0xdc00 && c <= 0xdfff) { // low surrogate
				if (first != 0) {
					writeCodepoint(Character.toCodePoint(first, c));
					System.out
							.println("" + i + ": " + ((int) c) + CodePoint.codePoint(Character.toCodePoint(first, c)));
				}
			} else {
				writeCodepoint(c);
				System.out.println("" + i + ": " + ((int) c) + ", " + CodePoint.codePoint(c));
			}
		}
	}

	private int addToCsiBuffer(int cp) {
		if (csiMode == 1) {
			switch (cp) {
			case '[':
				csiMode = 2;
				csiSeq.add(cp);
				break;
			case 'c':
				csiMode = 0;
				csiSeq.clear();
				dest.resetFull();
				break;
			default:
				csiReset();
				return cp;
			}
		} else if (csiMode == 2) {
			if (CodePoint.isInRange(cp, 0x30, 0x3f)) {
				csiSeq.add(cp);
			} else if (CodePoint.isInRange(cp, 0x20, 0x2f)) {
				csiMode = 3;
				csiSeq.add(cp);
			} else if (CodePoint.isInRange(cp, 0x40, 0x7e)) {
				csiSeq.add(cp);
				csiFinish();
			} else {
				csiReset();
				return cp;
			}

		} else if (csiMode == 3) {
			if (CodePoint.isInRange(cp, 0x20, 0x2f)) {
				csiSeq.add(cp);
			} else if (CodePoint.isInRange(cp, 0x40, 0x7e)) {
				csiSeq.add(cp);
				csiFinish();
			} else {
				csiReset();
				return cp;
			}
		}
		return 0;
	}

	private void csiFinish() {
		String s = csiSeq.stream().map(cp -> CodePoint.stringValue(cp)).collect(Collectors.joining());
		// String s = new String(csiSeq.stream().mapToInt((i) ->
		// i.stringValue()).toArray(), 0, csiSeq.size());
		ControlSequences.applyCsi(dest, s);
		csiReset();
	}

	private void csiReset() {
		csiMode = 0;
		csiSeq.clear();
		;
	}

}
