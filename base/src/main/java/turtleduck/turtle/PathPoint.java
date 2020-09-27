package turtleduck.turtle;

import org.joml.Vector3f;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path.PointType;

public interface PathPoint {

	public PathPoint copy();

	public Pen pen();

	public Point point();

	public PointType type();

	public Direction bearing();

	public double x();
	public double y();
	public double z();
}