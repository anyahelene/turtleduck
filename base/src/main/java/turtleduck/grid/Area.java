package turtleduck.grid;

import java.util.List;
import java.util.stream.Stream;

/**
 * Representation of a two-dimensional area.
 * <p>
 * This describes the coordinate system of the area and defines the set of valid
 * locations within the area.
 * <p>
 * See {@link #location(int, int)} to covert (x,y)-coordinates to
 * {@link Location}s.
 * <p>
 * An {@link Area} does not <em>store</em> anything, it just defines valid
 * storage locations (e.g., for an {@link Grid}), and the relationships between
 * locations (e.g., how to find neighbours of a given location).
 * 
 * @author Anya Helene Bagge, UiB
 */
public interface Area extends Iterable<Location> {
	/**
	 * Check if a (x,y) is inside the area.
	 * 
	 * @param x
	 *            X-coordinate
	 * @param y
	 *            Y-coordinate
	 * @return True if the (x,y) position lies within the area
	 */
	boolean contains(int x, int y);

	/**
	 * Check if a position is inside the area.
	 * 
	 * @param pos
	 *            A position
	 * @return True if the position lies within the area
	 */
	boolean contains(Position pos);

	@Override
	boolean equals(Object other);

	/**
	 * Convert a 1D coordinate to a location
	 * <p>
	 * Returns a location <code>l = fromIndex(i)</code> such that
	 * <code>toIndex(l.getX(), l.getY()) == i</code>.
	 * 
	 * @param i
	 * @return A location
	 */
	Location fromIndex(int i);

	/** @return Height of the area */
	int getHeight();

	/**
	 * Returns the number of legal positions in the area
	 *
	 * @return Same as getWidth()*getHeight()
	 */
	int getSize();

	/** @return Width of the area */
	int getWidth();

	@Override
	int hashCode();

	/**
	 * Get a location object corresponding to (x,y)
	 * 
	 * @param x
	 *            X-coordinate
	 * @param y
	 *            Y-coordinate
	 * @return The location object associated with (x,y)
	 * @throws IndexOutOfBoundsException
	 *             if {@link #contains(int, int)} returns false for (x,y)
	 */
	Location location(int x, int y);

	/**
	 * Get all locations in area
	 * <p>
	 * Since IArea is @{@link Iterable}, you can also use directly in a for-loop to
	 * iterate over the locations.
	 * <p>
	 * All locations in the list are guaranteed to be valid according to
	 * {@link #isValid(Location)}. The returned list cannot be modified.
	 * 
	 * @return An unmodifiable list with all the locations in the area
	 */
	List<Location> locations();

	/**
	 * Return an object for iterating over all the neighbours of the given position,
	 * suitable for use in a new-style for-loop.
	 * <p>
	 * The iterator will yield up to eight positions (less if the given position is
	 * at the edge of the area, and the coordinates are not wrapped). E.g., for a
	 * 1x1 area, the iterator will yield nothing (if the area is not wrapped), or
	 * the same position two or eight times (if the area is wrapped horizontally,
	 * vertically or both).
	 *
	 * @param pos
	 *            A position in the area
	 * @return An iterable over positions, with {@link #contains(Location)} being
	 *         true for each position.
	 * @see #wrapsHorizontally(), {@link #wrapsVertically()}
	 * @throws IndexOutOfBoundsException
	 *             if !contains(pos)
	 */
	Iterable<Location> neighboursOf(Location pos);

	/** @return A (possibly) parallel {@link Stream} of all locations in the area */
	Stream<Location> parallelStream();

	/** @return A {@link Stream} of all locations in the area */
	Stream<Location> stream();

	/**
	 * Convert a 2D coordinate to 1D
	 * 
	 * @param x
	 *            X-coordinate
	 * @param y
	 *            Y-coordinate
	 * @return x + y*getWidth()
	 */
	int toIndex(int x, int y);

	@Override
	String toString();

	/**
	 * If the area wraps horizontally, then x will be the same as x+(k*getWidth())
	 * for any k – i.e., it will be as if the 2D area is projected on a cylinder or
	 * torus in 3D-space.
	 * <p>
	 * With no wrapping, accessing positions outside (0,0)–(getWidth(),getHeight())
	 * is illegal.
	 *
	 * @return True if the area wraps around horizontally
	 */
	boolean wrapsHorizontally();

	/**
	 * If the area wraps vertically, then y will be the same as y+(k*getHeight())
	 * for any k – i.e., it will be as if the 2D area is projected on a cylinder or
	 * torus in 3D-space.
	 * <p>
	 * With no wrapping, accessing positions outside (0,0)–(getWidth(),getHeight())
	 * is illegal.
	 *
	 * @return True if the area wraps around vertically
	 */
	boolean wrapsVertically();
}
