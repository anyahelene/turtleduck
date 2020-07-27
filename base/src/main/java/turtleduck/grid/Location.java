package turtleduck.grid;

import java.util.Collection;
import java.util.List;

/**
 * Represents a location within an {@link Area}.
 * <p>
 * <em>Immutable:</em> Locations are immutable; i.e., a particular
 * <code>ILocation</code> will always have the same x- and y-coordinates and
 * cannot be changed. Methods that find neighbours (such as
 * {@link #go(GridDirection)}) will return another <code>ILocation</code>.
 * <p>
 * <em>Area:</em> All {@link Location}s are tied to an {@link Area}, thus they
 * will know whether they are on the edge of the area (and not all neighbouring
 * grid coordinates will be valid locations within the area) and other
 * coordinate system properties that are determined by the {@link Area} (e.g.,
 * if the coordinate system wraps around like on the surface of a donut).
 * <p>
 * <em>Unique objects:</em> All {@link Location} in an {@link Area} are
 * unique. This means that <code>area.location(x,y) == area.location(x,y)</code>
 * for all <em>x</em> and <em>y</em>, and that:
 * 
 * <pre>
 * // equality is reference equality for locations in the same area
 * if (l1.getArea() == l2.getArea())
 * 	assertEquals(l1.equals(l2), (l1 == l2));
 * </pre>
 * 
 * 
 * @author Anya Helene Bagge, UiB
 */
public interface Location extends Position {

	/**
	 * Iterate over neighbours in eight directions.
	 * <p>
	 * (The iterator may yield fewer than eight locations if the current location is
	 * at the edge of its containing area.
	 * 
	 * @return The neighbours in the eight cardinal and intercardinal directions
	 *         ({@link GridDirection#NORTH}, @link GridDirection#SOUTH}, @link
	 *         GridDirection#EAST}, @link GridDirection#WEST},
	 *         {@link GridDirection#NORTHEAST}, @link
	 *         GridDirection#NORTHWEST}, @link GridDirection#SOUTHEAST}, @link
	 *         GridDirection#SOUTHWEST}, )
	 */
	Collection<Location> allNeighbours();

	/**
	 * Checks whether you can go towards direction dir without going outside the
	 * area bounds
	 * 
	 * @param dir
	 * @return True if go(dir) will succeed
	 */
	boolean canGo(GridDirection dir);

	/**
	 * Iterate over north/south/east/west neighbours.
	 * <p>
	 * (The iterator may yield fewer than four locations if the current location is
	 * at the edge of its containing area.
	 * 
	 * @return The neighbours in the four cardinal directions
	 *         ({@link GridDirection#NORTH}, @link GridDirection#SOUTH}, @link
	 *         GridDirection#EAST}, @link GridDirection#WEST}
	 */
	Collection<Location> cardinalNeighbours();

	Area getArea();

	int getIndex();

	/**
	 * Return the next location in direction dir.
	 * <p>
	 * This <em>does not</em> change the location object; rather it returns the
	 * ILocation you would end up at if you go in the given direction from this
	 * ILocation.
	 * 
	 * @param dir
	 *            A direction
	 * @return A neighbouring location
	 * @throws IndexOutOfBoundsException
	 *             if !canGo(dir)
	 */
	Location go(GridDirection dir);

	/**
	 * Find the grid cells between this location (exclusive) and another location
	 * (inclusive).
	 * 
	 * This is will be a list of length {@link #gridDistanceTo(other)}, containing
	 * the cells that a chess queen would visit when moving to <code>other</code>.
	 * <p>
	 * Computes the maximum of the horizontal and the vertical distance. For
	 * example, to go from (0,0) to (3,5), you could go three steps SOUTHEAST and
	 * two steps SOUTH, so the {@link #gridDistanceTo(Position)} is 5.
	 *
	 * @param other
	 * @return Number of horizontal/vertical/diagonal (<em>queen</em>-like) steps to
	 *         other
	 */
	List<Location> gridLineTo(Location other);

}