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

	public static String worstOf(String level1, String level2) {
		int l1 = levels.indexOf(level1);
		int l2 = levels.indexOf(level2);
		if(l2 == -1)
			return level1;
		else if(l1 == -1)
			return level2;
		else if(l1 <= l2)
			return level1;
		else
			return level2;
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
