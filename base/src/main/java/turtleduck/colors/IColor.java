package turtleduck.colors;

public interface IColor {
	enum ColorModel { GREY, RGB, CMYK, HSL, HSV, XYZ, PALETTE };
	/**
	 * @param rgb 0xRRGGBB
	 * @return
	 */
	static IColor fromRGB(int rgb) {
		return new ColorRGB(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f, (rgb & 0xff) / 255f, 1f);
	}
	/**
	 * @param argb 0xAARRGGBB
	 * @return
	 */
	static IColor fromARGB(int argb) {
		return new ColorRGB(((argb >> 16) & 0xff) / 255f, ((argb >> 8) & 0xff) / 255f, (argb & 0xff) / 255f, ((argb >> 24) & 0xff) / 255f);
	}
	/**
	 * @param bgr 0xBBGGRR
	 * @return
	 */
	static IColor fromBGR(int bgr) {
		return new ColorRGB((bgr & 0xff) / 255f, ((bgr >> 8) & 0xff) / 255f, ((bgr >> 24) & 0xff) / 255f, 1f);
	}
	/**
	 * @param bgra 0xBBGGRRAA
	 * @return
	 */
	static IColor fromBGRA(int bgra) {
		return new ColorRGB(((bgra >> 8) & 0xff) / 255f, ((bgra >> 16) & 0xff) / 255f, ((bgra >> 24) & 0xff) / 255f, (bgra & 0xff) / 255f);
	}
	static IColor fromRGB(int r, int g, int b) {
		return fromRGBA(r, g, b, 255);
	}
	static IColor fromRGBA(int r, int g, int b, int a) {
		if(r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 || a < 0 || a > 255)
			throw new IllegalArgumentException("Must be from 0 to 255: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB(r / 255f, g / 255f, b / 255f, a);
	}
	static IColor fromBytes(byte[] bytes, int offset, PixelFormat format) {
		return format.decode(bytes, offset);
	}
	static IColor color(double r, double g, double b) {
		return color(r, g, b, 1);
	}
	static IColor color(double r, double g, double b, double a) {
		if(r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1 || a < 0 || a > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB((float)r, (float)g, (float)b, (float)a);
	}
	
	double red();
	double green();
	double blue();
	double opacity();
	IColor red(double r);
	IColor green(double g);
	IColor blue(double b);
	IColor opacity(double a);
	IColor mix(IColor other, double proportion);
	
	IColorProperties properties();
	IColor brighter();
	IColor darker();
	
	IColor asRGBA();
	IColor asCMYK();
	boolean isAdditive();
	boolean isSubtractive();
	
	<T> T as(Class<T> type);
}
