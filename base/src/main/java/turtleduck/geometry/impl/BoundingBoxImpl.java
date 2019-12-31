package turtleduck.geometry.impl;

import turtleduck.geometry.BoundingBox;
import turtleduck.geometry.Box;
import turtleduck.geometry.Point;

public class BoundingBoxImpl extends BoxImpl implements BoundingBox {

	public BoundingBoxImpl(double x, double y, double w, double h) {
		super(x, y, w, h);
	}

	@Override
	public BoundingBox extend(Point pt) {
		if(contains(pt))
			return this;
		double x = point.getX(), y = point.getY();
		double w = width, h = height;
		if(pt.isLeftOf(point))
			x = pt.getX();
		else if(pt.getX() > x + w)
			w = pt.getX() - x;
		if(pt.isAbove(point))
			y = pt.getY();
		else if(pt.getY() > y + h)
			h = pt.getY() - y;
		return new BoundingBoxImpl(x, y, w, h);
	}

	@Override
	public BoundingBox extend(Box box) {
		return extend(box.position()).extend(box.position().move(box.width(), box.height()));
	}

}
