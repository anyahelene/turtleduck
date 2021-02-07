package turtleduck.turtle;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.drawing.Functional;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.objects.IdentifiedObject;

public interface Chelonian<T extends Chelonian<T, C>, C> extends Functional<T>, Navigator<T>, IdentifiedObject {

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
	<U> U as(Class<U> clazz);

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
	T curveTo(Point to, double startControl, double endAngle, double endControl);

	/**
	 * Move to the given position while drawing a line
	 *
	 * @param to Position to move to
	 * @return {@code this}, for sending more draw commands
	 */
	T drawTo(Point to);

	/**
	 * Move forward the given distance while drawing a line.
	 * 
	 * <p>
	 * Negative distances will draw backwards.
	 *
	 * @param dist Relative distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	T draw(double dist);

	/**
	 * Move a distance without drawing.
	 *
	 * <p>
	 * Negative distances will move backwards.
	 * 
	 * @param dist Distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	T jump(double dist);

	/**
	 * Move a distance in the given direction, without drawing.
	 *
	 * <p>
	 * Negative distances will move backwards.
	 * 
	 * @param bearing A direction
	 * @param dist    Distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	T jump(Direction bearing, double dist);

	/**
	 * Move to a new position, without drawing.
	 *
	 * @param x New x position
	 * @param y New y position
	 * @return {@code this}, for sending more draw commands
	 */
	default T jumpTo(double x, double y) {
		return jumpTo(Point.point(x, y));
	}

	/**
	 * Move to a new position, without drawing.
	 *
	 * @param newPos New position
	 * @return {@code this}, for sending more draw commands
	 */
	T jumpTo(Point newPos);

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
	T turn(double angle);

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
	default T turnTo(double angle) {
		return bearing(Direction.absolute(angle));
	}

	PenBuilder<? extends T> penChange();

	Pen pen();

	T pen(Pen newPen);

	T pen(Stroke newPen);

	T pen(Fill newPen);

	T penColor(Color color);

	T penWidth(double width);

	/**
	 * Start painting a stroke.
	 * 
	 * Call {@link #done()} when you're done
	 * 
	 * @return {@code this}, for sending more draw commands
	 */
	T stroke();

	/**
	 * Finish with whatever we're doing, and continue with whatever we were doing
	 * 
	 * @return Either the result of the current operation, or the previous object we
	 *         were working on, for continued draw commands
	 */
	C done();

	/**
	 * Spawn a sub-turtle for making a sub-drawing.
	 * 
	 * <p>
	 * The sub-turtle will have its own position, heading and pen, all of which
	 * start off equal to that of <code>this</code> turtle. Further changes to
	 * <code>this</code> turtle will not affect the sub-turtle, and vice versa.
	 * 
	 * @return A fresh turtle, equal to this one, ready for new drawing adventures
	 */
	T spawn();

	T child(Canvas canvas);

	boolean isChild();

	T parent();

	SpriteBuilder sprite();

	DrawingBuilder drawing();

	T draw(Direction bearing, double dist);

	T onDraw(BiConsumer<PathPoint, PathPoint> action);

	T onMove(Consumer<PathPoint> action);

	T writePathsTo(PathWriter pathWriter);

	void beginPath();

	Path endPath();

	T turn(Direction dir);
	T turnTo(Direction dir);

	<U> T annotate(Annotation<U> anno, U value);

	<U> U annotation(Annotation<U> anno);

}