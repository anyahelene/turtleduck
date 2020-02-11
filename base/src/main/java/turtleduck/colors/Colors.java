package turtleduck.colors;

import java.util.stream.IntStream;

public class Colors {
	public static final Paint TRANSPARENT = Paint.color(0, 0, 0, 0);
	public static final Paint RED = Paint.color(1, 0, 0, 1);

	public static final Paint GREEN = Paint.color(0, 1, 0, 1);
	public static final Paint BLUE = Paint.color(0, 0, 1, 1);
	public static final Paint YELLOW = Paint.color(1, 1, 0, 1);
	public static final Paint MAGENTA = Paint.color(1, 0, 1, 1);
	public static final Paint CYAN = Paint.color(0, 1, 1, 1);
	public static final Paint GREY = Paint.color(.5, .5, .5, 1);
	public static final Paint WHITE = Paint.color(1, 1, 1, 1);
	public static final Paint BLACK = Paint.color(0, 0, 0, 1);
	public static final Paint PINK = Paint.color(1, .5, .5, 1);
	public static final Paint FORESTGREEN = Paint.color(.3, 1, 0, 1);

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
				int r = (int)(.5 + LINEAR_MAX_D * gammaExpand(c)) & LINEAR_MAX;
				return (short)r;
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
							.map((i) -> (short) (.5 + COMPRESSED_MAX_D * srgbCompress(i/LINEAR_MAX_D)) & 0xff) //
							.toArray();
				}
				linearComponent = scaleAndClamp(linearComponent, 0, maxVal, LINEAR_MAX);
				return (short)GAMMA_TABLE[linearComponent];
			} else {
				double c = linearComponent / (double) maxVal;
				int r = (int) (.5 + COMPRESSED_MAX_D * gammaCompress(c)) & COMPRESSED_MAX;
				return (short)r;
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
