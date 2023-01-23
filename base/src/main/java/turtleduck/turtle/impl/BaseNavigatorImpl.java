package turtleduck.turtle.impl;

import org.joml.Vector2d;

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
        current.point = p != null ? p : Point.ZERO;
        direction = b != null ? b : Direction.DUE_NORTH;
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

    @Override
    public Point offset(double dx, double dy) {
        Point p = direction.transform(Point.point(dx, dy));
        return current.point.add(p);
    }

    @Override
    public Point offsetAxisAligned(double dx, double dy) {
        return current.point.add(dx, dy);
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

    public T goTo(PositionVector p) {
        if (!current.point.equals(p)) {
            PathPointImpl pp = current.copy();
            pp.point = Point.point(p);
            addPoint(pp);
            current = pp;
        }
        return (T) this;
    }

    public T go(Direction dir, double dist) {
        if (dist != 0.0) {
            PathPointImpl pp = current.copy();
            pp.point = pp.point.add(dir, dist);
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

    public boolean isAt(PositionVector p) {
        return x() == p.x() && y() == p.y() && z() == p.z();
    }

}