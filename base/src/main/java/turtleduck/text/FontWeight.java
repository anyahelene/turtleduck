package turtleduck.text;

import java.util.HashMap;
import java.util.Map;

public enum FontWeight {
	THIN(null, 100, 2, "hairline"), EXTRALIGHT(null, 200, 2, "ultralight"), LIGHT(null, 300, 2),
	NORMAL("normal", 400, 22, "regular"), //
	MEDIUM(null, 500, 22), SEMIBOLD(null, 600, 1, "demibold"), BOLD("bold", 700, 1), EXTRABOLD(null, 800, 1, "ultrabold"), //
	BLACK(null, 900, 1, "heavy"), EXTRA_BLACK(null, 950, 1, "ultrablack");

	private final int weight;
	private final String css;
	private final int sgrParam;

	private FontWeight(String s, int w, int sgr, String... as) {
		weight = w;
		sgrParam =sgr; 
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

	public int toSGRParam() {
		return sgrParam;
	}
	/**
	 * Get around enum initialization restriction
	 *
	 */
	protected static class Dummy {
		protected static final Map<String, FontWeight> MAP = new HashMap<>();
	}

}
