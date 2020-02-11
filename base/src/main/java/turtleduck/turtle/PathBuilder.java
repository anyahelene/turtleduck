package turtleduck.turtle;

import java.util.ArrayList;
import java.util.List;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PathImpl;
import turtleduck.turtle.Path.PathPoint;
import turtleduck.turtle.Path.PointType;
import turtleduck.turtle.Path.RelativeTo;
import turtleduck.turtle.Path.SmoothType;

public interface PathBuilder {
	static PathBuilder beginPath(Point from, Bearing bearing, RelativeTo spec) {
		return new PathBuilderImpl(from, bearing, spec);
	}

	PathBuilder relativeTo(RelativeTo spec);

	RelativeTo relativeTo();

	/**
	 * Add another point to the path, specified relative to the given
	 * <code>from</code> point.
	 * 
	 * @param bearing
	 * 
	 * @param from     Point to calculate the next point from; {@link #last()} if
	 *                 <code>null</null>
	 * &#64;param bearing  Direction in which the path leaves the current point,
	 *                 measured as per {@link #relativeTo(RelativeTo)}; if
	 *                 <code>null</code>, calculated as needed from
	 *                 <code>from</code> and <code>to</code>
	 * @param distance Distance to travel to reach the next point; if
	 *                 <code>bearing == null</code>, calculated as needed from
	 *                 <code>from</code> and <code>to</code>
	 * @return <code>this</code>
	 */
	PathBuilder add(Bearing bearing, Point to, PointType type);

	PathBuilder smooth(SmoothType smooth);

	PathBuilder smooth(SmoothType smooth, double amount);

	PathBuilder color(Paint color);

	PathBuilder width(double width);

	SmoothType smoothType();

	double smoothAmount();

	Path done();

	public class PathBuilderImpl implements PathBuilder {
		protected List<PathPoint> points = new ArrayList<>();
		private Point firstPoint, currentPoint;
		private Bearing firstBearing, currentBearing;
		private RelativeTo rel;
		private SmoothType smoothType = SmoothType.CORNER;
		private double smoothAmount = 0.0;
		private Paint color = Colors.TRANSPARENT;
		private double width = 1;

		public PathBuilderImpl(Point from, Bearing bearing, RelativeTo spec) {
			firstPoint = currentPoint = from;
			firstBearing = currentBearing = bearing;
			this.rel = spec;
		}

		@Override
		public PathBuilder relativeTo(RelativeTo spec) {
			rel = spec;
			return this;
		}

		@Override
		public RelativeTo relativeTo() {
			return rel;
		}

		@Override
		public PathBuilder add(Bearing bearing, Point to, PointType type) {
			if (points.isEmpty())
				addFirst();
			switch (rel) {
			case CURRENT:
				if (bearing != null)
					currentBearing = bearing.add(currentBearing);
				else
					currentBearing = currentPoint.bearingTo(to.add(currentPoint));
				currentPoint = to.add(currentPoint);
				break;
			case FIRST:
				if (bearing != null)
					currentBearing = bearing.add(firstBearing);
				else
					currentBearing = currentPoint.bearingTo(to.add(firstPoint));
				currentPoint = to.add(firstPoint);
				break;
			case ORIGIN:
				if (bearing != null)
					currentBearing = bearing;
				else
					currentBearing = currentPoint.bearingTo(to);
				currentPoint = to;
				break;
			}
			PathPoint p = new PathPoint();
			p.bearing = currentBearing;
			p.point = currentPoint;
			p.smoothAmount = smoothAmount;
			p.smoothType = smoothType;
			p.type = type;
			p.color = color;
			p.width = width;
			points.add(p);
			return this;
		}

		protected void addFirst() {
			PathPoint p = new PathPoint();
			p.bearing = firstBearing;
			p.point = firstPoint;
			p.smoothAmount = smoothAmount;
			p.smoothType = smoothType;
			p.type = PointType.POINT;
			p.color = color;
			p.width = width;
			points.add(p);
		}

		@Override
		public PathBuilder smooth(SmoothType smooth) {
			this.smoothType = smooth;
			return this;
		}

		@Override
		public PathBuilder smooth(SmoothType smooth, double amount) {
			this.smoothType = smooth;
			this.smoothAmount = amount;
			return this;
		}

		@Override
		public SmoothType smoothType() {
			return smoothType;
		}

		@Override
		public double smoothAmount() {
			return smoothAmount;
		}

		@Override
		public Path done() {
			PathImpl impl = new Path.PathImpl(points);
			points = null;
			return impl;
		}

		@Override
		public PathBuilder color(Paint color) {
			this.color = color;
			return this;
		}

		@Override
		public PathBuilder width(double width) {
			this.width = width;
			return this;
		}

	}
}
