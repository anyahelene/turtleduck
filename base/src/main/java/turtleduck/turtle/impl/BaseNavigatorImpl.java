package turtleduck.turtle.impl;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;
import turtleduck.paths.Path;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.turtle.Navigator;

public abstract class BaseNavigatorImpl<T extends Navigator<T>> implements Navigator<T>, Cloneable {
	protected boolean recordMoves = false;
	protected boolean recordTurns = false;
	protected PathPointImpl current;
	protected Direction direction;

	public BaseNavigatorImpl(BaseNavigatorImpl<?> old) {
		current = old.current.copy();
		direction = old.direction;
	}

	public BaseNavigatorImpl(Point p, Direction b) {
		current = new PathPointImpl();
		current.point = p;
		direction = b;
		current.type = Path.PointType.POINT;
	}

	protected abstract void addPoint(PathPointImpl point);

	@Override
	public Direction direction() {
		return direction;
	}

	@Override
	public Point point() {
		return current.point;
	}

	public T go(double dist) {
		if (dist != 0) {
			PathPointImpl pp = current.copy();
			pp.point = pp.point.add(direction, dist);
			addPoint(pp);
			current = pp;
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T go(PositionVector off, RelativeTo rel) {
		PathPointImpl pp = current.copy();
		pp.point = findPoint(off, rel);
		if (!pp.point.equals(current.point)) {
			addPoint(pp);
			current = pp;
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T turnTo(PositionVector dest) {
		direction(current.point.directionTo(dest));
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T direction(Direction dest) {
		if (dest.isAbsolute())
			direction = direction.rotateTo(dest);
		else
			direction = direction.add(dest);
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
		return direction.dirX();
	}

	@Override
	public double dirY() {
		return direction.dirY();
	}

	@Override
	public double dirZ() {
		return direction.dirZ();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T goTo(Point dest) {
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
		Point dest;
		switch (rel) {
		case POSITION:
			dest = current.point.add(point);
			break;
		case SELF:
			dest = current.point.add(direction, point.y()); // TODO
			break;
		case WORLD:
			dest = Point.point(point);
			break;
		default:
			throw new IllegalStateException();
		}

		return dest;

	}
}