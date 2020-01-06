package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.geometry.unused.Orientation;

public interface TurtleDuck extends SimpleTurtle {

	/**
	 * Move to the given position while drawing a curve
	 *
	 * <p>
	 * The resulting curve is a cubic Bézier curve with the control points located
	 * at <code>getPos().move(getBearing, startControl)</code> and
	 * <code>to.move(Bearing.fromDegrees(endAngle+180), endControl)</code>.
	 * <p>
	 * The turtle is left at point <code>to</code>, facing <code>endAngle</code>.
	 * <p>
	 * The turtle will start out moving in its current direction, aiming for a point
	 * <code>startControl</code> pixels away, then smoothly turning towards its
	 * goal. It will approach the <code>to</code> point moving in the direction
	 * <code>endAngle</code> (an absolute bearing, with 0° pointing right and 90°
	 * pointing up).
	 *
	 * @param to           Position to move to
	 * @param startControl Distance to the starting control point.
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck curveTo(Point to, double startControl, double endAngle, double endControl);

	void debugTurtle();

	/**
	 * Calculate direction towards a point
	 *
	 * @param point A point
	 * @return Bearing towards that point
	 */
//	Bearing directionTo(Point point);

	/**
	 * Calculate distance to a point
	 *
	 * @param point
	 * @return The distance
	 */
//	double distanceTo(Point point);

	@Override
	TurtleDuck draw(double dist);

	/**
	 * Turn <code>angle</code> degrees, then draw a line <code>dist</code> long.
	 *
	 * @param angle Relative angle to turn
	 * @param dist  Relative distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck draw(double angle, double dist);

	/**
	 * Move to the given relative position while drawing a line
	 * <p>
	 * The new position will be equal to getPos().move(relPos).
	 *
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck draw(Point relPos);

	/**
	 * Move to the given position while drawing a line
	 *
	 * @param x X-position to move to
	 * @param y Y-position to move to
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck drawTo(double x, double y);

	@Override
	TurtleDuck drawTo(Point to);

	/**
	 * @return The current direction of the turtle.
	 * @see {@link #angle()} – to get the heading as an angle
	 */
	Bearing heading();

	@Override
	TurtleDuck move(double dist);

	/**
	 * Jump (without drawing) to the given relative position.
	 * <p>
	 * The new position will be equal to getPos().move(relPos).
	 *
	 * @param relPos A position, interpreted relative to current position
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck move(Point relPos);

	@Override
	TurtleDuck moveTo(double x, double y);

	/**
	 * Reposition the turtle without without drawing.
	 *
	 * @param to position to move to
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck moveTo(Point to);

	/**
	 * Get the turtle's orientation in 3D space.
	 * 
	 * The orientation combines the turtle's heading (the direction it's facing)
	 * with its sense of which direction is ‘up’.
	 * 
	 * @return The current orientation of the turtle.
	 * @see {@link #heading()} – to get the heading
	 */
	Orientation orientation();

	/**
	 * Set the turtle's orientation in 3D space.
	 * 
	 * The orientation combines the turtle's heading (the direction it's facing)
	 * with its sense of which direction is ‘up’.
	 * 
	 * @param orient The new orientation of the turtle.
	 * @return {@code this}, for sending more draw commands
	 * @see {@link #heading()} – to get the heading
	 */
	TurtleDuck orientation(Orientation orient);

	/**
	 * Adjust heading pitch by turning the given number of degrees (relative to the
	 * current direction). Only relevant in 3D environments.
	 *
	 * <p>
	 * Positive angles turn <em>up</em> while negative angles turn <em>down</em>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>left-right</em>
	 * (intrinsic X) axis. E.g., imagine a turtle standing on four legs and turning
	 * to go up or down a hill.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more draw commands
	 * @see #useDegrees(), {@link #useRadians()}
	 */
	TurtleDuck pitch(double angle);

	@Override
	TurtleDuck child();
	
	@Override
	TurtleDuck parent();

	/**
	 * Begin constructing a path at the current turtle position.
	 * 
	 * @return Path builder
	 */
	TurtlePathBuilder path();

	/**
	 * @return The current position of the turtle.
	 */
	Point position();

	/**
	 * Adjust turtle's orientation by rolling the given number of degrees. Only
	 * relevant in 3D environments.
	 *
	 * <p>
	 * Positive angles roll <em>left/counter-clockwise</em> while negative angles
	 * roll <em>right/clockwise</em>.
	 *
	 * <p>
	 * This method will rotate the turtle around its own <em>head-tail</em>
	 * (intrinsic Y) axis.
	 * 
	 * @param angle Adjustment
	 * @return {@code this}, for sending more draw commands
	 * @see #useDegrees(), {@link #useRadians()}
	 */
	TurtleDuck roll(double angle);

	/**
	 * Start drawing a shape at the current turtle position.
	 *
	 * <p>
	 * The shape's default origin and rotation will be set to the turtle's current
	 * position and direction, but can be modified with {@link IShape#at(Point)} and
	 * {@link IShape#rotation(double)}.
	 * <p>
	 * The turtle's position and attributes are unaffected by drawing the shape.
	 *
	 * @return An IDrawParams object for setting up and drawing the shape
	 */
	IShape shape();

	@Override
	TurtleDuck turn(double angle);

	/**
	 * Turn 180°.
	 *
	 * <p>
	 * Same as <code>turn(180)</code> and <code>turn(-180)</code>.
	 *
	 * @return {@code this}, for sending more draw commands
	 */
	default TurtleDuck turnAround() { return turn(180); }

	/**
	 * Set the current heading of the turtle.
	 */
	TurtleDuck turnTo(Bearing dir);

	@Override
	TurtleDuck turnTo(double angle);

	/**
	 * Turn to face the given point.
	 *
	 * <p>
	 * Should give same result as
	 * <code>turtle.turnTo(turtle.directionTo(to))</code>. Afterwards,
	 * <code>turtle.move(turtle.distanceTo(to))</code> should leave the turtle
	 * position at point <code>to</code>.
	 * 
	 * @param to Point to turn to
	 * @return {@code this}, for sending more draw commands
	 */
	TurtleDuck turnTowards(Point to);

	/*@Override
	TurtleDuck useDegrees();

	@Override
	TurtleDuck useRadians();
*/
	@Override
	TurtleDuck trace(boolean enabled);
	@Override
	PenBuilder<TurtleDuck> changePen();
	@Override
	TurtleDuck pen(Pen newPen);
	TurtleDuck pen(Stroke newPen);
	TurtleDuck pen(Fill newPen);

	TurtleMark mark(String name);

	TurtleDuck fill();

	TurtleDuck fillAndStroke();
	TurtleDuck child(Canvas canvas);

}