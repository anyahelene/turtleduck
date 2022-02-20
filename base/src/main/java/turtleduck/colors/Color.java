package turtleduck.colors;

import turtleduck.annotations.Icon;

@Icon("🎨")
public interface Color {
	enum ColorModel {
		GREY, RGB, CMYK, HSL, HSV, XYZ, PALETTE
	};

	/**
	 * @param rgb 0xRRGGBB
	 * @return
	 */
	static Color fromRGB(int rgb) {
		return new ColorRGB(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f, (rgb & 0xff) / 255f, 1f, false);
	}

	/**
	 * @param argb 0xAARRGGBB
	 * @return
	 */
	static Color fromARGB(int argb) {
		return new ColorRGB(((argb >> 16) & 0xff) / 255f, ((argb >> 8) & 0xff) / 255f, (argb & 0xff) / 255f,
				((argb >> 24) & 0xff) / 255f, false);
	}

	/**
	 * @param argb 0xRRGGBBAA
	 * @return
	 */
	static Color fromRGBA(int argb) {
		return new ColorRGB(((argb >> 24) & 0xff) / 255f, ((argb >> 16) & 0xff) / 255f, ((argb >> 8) & 0xff) / 255f,
				((argb >> 0) & 0xff) / 255f, false);
	}

	static Color fromString(String s) {
		if (s.startsWith("#")) {
			if (s.length() == 7)
				return Color.fromRGB(Integer.valueOf(s.substring(1), 16));
			else if (s.length() == 9)
				return Color.fromRGBA(Integer.valueOf(s.substring(1), 16));

		}

		throw new IllegalArgumentException("not a color: " + s);
	}

	/**
	 * @param bgr 0xBBGGRR
	 * @return
	 */
	static Color fromBGR(int bgr) {
		return new ColorRGB((bgr & 0xff) / 255f, ((bgr >> 8) & 0xff) / 255f, ((bgr >> 24) & 0xff) / 255f, 1f, false);
	}

	/**
	 * @param bgra 0xBBGGRRAA
	 * @return
	 */
	static Color fromBGRA(int bgra) {
		return new ColorRGB(((bgra >> 8) & 0xff) / 255f, ((bgra >> 16) & 0xff) / 255f, ((bgra >> 24) & 0xff) / 255f,
				(bgra & 0xff) / 255f, false);
	}

	static Color fromRGB(int r, int g, int b) {
		return fromRGBA(r, g, b, 255);
	}

	static Color fromRGB(byte r, byte g, byte b) {
		return fromRGBA(r & 0xff, g & 0xff, b & 0xff, 255);
	}

	static Color fromRGBA(byte r, byte g, byte b, byte a) {
		return fromRGBA(r & 0xff, g & 0xff, b & 0xff, a & 0xff);
	}

	static Color fromRGBA(int r, int g, int b, int a) {
		if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255 || a < 0 || a > 255)
			throw new IllegalArgumentException("Must be from 0 to 255: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB(r / 255f, g / 255f, b / 255f, a / 255f, false);
	}

	static Color fromBytes(byte[] bytes, int offset, PixelFormat format) {
		return format.decode(bytes, offset);
	}

	static Color color(double r, double g, double b) {
		return color(r, g, b, 1);
	}

	/**
	 * Create a new grey color
	 * 
	 * Equivalent to color(g, g, g, 1).
	 * 
	 * @param g The grey component (linear)
	 * @return
	 */
	static Color grey(double g) {
		return color(g, g, g, 1);
	}

	/**
	 * Create a new color, from hue/saturation/value
	 * 
	 * @param h Hue, in degrees, 0–360
	 * @param s Saturation, 0–1
	 * @param v Value, 0–1
	 * @return A color
	 */
	static Color hsv(double h, double s, double v) {
		return ColorRGB.hsv((float) h, (float) s, (float) v, 1);
	}

	/**
	 * Create a new color, from hue/saturation/value
	 * 
	 * @param h Hue, in degrees, 0–360
	 * @param s Saturation, 0–1
	 * @param v Value, 0–1
	 * @param a Alpha/opacity, 0–1
	 * @return A color
	 */
	static Color hsva(double h, double s, double v, double a) {
		return ColorRGB.hsv((float) h, (float) s, (float) v, (float) a);
	}

	/**
	 * Create a new color, from linear floating-point components.
	 * 
	 * Use {@link #sRGB(double, double, double, double)} if your color components is
	 * in sRGB (compressed) color space.
	 * 
	 * @param r Red component, 0.0–1.0
	 * @param g Green component, 0.0–1.0
	 * @param b Blue component, 0.0–1.0
	 * @param a Alpha component 0.0–1.0
	 * @return A color
	 */
	static Color color(double r, double g, double b, double a) {
		if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1 || a < 0 || a > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB((float) r, (float) g, (float) b, (float) a, true);
	}

	/**
	 * Create a new color, from sRGB floating-point components.
	 * 
	 * Use {@link #color(double, double, double, double)} if your color components
	 * is in linear color space.
	 * 
	 * @param r Red component, 0.0–1.0
	 * @param g Green component, 0.0–1.0
	 * @param b Blue component, 0.0–1.0
	 * @param a Alpha component 0.0–1.0
	 * @return A color
	 */
	static Color sRGB(double r, double g, double b, double a) {
		if (r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1 || a < 0 || a > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ", " + g + ", " + b + ", " + a + ")");
		return new ColorRGB((float) r, (float) g, (float) b, (float) a, false);
	}

	float red();

	float green();

	float blue();

	float opacity();

	Color red(double r);

	Color green(double g);

	Color blue(double b);

	Color opacity(double a);

	Color mix(Color other, double proportion);

	IColorProperties properties();

	Color brighter();

	Color darker();

	Color asRGBA();

	Color asCMYK();

	Color perturb();

	Color perturb(double factor);

	boolean isAdditive();

	boolean isSubtractive();

	<T> T as(Class<T> type);

	String toCss();

	String toSGRParam(int i);

	/**
	 * Apply this colour to a string as text foreground, using Select Graphic
	 * Rendition control sequences
	 * 
	 * @param s A string
	 * @return The string, with control sequences applied
	 */
	String applyFg(String s);

	/**
	 * Apply this colour to a string as text background, using Select Graphic
	 * Rendition control sequences
	 * 
	 * @param s A string
	 * @return The string, with control sequences applied
	 */
	String applyBg(String s);

	int toARGB();

	Color writeTo(short[] data, int offset);

}
