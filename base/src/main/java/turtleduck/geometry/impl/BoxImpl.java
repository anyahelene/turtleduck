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
			return new BoxImpl(point.add(Math.min(0, newW), Math.min(0, newH)), Math.abs(newW), Math.abs(newH));
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
		return point.x();
	}

	@Override
	public double y() {
		return point.y();
	}

	@Override
	public Box position(Point newPos) {
		return new BoxImpl(newPos, width, height);
	}

	@Override
	public boolean contains(Point point) {
		return point.x() >= point.x() && point.x() <= point.x() + width //
				&& point.y() >= point.y() && point.y() <= point.y() + height;
	}

	@Override
	public boolean contains(Box other) {
		return contains(other.position()) && contains(other.position().add(other.x(), other.y()));
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
