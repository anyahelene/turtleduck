package turtleduck.colors;

import java.util.stream.IntStream;

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
}
