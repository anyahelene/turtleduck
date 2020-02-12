package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;
import turtleduck.turtle.Pen.SmoothType;

/**
 * An abstraction over the points in a path. Each {@link PointType#POINT} will
 * have one path node, and also incorporate information from the preceding and
 * succeeding {@link PointType#CONTROL} point, if any.
 * 
 */
public interface PathNode {
	/**
	 * @return True if next() != null
	 */
	boolean hasNext();

	/**
	 * @return True if prev() != null
	 */
	boolean hasPrev();

	/**
	 * @return Next node in the path
	 */
	PathNode next();

	/**
	 * @return Previous node in the path
	 */
	PathNode prev();

	/**
	 * @return The position of this node
	 */
	Point point();

	/**
	 * @return The previous control point; will be {@link #point()} if the incoming
	 *         line segment is not a Bézier curve.
	 */
	Point ctrlIn();

	/**
	 * @return The next control point; will be {@link #point()} if the outgoing line
	 *         segment is not a Bézier curve.
	 */
	Point ctrlOut();

	/**
	 * @return Angle of the incoming line segment; bearing from {@link #ctrlIn()} to
	 *         {@link #point()} for Bézier curves, or from {@link #prev()} for
	 *         straight lines; or {@link #bearingOut()} for the first node in a
	 *         path.
	 */
	Bearing bearingIn();

	/**
	 * @return Angle of the outgoing line segment; bearing from {@link #point()} to
	 *         {@link #ctrlOut()} for Bézier curves, or to {@link #next()} for
	 *         straight lines; or {@link #bearingIn()} for the last node in a path.
	 */
	Bearing bearingOut();

	/**
	 * @return Distance to {@link #ctrlIn()}; or 0 for a straight line.
	 */
	double ctrlDistIn();

	/**
	 * @return Distance to {@link #ctrlOut()}; or 0 for a straight line
	 */
	double ctrlDistOut();

	/**
	 * @return True if node has non-trivial control points, i.e.,
	 *         <code>ctrlIn() != point() || ctrlOut() != point()</code>
	 */
	boolean hasControls();

	/**
	 * @return The smoothness type of this node, determined based on the control
	 *         points
	 */
	Pen.SmoothType smoothType();

	/**
	 * @return A measure of the smoothness of the path at this point, based on the
	 *         distance to the control points
	 */
	double smoothAmount();
}