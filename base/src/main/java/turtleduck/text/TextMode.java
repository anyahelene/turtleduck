package turtleduck.text;
import turtleduck.display.Screen;

public enum TextMode {
	/** Low resolution, wide screen (20:11, fits 16:9) text mode 40x22 */
	MODE_40X22(Constants.H40, Constants.V22, Screen.ASPECT_WIDE),
	/** Low resolution, 16:10 aspect text mode 40x25 */
	MODE_40X25(Constants.H40, Constants.V25, Screen.ASPECT_MEDIUM),
	/** Low resolution, 4:3 aspect text mode 40x30 */
	MODE_40X30(Constants.H40, Constants.V30, Screen.ASPECT_CLASSIC),
	/** High resolution, wide screen (20:11, fits 16:9) text mode 80x22 */
	MODE_80X22(Constants.H80, Constants.V22, Screen.ASPECT_WIDE),
	/** High resolution, 16:10 aspect text mode 80x25 */
	MODE_80X25(Constants.H80, Constants.V25, Screen.ASPECT_MEDIUM),
	/** High resolution, 4:3 aspect text mode 80x30 */
	MODE_80X30(Constants.H80, Constants.V30, Screen.ASPECT_CLASSIC);

	protected static class Constants {
		protected static final int H40 = 0, H80 = 1;
		protected static final int[] HREZ = { 40, 80 };
		protected static final int V22 = 0, V25 = 1, V30 = 2;
		protected static final int[] VREZ = { 22, 25, 30 };
		private static TextMode[] MODES = null;

		// work around initialization order for statics and enums
		protected static TextMode getMode(int i) {
			if (MODES == null)
				MODES = new TextMode[] { MODE_40X22, MODE_40X25, MODE_40X30, MODE_80X22, MODE_80X25, MODE_80X30 };
			return MODES[(i + MODES.length) % MODES.length];
		}
	}

	/**
	 * Size of the square-shaped "box" bounds of character cells.
	 * 
	 * For "high" resolution, characters will be squeezed horizontally to fit half
	 * the width.
	 */
	public static final double CHAR_BOX_SIZE = 32;
	/**
	 * Maximum length of a line in any resolution mode
	 */
	public static final int LINE_WIDTH_MAX = Constants.HREZ[Constants.HREZ.length - 1];
	/**
	 * Maximum height of a page in any resolution mode
	 */
	public static final int PAGE_HEIGHT_MAX = Constants.VREZ[Constants.VREZ.length - 1];
	private int aspect;
	private int w;

	private int h;

	private int hIndex;

	private int vIndex;

	private TextMode(int w, int h, int aspect) {
		this.hIndex = w;
		this.vIndex = h;
		this.aspect = aspect;
		this.w = Constants.HREZ[w];
		this.h = Constants.VREZ[h];
	}

	private TextMode findMode(int hIndex, int vIndex) {
		hIndex = (hIndex + Constants.HREZ.length) % Constants.HREZ.length;
		vIndex = (vIndex + Constants.VREZ.length) % Constants.VREZ.length;
		return Constants.getMode(vIndex + hIndex * Constants.VREZ.length);
	}

	/**
	 * Get aspect ration descriptor for use with {@link Screen#setAspect()}
	 * 
	 * @return One of {@link Screen#ASPECT_WIDE}, {@link Screen#ASPECT_MEDIUM} or
	 *         {@link Screen#ASPECT_CLASSIC}
	 */
	public int getAspect() {
		return aspect;
	}

	public double getCharBoxSize() {
		return CHAR_BOX_SIZE;
	}

	public double getCharHeight() {
		return CHAR_BOX_SIZE;
	}

	public double getCharWidth() {
		return w == 80 ? CHAR_BOX_SIZE / 2 : CHAR_BOX_SIZE;
	}

	public int getLineWidth() {
		return w;
	}

	public int getPageHeight() {
		return h;
	}

	/**
	 * Cycle through horizontal modes
	 * 
	 * @return Next available horizontal mode (vertical resolution unchanged)
	 */
	public TextMode nextHorizMode() {
		return findMode((hIndex + 1) % Constants.HREZ.length, vIndex);
	}

	/**
	 * Cycle through modes
	 * 
	 * @return Next available mode
	 */
	public TextMode nextMode() {
		int m = vIndex + hIndex * Constants.VREZ.length;
		return Constants.getMode(m + 1);
	}

	/**
	 * Cycle through vertical modes
	 * 
	 * @return Next available vertical mode (horizontal resolution unchanged)
	 */
	public TextMode nextVertMode() {
		return findMode((hIndex + 1) % Constants.HREZ.length, vIndex);
	}

	/**
	 * Cycle through horizontal modes
	 * 
	 * @return Previous available horizontal mode (vertical resolution unchanged)
	 */
	public TextMode prevHorizMode() {
		return findMode((hIndex - 1) % Constants.HREZ.length, vIndex);
	}

	/**
	 * Cycle through modes
	 * 
	 * @return Previous available mode
	 */
	public TextMode prevMode() {
		int m = vIndex + hIndex * Constants.VREZ.length;
		return Constants.getMode(m);
	}

	/**
	 * Cycle through vertical modes
	 * 
	 * @return Previous available vertical mode (horizontal resolution unchanged)
	 */
	public TextMode prevVertMode() {
		return findMode((hIndex - 1) % Constants.HREZ.length, vIndex);
	}

}
