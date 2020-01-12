package turtleduck.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FontWeight {
	THIN(null, 100, "hairline"), EXTRALIGHT(null, 200, "ultralight"), LIGHT(null, 300),
	NORMAL("normal", 400, "regular"), //
	MEDIUM(null, 500), SEMIBOLD(null, 600, "demibold"), BOLD("bold", 700), EXTRABOLD(null, 800, "ultrabold"), //
	BLACK(null, 900, "heavy"), EXTRA_BLACK(null, 950, "ultrablack");

	private final int weight;
	private final String css;

	private FontWeight(String s, int w, String... as) {
		weight = w;
		if (s != null)
			css = s;
		else
			css = String.valueOf(w);
		for (String a : as)
			Dummy.MAP.put(s, this);
		Dummy.MAP.put(name().toLowerCase().replace("_", "-"), this);
	}

	public FontWeight forName(String name) {
		return Dummy.MAP.get(name.toLowerCase().replace("[_-]", ""));
	}

	public String toString() {
		return name();
	}

	public String toCss() {
		return css;
	}

	/**
	 * Get around enum initialization restriction
	 *
	 */
	protected static class Dummy {
		protected static final Map<String, FontWeight> MAP = new HashMap<>();
	}

}
