package turtleduck.turtle.impl;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;
import turtleduck.turtle.Navigator;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.BasicNavigator.RelativeTo;

public abstract class NavigatorImpl<T extends Navigator<T>> implements Navigator<T>, Cloneable {
	protected boolean recordMoves = false;
	protected boolean recordTurns = false;
	protected PathPointImpl current;

	public NavigatorImpl(NavigatorImpl<T> old) {
		current = old.current.copy();
	}

	public NavigatorImpl(Point p, Bearing b) {
		current = new PathPointImpl();
		current.point = p;
		current.bearing = b;
		current.type = Path.PointType.POINT;
	}

	protected abstract void addPoint(PathPointImpl point); 

	@Override
	public Bearing bearing() {
		return current.bearing;
	}

	@Override
	public Point at() {
		return current.point;
	}

	@Override
	public T go(PositionVector off, RelativeTo rel) {
		PathPointImpl pp = current.copy();
		pp.point = findPoint(off, rel);
		addPoint(pp);
		current = pp;
		return (T) this;
	}

	protected void updateBearing(Bearing b) {
		if (recordTurns) {
			PathPointImpl pp = current.copy();
			pp.bearing = current.bearing.add(b);
			addPoint(pp);
			current = pp;
		} else {
			current.bearing = current.bearing.add(b);
		}
	}

	public T turnTo(PositionVector dest) {
		bearing(Bearing.absolute(dest.x() - current.point.x(), dest.y() - current.point.y()).sub(current.bearing));
		return (T) this;
	}

	@Override
	public T bearing(Bearing dest) {
		if (dest.isAbsolute())
			updateBearing(dest.sub(current.bearing));
		else
			updateBearing(dest);
		return (T) this;
	}

	@Override
	public double x() {
		return current.point.x();
	}

	@Override
	public double y() {
		return current.point.y();
	}

	@Override
	public double z() {
		return current.point.z();
	}

	@Override
	public double dirX() {
		return current.bearing.dirX();
	}

	@Override
	public double dirY() {
		return current.bearing.dirY();
	}

	@Override
	public double dirZ() {
		return current.bearing.dirZ();
	}

	public T at(Point dest) {
		if (!isAt(dest)) {
			PathPointImpl pp = current.copy();
			pp.point = dest;
			addPoint(pp);
			current = pp;
		}
		return (T) this;
	}

	public boolean isAt(PositionVector p) {
		return x() == p.x() && y() == p.y() && z() == p.z();
	}

	@Override
	public Point findPoint(PositionVector point, RelativeTo rel) {
		switch (rel) {
		case POSITION:
			return current.point.add(point);
		case SELF:
			return current.point.add(current.bearing, point.y()); // TODO
		case WORLD:
			return Point.point(point);
		default:
			throw new IllegalStateException();
		}
	}

}