package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

@Icon("🧭")
public interface Navigator<T extends Navigator<T>> extends BaseNavigator<T> {

	/**
	 * Set the current position
	 * 
	 * @param x the new X position
	 * @param y the new Y position
	 * @return {@code this}, for sending more draw commands
	 */
	default T goTo(double x, double y) {
		return goTo(Point.point(x, y));
	}

	/**
	 * Set the current position
	 * 
	 * @param p the new position
	 * @return {@code this}, for sending more draw commands
	 */
	default T goTo(Point p) {
		return go(p, RelativeTo.WORLD);
	}

	/**
	 * Turn to face the given point.
	 *
	 * <p>
	 * Afterwards, <code>turtle.forward(turtle.distanceTo(to))</code> should leave
	 * the turtle position at point <code>to</code>.
	 * 
	 * @param to the Point to turn towards
	 * @return {@code this}, for sending more draw commands
	 */
	default T turnTo(PositionVector to, RelativeTo rel) {
		Point p = findPoint(to, rel);
		point().directionTo(p);
		return direction(point().directionTo(p));
	}

	/**
	 * Adjust bearing by turning left given number of degrees
	 *
	 * <p>
	 * Positive angles turn <em>left</em> while negative angles turn <em>right</em>.
	 * Same as <code>right(-degrees)</code>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>up</em> (yaw, intrinsic
	 * Z) axis. E.g., imagine a turtle standing on four legs and turning left or
	 * right.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more commands
	 */
	default T left(double degrees) {
		return direction(Direction.relative(-degrees));
	}

	/**
	 * Adjust bearing by turning right given number of degrees
	 *
	 * <p>
	 * Positive angles turn <em>right</em> while negative angles turn <em>left</em>.
	 * Same as <code>left(-degrees)</code>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>up</em> (yaw, intrinsic
	 * Z) axis. E.g., imagine a turtle standing on four legs and turning left or
	 * right.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more commands
	 */
	default T right(double degrees) {
		return direction(Direction.relative(degrees));
	}

	/**
	 * Move forward.
	 *
	 * <p>
	 * Negative distances will move backwards.
	 * 
	 * @param dist Distance to move
	 * @return {@code this}, for sending more commands
	 */
	default T forward(double distance) {
		return go(Point.point(0, distance), RelativeTo.SELF);
	}

	/**
	 * Move a distance in the given direction
	 *
	 * <p>
	 * Negative distances will move backwards.
	 * 
	 * @param dir A direction
	 * @param dist    Distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	default T go(Direction dir, double dist) {
		return go(Point.ZERO.add(dir, dist), RelativeTo.POSITION);
	}
}
