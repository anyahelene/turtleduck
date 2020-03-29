package turtleduck.display;

public interface AspectRatio {
	public static final AspectRatio ASPECT_WIDE = aspect(16, 9);
	public static final AspectRatio ASPECT_MEDIUM = aspect(16, 10);
	public static final AspectRatio ASPECT_CLASSIC = aspect(4, 3);

	public static AspectRatio aspect(int x, int y) {
		return new AspectRatio() {

			@Override
			public int x() {
				return x;
			}

			@Override
			public int y() {
				return y;
			}

			@Override
			public boolean equals(Object other) {
				
				if (this == other)
					return true;
				if (!(other instanceof AspectRatio))
					return false;
				AspectRatio otherAspect = (AspectRatio) other;
				return x() * otherAspect.y() == otherAspect.x() * y();
			}

			@Override
			public int hashCode() {
				return Double.hashCode(ratio());
			}
			
			@Override
			public String toString() {
				return String.format("%d:%d", x, y);
			}
		};
	}

	/**
	 * Aspect width.
	 * 
	 * E.g., 4 for (old) standard TV, 16 for widescreen
	 * 
	 * @return X-component of aspect ratio
	 */
	int x();

	/**
	 * Aspect height.
	 * 
	 * E.g., 3 for (old) standard TV, 9 for widescreen
	 * 
	 * @return Y-component of aspect ratio
	 */
	int y();

	/**
	 * Returns the aspect ratio – i.e., the number of width units per height unit.
	 * 
	 * E.g., 1.3333… for (old) stanard TV, 1.7777… for widescreen
	 * 
	 * @return the aspect ratio as a double
	 */
	default double ratio() {
		return ((double) x()) / ((double) y());
	}

}
