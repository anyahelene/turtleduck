package turtleduck.util;

public class Strings {
	public static String termEscape(String s) {
		StringBuffer b = new StringBuffer(s.length());
//		s.codePoints().forEach((cp) -> {
		for (int i = 0; i < s.length(); i++) {
			char cp = s.charAt(i);
			if (cp < 0x20) {
				b.append('^');
				b.appendCodePoint(cp + 0x40);
			} else if (cp == 0x7f) {
				b.append("^?");
			} else if (Character.getType(cp) == Character.FORMAT) {
				if (cp < 0x10000)
					b.append(String.format("\\u%04x", cp));
				else
					b.append(String.format("\\U%08x", cp));
			} else {
				b.appendCodePoint(cp);
			}
		}
		return b.toString();
	}

	public static String jsonEscape(String s) {
		StringBuffer b = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); i++) {
			char cp = s.charAt(i);
			if (cp == 0x08) {
				b.append("\\b");
			} else if (cp == 0x09) {
				b.append("\\t");
			} else if (cp == 0x0a) {
				b.append("\\n");
			} else if (cp == 0x0c) {
				b.append("\\f");
			} else if (cp == 0x0d) {
				b.append("\\r");
			} else if (cp == 0x12) {
				b.append("\\f");
			} else if (cp == '"') {
				b.append("\\\"");
			} else if (cp == '\\') {
				b.append("\\\\");
			} else if (cp == 0x7f) {
				b.append("\\u007f");
			} else if (Character.getType(cp) == Character.FORMAT || Character.getType(cp) == Character.CONTROL) {
				b.append(String.format("\\u%04x", (int)cp));
			} else {
				b.append((char)cp);
			}
		}

		return b.toString();
	}
}
