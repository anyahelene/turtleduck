package turtleduck.geometry.impl;

import turtleduck.geometry.Direction;
import turtleduck.geometry.IPoint3;
import turtleduck.geometry.Point;

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
	public double distanceTo(Point otherPoint) {
		double xyDist = Math.sqrt(Math.pow(x - otherPoint.getX(), 2) + Math.pow(y - otherPoint.getY(), 2));
		if (otherPoint instanceof Point3)
			return Math.sqrt(xyDist * xyDist + Math.pow(z - otherPoint.getZ(), 2));
		else
			return xyDist;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public IPoint3 move(Direction dir, double distance) {
		return new Point3(x + dir.getX() * distance, y - dir.getY() * distance, z - dir.getZ() * distance);
	}

	@Override
	public IPoint3 move(double deltaX, double deltaY) {
		return new Point3(x + deltaX, y + deltaY, z);
	}

	@Override
	public IPoint3 move(double deltaX, double deltaY, double deltaZ) {
		return new Point3(x + deltaX, y + deltaY, z + deltaZ);
	}

	@Override
	public IPoint3 move(Point deltaPos) {
		return new Point3(x + deltaPos.getX(), y + deltaPos.getY(), z + deltaPos.getZ());
	}

	@Override
	public IPoint3 moveTo(double newX, double newY) {
		return new Point3(newX, newY, z);
	}

	@Override
	public IPoint3 moveTo(double newX, double newY, double newZ) {
		return new Point3(newX, newY, newZ);
	}

	@Override
	public IPoint3 moveX(double deltaX) {
		return move(deltaX, 0.0, 0.0);
	}

	@Override
	public IPoint3 moveXTo(double newX) {
		return moveTo(newX, getY(), getZ());
	}

	@Override
	public IPoint3 moveY(double deltaY) {
		return move(0.0, deltaY, 0.0);
	}

	@Override
	public IPoint3 moveYTo(double newY) {
		return moveTo(getX(), newY, getZ());
	}

	@Override
	public IPoint3 moveZ(double deltaZ) {
		return move(0.0, 0.0, deltaZ);
	}

	@Override
	public IPoint3 moveZTo(double newZ) {
		return moveTo(getX(), getY(), getZ());
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
			return new Point3(x + (otherPoint.getX() - x) * fraction, y + (otherPoint.getY() - y) * fraction,
				z + (otherPoint.getZ() - z) * fraction);
	}

}
