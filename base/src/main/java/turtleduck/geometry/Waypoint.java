package turtleduck.geometry;

public interface Waypoint extends XY {
	Bearing bearing();

	Point position();

	Navigator navigate();
}
