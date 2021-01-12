package turtleduck.geometry.impl;

import org.joml.Vector3d;
import org.joml.Vector3f;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.IPoint3;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

/**
 * A 3D version of {@link Point2}
 *
 */
public class Point3 extends Point2 implements IPoint3 {
	protected final double z;

	public Point3(double x, double y, double z) {
		super(x, y);
		this.z = z;
	}


	@Override
	public double z() {
		return z;
	}

	@Override
	public IPoint3 add(double deltaX, double deltaY) {
		return new Point3(x + deltaX, y + deltaY, z);
	}

	@Override
	public Point add(Direction dir, double distance) {
		Vector3f vec = dir.directionVector(new Vector3f()).mul((float)distance);
		return new Point3(x+vec.x,y+vec.y,z+vec.z);
	}

	@Override
	public Point add(PositionVector deltaPos) {
			return new Point3(x + deltaPos.x(), y + deltaPos.y(), z + deltaPos.z());
	}
	@Override
	public Point sub(PositionVector deltaPos) {
		return new Point3(x - deltaPos.x(), y - deltaPos.y(), z - deltaPos.z());
	}


	@Override
	public IPoint3 xy(double newX, double newY) {
		return new Point3(newX, newY, z);
	}

	@Override
	public String toString() {
		return String.format("(%.2f,%.2f,%.2f)", x, y, z);
	}

	@Override
	public Point interpolate(Point otherPoint, double fraction) {
		if (fraction <= 0.0)
			return this;
		else if (fraction >= 1.0)
			return otherPoint;
		else
			return new Point3(x + (otherPoint.x() - x) * fraction, y + (otherPoint.y() - y) * fraction,
				z + (otherPoint.z() - z) * fraction);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		else return super.equals(obj);
	}
	@Override
	public Orientation asBearing() {
		return Orientation.absoluteVec(x(), y(), z());
	}
	@Override
	public Direction bearingTo(PositionVector otherPoint) {
		return Orientation.absoluteVec(otherPoint.x() - x(), otherPoint.y() - y(), otherPoint.z() -z());
	}
	@Override
	public Vector3f toVector(Vector3f dest) {
		return dest.set(x, y, z);
	}
	@Override
	public Vector3d toVector(Vector3d dest) {
		return dest.set(x, y, z);
	}
}
