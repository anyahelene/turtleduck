package turtleduck.grid;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface MultiGrid<T> extends Grid<List<T>> {
	/**
	 * Add to the cell at the given location.
	 *
	 * @param loc
	 *            The (x,y) position of the grid cell to get the contents of.
	 * @param element
	 *            An element to be added to the cell.
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(loc)
	 */
	default void add(Location loc, T element) {
		get(loc).add(element);
	}

	/**
	 * Add to the cell at the given x,y location.
	 *
	 * y must be greater than or equal to 0 and less than getHeight(). x must be
	 * greater than or equal to 0 and less than getWidth().
	 *
	 * @param pos
	 *            The (x,y) position of the grid cell to get the contents of.
	 * @param element
	 *            An element to be added to the cell
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	default void add(int x, int y, T element) {
		get(x, y).add(element);
	}

	/**
	 * Check if a cell contains an element.
	 *
	 *
	 * @param loc
	 *            The (x,y) position of the grid cell
	 * @param predicate
	 *            Search predicate.
	 * @return true if an element matching the predicate was found
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(loc)
	 */
	default boolean contains(Location loc, Predicate<T> predicate) {
		for (T t : get(loc)) {
			if (predicate.test(t))
				return true;
		}
		return false;
	}

	/**
	 * Check if a cell contains an element.
	 *
	 *
	 * @param loc
	 *            The (x,y) position of the grid cell
	 * @param element
	 *            An element to search for.
	 * @return true if element is at the given location
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(loc)
	 */
	default boolean contains(Location loc, T element) {
		return get(loc).contains(element);
	}

	/**
	 * Check if a cell contains an element.
	 *
	 * y must be greater than or equal to 0 and less than getHeight(). x must be
	 * greater than or equal to 0 and less than getWidth().
	 *
	 * @param pos
	 *            The (x,y) position of the grid cell to get the contents of.
	 * @param predicate
	 *            Search predicate.
	 * @return true if an element matching the predicate was found
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	default boolean contains(int x, int y, Predicate<T> predicate) {
		return contains(this.getArea().location(x, y), predicate);
	}

	/**
	 * Check if a cell contains an element.
	 *
	 * y must be greater than or equal to 0 and less than getHeight(). x must be
	 * greater than or equal to 0 and less than getWidth().
	 *
	 * @param pos
	 *            The (x,y) position of the grid cell to get the contents of.
	 * @param element
	 *            An element to search for.
	 * @return true if element is at the given location
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	default boolean contains(int x, int y, T element) {
		return get(x, y).contains(element);
	}

	/**
	 * Get all elements in a cell that match the predicate
	 *
	 *
	 * @param loc
	 *            The (x,y) position of the grid cell
	 * @param predicate
	 *            Search predicate.
	 * @return true if an element matching the predicate was found
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(loc)
	 */
	default List<T> get(Location loc, Predicate<T> predicate) {
		return get(loc).stream().filter(predicate).collect(Collectors.toList());
	}

	/**
	 * Check if a cell contains an element.
	 *
	 * y must be greater than or equal to 0 and less than getHeight(). x must be
	 * greater than or equal to 0 and less than getWidth().
	 *
	 * @param pos
	 *            The (x,y) position of the grid cell to get the contents of.
	 * @param predicate
	 *            Search predicate.
	 * @return true if an element matching the predicate was found
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	default List<T> get(int x, int y, Predicate<T> predicate) {
		return get(this.getArea().location(x, y), predicate);
	}

	/**
	 * Remove an element from the cell at the given location.
	 *
	 * @param loc
	 *            The location of the grid cell
	 * @param predicate
	 *            Predicate which should be true for elements to be removed
	 * @return Number of elements removed
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(loc)
	 */
	default int remove(Location loc, Predicate<T> predicate) {
		List<T> list = get(loc);
		int s = list.size();
		get(loc).removeIf(predicate);
		return s - list.size();
	}

	/**
	 * Remove an element from the cell at the given location.
	 *
	 * @param loc
	 *            The location of the grid cell
	 * @param element
	 *            An element to be removed from the cell.
	 * @return Number of elements removed
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(loc)
	 */
	default int remove(Location loc, T element) {
		return get(loc).remove(element) ? 1 : 0;
	}

	/**
	 * Remove an element from the cell at the given x,y location.
	 *
	 * y must be greater than or equal to 0 and less than getHeight(). x must be
	 * greater than or equal to 0 and less than getWidth().
	 *
	 * @param pos
	 *            The (x,y) position of the grid cell
	 * @param predicate
	 *            Predicate which should be true for elements to be removed
	 * @return Number of elements removed
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	default int remove(int x, int y, Predicate<T> predicate) {
		return remove(getArea().location(x, y), predicate);
	}

	/**
	 * Remove an element from the cell at the given x,y location.
	 *
	 * y must be greater than or equal to 0 and less than getHeight(). x must be
	 * greater than or equal to 0 and less than getWidth().
	 *
	 * @param pos
	 *            The (x,y) position of the grid cell
	 * @param element
	 *            An element to be removed from the cell
	 * @return Number of elements removed
	 * @throws IndexOutOfBoundsException
	 *             if !isValid(x,y)
	 */
	default int remove(int x, int y, T element) {
		return get(x, y).remove(element) ? 1 : 0;
	}

}