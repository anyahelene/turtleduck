package turtleduck.grid;

import java.util.Arrays;
import java.util.List;

public enum GridDirection {
	EAST(0, 1, 0, 4), //
	NORTHEAST(45, 1, 1, 5),//
	NORTH(90, 0, 1, 1), //
	NORTHWEST(135, -1, 1, 9), //
	WEST(180, -1, 0, 8), //
	SOUTHWEST(225, -1, -1, 10), //
	SOUTH(270, 0, -1, 2), //
	SOUTHEAST(315, 1, -1, 6), //
	CENTER(0, 0, 0, 0);

	/**
	 * Set this to -1 if the Y-axis points downwards (typical with in 2D graphics),
	 * and 1 otherwise (typical with 3D graphics).
	 */
	private static final int UP = 1;
	/**
	 * The four cardinal directions: {@link #NORTH}, {@link #SOUTH}, {@link #EAST},
	 * {@link #WEST}.
	 */
	public static final List<GridDirection> FOUR_DIRECTIONS = Arrays.asList(EAST, NORTH, WEST, SOUTH);
	/**
	 * The eight cardinal and intercardinal directions: {@link #NORTH},
	 * {@link #SOUTH}, {@link #EAST}, {@link #WEST}, {@link #NORTHWEST},
	 * {@link #NORTHEAST}, {@link #SOUTHWEST}, {@link #SOUTHEAST}.
	 */
	public static final List<GridDirection> EIGHT_DIRECTIONS = Arrays.asList( //
			EAST, NORTHEAST, NORTH, NORTHWEST, //
			WEST, SOUTHWEST, SOUTH, SOUTHEAST);
	/**
	 * The eight cardinal and intercardinal directions ({@link #EIGHT_DIRECTIONS}),
	 * plus {@link #CENTER}.
	 */
	public static final List<GridDirection> NINE_DIRECTIONS = Arrays.asList(EAST, NORTHEAST, NORTH, NORTHWEST, WEST,
			SOUTHWEST, SOUTH, SOUTHEAST, CENTER);

	private final double degrees;
	private final int dx;
	private final int dy;
	private final int mask;

	private GridDirection(double degrees, int dx, int dy, int mask) {
		this.degrees = degrees;
		this.dx = dx;
		this.dy = dy*UP;
		this.mask = mask;
	}

	/**
	 * @return The angle of this direction, with 0° facing due {@link #EAST} and 90°
	 *         being {@link #NORTH}.
	 */
	public double getDegrees() {
		return degrees;
	}

	/**
	 * @return The change to your X-coordinate if you were to move one step in this
	 *         direction
	 */
	public int getDx() {
		return dx;
	}

	/**
	 * @return The change to your Y-coordinate if you were to move one step in this
	 *         direction
	 */
	public int getDy() {
		return dy;
	}

	public int getMask() {
		return mask;
	}

	/**
	 * @return The direction opposite this (e.g., SOUTH if this == NORTH)
	 */
	public GridDirection reverse() {
		return go(4);
	}

	/**
	 * @return This rotated 90° left (e.g., WEST if this == NORTH)
	 */
	public GridDirection left90() {
		return go(2);
	}

	/**
	 * @return This rotated 90° right (e.g., EAST if this == NORTH)
	 */
	public GridDirection right90() {
		return go(6);
	}

	/**
	 * @return This rotated 45° left (e.g., NORTHWEST if this == NORTH)
	 */
	public GridDirection left45() {
		return go(1);
	}

	/**
	 * @return This rotated 45° left (e.g., NORTHEAST if this == NORTH)
	 */
	public GridDirection right45() {
		return go(7);
	}

	private GridDirection go(int i) {
		if(this == CENTER) {
			return CENTER;
		} else {
			return EIGHT_DIRECTIONS.get((ordinal()+i) % 8);
		}
	}
}
