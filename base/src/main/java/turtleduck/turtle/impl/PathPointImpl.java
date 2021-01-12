package turtleduck.turtle.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.turtle.Annotation;
import turtleduck.turtle.Path.PointType;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.Pen;

public class PathPointImpl implements PathPoint, Cloneable {
	protected Point point;
	protected Direction bearing;
	public Pen pen;
	protected PointType type;
	protected Direction incoming;
	protected String rotation = "";
	protected Map<Annotation, Object> annos;

//	public String toString() {
//		return point.toString();
//	}

	public PathPointImpl copy() {
		try {
			PathPointImpl copy = (PathPointImpl) super.clone();
			copy.rotation = "";
			copy.annos = null;
			return copy;
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

	public Direction bearing() {
		return bearing;
	}

	public Direction incoming() {
		return incoming;
	}

	@Override
	public double x() {
		return point.x();
	}

	@Override
	public double y() {
		return point.y();
	}

	@Override
	public double z() {
		return point.z();
	}
	public String toString() {
		return rotation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T annotation(Annotation<T> anno) {
		if(annos == null) {
			return null;
		}
		return (T) annos.get(anno);
	}
	

}