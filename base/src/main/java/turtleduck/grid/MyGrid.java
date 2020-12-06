package turtleduck.grid;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;


/** A Grid contains a set of cells in a square 2D matrix. */
public class MyGrid<T> implements Grid<T>, Grid3<T> {
	private final Area area;
	private final T[] cells;

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
	 * @param initialiser The initialiser function
	 */
	@SuppressWarnings("unchecked")
	public MyGrid(Area area, Function<Location, T> initialiser) {
		if (area == null || initialiser == null) {
			throw new IllegalArgumentException();
		}

		this.area = area;
		this.cells = (T[]) new Object[area.size()];
		for (Location loc : area) {
			cells[loc.index()] = initialiser.apply(loc);
		}
	}

	/**
	 * Construct a grid with the given dimensions.
	 *
	 * @param width
	 * @param height
	 * @param initElement What the cells should initially hold (possibly null)
	 */
	@SuppressWarnings("unchecked")
	public MyGrid(Area area, T initElement) {
		if (area == null) {
			throw new IllegalArgumentException();
		}

		this.area = area;
		this.cells = (T[]) new Object[area.size()];
		for (int i = 0; i < area.size(); ++i) {
			cells[i] = initElement;
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
	 * @param initialiser The initialiser function
	 */
	public MyGrid(int width, int height, Function<Location, T> initialiser) {
		this(new RectArea(width, height), initialiser);
	}

	/**
	 * Construct a grid with the given dimensions.
	 *
	 * @param width
	 * @param height
	 * @param initElement What the cells should initially hold (possibly null)
	 */
	public MyGrid(int width, int height, T initElement) {
		this(new RectArea(width, height), initElement);
	}

	@Override
	public Grid<T> copy() {
		MyGrid<T> newGrid = new MyGrid<>(width(), height(), (l) -> get(l));

		return newGrid;
	}

	@Override
	public Stream<T> elementParallelStream() {
		return Stream.of(cells).parallel();
	}

	@Override
	public Stream<T> elementStream() {
		return Stream.of(cells);
	}

	@Override
	public void fill(Function<Location, T> initialiser) {
		if (initialiser == null)
			throw new NullPointerException();

		for (int i = 0; i < area.size(); i++) {
			cells[i] = initialiser.apply(area.fromIndex(i));
		}
	}

	@Override
	public void fill(T element) {
		for (int i = 0; i < area.size(); i++) {
			cells[i] = element;
		}
	}

	@Override
	public T get(Location loc) {
		if (loc.area() == area)
			return cells[loc.index()];
		else
			return cells[area.toIndex(loc.x(), loc.y(), loc.z())];
	}

	@Override
	public T get(int x, int y) {
		return cells[area.toIndex(x, y, 0)];
	}

	@Override
	public T get(int x, int y, int z) {
		return cells[area.toIndex(x, y, z)];
	}

	@Override
	public Area area() {
		return area;
	}

	@Override
	public int height() {
		return area.height();
	}

	@Override
	public T getOrDefault(Location loc, T defaultResult) {
		if (loc.area() == area) {
			T r = cells[loc.index()];
			if (r != null)
				return r;
			else
				return defaultResult;
		} else {
			return getOrDefault(loc.x(), loc.y(), loc.y(), defaultResult);
		}
	}

	@Override
	public T getOrDefault(int x, int y, T defaultResult) {
		return getOrDefault(x, y, 0, defaultResult);
	}

	@Override
	public T getOrDefault(int x, int y, int z, T defaultResult) {
		T r = null;
		if (isValid(x, y, z))
			r = get(x, y, z);
		if (r != null)
			return r;
		else
			return defaultResult;
	}

	@Override
	public int width() {
		return area.width();
	}

	@Override
	public boolean isValid(Location loc) {
		return loc.area() == area || area.contains(loc.x(), loc.y());
	}

	@Override
	public boolean isValid(int x, int y) {
		return area.contains(x, y, 0);
	}

	@Override
	public boolean isValid(int x, int y, int z) {
		return area.contains(x, y, z);
	}

	@Override
	public Iterator<T> iterator() {
		return elementStream().iterator();
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
		if (loc.area() == area) {
			cells[loc.index()] = element;
		} else {
			set(loc.x(), loc.y(), element);
		}
	}

	@Override
	public void set(int x, int y, T elem) {
		cells[area.toIndex(x, y, 0)] = elem;
	}

	@Override
	public void set(int x, int y, int z, T elem) {
		cells[area.toIndex(x, y, z)] = elem;
	}

}
