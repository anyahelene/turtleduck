package turtleduck.colors;

import java.util.stream.IntStream;

import turtleduck.text.ControlSequences;

public class Colors {
	public static final Color TRANSPARENT = new ColorRGB(0, 0, 0, 0, true);
	public static final Color WHITE = new ColorRGB(1, 1, 1, 1, true, 15);
	public static final Color GRAY = new ColorRGB(.5f, .5f, .5f, 1, true, 8);
	public static final Color GREY = GRAY;
	public static final Color BLACK = new ColorRGB(0, 0, 0, 1, true, 0);
	public static final Color SILVER = new ColorRGB(.5f, .5f, .5f, 1, true, 7);

	public static final Color RED = new ColorRGB(1, 0, 0, 1, true, 9);
	public static final Color MAROON = new ColorRGB(.5f, 0, 0, 1, true, 1);
	public static final Color PINK = new ColorRGB(1, .5f, .5f, 1, true, 210);

	public static final Color BROWN = new ColorRGB(1, .165f, .165f, 1, true, 210);

	public static final Color LIME = new ColorRGB(0, 1, 0, 1, true, 10);
	public static final Color GREEN = new ColorRGB(0, .5f, 0, 1, true, 2);
	public static final Color FORESTGREEN = new ColorRGB(.3f, 1, 0, 1, true, 82);
	public static final Color LIGHT_GREEN = new ColorRGB(.5f, .5f, 1, 1, true, 120);

	public static final Color BLUE = new ColorRGB(0, 0, 1, 1, true, 12);
	public static final Color NAVY = new ColorRGB(0, 0, .5f, 1, true, 4);
	public static final Color LIGHT_BLUE = new ColorRGB(.5f, .5f, 1, 1, true, 105);

	public static final Color YELLOW = new ColorRGB(1, 1, 0, 1, true, 11);
	public static final Color OLIVE = new ColorRGB(.5f, .5f, 0, 1, true, 3);

	public static final Color MAGENTA = new ColorRGB(1, 0, 1, 1, true, 13);
	public static final Color PURPLE = new ColorRGB(.5f, 0, .5f, 1, true, 5);

	public static final Color CYAN = new ColorRGB(0, 1, 1, 1, true, 14);
	public static final Color TEAL = new ColorRGB(0, .5f, .5f, 1, true, 6);

	/**
	 * Convert colours to/from gamma-compressed form, using the sRGB transfer
	 * function
	 * 
	 * <em>Gamma-compressed</em> colour values are appropriate when using 8-bit
	 * colour components (0–255), for example in the usual hex notation
	 * (<code>#aabbcc</code>).
	 * 
	 * @author anya
	 * @see {@link https://en.wikipedia.org/wiki/SRGB#The_sRGB_transfer_function_(%22gamma%22)
	 *      sRGB gamma transfer function}
	 */
	public static class Gamma {
		public static final short LINEAR_MAX = 4095, COMPRESSED_MAX = 255;
		protected static final double LINEAR_MAX_D = (double) LINEAR_MAX;
		protected static final double COMPRESSED_MAX_D = (double) COMPRESSED_MAX;
		public static boolean USE_TABLE = false;
		private static int INV_GAMMA_TABLE[];
		private static int GAMMA_TABLE[];

		public static double gammaExpand(double srgbComponent) {
			srgbComponent = Math.max(0, Math.min(srgbComponent, 1.0));
			return srgbExpand(srgbComponent);
		}

		protected static double srgbExpand(double srgbComponent) {
			if (srgbComponent <= 0.04045)
				return srgbComponent / 12.92;
			else
				return Math.pow((srgbComponent + 0.055) / 1.055, 2.4);
		}

		public static float gammaExpand(float srgbComponent) {
			srgbComponent = Math.max(0, Math.min(srgbComponent, 1.0f));
			return srgbExpand(srgbComponent);
		}

		protected static float srgbExpand(float srgbComponent) {
			if (srgbComponent <= 0.04045f)
				return srgbComponent / 12.92f;
			else
				return (float) Math.pow((srgbComponent + 0.055) / 1.055, 2.4);
		}

		public static double gammaCompress(double linearComponent) {
			return Math.max(0, Math.min(1.0, srgbCompress(linearComponent)));
		}

		protected static double srgbCompress(double linearComponent) {
			if (linearComponent <= 0.0031308)
				return linearComponent * 12.92;
			else
				return 1.055 * Math.pow(linearComponent, 1.0 / 2.4) - 0.055;
		}

		public static short gammaExpand(int srgbComponent, int maxVal) {
			if (maxVal <= 0)
				throw new IllegalArgumentException("maxVal must be >= 0");
			if (USE_TABLE) {
				if (INV_GAMMA_TABLE == null) {
					INV_GAMMA_TABLE = IntStream.range(0, COMPRESSED_MAX + 1) //
							.map((i) -> (int) (.5 + LINEAR_MAX_D * srgbExpand(i / COMPRESSED_MAX_D)) & 0xfff) //
							.toArray();
				}
				srgbComponent = scaleAndClamp(srgbComponent, 0, maxVal, COMPRESSED_MAX);
				return (short) INV_GAMMA_TABLE[srgbComponent];
			} else {
				double c = srgbComponent / (double) maxVal;
				int r = (int) (.5 + LINEAR_MAX_D * gammaExpand(c)) & LINEAR_MAX;
				return (short) r;
			}
		}

		public static int scaleAndClamp(int n, int inputMin, int inputMax, int outputMax) {
			n = Math.max(n, inputMin) - inputMin;
			n = Math.min(n, inputMax);
			n = (n * outputMax) / inputMax;
			assert 0 <= n && n <= outputMax;
			return n;
		}

		public static double scaleAndClamp(double n, double inputMin, double inputMax, double outputMax) {
			n = Math.max(n, inputMin) - inputMin;
			n = Math.min(n, inputMax);
			n = (n * outputMax) / inputMax;
			assert 0 <= n && n <= outputMax;
			return n;
		}

		public static short gammaCompress(int linearComponent, int maxVal) {
			if (maxVal <= 0)
				throw new IllegalArgumentException("maxVal must be >= 0");
			if (USE_TABLE) {
				if (GAMMA_TABLE == null) {
					GAMMA_TABLE = IntStream.range(0, LINEAR_MAX + 1) //
							.map((i) -> (short) (.5 + COMPRESSED_MAX_D * srgbCompress(i / LINEAR_MAX_D)) & 0xff) //
							.toArray();
				}
				linearComponent = scaleAndClamp(linearComponent, 0, maxVal, LINEAR_MAX);
				return (short) GAMMA_TABLE[linearComponent];
			} else {
				double c = linearComponent / (double) maxVal;
				int r = (int) (.5 + COMPRESSED_MAX_D * gammaCompress(c)) & COMPRESSED_MAX;
				return (short) r;
			}
		}

		@SuppressWarnings("unused")
		private static void dumpTable(int max, int[] table) {
			for (int j = 0; j < max; j += 16) {
				System.out.printf("%5d: ", j);
				for (int i = 0; i < 16; i++) {
					System.out.printf("%4d ", GAMMA_TABLE[i + j]);
				}
				System.out.println();
			}
		}
	}

	/**
	 * Convert colours to/from the scheme used by the 256-colour mode of the Select
	 * Graphic Rendition escape sequence
	 * 
	 * These are the relevant sequences:
	 * 
	 * <pre>
	 ESC[ 38;5;⟨n⟩ m Select foreground color
	 ESC[ 48;5;⟨n⟩ m Select background color
	 0-  7:  standard colors (as in ESC [ 30–37 m)
	 8- 15:  high intensity colors (as in ESC [ 90–97 m)
	 16-231:  6 × 6 × 6 cube (216 colors): 16 + 36 × r + 6 × g + b (0 ≤ r, g, b ≤ 5)
	 232-255:  grayscale from black to white in 24 steps
	 * </pre>
	 * 
	 * The 6x6x6 cube has colour values 0–5 that correspond to:
	 * <dl>
	 * <dt>0
	 * <dt>
	 * <dd>0x00 / 0% intensity</dd>
	 * <dt>1
	 * <dt>
	 * <dd>0x5f / 37% intensity</dd>
	 * <dt>2
	 * <dt>
	 * <dd>0x87 / 53% intensity</dd>
	 * <dt>3
	 * <dt>
	 * <dd>0xaf / 69% intensity</dd>
	 * <dt>4
	 * <dt>
	 * <dd>0xd7 / 84% intensity</dd>
	 * <dt>5
	 * <dt>
	 * <dd>0xff / 100% intensity</dd>
	 * </dl>
	 * (or <code>55+v*40</code> for 1 &leq; <code>v</code> &leq; 5)
	 * 
	 * The standard / high intensity colours come from a fixed palette.
	 * 
	 * The 24 gray colours in the 232–255 block range from 0x080808 to 0xeeeeee, the
	 * 8-bit value can be computed as <code>8 + 10 * (col - 232)</code> with black
	 * and white represented by 0 (standard color) and 15 (high intensity white).
	 * 
	 * @author anya
	 * @see https://en.wikipedia.org/wiki/ANSI_escape_code#SGR
	 */
	public static class ANSI_256 {
		/**
		 * Convert an 8-bit RGB colour to the ANSI 256-colour palette.
		 * 
		 * The “standard” (0–7) and “high intensity” (8–15, will be mapped to 16–255)
		 * colours are not used, except for black and white.
		 * 
		 * @param r Red value, 0–255
		 * @param g Green value, 0–255
		 * @param b Blue value, 0–255
		 * @return ANSI colour value, either 0 (black), 15 (white), 16–231 (6x6x6 colour
		 *         cube) or 232–255 (gray)
		 */
		public static int rgb8ToANSI(int r, int g, int b) {
			if (r == g && g == b) {
				g = (g + 2) / 10;
				if (g == 0)
					return 0;
				else if (g == 25)
					return 15;
				else
					return 232 + g - 1;
			} else {
				int r2 = (int) Math.max(0, (r - 55) / Math.round(40.0));
				int g2 = (int) Math.max(0, (g - 55) / Math.round(40.0));
				int b2 = (int) Math.max(0, (b - 55) / Math.round(40.0));
				System.out.printf("1: %02x%02x%02x\n", r2, g2, b2);
				r = Math.max(0, (r - 55) / 40);
				g = Math.max(0, (g - 55) / 40);
				b = Math.max(0, (b - 55) / 40);
				System.out.printf("2: %02x%02x%02x\n", r, g, b);
				return 16 + 36 * r + 6 * g + b;
			}
		}

		/**
		 * Convert an RGB colour to the ANSI 256-colour palette.
		 * 
		 * The “standard” (0–7) and “high intensity” (8–15, will be mapped to 16–255)
		 * colours are not used, except for black and white.
		 * 
		 * @param r Red value, 0–1
		 * @param g Green value, 0–1
		 * @param b Blue value, 0–1
		 * @return ANSI colour value, either 0 (black), 15 (white), 16–231 (6x6x6 colour
		 *         cube) or 232–255 (gray)
		 */
		public static int toANSI(double r, double g, double b) {
			return rgb8ToANSI((int) (r * 255), (int) (g * 255), (int) (b * 255));
		}

		/**
		 * Convert from the ANSI 256-colour palette to a {@link turtleduck.colors.Color
		 * Color}
		 * 
		 * @param col Colour index, 0–255
		 * @return The corresponding {@link turtleduck.colors.Color Color}
		 */
		public static Color fromANSI(int col) {
			if (col < 0) {
				throw new IllegalArgumentException("" + col);
			} else if (col < 16) {
				return ControlSequences.PALETTE_VGA[col];
			} else if (col < 232) {
				col = col - 16;
				int r = col / 36, g = (col / 6) % 6, b = col % 6;
				r = r == 0 ? 0 : (55 + r * 40);
				g = g == 0 ? 0 : (55 + g * 40);
				b = b == 0 ? 0 : (55 + b * 40);
				return Color.fromRGB(r, g, b);
			} else if (col < 256) {
				int g = 8 + 10 * (col - 232);
				return Color.fromRGB(g, g, g);
			} else {
				throw new IllegalArgumentException("" + col);
			}
		}
	}
}
