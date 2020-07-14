package turtleduck.turtle.impl;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.Pen;

public class PathPointImpl implements PathPoint, Cloneable {
	protected Point point;
	protected Bearing bearing;
	protected Pen pen;
	protected PointType type;

	public String toString() {
		return point.toString();
	}

	public PathPointImpl copy() {
		try {
			return (PathPointImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Pen pen() {
		return pen;
	}
	
	public Point point() {
		return point;
	}
	public PointType type() {
		return type;
	}
	public Bearing bearing() {
		return bearing;
	}
}