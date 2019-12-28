package turtleduck.colors;

public class ColorRGB implements Paint {
	private final float red;
	private final float green;
	private final float blue;
	private final float alpha;

	protected ColorRGB(float r, float g, float b, float a) {
		assert r >= 0.0 && r <= 1.0;
		assert g >= 0.0 && g <= 1.0;
		assert b >= 0.0 && b <= 1.0;
		assert a >= 0.0 && a <= 1.0;
		this.red = r;
		this.green = g;
		this.blue = b;
		this.alpha = a;
	}

	@Override
	public double red() {
		return red;
	}

	@Override
	public double green() {
		return green;
	}

	@Override
	public double blue() {
		return blue;
	}

	@Override
	public double opacity() {
		return alpha;
	}

	@Override
	public Paint red(double r) {
		if (r < 0 || r > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + r + ")");
		return new ColorRGB((float) r, green, blue, alpha);
	}

	@Override
	public Paint green(double g) {
		if (g < 0 || g > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + g + ")");
		return new ColorRGB(red, (float) g, blue, alpha);
	}

	@Override
	public Paint blue(double b) {
		if (b < 0 || b > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + b + ")");
		return new ColorRGB(red, green, (float) b, alpha);
	}

	@Override
	public Paint opacity(double a) {
		if (a < 0 || a > 1)
			throw new IllegalArgumentException("Must be from 0.0 to 1.0: (" + a + ")");
		return new ColorRGB(red, green, blue, (float) a);
	}

	@Override
	public Paint mix(Paint other, double proportion) {
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
					alpha + (o.alpha - alpha) * f);
		} else {
			return new ColorRGB((float) (red + (other.red() - red) * proportion), //
					(float) (green + (other.green() - green) * proportion), //
					(float) (blue + (other.blue() - blue) * proportion), //
					(float) (alpha + (other.opacity() - alpha) * proportion));
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
	public Paint brighter() {
		if (true)
			return new ColorRGB(.1f + .9f * red, .1f + .9f * green, .1f + .9f * blue, alpha);
		else {
			YCC ycc = new YCC(red, green, blue, alpha);
			ycc.Y = (1 + 9 * ycc.Y) / 10;
			return ycc.rgb();
		}
	}

	@Override
	public Paint darker() {
		if (true)
			return new ColorRGB(.9f * red, .9f * green, .9f * blue, alpha);
		else {
			YCC ycc = new YCC(red, green, blue, alpha);
			ycc.Y = (0 + 9 * ycc.Y) / 10;
			return ycc.rgb();
		}
	}

	@Override
	public Paint asRGBA() {
		return this;
	}

	@Override
	public Paint asCMYK() {
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
			if(Cg + tmp < 0) 
				System.out.println("oops!");
			return new ColorRGB(Math.max(0, B + Co), Math.max(0, Cg + tmp), Math.max(0, tmp - Co / 2), A);
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
}
