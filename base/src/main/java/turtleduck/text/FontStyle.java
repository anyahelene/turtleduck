package turtleduck.text;

public enum FontStyle {
	NORMAL, OBLIQUE, ITALIC, SCRIPT;
	
	public String toCss() {
		switch(this) {
		case NORMAL:
			return "normal";
		case ITALIC:
		case SCRIPT:
			return "italic";
		case OBLIQUE:
			return "oblique";
		}
		throw new IllegalStateException();
	}
}
