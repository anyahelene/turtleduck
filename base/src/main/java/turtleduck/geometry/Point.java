package turtleduck.geometry;

import turtleduck.geometry.impl.Point2;
import turtleduck.geometry.impl.Point3;

public interface Point {

	static Point point(double x, double y) {
		return new Point2(x, y);
	}
	static IPoint3 point(double x, double y, double z) {
		return new Point3(x, y, z);
	}
	
	/**
	 * Calculate direction towards other point
	 *
	 * @param otherPoint
	 * @return
	 */
	Direction directionTo(Point otherPoint);
	/**
	 * Calculate direction towards other point
	 *
	 * @param otherPoint
	 * @return
	 */
	double angleTo(Point otherPoint);
	/**
	 * Calculate direction towards other point
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	double angleTo(double x, double y);

	/**
	 * Calculate distance to other point
	 *
	 * @param otherPoint
	 * @return
	 */
	double distanceTo(Point otherPoint);

	/**
	 * @return The X coordinate
	 */
	double getX();

	/**
	 * @return The Y coordinate
	 */
	double getY();

	/**
	 * @return The Z coordinate (normally zero)
	 */
	double getZ();

	/**
	 * Relative move
	 *
	 * @param dir
	 *            Direction
	 * @param distance
	 *            Distance to move
	 */
	Point move(Direction dir, double distance);

	/**
	 * Relative move
	 *
	 * @param deltaX
	 * @param deltaY
	 * @return A new point at x+deltaX, y+deltaY
	 */
	Point move(double deltaX, double deltaY);

	/**
	 * Relative move
	 *
	 * @param deltaPoint
	 */
	Point move(Point deltaPoint);

	/**
	 * Change point
	 *
	 * @param newX
	 *            the new X coordinate
	 * @param newY
	 *            the new Y coordinate
	 * @return A new point at newX, newY
	 */
	Point moveTo(double newX, double newY);

	/**
	 * Relative move in X-direction
	 *
	 * @param deltaX
	 * @return A new point at (getX(), getY())
	 */
	Point moveX(double deltaX);

	/**
	 * Absolute move in X direction
	 *
	 * @param newX
	 * @return A new point at (newX, getY())
	 */
	Point moveXTo(double newX);

	/**
	 * Relative move in Y-direction
	 *
	 * @param deltaY
	 *            (positive means "down")
	 * @return A new point at (getX(), getY()+deltaY)
	 */
	Point moveY(double deltaY);

	/**
	 * Absolute move in Y direction
	 *
	 * @param newX
	 * @return A new point at (getX(), newY)
	 */
	Point moveYTo(double newY);
	/**
	 * Relative move in Z-direction
	 *
	 * @param deltaZ
	 *            (positive means "down")
	 * @return A new point at (getX(), getY(), getZ()+deltaY)
	 */
	Point moveZ(double deltaZ);

	/**
	 * Absolute move in Z direction
	 *
	 * @param newZ
	 * @return A new point at (getX(), getY(), newZ)
	 */
	Point moveZTo(double newZ);

	@Override
	String toString();

	/**
	 * Multiply this point by a scale factor.
	 *
	 * @param factor
	 *            A scale factor
	 * @return A new IPoint, (getX()*factor, getY()*factor)
	 */
	Point scale(double factor);

	/**
	 * Find difference between points.
	 * <p>
	 * The returned value will be such that
	 * <code>this.move(deltaTo(point)).equals(point)</code>.
	 *
	 * @param point
	 *            Another point
	 * @return A new IPoint, (point.getX()-getX(), point.getY()-getY())
	 */
	Point deltaTo(Point point);

	Direction asDirection();

	double asLength();

	double distanceToSq(Point otherPoint);

}