package turtleduck.turtle;

import java.util.List;

import turtleduck.colors.Paint;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.RelativeTo;
import turtleduck.turtle.Path.SmoothType;

public interface Path {
	/**
	 * Describes how positions and bearings are measured, unless otherwise noted –
	 * either relative to the {@link #FIRST} node in the path, or the
	 * {@link #CURRENT} (last added) node, or the screen {@link #ORIGIN}.
	 *
	 */
	public enum RelativeTo {
		/**
		 * Points are relative to the first point in the path; bearings are relative to
		 * the starting direction. In this relative coordinate system, the first point
		 * is at (0,0) and the initial direction is 0° ({@link Bearing#FORWARD}).
		 */
		FIRST,
		/**
		 * Points are relative to the current point in the path and bearings are
		 * relative to the current direction. In this relative coordinate system, the
		 * current point is always (0,0) and the bearing is 0°
		 * ({@link Bearing#FORWARD}).
		 */
		CURRENT,
		/**
		 * Coordinates are absolute, with points relative to (0,0) in the screen
		 * coordinate system, and bearings are measured agains
		 * {@link Bearing#DUE_NORTH}.
		 */
		ORIGIN
	}

	/**
	 * Describes whether a point is a control point or a mid/end-point.
	 *
	 */
	public enum PointType {
		/**
		 * The point is an end-point of a line or curve segment; i.e. the path passed
		 * through this point.
		 */
		POINT,
		/**
		 * The point is a control point of a
		 * <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">Bézier curve</a>. A
		 * quadratic curve has one control point, while a cubic curve has two. The path
		 * won't pass through the control point, but head towards it before veering off
		 * in the direction of the next point.
		 */
		CONTROL
	};

	/**
	 * Describes the smoothness of a path as it passes through a point. When
	 * building a path, this can be used to create smooth paths without explicitly
	 * adding control points.
	 * <p>
	 * Points on straight-line
	 * (non-<a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">Bézier</a>)
	 * paths are always {@link #CORNER}.
	 * <ul>
	 * <li>A {@link #CORNER} has a sharp break at any angle; the direction of the
	 * incoming and outgoing line segments are unrelated. This is a
	 * <em>C<sup>0</sup></em> continuous curve; the curve itself is continuous, but
	 * the first derivative is not.
	 * <li>A {@link #SMOOTH} point will have the incoming and outgoing line segments
	 * form a straight line at that point; the control points will be on a straight
	 * line on opposite sides of the point itself. This is a <em>C<sup>1</sup></em>
	 * continuous curve; the curve itself and the first derivative is continuous.
	 * <li>A {@link #SYMMETRIC} is smooth, but with the same distance to the control
	 * points on each side.This is a <em>C<sup>2</sup></em> continuous curve; the
	 * curve itself, the first and the second derivative is continuous.
	 * <ul>
	 * 
	 */
	public enum SmoothType {
		/**
		 * <li>A {@link #CORNER} has a sharp break at any angle; the direction of the
		 * incoming and outgoing line segments are unrelated. This is a <a href=
		 * "https://en.wikipedia.org/wiki/Smoothness#Parametric_continuity"><em>C<sup>0</sup></em>
		 * continuous curve</a>; the curve itself is continuous, but the first
		 * derivative is not.
		 */
		CORNER,
		/**
		 * <li>A {@link #SMOOTH} point will have the incoming and outgoing line segments
		 * form a straight line at that point; the control points will be on a straight
		 * line on opposite sides of the point itself. This is a <a href=
		 * "https://en.wikipedia.org/wiki/Smoothness#Parametric_continuity"><em>C<sup>1</sup></em>
		 * continuous curve</a>; the curve itself and the first derivative is
		 * continuous.
		 */
		SMOOTH,
		/**
		 * A {@link #SYMMETRIC} point is also {@link #SMOOTH}, but with the same
		 * distance to the control points on each side.This is a <a href=
		 * "https://en.wikipedia.org/wiki/Smoothness#Parametric_continuity"><em>C<sup>1</sup></em>
		 * continuous curve</a>; the curve itself, the first and the second derivative
		 * is continuous.
		 * 
		 */
		SYMMETRIC
	}

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
		SmoothType smoothType();

		/**
		 * @return A measure of the smoothness of the path at this point, based on the
		 *         distance to the control points
		 */
		double smoothAmount();
	}

	/**
	 * @return The first point in the path (given to
	 *         {@link PathBuilder#beginPath(Point, Bearing, RelativeTo)}
	 */
	Point first();

	double pointWidth(int i);

	Paint pointColor(int i);

	int size();

	Iterable<PathPoint> points();

	/**
	 * @return The most recently added (i.e., current or last) point in the path
	 */
	Point last();

	Point point(int i);

	PointType pointType(int i);

	SmoothType pointSmooth(int i);

	Iterable<PathNode> nodes();

	Iterable<PathNode> nodes(PathNode dest);

	public class PathPoint {
		protected Point point;
		protected Bearing bearing;
		protected PointType type;
		protected SmoothType smoothType = SmoothType.CORNER;
		protected double smoothAmount = 0.0;
		protected Paint color;
		protected double width = 0.0;

		public String toString() {
			return point.toString();
		}
	}

	public class PathImpl implements Path {
		private List<PathPoint> points;

		protected PathImpl(List<PathPoint> points) {
			this.points = points;
		}

		@Override
		public Point first() {
			return points.get(0).point;
		}

		@Override
		public Point last() {
			return points.get(points.size() - 1).point;
		}

		@Override
		public Point point(int i) {
			return points.get(i).point;
		}

		@Override
		public PointType pointType(int i) {
			return points.get(i).type;
		}
		@Override
		public Paint pointColor(int i) {
			return points.get(i).color;
		}
		@Override
		public double pointWidth(int i) {
			return points.get(i).width;
		}
		@Override
		public SmoothType pointSmooth(int i) {
			return points.get(i).smoothType;
		}
		@Override
		public Iterable<PathPoint> points() {
			return points;
		}

		@Override
		public Iterable<PathNode> nodes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterable<PathNode> nodes(PathNode dest) {
			// TODO Auto-generated method stub
			return null;
		}

		public String toString() {
			return points.toString();
		}
		@Override
		public int size() {
			return points.size();
		}
	}
}
