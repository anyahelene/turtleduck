package turtleduck.geometry.impl;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Direction;
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
	public Point add(Bearing dir, double distance) {
		return new Point3(Math.fma(dir.dirX(), distance, x), Math.fma(dir.dirY(), distance, y), Math.fma(dir.dirZ(), distance, z));
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

}
