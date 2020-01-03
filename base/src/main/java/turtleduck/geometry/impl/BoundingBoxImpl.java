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
		double x = point.x(), y = point.y();
		double w = width, h = height;
		if(pt.isLeftOf(point))
			x = pt.x();
		else if(pt.x() > x + w)
			w = pt.x() - x;
		if(pt.isAbove(point))
			y = pt.y();
		else if(pt.y() > y + h)
			h = pt.y() - y;
		return new BoundingBoxImpl(x, y, w, h);
	}

	@Override
	public BoundingBox extend(Box box) {
		return extend(box.position()).extend(box.position().add(box.width(), box.height()));
	}

}
