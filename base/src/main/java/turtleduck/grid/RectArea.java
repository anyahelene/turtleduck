package turtleduck.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class RectArea implements Area {
	/** A class to represent an (x, y)-location on a grid. */
	class RectLocation implements Location {

		/** value of the x-coordinate */
		protected final int x;
		/** value of the y-coordinate */
		protected final int y;
		protected final int z;
		protected final int idx;
		protected final int edgeMask;

		/**
		 * Main constructor. Initializes a new {@link #Location} objects with the
		 * corresponding values of x and y.
		 * 
		 * @param x        X coordinate
		 * @param y        Y coordinate
		 * @param idx      1-dimensional index
		 * @param edgeMask mask with bits {@link RectArea#N}, {@link RectArea#S},
		 *                 {@link RectArea#E}, {@link RectArea#W} set if we're on the
		 *                 corresponding edge of the area
		 */
		RectLocation(int x, int y, int z, int idx, int edgeMask) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.idx = idx;
			this.edgeMask = edgeMask;
		}

		@Override
		public Collection<Location> allNeighbours() {
			Collection<Location> ns = new ArrayList<>(8);
			for (GridDirection d : GridDirection.EIGHT_DIRECTIONS) {
				if (canGo(d))
					ns.add(go(d));
			}
			return ns;
		}

		@Override
		public boolean canGo(GridDirection dir) {
			return (edgeMask & dir.getMask()) == 0;
		}

		@Override
		public Collection<Location> cardinalNeighbours() {
			Collection<Location> ns = new ArrayList<>(4);
			for (GridDirection d : GridDirection.FOUR_DIRECTIONS) {
				if (canGo(d))
					ns.add(go(d));
			}
			return ns;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Position)) {
				return false;
			}
			Position other = (Position) obj;
			if (x != other.x()) {
				return false;
			}
			if (y != other.y()) {
				return false;
			}
			return true;
		}

		@Override
		public double geometricDistanceTo(Position other) {
			return Math.sqrt(Math.pow(this.x - other.x(), 2) + Math.pow(this.y - other.y(), 2));
		}

		@Override
		public Area area() {
			return RectArea.this;
		}

		@Override
		public int index() {
			return idx;
		}

		@Override
		public int x() {
			return x;
		}

		@Override
		public int y() {
			return y;
		}

		@Override
		public Location go(GridDirection dir) {
			return location(x + dir.getDx(), y + dir.getDy());
		}

		@Override
		public int gridDistanceTo(Position other) {
			return Math.max(Math.abs(this.x - other.x()), Math.abs(this.y - other.y()));
		}

		@Override
		public List<Location> gridLineTo(Location other) {
			if (!contains(other))
				throw new IllegalArgumentException();
			int distX = other.x() - x;
			int distY = other.y() - y;
			int length = Math.max(Math.abs(distX), Math.abs(distY));
			List<Location> line = new ArrayList<>(length);
			if (length == 0)
				return line;
			double dx = (double) distX / (double) length;
			double dy = (double) distY / (double) length;
			// System.out.printf("dx=%g, dy=%g, length=%d%n", dx, dy, length);
			for (int i = 1; i <= length; i++) {
				line.add(location(x + (int) Math.round(dx * i), y + (int) Math.round(dy * i)));
			}
			return line;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public int stepDistanceTo(Position other) {
			return Math.abs(this.x - other.x()) + Math.abs(this.y - other.y());
		}

		@Override
		public String toString() {
			return String.format("(%d,%d,%d)", x, y, z);
		}

		@Override
		public int z() {
			return z;
		}

	}

	protected final int width;
	protected final int height;
	protected final int depth;
	protected final int size;
	protected final List<Location> locs;

	protected final boolean hWrap, vWrap, zWrap;

	public RectArea(int width, int height) {
		this(width, height, false, false);
	}

	private RectArea(int width, int height, boolean horizWrap, boolean vertWrap) {
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException("Width and height must be positive");
		}
		this.hWrap = horizWrap;
		this.vWrap = vertWrap;
		this.zWrap = false;
		this.width = width;
		this.height = height;
		this.depth = 1;
		this.size = width * height;
		List<Location> l = new ArrayList<>(size);
		for (int y = 0, i = 0; y < height; y++) {
			// set N or S bits if we're on the northern or southern edge
			int edge = (y == 0 ? GridDirection.NORTH.getMask() : 0)
					| (y == height - 1 ? GridDirection.SOUTH.getMask() : 0);
			for (int x = 0; x < width; x++, i++) {
				// set W or E bits if we're on the western or eastern edge
				int e = edge | (x == 0 ? GridDirection.WEST.getMask() : 0)
						| (x == width - 1 ? GridDirection.EAST.getMask() : 0);
				l.add(new RectLocation(x, y, 0, i, e));
			}
		}
		locs = Collections.unmodifiableList(l);
	}

	/**
	 * @param x X-coordinate
	 * @return The same x, wrapped to wrapX(x)
	 * @throws IndexOutOfBoundsException if coordinate is out of range, and wrapping
	 *                                   is not enabled
	 */
	protected int checkX(int x) {
		x = wrapX(x);
		if (x < 0 || x >= width) {
			throw new IndexOutOfBoundsException("x=" + x);
		}

		return x;
	}

	/**
	 * @param y Y-coordinate
	 * @return The same y, wrapped to wrapY(y)
	 * @throws IndexOutOfBoundsException if coordinate is out of range, and wrapping
	 *                                   is not enabled
	 */
	protected int checkY(int y) {
		y = wrapY(y);
		if (y < 0 || y >= height) {
			throw new IndexOutOfBoundsException("y=" + y);
		}
		return y;
	}

	/**
	 * @param z Z-coordinate
	 * @return The same z, wrapped to wrapZ(z)
	 * @throws IndexOutOfBoundsException if coordinate is out of range, and wrapping
	 *                                   is not enabled
	 */
	protected int checkZ(int z) {
		z = wrapZ(z);
		if (z < 0 || z >= depth) {
			throw new IndexOutOfBoundsException("z=" + z);
		}
		return z;
	}

	@Override
	public boolean contains(int x, int y) {
		return contains(x, y, 0);
	}

	@Override
	public boolean contains(int x, int y, int z) {
		x = wrapX(x);
		y = wrapY(y);
		z = wrapZ(z);
		return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
	}

	@Override
	public boolean contains(Position pos) {
		return (pos instanceof Location && ((Location) pos).area() == this) || contains(pos.x(), pos.y());
	}

	@Override
	public Location fromIndex(int i) {
		if (i >= 0 && i < size)
			return locs.get(i);
		else
			throw new IndexOutOfBoundsException("" + i);
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public Iterator<Location> iterator() {
		return locs.iterator();
	}

	@Override
	public Location location(int x, int y) {
		return location(x, y, 0);
	}

	@Override
	public Location location(int x, int y, int z) {
		if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth)
			throw new IndexOutOfBoundsException("(" + x + "," + y + ")");
		int i = x + y * width + z * width * height;
		return locs.get(i);
	}

	@Override
	public List<Location> locations() {
		return locs; // (OK since locs has been through Collections.unmodifiableList())
	}

	@Override
	public Iterable<Location> neighboursOf(Location pos) {
		return pos.allNeighbours();
	}

	@Override
	public Stream<Location> parallelStream() {
		return locs.parallelStream();
	}

	@Override
	public Stream<Location> stream() {
		return locs.stream();
	}

	@Override
	public int toIndex(int x, int y, int z) {
		x = checkX(x);
		y = checkY(y);
		z = checkZ(z);
		return z * width * height + y * width + x;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RectArea [width=").append(width).append(", height=").append(height).append(", hWrap=")
				.append(hWrap).append(", vWrap=").append(vWrap).append("]");
		return builder.toString();
	}

	@Override
	public boolean wrapsHorizontally() {
		return hWrap;
	}

	@Override
	public boolean wrapsVertically() {
		return vWrap;
	}

	protected int wrapX(int x) {
		if (hWrap) {
			if (x < 0) {
				return width() + x % width();
			} else {
				return x % width();
			}
		} else {
			return x;
		}
	}

	protected int wrapY(int y) {
		if (hWrap) {
			if (y < 0) {
				return height() + y % height();
			} else {
				return y % height();
			}
		} else {
			return y;
		}
	}

	protected int wrapZ(int z) {
		if (zWrap) {
			if (z < 0) {
				return depth() + z % depth();
			} else {
				return z % depth();
			}
		} else {
			return z;
		}
	}

	@Override
	public int depth() {
		return depth;
	}

}
