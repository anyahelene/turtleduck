package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;

public interface PathPoint {

	public PathPoint copy();

	public Pen pen();

	public Point point();

	public PointType type();

	public Bearing bearing();
}