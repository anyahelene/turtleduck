package turtleduck.geometry;

import turtleduck.geometry.unused.XY;
import turtleduck.turtle.Navigator;

public interface Waypoint extends XY {
	Bearing bearing();

	Point position();

	Navigator navigate();
}
