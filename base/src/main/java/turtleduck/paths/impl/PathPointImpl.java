package turtleduck.paths.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.geometry.impl.AngleImpl;
import turtleduck.paths.PathPoint;
import turtleduck.paths.Pen;
import turtleduck.paths.Path.PointType;
import turtleduck.turtle.Annotation;

public class PathPointImpl implements PathPoint, Cloneable {
	public Point point;
	public Pen pen;
	public PointType type;
	public Map<Annotation<?>, Object> annos;
	public Orientation orient;

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
	}

	public PathPointImpl copy() {
		try {
			PathPointImpl copy = (PathPointImpl) super.clone();
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
		return point.toString();
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
		if (annos == null) {
			annos = new IdentityHashMap<>();
		}
		annos.put(anno, val);
	}

	@Override
	public Vector3fc position() {
		return point.toVector(new Vector3f());
	}

	@Override
	public Orientation orientation() {
		return orient;
	}

	@Override
	public void position(Vector3f dest) {
		point.toVector(dest);
	}

	@Override
	public void normal(Vector3f dest) {
		if (orient != null)
			dest.set(orient.normalVector());
		else
			dest.set(0, 0, 1);
	}

}