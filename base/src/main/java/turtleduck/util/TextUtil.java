package turtleduck.util;

public class TextUtil {
	public static final String[] prefixes = { "y", "z", "a", "f", "p", "n", "µ", "m", "", "k", "M", "G", "T", "P", "E",
			"Z", "Y" };
	public static final int NUM_PREFIXES = 8;

	public static String humanFriendlySI(double number) {
		double n = Math.abs(number);
		int i = NUM_PREFIXES;
		for (; n >= 1000 && i < prefixes.length - 1; i++) {
			n /= 1000.0;
			number /= 1000.0;
		}
		for (; n < 1 && i > 0; i--) {
			n *= 1000.0;
			number *= 1000.0;
		}

		return String.format("%.1f %s", number, prefixes[i]);
	}

	public static String humanFriendlyBinary(double number) {
		double n = Math.abs(number);
		int i = NUM_PREFIXES;
		for (; n >= 1000 && i < prefixes.length - 1; i++) {
			n /= 1024.0;
			number /= 1024.0;
		}

		return String.format("%.1f %s%s", number, prefixes[i], i != NUM_PREFIXES ? "i" : "");
	}
}
