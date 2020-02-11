package turtleduck.geometry;

import turtleduck.turtle.Path;
import turtleduck.turtle.PathBuilder;
import turtleduck.turtle.Path.PointType;
import turtleduck.turtle.Path.RelativeTo;

public interface Navigator extends PositionVector, DirectionVector {

	Bearing bearing();

	Bearing bearing(int index);

	Point position();

	Point position(int index);

	Navigator left(double degrees);

	Navigator right(double degrees);

	Navigator forward(double distance);

	Navigator copy();

	Navigator face(PositionVector dest);

	Navigator face(Bearing dest);

	Navigator go(double radians, double distance);

	Navigator goTo(Waypoint dest);

	Navigator goTo(Point dest);

	Waypoint waypoint();

	default double distanceTo(PositionVector dest) {
		return position().distanceTo(dest);
	}

	PathBuilder beginPath();

	Path endPath();

	static class DefaultNavigator implements Navigator, Cloneable {
		Point point = Point.point(0, 0), point1 = point, point2 = point;
		Bearing bearing = Bearing.absolute(0), bearing1 = bearing, bearing2 = bearing;
		PathBuilder builder;

		@Override
		public Bearing bearing() {
			return bearing;
		}

		@Override
		public Point position() {
			return point;
		}

		@Override
		public Navigator left(double degrees) {
			if (degrees != 0) {
				bearing(Bearing.relative(-degrees));
			}
			return this;
		}

		@Override
		public Navigator right(double degrees) {
			if (degrees != 0) {
				bearing(Bearing.relative(degrees));
			}
			return this;
		}

		@Override
		public Navigator forward(double distance) {
			point2 = point1;
			point1 = point;
			if (distance != 0) {
				point = point.add(bearing, distance);
			}
			if (builder != null)
				builder.add(bearing, point, PointType.POINT);

			return this;
		}

		protected void bearing(Bearing b) {
//			if (recorder != null)
//				recorder.turn(b.azimuth());
			bearing2 = bearing1;
			bearing1 = bearing;
			bearing = bearing.add(b);
		}

		@Override
		public Navigator face(PositionVector dest) {
			bearing(Bearing.absolute(dest.x() - point.x(), dest.y() - point.y()).sub(bearing));
			return this;
		}

		@Override
		public Navigator face(Bearing dest) {
			if (dest.isAbsolute())
				bearing(dest.sub(bearing));
			else
				bearing(dest);
			return this;
		}

		@Override
		public Navigator go(double radians, double distance) {
			bearing(Bearing.relative(radians));
			forward(distance);
			return this;
		}

		@Override
		public Navigator goTo(Waypoint dest) {
			face(dest.position());
			forward(point.distanceTo(dest.position()));
			bearing(dest.bearing().sub(bearing));
			return this;
		}

		@Override
		public Waypoint waypoint() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Navigator copy() {
			try {
				return (Navigator) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public double x() {
			return point.x();
		}

		@Override
		public double y() {
			return point.y();
		}

		@Override
		public double z() {
			return point.z();
		}

		@Override
		public double dirX() {
			return bearing.dirX();
		}

		@Override
		public double dirY() {
			return bearing.dirY();
		}

		@Override
		public double dirZ() {
			return bearing.dirZ();
		}

		@Override
		public Navigator goTo(Point dest) {
			face(dest);
			forward(point.distanceTo(dest));
			return this;
		}

		@Override
		public PathBuilder beginPath() {
			builder = PathBuilder.beginPath(point, bearing, RelativeTo.ORIGIN);
			return builder;
		}
		@Override
		public Path endPath() {
			if(builder == null)
				throw new IllegalStateException("endPath() without beginPath()");
			Path path = builder.done();
			builder = null;
			return path;
		}

		@Override
		public boolean isAt(PositionVector p) {
			return x() == p.x() && y() == p.y() && z() == p.z();
		}

		@Override
		public Bearing bearing(int index) {
			switch (index) {
			case 0:
				return bearing;
			case 1:
				return bearing1;
			case 2:
				return bearing2;
			default:
				throw new IndexOutOfBoundsException(index);
			}
		}

		@Override
		public Point position(int index) {
			switch (index) {
			case 0:
				return point;
			case 1:
				return point1;
			case 2:
				return point2;
			default:
				throw new IndexOutOfBoundsException(index);
			}
		}

	}

	boolean isAt(PositionVector point);
}
