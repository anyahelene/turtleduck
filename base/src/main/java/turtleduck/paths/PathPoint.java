package turtleduck.paths;

import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.paths.Path.PointType;
import turtleduck.turtle.Annotation;

public interface PathPoint {

	public PathPoint copy();

	public Pen pen();

	public Point point();

	public PointType type();

//	public Direction bearing();

//	public Direction incoming();

	public Vector3fc position();

	public Orientation orientation();

	public void position(Vector3f dest);

	public void normal(Vector3f dest);

	public double x();

	public double y();

	public double z();

	<T> T annotation(Annotation<T> anno);
}