package turtleduck.turtle;

import java.util.Map;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;

public interface PathPoint {

	public PathPoint copy();

	public Pen pen();

	public Point point();

	public PointType type();

	public Direction bearing();

	public Direction incoming();

	public double x();

	public double y();

	public double z();
	
	<T> T annotation(Annotation<T> anno);
}