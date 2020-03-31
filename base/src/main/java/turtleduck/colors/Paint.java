package turtleduck.colors;

public interface Paint  {
	enum ColorModel { GREY, RGB, CMYK, HSL, HSV, XYZ, PALETTE };
	/**
	 * @param rgb 0xRRGGBB
	 * @return
	 */
	static Paint fromRGB(int rgb) {
		return new ColorRGB(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f, (rgb & 0xff) / 255f, 1f);
	}
	/**
	 * @param argb 0xAARRGGBB
	 * @return
	 */
	static Paint fromARGB(int argb) {
		return new ColorRGB(((argb >> 16) & 0xff) / 255f, ((argb >> 8) & 0xff) / 255f, (argb & 0xff) / 255f, ((argb >> 24) & 0xff) / 255f);
	}
	/**
	 * @param bgr 0xBBGGRR
	 * @return
	 */
	static Paint fromBGR(int bgr) {
		return new ColorRGB((bgr & 0xff) / 255f, ((bgr >> 8) & 0xff) / 255f, ((bgr >> 24) & 0xff) / 255f, 1f);
	}
	/**
	 * @param bgra 0xBBGGRRAA
	 * @return
	 */
	static Paint fromBGRA(int bgra) {
		return new ColorRGB(((bgra >> 8) & 0xff) / 255f, ((bgra >> 16) & 0xff) / 255f, ((bgra >> 24) & 0xff) / 255f, (bgra & 0xff) / 255f);
	}
	static Paint fromRGB(int r, int g, int b) {
		return fromRGBA(r, g, b, 255);
	}
	static Paint fromRGBA(int r, int g, int b, int a) {
		if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 || a < 0 || a > 255)
			throw new IllegalArgumentException("Must be from 0 to 255: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB(r / 255f, g / 255f, b / 255f, a);
	}
	static Paint fromBytes(byte[] bytes, int offset, PixelFormat format) {
		return format.decode(bytes, offset);
	}
	static Paint color(double r, double g, double b) {
		return color(r, g, b, 1);
	}
	static Paint grey(double g) {
		return color(g, g, g, 1);
	}
	static Paint color(double r, double g, double b, double a) {
		if(r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1 || a < 0 || a > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB((float)r, (float)g, (float)b, (float)a);
	}
	
	double red();
	double green();
	double blue();
	double opacity();
	Paint red(double r);
	Paint green(double g);
	Paint blue(double b);
	Paint opacity(double a);
	Paint mix(Paint other, double proportion);
	
	IColorProperties properties();
	Paint brighter();
	Paint darker();
	
	Paint asRGBA();
	Paint asCMYK();
	boolean isAdditive();
	boolean isSubtractive();
	
	<T> T as(Class<T> type);
	String toCss();
	String toSGRParam(int i);
}
