package turtleduck.text;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

public class BlocksAndBoxes {
	public enum PixelOrder implements Iterable<Integer> {
		LEFT_TO_RIGHT(8, 4, 2, 1), RIGHT_TO_LEFT(4, 8, 1, 2), LEFT_TO_RIGHT_UPWARDS(2, 1, 8,
				4), RIGHT_TO_LEFT_UPWARDS(1, 2, 4, 8);

		private List<Integer> order;

		private PixelOrder(int a, int b, int c, int d) {
			order = Arrays.asList(a, b, c, d);
		}

		@Override
		public Iterator<Integer> iterator() {
			return order.iterator();
		}
	}

	public static final String[] unicodeBlocks = { " ", "▗", "▖", "▄", "▝", "▐", "▞", "▟", "▘", "▚", "▌", "▙", "▀", "▜",
			"▛", "█", "▒" };

	public static final int[] unicodeBlocks_NumPixels = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 2 };
	public static final String unicodeBlocksString = String.join("", unicodeBlocks);
	public static final String BLOCK_EMPTY = " ";
	public static final String BLOCK_BOTTOM_RIGHT = "▗";
	public static final String BLOCK_BOTTOM_LEFT = "▖";
	public static final String BLOCK_BOTTOM = "▄";
	public static final String BLOCK_TOP_RIGHT = "▝";
	public static final String BLOCK_RIGHT = "▐";
	public static final String BLOCK_DIAG_FORWARD = "▞";
	public static final String BLOCK_REVERSE_TOP_LEFT = "\u255D"; 		// ╝
	public static final String BLOCK_TOP_LEFT = "▘";
	public static final String BLOCK_DIAG_BACKWARD = "▚";
	public static final String BLOCK_LEFT = "▌";
	public static final String BLOCK_REVERSE_TOP_RIGHT = "\u255A"; 		// ╚
	public static final String BLOCK_TOP = "▀";
	public static final String BLOCK_REVERSE_BOTTOM_LEFT = "\u2557"; 	// ╗
	public static final String BLOCK_REVERSE_BOTTOM_RIGHT = "\u2554";	// ╔
	public static final String BLOCK_FULL2 = "\u2551";					// ║
	public static final String BLOCK_LEFT_TO_RIGHT = "\u2550";			// ═
	public static final String BLOCK_FULL = "█";
	public static final String BLOCK_BOTTOM_LEFT_TOP = "\u2563";		// ╣
	public static final String BLOCK_BOTTOM_LEFT_TOP2 = "\u2555";		// ╕
	public static final String BLOCK_BOTTOM_RIGHT_TOP = "\u2560";		// ╠
	public static final String BLOCK_BOTTOM_RIGHT_TOP2 = "\u2552";		// ╒
	public static final String BLOCK_LEFT_RIGHT_TOP = "\u2559";			// ╙
	public static final String BLOCK_LEFT_RIGHT_TOP2 = "\u2569";		// ╩
	public static final String BLOCK_LEFT_RIGHT_BOTTOM = "\u2553";		// ╓
	public static final String BLOCK_LEFT_RIGHT_BOTTOM2 = "\u2566";		// ╦
	public static final String BLOCK_FOUR_WAYS = "\u256C";				// ╬
	public static final String BLOCK_HALF = "▒";


	public static final String BLOCK_SHADE = "\u2591";
	public static final String BLOCK_LOCKED_DOOR = "\uD83D\uDD12";
	public static final String BLOCK_KEY = "\uD83D\uDD11";
	public static final String BLOCK_SWORD = "\u2694";
	public static final String BLOCK_SHIELD = "\u26E8";
	public static final String BLOCK_POTION = "\uD83C\uDF7E";
	public static final String BLOCK_RABBIT = "\uD83D\uDC30";
	public static final String BLOCK_PLAYER = "\uD83E\uDDD4";
	public static final String BLOCK_ZOMBIE = "\uD83E\uDDDF";


	public static String blockAddOne(String s, PixelOrder order) {
		int i = BlocksAndBoxes.unicodeBlocksString.indexOf(s);
		if (i >= 0) {
			for (int bit : order) {
				if ((i & bit) == 0) {
					return unicodeBlocks[i | bit];
				}
			}
		}
		return s;
	}

	/**
	 * Convert a string into a Unicode block graphics character.
	 *
	 * <p>
	 * The block characters corresponds to 2x2-pixel images, and can be used, e.g.,
	 * to draw a 80x44 pixel image on a 40x22 character text screen.
	 *
	 * <p>
	 * The blocks are specified by four-character strings of spaces and asterisks,
	 * with space indicating an open space and asterisk indicating a filled "pixel",
	 * with the pixels arranged in a left-to-right, top-to-bottom order:
	 *
	 * <pre>
	 * 01
	 * 23
	 * </pre>
	 *
	 * <p>
	 * So <code>"* **"</code> corresponds to the block character <code>"▚"</code>,
	 * with this layout:
	 *
	 * <pre>
	 * *
	 *  *
	 * </pre>
	 *
	 * <p>
	 * The special codes <code>"++++"</code> and <code>"+"</code> corresponds to the
	 * "grey" block <code>"▒"</code>, and <code>"*"</code> corresponds to the
	 * "black" block <code>"█"</code>.
	 *
	 * @param s
	 *            A four character string, indicating which block character to
	 *            select.
	 * @return A Unicode block character
	 * @throws IllegalArgumentException
	 *             if the string isn't of the expected form
	 */
	public static String blockChar(String s) {
		switch (s.replaceAll("\n", s)) {
		case "    ":
			return unicodeBlocks[0];
		case "   *":
			return unicodeBlocks[1];
		case "  * ":
			return unicodeBlocks[2];
		case "  **":
			return unicodeBlocks[3];
		case " *  ":
			return unicodeBlocks[4];
		case " * *":
			return unicodeBlocks[5];
		case " ** ":
			return unicodeBlocks[6];
		case " ***":
			return unicodeBlocks[7];
		case "*   ":
			return unicodeBlocks[8];
		case "*  *":
			return unicodeBlocks[9];
		case "* * ":
			return unicodeBlocks[10];
		case "* **":
			return unicodeBlocks[11];
		case "**  ":
			return unicodeBlocks[12];
		case "** *":
			return unicodeBlocks[13];
		case "*** ":
			return unicodeBlocks[14];
		case "****":
			return unicodeBlocks[15];
		case "++++":
			return unicodeBlocks[16];
		case ".":
			return BLOCK_BOTTOM_LEFT;
		case "_":
			return BLOCK_BOTTOM;
		case "/":
			return BLOCK_DIAG_FORWARD;
		case "\\":
			return BLOCK_DIAG_BACKWARD;
		case "|":
			return BLOCK_LEFT;
		case "#":
			return BLOCK_FULL;
		case "`":
			return BLOCK_TOP_LEFT;
		case "'":
			return BLOCK_TOP_RIGHT;
		}
		throw new IllegalArgumentException(
				"Expected length 4 string of \" \" and \"*\", or \"++++\", got \"" + s + "\"");
	}

	public static String blockCompact(String s) {
		int i = BlocksAndBoxes.unicodeBlocksString.indexOf(s);
		if (i > 0) {
			int lower = i & 3;
			int upper = (i >> 2) & 3;
			i = (lower | upper) | ((lower & upper) << 2);
			// System.out.println("Compact: " + s + " -> " + BlocksAndBoxes.unicodeBlocks[i]
			// + "\n");
			return BlocksAndBoxes.unicodeBlocks[i];
		}
		return s;
	}

	public static String blockCompose(String b1, String b2, BiFunction<Integer, Integer, Integer> op) {
		int i1 = unicodeBlocksString.indexOf(b1);
		if (i1 < 0) {
			throw new IllegalArgumentException("Not a block character: " + b1);
		}
		int i2 = unicodeBlocksString.indexOf(b2);
		if (i2 < 0) {
			throw new IllegalArgumentException("Not a block character: " + b1);
		}
		if (i1 == 16 || i2 == 16) {
			return b2;
		} else {
			return unicodeBlocks[op.apply(i1, i2)];
		}
	}

	public static String blockComposeOrOverwrite(String b1, String b2, BiFunction<Integer, Integer, Integer> op) {
		int i1 = unicodeBlocksString.indexOf(b1);
		int i2 = unicodeBlocksString.indexOf(b2);
		if (i1 < 0 || i2 < 0 || i1 == 16 || i2 == 16) {
			return b2;
		} else {
			return unicodeBlocks[op.apply(i1, i2)];
		}
	}

	public static String blockRemoveOne(String s, PixelOrder order) {
		int i = BlocksAndBoxes.unicodeBlocksString.indexOf(s);
		if (i >= 0) {
			for (int bit : order) {
				if ((i & bit) != 0) {
					return unicodeBlocks[i & ~bit];
				}
			}
		}
		return s;
	}

}
