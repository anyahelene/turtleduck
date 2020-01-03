package turtleduck.geometry;

import turtleduck.turtle.CommandRecorder;

public interface Navigator extends PositionVector, DirectionVector {

	Bearing bearing();

	Point position();

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

	Navigator recordTo(CommandRecorder recorder);

	static class DefaultNavigator implements Navigator, Cloneable {
		Point point = Point.point(0, 0);
		Bearing bearing = Bearing.absolute(0);
		CommandRecorder recorder;

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
			if (distance != 0) {
				point = point.add(bearing, distance);
			}
			if (recorder != null)
				recorder.move(distance);
			return this;
		}

		protected void bearing(Bearing b) {
			if (recorder != null)
				recorder.turn(b.azimuth());
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
		public Navigator recordTo(CommandRecorder recorder) {
			this.recorder = recorder;
			return this;
		}

	}
}
