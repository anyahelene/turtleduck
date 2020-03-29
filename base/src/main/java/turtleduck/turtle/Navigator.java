package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;
import turtleduck.geometry.Waypoint;

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

	Navigator pen(Pen pen);

	Pen pen();

	Waypoint waypoint();

	default double distanceTo(PositionVector dest) {
		return position().distanceTo(dest);
	}

	void beginPath();

	Path endPath();

	boolean isAt(PositionVector point);

}
