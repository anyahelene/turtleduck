package turtleduck.turtle.impl;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Path.PointType;

public class PathPoint implements Cloneable {
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