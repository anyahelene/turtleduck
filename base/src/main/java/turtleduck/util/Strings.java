package turtleduck.util;

public class Strings {
	public static String escape(String s) {
		StringBuffer b = new StringBuffer(s.length());
//		s.codePoints().forEach((cp) -> {
		for(int i = 0; i < s.length(); i++) {
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
}
