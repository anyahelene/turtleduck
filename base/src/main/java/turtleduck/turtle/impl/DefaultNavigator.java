package turtleduck.turtle.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;
import turtleduck.geometry.Waypoint;
import turtleduck.turtle.Navigator;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Path;

public class DefaultNavigator implements Navigator, Cloneable {
	protected boolean recordMoves = false;
	protected boolean recordTurns = false;
	protected PathPoint current;
	protected List<PathPoint> points = new ArrayList<>();

	public DefaultNavigator(Point p, Bearing b, Pen pen) {
		current = new PathPoint();
		current.point = p;
		current.bearing = b;
		current.pen = pen;
		current.type = Path.PointType.POINT;
		points.add(current);
	}

	@Override
	public Bearing bearing() {
		return current.bearing;
	}

	@Override
	public Point position() {
		return current.point;
	}

	@Override
	public Navigator left(double degrees) {
		if (degrees != 0) {
			bearing(Bearing.relative(-degrees));
		}
		return this;
	}

	@Override
	public Navigator right(double degrees) {
		if (degrees != 0) {
			bearing(Bearing.relative(degrees));
		}
		return this;
	}

	@Override
	public Navigator forward(double distance) {
		PathPoint pp = current.copy();
		pp.point = current.point.add(current.bearing, distance);
		points.add(pp);
		current = pp;
		return this;
	}

	protected void bearing(Bearing b) {
		if (recordTurns) {
			PathPoint pp = current.copy();
			pp.bearing = current.bearing.add(b);
			points.add(pp);
			current = pp;
		} else {
			current.bearing = current.bearing.add(b);
		}
	}

	@Override
	public Navigator face(PositionVector dest) {
		bearing(Bearing.absolute(dest.x() - current.point.x(), dest.y() - current.point.y()).sub(current.bearing));
		return this;
	}

	@Override
	public Navigator face(Bearing dest) {
		if (dest.isAbsolute())
			bearing(dest.sub(current.bearing));
		else
			bearing(dest);
		return this;
	}

	@Override
	public Navigator go(double radians, double distance) {
		if (radians != 0.0)
			bearing(Bearing.relative(radians));
		forward(distance);
		return this;
	}

	@Override
	public Navigator goTo(Waypoint dest) {
		face(dest.position());
		forward(current.point.distanceTo(dest.position()));
		bearing(dest.bearing().sub(current.bearing));
		return this;
	}

	@Override
	public Waypoint waypoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Navigator copy() {
		try {
			DefaultNavigator nav = (DefaultNavigator) super.clone();
			nav.current = current.copy();
			nav.points = new ArrayList<>();
			nav.points.add(current);
			return nav;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
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

	@Override
	public Navigator goTo(Point dest) {
		face(dest);
		forward(current.point.distanceTo(dest));
		return this;
	}

	@Override
	public void beginPath() {
		points = new ArrayList<>();
		points.add(current);
	}

	@Override
	public Path endPath() {
		Path path = Path.fromList(points);
		return path;
	}

	@Override
	public boolean isAt(PositionVector p) {
		return x() == p.x() && y() == p.y() && z() == p.z();
	}

	@Override
	public Bearing bearing(int index) {
		if (index < 0)
			index = points.size() + index;
		return points.get(index).bearing;
	}

	@Override
	public Point position(int index) {
		if (index < 0)
			index = points.size() + index;
		return points.get(index).point;
	}

	@Override
	public Navigator pen(Pen pen) {
		current.pen = pen;
		return this;
	}

	@Override
	public Pen pen() {
		return current.pen;
	}

}