package turtleduck.geometry;

public interface IPoint3 extends Point {

	/** @return The Z coordinate */
	double getZ();

	/** Relative move
	 * 
	 * @param dir
	 *            Direction
	 * @param distance
	 *            Distance to move */
	IPoint3 move(Direction dir, double distance);

	/** Relative move
	 * 
	 * @param deltaX
	 * @param deltaY
	 * @return A new point at x+deltaX, y+deltaY */
	IPoint3 move(double deltaX, double deltaY);

	/** Relative move
	 * 
	 * @param deltaX
	 * @param deltaY
	 * @param deltaZ
	 * @return A new point at x+deltaX, y+deltaY, z+deltaZ */
	IPoint3 move(double deltaX, double deltaY, double deltaZ);

	/** Relative move
	 * 
	 * @param deltaPoint
	 */
	IPoint3 move(Point deltaPoint);

	/** Change point
	 * 
	 * @param newX
	 *            the new X coordinate
	 * @param newY
	 *            the new Y coordinate
	 * @return A new point at (newX, newY, getZ()) */
	IPoint3 moveTo(double newX, double newY);

	/** Change point
	 * 
	 * @param newX
	 *            the new X coordinate
	 * @param newY
	 *            the new Y coordinate
	 * @param newZ
	 *            the new Z coordinate
	 * @return A new point at (newX, newY, newZ) */
	IPoint3 moveTo(double newX, double newY, double newZ);

	/**
	 * Relative move in X-direction
	 * 
	 * @param deltaX
	 * @return A new point at (getX()+deltaX, getY(), getZ())
	 */
	IPoint3 moveX(double deltaX);

	/**
	 * Absolute move in X direction
	 * 
	 * @param newX
	 * @return A new point at (newX, getY(), getZ())
	 */
	IPoint3 moveXTo(double newX);

	/**
	 * Relative move in Y-direction
	 * 
	 * @param deltaY
	 *            (positive means "down")
	 * @return A new point at (getX(), getY()+deltaY, getZ())
	 */
	IPoint3 moveY(double deltaY);

	/**
	 * Absolute move in Y direction
	 * 
	 * @param newZ
	 * @return A new point at (getX(), newY, getZ())
	 */
	IPoint3 moveYTo(double newY);
	
	/**
	 * Relative move in Z-direction
	 * 
	 * @param deltaY
	 *            (positive means "down")
	 * @return A new point at (getX(), getY(), getZ()+deltaZ)
	 */
	IPoint3 moveZ(double deltaZ);

	/**
	 * Absolute move in Z direction
	 * 
	 * @param newZ
	 * @return A new point at (getX(), getY(), newZ)
	 */
	IPoint3 moveZTo(double newZ);
}