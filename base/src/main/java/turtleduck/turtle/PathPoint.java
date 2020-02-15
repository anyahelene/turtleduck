package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;

 class PathPoint implements Cloneable {
	protected Point point;
	protected Bearing bearing;
	protected Pen pen;
	protected PointType type;

	public String toString() {
		return point.toString();
	}

	public PathPoint copy() {
		try {
			return (PathPoint) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}