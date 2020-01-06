package turtleduck.geometry;

import turtleduck.geometry.unused.XY;

public interface Waypoint extends XY {
	Bearing bearing();

	Point position();

	Navigator navigate();
}
