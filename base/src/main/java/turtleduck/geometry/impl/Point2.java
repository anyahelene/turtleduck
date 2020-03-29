package turtleduck.geometry.impl;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;
import turtleduck.geometry.unused.Direction;

public class Point2 implements Point {
	protected final double x;
	protected final double y;

	public Point2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Bearing bearingTo(PositionVector otherPoint) {
		return Bearing.absolute(otherPoint.x() - x(), otherPoint.y() - y());
	}

	@Override
	public double distanceTo(PositionVector otherPoint) {
		double x = otherPoint.x() - x();
		double y = otherPoint.y() - y();
		double z = otherPoint.z() - z();
		if (z == 0)
			return Math.hypot(x, y);
		else
			return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public double distanceToSq(PositionVector otherPoint) {
		double x = otherPoint.x() - x();
		double y = otherPoint.y() - y();
		double z = otherPoint.z() - z();
		return x * x + y * y + z * z;
	}

	@Override
	public double asLength() {
		return Math.sqrt(Math.pow(x(), 2) + Math.pow(y(), 2) + Math.pow(z(), 2));
	}

	@Override
	public Bearing asBearing() {
		return Bearing.absolute(x(), y());
	}

	@Override
	public double x() {
		return x;
	}

	@Override
	public double y() {
		return y;
	}

	@Override
	public double z() {
		return 0;
	}

	@Override
	public Point add(Bearing dir, double distance) {
//	TODO:	return new Point2(Math.fma(dir.dirX(), distance, x), Math.fma(dir.dirY(), distance, y));
		return new Point2(dir.dirX() * distance + x, dir.dirY() * distance + y);
	}

	@Override
	public Point add(double deltaX, double deltaY) {
		return new Point2(x() + deltaX, y() + deltaY);
	}

	@Override
	public Point add(PositionVector deltaPos) {
		double dz = deltaPos.z();
		if (dz != 0)
			return new Point3(x() + deltaPos.x(), y() + deltaPos.y(), dz);
		else
			return new Point2(x() + deltaPos.x(), y() + deltaPos.y());
	}

	@Override
	public Point sub(PositionVector deltaPos) {
		double dz = deltaPos.z();
		if (dz != 0)
			return new Point3(x() - deltaPos.x(), y() - deltaPos.y(), -dz);
		else
			return new Point2(x() - deltaPos.x(), y() - deltaPos.y());
	}

	@Override
	public Point xy(double newX, double newY) {
		return new Point2(newX, newY);
	}

	@Override
	public String toString() {
		return String.format("(%.2f,%.2f)", x, y);
	}

	@Override
	public Point scale(double factor) {
		return new Point2(x * factor, y * factor);
	}

	@Override
	public Point xyz(double newX, double newY, double newZ) {
		return new Point3(newX, newY, newZ);
	}

	@Override
	public Point diff(PositionVector point) {
		return new Point2(x() - x, y() - y);
	}

	@Override
	public double angleTo(PositionVector otherPoint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLeftOf(PositionVector otherPoint) {
		return x < otherPoint.x();
	}

	@Override
	public boolean isAbove(PositionVector otherPoint) {
		return y < otherPoint.y();
	}

	@Override
	public Point interpolate(Point otherPoint, double fraction) {
		if (fraction <= 0.0)
			return this;
		else if (fraction >= 1.0)
			return otherPoint;
		else if (otherPoint.z() != 0.0)
			return new Point3(x + (otherPoint.x() - x) * fraction, y + (otherPoint.y() - y) * fraction,
					otherPoint.z() * fraction);
		else
			return new Point2(x + (otherPoint.x() - x) * fraction, y + (otherPoint.y() - y) * fraction);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Point)) {
			return false;
		}
		Point other = (Point) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x())) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y())) {
			return false;
		}
		if (Double.doubleToLongBits(z()) != Double.doubleToLongBits(other.z())) {
			return false;
		}
		return true;
	}

}
