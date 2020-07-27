package turtleduck.grid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/** A Grid contains a set of cells in a square 2D matrix. */
public class MyGrid<T> implements Grid<T> {
	private final Area area;
	private final List<T> cells;

	/**
	 * Construct a grid with the given dimensions.
	 * 
	 * The initialiser function will be called with the (x,y) position of an
	 * element, and is expected to return the element to place at that position. For
	 * example:
	 * 
	 * <pre>
	 * // fill all cells with the position as a string (e.g., "(2,2)")
	 * new MyGrid(10, 10, ((x, y) -> String.format("(%d,%d)", x, y));
	 * </pre>
	 * 
	 * @param width
	 * @param height
	 * @param initialiser
	 *            The initialiser function
	 */
	public MyGrid(Area area, Function<Location, T> initialiser) {
		if (area == null || initialiser == null) {
			throw new IllegalArgumentException();
		}

		this.area = area;
		this.cells = new ArrayList<T>(area.getSize());
		for (Location loc : area) {
			cells.add(initialiser.apply(loc));
		}
	}

	/**
	 * Construct a grid with the given dimensions.
	 *
	 * @param width
	 * @param height
	 * @param initElement
	 *            What the cells should initially hold (possibly null)
	 */
	public MyGrid(Area area, T initElement) {
		if (area == null) {
			throw new IllegalArgumentException();
		}

		this.area = area;
		this.cells = new ArrayList<T>(area.getSize());
		for (int i = 0; i < area.getSize(); ++i) {
			cells.add(initElement);
		}
	}

	/**
	 * Construct a grid with the given dimensions.
	 * 
	 * The initialiser function will be called with the (x,y) position of an
	 * element, and is expected to return the element to place at that position. For
	 * example:
	 * 
	 * <pre>
	 * // fill all cells with the position as a string (e.g., "(2,2)")
	 * new MyGrid(10, 10, ((x, y) -> String.format("(%d,%d)", x, y));
	 * </pre>
	 * 
	 * @param width
	 * @param height
	 * @param initialiser
	 *            The initialiser function
	 */
	public MyGrid(int width, int height, Function<Location, T> initialiser) {
		this(new RectArea(width, height), initialiser);
	}

	/**
	 * Construct a grid with the given dimensions.
	 *
	 * @param width
	 * @param height
	 * @param initElement
	 *            What the cells should initially hold (possibly null)
	 */
	public MyGrid(int width, int height, T initElement) {
		this(new RectArea(width, height), initElement);
	}

	@Override
	public Grid<T> copy() {
		MyGrid<T> newGrid = new MyGrid<>(getWidth(), getHeight(), (l) -> get(l));

		return newGrid;
	}

	@Override
	public Stream<T> elementParallelStream() {
		return cells.parallelStream();
	}

	@Override
	public Stream<T> elementStream() {
		return cells.stream();
	}

	@Override
	public void fill(Function<Location, T> initialiser) {
		if (initialiser == null)
			throw new NullPointerException();

		for (int i = 0; i < area.getSize(); i++) {
			cells.set(i, initialiser.apply(area.fromIndex(i)));
		}
	}

	@Override
	public void fill(T element) {
		for (int i = 0; i < area.getSize(); i++) {
			cells.set(i, element);
		}
	}

	@Override
	public T get(Location loc) {
		if (loc.getArea() == area)
			return cells.get(loc.getIndex());
		else
			return cells.get(area.toIndex(loc.getX(), loc.getY()));
	}

	@Override
	public T get(int x, int y) {
		return cells.get(area.toIndex(x, y));
	}

	@Override
	public Area getArea() {
		return area;
	}

	@Override
	public int getHeight() {
		return area.getHeight();
	}

	@Override
	public T getOrDefault(Location loc, T defaultResult) {
		if (loc.getArea() == area) {
			T r = cells.get(loc.getIndex());
			if (r != null)
				return r;
			else
				return defaultResult;
		} else {
			return getOrDefault(loc.getX(), loc.getY(), defaultResult);
		}
	}

	@Override
	public T getOrDefault(int x, int y, T defaultResult) {
		T r = null;
		if (isValid(x, y))
			r = get(x, y);
		if (r != null)
			return r;
		else
			return defaultResult;
	}

	@Override
	public int getWidth() {
		return area.getWidth();
	}

	@Override
	public boolean isValid(Location loc) {
		return loc.getArea() == area || area.contains(loc.getX(), loc.getY());
	}

	@Override
	public boolean isValid(int x, int y) {
		return area.contains(x, y);
	}

	@Override
	public Iterator<T> iterator() {
		return cells.iterator();
	}

	@Override
	public Stream<Location> locationParallelStream() {
		return area.parallelStream();
	}

	@Override
	public Iterable<Location> locations() {
		return area;
	}

	@Override
	public Stream<Location> locationStream() {
		return area.stream();
	}

	@Override
	public void set(Location loc, T element) {
		if (loc.getArea() == area) {
			cells.set(loc.getIndex(), element);
		} else {
			set(loc.getX(), loc.getY(), element);
		}
	}

	@Override
	public void set(int x, int y, T elem) {
		cells.set(area.toIndex(x, y), elem);
	}
}
