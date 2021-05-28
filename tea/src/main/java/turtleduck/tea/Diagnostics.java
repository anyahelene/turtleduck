package turtleduck.tea;

import java.util.Arrays;
import java.util.List;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;

public class Diagnostics {
	private static List<String> levels = Arrays.asList("error", "warning", "info", "debug");

	public static String levelOf(String name) {
		if (levels.contains(name))
			return name;
		else
			return "error";
	}

	public static Color colorOf(String level) {
		switch (level) {
		case "debug":
			return Colors.LIGHT_BLUE;
		case "info":
			return Colors.BLUE;
		case "warning":
			return Colors.OLIVE;
		case "error":
		default:
			return Colors.MAROON;
		}
	}

}
