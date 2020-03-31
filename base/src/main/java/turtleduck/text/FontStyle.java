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
	
	public int toSGRParam() {
		switch(this) {
		case NORMAL:
			return 23;
		case ITALIC:
		case SCRIPT:
		case OBLIQUE:
			return 3;
		}
		throw new IllegalStateException();		
	}
}
