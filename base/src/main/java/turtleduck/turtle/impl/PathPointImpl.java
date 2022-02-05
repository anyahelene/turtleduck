package turtleduck.turtle.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.paths.PathPoint;
import turtleduck.paths.Pen;
import turtleduck.paths.Path.PointType;
import turtleduck.turtle.Annotation;

public class PathPointImpl implements PathPoint, Cloneable {
	public Point point;
	public Direction bearing;
	public Pen pen;
	public PointType type;
	public Direction incoming;
	public String rotation = "";
	public Map<Annotation<?>, Object> annos;

//	public String toString() {
//		return point.toString();
//	}
	public PathPointImpl() {
	}

	public PathPointImpl(Point point, Pen pen) {
		this.point = point;
		this.pen = pen;
	}

	public PathPointImpl(Point point, Pen pen, Direction bearing, Direction incoming) {
		this.point = point;
		this.pen = pen;
		this.bearing = bearing;
		this.incoming = incoming;
	}

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
		if (annos == null) {
			return null;
		}
		return (T) annos.get(anno);
	}
	
	public <T> void annotation(Annotation<T> anno, T val) {
		if(annos == null) {
			annos = new IdentityHashMap<>();
		}
		annos.put(anno, val);
	}

}