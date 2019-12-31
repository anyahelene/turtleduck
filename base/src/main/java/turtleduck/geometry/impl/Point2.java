package turtleduck.geometry.impl;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public class Point2 implements Point {
	protected final double x;
	protected final double y;

	public Point2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Direction directionTo(Point otherPoint) {
		return new Direction(otherPoint.getX() - getX(), otherPoint.getY() - getY(), otherPoint.getZ() - getZ());
	}

	@Override
	public double distanceTo(Point otherPoint) {
		return Math.sqrt(distanceToSq(otherPoint));
	}

	@Override
	public double distanceToSq(Point otherPoint) {
		double x = otherPoint.getX() - getX();
		double y = otherPoint.getY() - getY();
		double z = otherPoint.getZ() - getZ();
		return x * x + y * y + z * z;
	}

	@Override
	public double asLength() {
		return Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2) + Math.pow(getZ(), 2));
	}

	@Override
	public Direction asDirection() {
		return new Direction(getX(), getY(), getZ());
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
		return 0;
	}

	@Override
	public Point move(Direction dir, double distance) {
		return new Point2(x + dir.getX() * distance, y + dir.getY() * distance);
	}

	@Override
	public Point move(double deltaX, double deltaY) {
		return new Point2(getX() + deltaX, getY() + deltaY);
	}

	@Override
	public Point move(Point deltaPos) {
		return new Point2(getX() + deltaPos.getX(), getY() + deltaPos.getY());
	}

	@Override
	public Point moveTo(double newX, double newY) {
		return new Point2(newX, newY);
	}

	@Override
	public Point moveX(double deltaX) {
		return move(deltaX, 0.0);
	}

	@Override
	public Point moveXTo(double newX) {
		return moveTo(newX, getY());
	}

	@Override
	public Point moveY(double deltaY) {
		return move(0.0, deltaY);
	}

	@Override
	public Point moveYTo(double newY) {
		return moveTo(getX(), newY);
	}

	@Override
	public Point moveZ(double deltaZ) {
		return new Point3(getX(), getY(), getZ() + deltaZ);
	}

	@Override
	public Point moveZTo(double newZ) {
		return new Point3(getX(), getY(), newZ);
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
	public Point deltaTo(Point point) {
		return new Point2(getX() - x, getY() - y);
	}

	@Override
	public double angleTo(Point otherPoint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double angleTo(double x, double y) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLeftOf(Point otherPoint) {
		return x < otherPoint.getX();
	}

	@Override
	public boolean isAbove(Point otherPoint) {
		return y < otherPoint.getY();
	}

	@Override
	public Point interpolate(Point otherPoint, double fraction) {
		if (fraction <= 0.0)
			return this;
		else if (fraction >= 1.0)
			return otherPoint;
		else if (otherPoint.getZ() != 0.0)
			return new Point3(x + (otherPoint.getX() - x) * fraction, y + (otherPoint.getY() - y) * fraction,
					otherPoint.getZ() * fraction);
		else
			return new Point2(x + (otherPoint.getX() - x) * fraction, y + (otherPoint.getY() - y) * fraction);
	}

}
