package turtleduck.geometry;

import turtleduck.geometry.unused.XY;
import turtleduck.turtle.Navigator;

public interface Waypoint extends XY {
	Direction bearing();

	Point position();

	Navigator navigate();
}
