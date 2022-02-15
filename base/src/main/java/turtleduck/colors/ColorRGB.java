package turtleduck.colors;

import turtleduck.colors.Colors.Gamma;

public class ColorRGB implements Color {
//	private final short r, g, b, a;
	private final float red;
	private final float green;
	private final float blue;
	private final float alpha;
	private final short sgr;

	/*
	 * protected ColorRGB(int r, int g, int b, int a) { this(r/255f, g/255f, b/255f,
	 * a/255f, false); }
	 */
	protected ColorRGB(float r, float g, float b, float a, boolean linear) {
		this(r, g, b, a, linear, -1);
	}

	protected ColorRGB(float r, float g, float b, float a, boolean linear, int sgr) {
		assert r >= 0.0 && r <= 1.0;
		assert g >= 0.0 && g <= 1.0;
		assert b >= 0.0 && b <= 1.0;
		assert a >= 0.0 && a <= 1.0;
//		linear = true;
		this.red = linear ? r : Colors.Gamma.gammaExpand(r);
		this.green = linear ? g : Colors.Gamma.gammaExpand(g);
		this.blue = linear ? b : Colors.Gamma.gammaExpand(b);
		this.alpha = a;
		this.sgr = (short) sgr;
	}

	public static ColorRGB hsv(float h, float s, float v, float a) {
		h = h % 360f;
		if (h < 0)
			h += 360f;
		s = Math.max(0, Math.min(1, s));
		v = Math.max(0, Math.min(1, v));

		float c = v * s;
		float h2 = h / 60f;
		float x = c * (1 - Math.abs(h2 % 2f - 1));
		float r1, g1, b1;
		if (h2 <= 1) {
			r1 = c;
			g1 = x;
			b1 = 0;
		} else if (h2 <= 2) {
			r1 = x;
			g1 = c;
			b1 = 0;
		} else if (h2 <= 3) {
			r1 = 0;
			g1 = c;
			b1 = x;
		} else if (h2 <= 4) {
			r1 = 0;
			g1 = x;
			b1 = c;
		} else if (h2 <= 5) {
			r1 = x;
			g1 = 0;
			b1 = c;
		} else if (h2 <= 6) {
			r1 = c;
			g1 = 0;
			b1 = x;
		} else {
			throw new IllegalStateException("this shouldn't happen, h=" + h + ", h2=" + h2);
		}
		float m = v - c;
		return new ColorRGB(r1 + m, g1 + m, b1 + m, a, true);

	}

	@Override
	public float red() {
		return red;
	}

	@Override
	public float green() {
		return green;
	}

	@Override
	public float blue() {
		return blue;
	}

	@Override
	public float opacity() {
		return alpha;
	}

	@Override
	public Color red(double r) {
		if (r < 0 || r > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ")");
		return new ColorRGB((float) r, green, blue, alpha, true);
	}

	@Override
	public Color green(double g) {
		if (g < 0 || g > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + g + ")");
		return new ColorRGB(red, (float) g, blue, alpha, true);
	}

	@Override
	public Color blue(double b) {
		if (b < 0 || b > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + b + ")");
		return new ColorRGB(red, green, (float) b, alpha, true);
	}

	@Override
	public Color opacity(double a) {
		if (a < 0 || a > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + a + ")");
		float f = (float) a;
		return new ColorRGB(red*f, green*f, blue*f, f, true);
	}

	@Override
	public Color mix(Color other, double proportion) {
		if (proportion <= 0)
			return this;
		else if (proportion >= 1.0)
			return other;
		else if (other instanceof ColorRGB) {
			ColorRGB o = (ColorRGB) other;
			float f = (float) proportion;
			return new ColorRGB(red + (o.red - red) * f, //
					green + (o.green - green) * f, //
					blue + (o.blue - blue) * f, //
					alpha + (o.alpha - alpha) * f, true);
		} else {
			return new ColorRGB((float) (red + (other.red() - red) * proportion), //
					(float) (green + (other.green() - green) * proportion), //
					(float) (blue + (other.blue() - blue) * proportion), //
					(float) (alpha + (other.opacity() - alpha) * proportion), true);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(alpha);
		result = prime * result + Float.floatToIntBits(blue);
		result = prime * result + Float.floatToIntBits(green);
		result = prime * result + Float.floatToIntBits(red);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ColorRGB)) {
			return false;
		}
		ColorRGB other = (ColorRGB) obj;
		if (Float.floatToIntBits(alpha) != Float.floatToIntBits(other.alpha)) {
			return false;
		}
		if (Float.floatToIntBits(blue) != Float.floatToIntBits(other.blue)) {
			return false;
		}
		if (Float.floatToIntBits(green) != Float.floatToIntBits(other.green)) {
			return false;
		}
		if (Float.floatToIntBits(red) != Float.floatToIntBits(other.red)) {
			return false;
		}
		return true;
	}

	@Override
	public Color brighter() {
		if (true)
			return new ColorRGB(.1f + .9f * red, .1f + .9f * green, .1f + .9f * blue, alpha, true);
		else {
			YCC ycc = new YCC(red, green, blue, alpha);
			ycc.Y = (1 + 9 * ycc.Y) / 10;
			return ycc.rgb();
		}
	}

	@Override
	public Color darker() {
		if (true)
			return new ColorRGB(.9f * red, .9f * green, .9f * blue, alpha, true);
		else {
			YCC ycc = new YCC(red, green, blue, alpha);
			ycc.Y = (0 + 9 * ycc.Y) / 10;
			return ycc.rgb();
		}
	}

	@Override
	public Color asRGBA() {
		return this;
	}

	@Override
	public Color asCMYK() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAdditive() {
		return true;
	}

	@Override
	public boolean isSubtractive() {
		return false;
	}

	protected static class YCC {
		float Y, Co, Cg, A;

		protected YCC(float red, float green, float blue, float a) {
			Co = red - blue;
			float tmp = blue + Co / 2;
			Cg = green - tmp;
			Y = tmp + Cg / 2;
			A = a;
		}

		protected ColorRGB rgb() {
			float tmp = Y - Cg / 2;
			float B = tmp - Co / 2;
			if (Cg + tmp < 0)
				System.out.println("oops!");
			return new ColorRGB(Math.max(0, B + Co), Math.max(0, Cg + tmp), Math.max(0, tmp - Co / 2), A, true);
		}
	}

	static class ColorProperties implements IColorProperties {
		final float M, m, C;
		final ColorRGB color;

		ColorProperties(ColorRGB c) {
			color = c;
			M = Math.max(Math.max(c.red, c.green), c.blue);
			m = Math.min(Math.min(c.red, c.green), c.blue);
			C = M - m;
		}

		@Override
		public double hue() {
			if (C == 0)
				return 0.0;
			else if (M == color.red)
				return 60.0 * (((color.green - color.blue) / C) % 6);
			else if (M == color.green)
				return 60.0 * (((color.blue - color.red) / C) + 2);
			else // M == color.blue
				return 60.0 * (((color.red - color.green) / C) + 4);
		}

		@Override
		public double intensity() {
			return (color.red + color.green + color.blue) / 3.0;
		}

		@Override
		public double value() {
			return M;
		}

		@Override
		public double lightness() {
			return (M + m) / 2.0;
		}

		@Override
		public double luma() {
			return 0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue;
		}

		@Override
		public double saturation() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double brightness() {
			// TODO Auto-generated method stub
			return 0;
		}
	}

	@Override
	public IColorProperties properties() {
		return new ColorProperties(this);
	}

	@Override
	public <T> T as(Class<T> type) {
		return null;
	}

	private float clamp(float f) {
		if (f < 0f)
			return 0f;
		else if (f > 1f)
			return 1f;
		else
			return f;
	}

	@Override
	public String toString() {
		int r = (int) Math.round(Gamma.gammaCompress(red) * 255);
		int g = (int) Math.round(Gamma.gammaCompress(green) * 255);
		int b = (int) Math.round(Gamma.gammaCompress(blue) * 255);
//		 r = (int) (red * 255);
//		 g = (int) (green * 255);
//		 b = (int) (blue * 255);
		 if (alpha == 1.0)
			return String.format("#%02X%02X%02X", r, g, b);
		else
			return String.format("#%02X%02X%02X%02X", r, g, b, Math.round(alpha * 255));
	}

	@Override
	public String toCss() {
		int r = (int) Math.round(Gamma.gammaCompress(red) * 255);
		int g = (int) Math.round(Gamma.gammaCompress(green) * 255);
		int b = (int) Math.round(Gamma.gammaCompress(blue) * 255);
		if (alpha == 1.0)
			return String.format("rgb(%d,%d,%d)", r, g, b);
		else
			return String.format("rgba(%d,%d,%d,%d)", r, g, b, Math.round(alpha * 255));
	}

	@Override
	public String toSGRParam(int i) {
		if (sgr < 0) {
			if (this == Colors.TRANSPARENT) {
				return String.format("%d;2;", i + 8);
			} else {
				int r = (int) Math.round(Gamma.gammaCompress(red) * 255);
				int g = (int) Math.round(Gamma.gammaCompress(green) * 255);
				int b = (int) Math.round(Gamma.gammaCompress(blue) * 255);
				return String.format("%d;2;%d;%d;%d", i + 8, r, g, b);
			}
		} else if (sgr < 8) {
			return String.valueOf(i + sgr);
		} else {
			return String.format("%d;5;%d", i + 8, sgr);
		}
	}

	@Override
	public String applyFg(String s) {
		return String.format("\u001b[%sm%s\u001b[0m", toSGRParam(30), s);
	}

	@Override
	public String applyBg(String s) {
		return String.format("\u001b[%sm%s\u001b[0m", toSGRParam(40), s);
	}

	@Override
	public Color perturb() {
		return perturb(.1);
	}

	@Override
	public Color perturb(double factor) {
		float r = (float) Math.random();
		float f = (float) factor;
		return new ColorRGB(red + (r - red) * f, //
				green + (r - green) * f, //
				blue + (r - blue) * f, //
				alpha, true);
	}

	@Override
	public int toARGB() {
		int r = (int) (Gamma.gammaCompress(red) * 255);
		int g = (int) (Gamma.gammaCompress(green) * 255);
		int b = (int) (Gamma.gammaCompress(blue) * 255);
		int a = (int) (alpha * 255);
		 r = (int) (red * 255);
		 g = (int) (green * 255);
		 b = (int) (blue * 255);
		return ((r & 0xff) << 16) | ((g & 0xff) << 8) | ((b & 0xff) << 0) | ((a & 0xff) << 24);
	}
}
