package turtleduck.turtle;

public interface SimpleTurtle {

	/**
	 * @return The current heading of the turtle, with 0° pointing to the right and
	 *         90° (π/2) pointing up. Same as {@link #heading()}.getAngle()
	 * @see {@link #heading()} – to get the heading as a direction object
	 */
	double angle();

	/**
	 * Calculate angle towards a point
	 *
	 * @param x X-coordinate of the point
	 * @param y Y-coordinate of the point
	 * @return Relative angle towards that point
	 */
	double angleTo(double x, double y);

	/**
	 * This method is used to convert the turtle to an other type, determined by the
	 * class object given as an argument.
	 * <p>
	 * This can be used to access extra functionality not provided by this
	 * interface, such as direct access to the underlying graphics context.
	 *
	 * @param clazz
	 * @return This object or an appropriate closely related object of the given
	 *         type; or <code>null</code> if no appropriate object can be found
	 */
	<T> T as(Class<T> clazz);

	/**
	 * Calculate distance to a point
	 *
	 * @param x X-coordinate of the point
	 * @param y Y-coordinate of the point
	 * @return The distance
	 */
	double distanceTo(double x, double y);

	/**
	 * Move forward while drawing a line
	 *
	 * <p>
	 * The distance is pre-set by {@link #stepSize(double, double)}.
	 * 
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle draw();

	/**
	 * Move forward the given distance while drawing a line.
	 * 
	 * <p>Negative distances will draw backwards.
	 *
	 * @param dist Relative distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle draw(double dist);

	/**
	 * Adjust heading by rotating one step to the right.
	 *
	 * 
	 * @return {@code this}, for sending more draw commands
	 * @see {@link #stepSize(double, double)}
	 */
	SimpleTurtle left();

	/**
	 * Move a distance without drawing.
	 *
	 * <p>
	 * The distance is pre-set by {@link #stepSize(double, double)}.
	 *
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle move();

	/**
	 * Move a distance without drawing.
	 *
	 * <p>Negative distances will move backwards.
	 * 
	 * @param dist Distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle move(double dist);

	/**
	 * Move to a new position, without drawing.
	 *
	 * @param dist Distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle moveTo(double x, double y);

	/**
	 * Adjust heading by rotating one step to the right.
	 *
	 * 
	 * @return {@code this}, for sending more draw commands
	 * @see {@link #stepSize(double, double)}
	 */
	SimpleTurtle right();

	/**
	 * Adjust the step size for future calls to {@link #draw()}, {@link #move()},
	 * {@link #left()} and {@link #right()}.
	 * 
	 * <p>
	 * The default is 10 for moving and 90° for turning.
	 * 
	 * @param moving  Step size to use when moving
	 * @param turning Step size to use when turning (in degrees or radians, as set
	 *                by {@link #useDegrees()} or {@link #useRadians()})
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle stepSize(double moving, double turning);

	/**
	 * Adjust heading by turning the given number of degrees (relative to the
	 * current direction).
	 *
	 * <p>
	 * Positive angles turn <em>left</em> while negative angles turn <em>right</em>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>up</em> (yaw, intrinsic
	 * Z) axis. E.g., imagine a turtle standing on four legs and turning left or
	 * right.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more draw commands
	 * @see {@link #useDegrees()}, {@link #useRadians()}
	 */
	SimpleTurtle turn(double angle);

	/**
	 * Change heading to the given angle.
	 *
	 * <p>
	 * 0 is due right, 90° (π/2 in radians) is up.
	 *
	 * @param angle Absolute heading angle
	 * @return {@code this}, for sending more draw commands
	 * @see #useDegrees(), {@link #useRadians()}
	 */
	SimpleTurtle turnTo(double angle);

	/**
	 * Measure angles in degrees (0°–360°).
	 * 
	 * This will apply to all methods accepting an angle parameter (as a
	 * <code>double</code>) or returning an angle.
	 * 
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle useDegrees();

	/**
	 * Measure angles in radians (0–2π).
	 * 
	 * This will apply to all methods accepting an angle parameter (as a
	 * <code>double</code>) or returning an angle.
	 * 
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle useRadians();

	/**
	 * @return The current X-position of the turtle.
	 */
	double x();

	/**
	 * @return The current Y-position of the turtle.
	 */
	double y();

	/**
	 * Turn on or off tracing.
	 * 
	 * <p>Tracing may be useful for debugging, and leaves behind a trail of footprints as the turtle moves.
	 * 
	 * @param enabled True if tracing should be enabled, false to disable (the default)
	 * @return {@code this}, for sending more draw commands
	 */
	SimpleTurtle trace(boolean enabled);
	
	PenBuilder<? extends SimpleTurtle> changePen();

	Pen pen();
	
	SimpleTurtle pen(Pen newPen);
}