package turtleduck;

import java.util.Arrays;

public class Debug {

	public static void println(Object... strings) {
		System.out.println(Arrays.toString(strings));
	}

	public static void print(Object... strings) {
		System.out.print(Arrays.toString(strings));

	}

	public static void printf(String format, Object... strings) {
		System.out.printf(format, strings);
	}

}
