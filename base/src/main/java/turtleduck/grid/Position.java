package turtleduck.grid;

public interface Position {

	/**
	 * @param obj Another object
	 * @return true if obj is also an IPosition, and the x and y coordinates are
	 *         equal
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Find the Euclidian distance between the midpoint of this position and another
	 * position.
	 * 
	 * The distance is computed with the Pythagorean formula, with the assumption
	 * that the grid cells are square shaped with <em>width</em> = <em>height</em> =
	 * 1. For example, the distance from (0,0) to (3,5) is √(3²+5²) = 5.83.
	 *
	 * @param other
	 * @return Euclidian distance between this and other's midpoints
	 */
	double geometricDistanceTo(Position other);

	/**
	 * Gets the x-coordinate
	 * 
	 * @return
	 */
	int x();

	/**
	 * Gets the y-coordinate
	 * 
	 * @return
	 */
	int y();

	/**
	 * Gets the z-coordinate
	 * 
	 * @return
	 */
	int z();

	/**
	 * Find the distance in grid cells to another location.
	 * 
	 * This is different from {@link #stepDistanceTo(Position)} in that diagonal
	 * steps are allowed, and is the same as the number of steps a queen would take
	 * on a chess board.
	 * <p>
	 * Computes the maximum of the horizontal and the vertical distance. For
	 * example, to go from (0,0) to (3,5), you could go three steps SOUTHEAST and
	 * two steps SOUTH, so the {@link #gridDistanceTo(Position)} is 5.
	 *
	 * @param other
	 * @return Number of horizontal/vertical/diagonal (<em>queen</em>-like) steps to
	 *         other
	 */
	int gridDistanceTo(Position other);

	@Override
	int hashCode();

	/**
	 * Find the number of non-diagonal steps needed to go from this location the
	 * other location.
	 * 
	 * This is different from {@link #gridDistanceTo(Position)} in that only
	 * non-diagonal steps are allowed, and is the same as the number of steps a rook
	 * would take on a chess board.
	 * <p>
	 * Computes the distance to another location as the sum of the absolute
	 * difference between the x- and y-coordinates. For example, to go from (0,0) to
	 * (3,5), you would need to go three steps EAST and five steps SOUTH, so the
	 * {@link #stepDistanceTo(Position)} is 8.
	 *
	 * @param other
	 * @return Number of horizontal/vertical (<em>rook</em>-like) steps to other
	 */
	int stepDistanceTo(Position other);

	/** @return Position as a string, "(x,y)" */
	@Override
	String toString();

}