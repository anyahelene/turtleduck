package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;

public interface TurtleControl {
	boolean validationMode();

	TurtleControl validationMode(boolean enable);

	TurtleControl begin(Stroke stroke, Fill fill);

	TurtleControl pen(Stroke stroke, Fill fill);

	TurtleControl turn(Bearing from, double degrees, Bearing to);

	TurtleControl go(Bearing bearing, Point from, double distance, Point to);

	TurtleControl control(Bearing heading, Point from, double distance, Point to);

	TurtleControl cancel();

	TurtleControl end();

	TurtleControl child();
}
