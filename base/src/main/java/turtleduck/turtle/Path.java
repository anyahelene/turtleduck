package turtleduck.turtle;

import java.util.List;

import turtleduck.colors.Paint;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.RelativeTo;
import turtleduck.turtle.Pen.SmoothType;

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

	Pen.SmoothType pointSmooth(int i);

	Iterable<PathNode> nodes();

	Iterable<PathNode> nodes(PathNode dest);

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
		public Pen.SmoothType pointSmooth(int i) {
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
