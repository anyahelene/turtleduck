package turtleduck.geometry;

public interface BoundingBox extends Box {

	/**
	 * Extend the bounding box to include the given point.
	 * 
	 * <p>
	 * Does nothing if the bounding box already contains the point.
	 * 
	 * @param point A point
	 * @return An extended bounding box
	 */
	BoundingBox extend(Point point);

	/**
	 * Extend the bounding box to include the given box.
	 * 
	 * <p>
	 * Does nothing if the bounding box already contains all four corners of the
	 * box.
	 * 
	 * @param box A box
	 * @return An extended bounding box
	 */
	BoundingBox extend(Box box);
}
