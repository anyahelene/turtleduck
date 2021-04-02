package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.geometry.Point;

@Icon("🐢")
public interface Chelonian3<T extends Chelonian3<T, C>, C> extends Chelonian<T,C> {
	/**
	 * Move to a new position, without drawing.
	 *
	 * @param x New x position
	 * @param y New y position
	 * @param z New z position
	 * @return {@code this}, for sending more draw commands
	 */
	default T jumpTo(double x, double y, double z) {
		return jumpTo(Point.point(x, y,z));
	}
	
	/**
	 * Set the current position
	 * 
	 * @param x the new X position
	 * @param y the new Y position
	 * @param z the new Z position
	 * @return {@code this}, for sending more draw commands
	 */
	default T at(double x, double y, double z) {
		return goTo(Point.point(x, y, z));
	}
	
	
	/**
	 * Adjust heading by climbing the given number of degrees (relative to the
	 * current direction).
	 *
	 * <p>
	 * Positive angles climb <em>upwards</em> while negative angles dive <em>downwards</em>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>right</em> (pitch, intrinsic
	 * X) axis. E.g., imagine a turtle rising up on its hind legs.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more draw commands
	 * @see {@link #useDegrees()}, {@link #useRadians()}
	 */
	T pitch(double angle);
	/**
	 * Adjust heading by climbing the given number of degrees (relative to the
	 * current direction).
	 *
	 * <p>
	 * Positive angles climb <em>upwards</em> while negative angles dive <em>downwards</em>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>right</em> (pitch, intrinsic
	 * X) axis. E.g., imagine a turtle rising up on its hind legs.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more draw commands
	 * @see {@link #useDegrees()}, {@link #useRadians()}
	 */
	T roll(double angle);
}
