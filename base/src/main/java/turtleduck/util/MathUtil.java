package turtleduck.util;

public class MathUtil {
	public static int gcd(int a, int b) {
		return (a == 0 || b == 0) ? Math.abs(a + b) : gcd(b, a % b);
	
	}
}
