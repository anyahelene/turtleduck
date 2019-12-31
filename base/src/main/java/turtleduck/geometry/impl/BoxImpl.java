package turtleduck.geometry.impl;

import turtleduck.geometry.Box;
import turtleduck.geometry.Point;

public class BoxImpl implements Box {
	protected final Point point;
	protected final double width, height;

	public BoxImpl(Point p, double w, double h) {
		this.point = p;
		this.width = w;
		this.height = h;
	}

	public BoxImpl(double x, double y, double w, double h) {
		this.point = Point.point(x, y);
		this.width = w;
		this.height = h;
	}

	@Override
	public Box size(double newW, double newH) {
		if (newW < 0 || newH < 0) {
			return new BoxImpl(point.move(Math.min(0, newW), Math.min(0, newH)), Math.abs(newW), Math.abs(newH));
		}
		return new BoxImpl(point, newW, newH);
	}

	@Override
	public double width() {
		return width;
	}

	@Override
	public double height() {
		return height;
	}

	@Override
	public double x() {
		return point.getX();
	}

	@Override
	public double y() {
		return point.getY();
	}

	@Override
	public Box position(Point newPos) {
		return new BoxImpl(newPos, width, height);
	}

	@Override
	public boolean contains(Point point) {
		return point.getX() >= point.getX() && point.getX() <= point.getX() + width //
				&& point.getY() >= point.getY() && point.getY() <= point.getY() + height;
	}

	@Override
	public boolean contains(Box other) {
		return contains(other.position()) && contains(other.position().move(other.x(), other.y()));
	}

	@Override
	public boolean overlaps(Box other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Point position() {
		return point;
	}

}
