package turtleduck.paths;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.paths.Path.PointType;
import turtleduck.turtle.Annotation;

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