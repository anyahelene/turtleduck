package turtleduck.turtle;

import turtleduck.colors.Paint;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;
import turtleduck.turtle.Pen.SmoothType;

public class PathPoint {
	protected Point point;
	protected Bearing bearing;
	protected Pen pen;
	protected PointType type;
	protected Pen.SmoothType smoothType = Pen.SmoothType.CORNER;
	protected double smoothAmount = 0.0;
	protected Paint color;
	protected double width = 0.0;

	public String toString() {
		return point.toString();
	}
}