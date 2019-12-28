package turtleduck.turtle.impl;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.IShape;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.TurtlePathBuilder;

public class TurtleDuckImpl implements TurtleDuck {
	protected Point position = Point.point(0, 0);
	protected Direction heading = Direction.fromDegrees(0);
	protected double angle = 0, circle = 360;
	protected double angleScale = 2.0 * Math.PI / 360.0;
	protected double moveStep = 10, angleStep = 90;
	protected Pen pen;
	protected PenBuilder<Pen> penBuilder;
	protected final Canvas canvas;

	public TurtleDuckImpl(Canvas canvas) {
		this.canvas = canvas;
		this.pen = canvas.createPen();
	}

	public TurtleDuckImpl(TurtleDuckImpl td) {
		position = td.position;
		heading = td.heading;
		angle = td.angle;
		angleScale = td.angleScale;
		circle = td.circle;
		moveStep = td.moveStep;
		angleStep = td.angleStep;
		canvas = td.canvas;
		pen = td.pen();
		penBuilder = null;
	}

	@Override
	public double angle() {
		return angle / angleScale;
	}

	@Override
	public double angleTo(double x, double y) {
		return position.directionTo(Point.point(x, y)).toRadians() / angleScale;
	}

	@Override
	public <T> T as(Class<T> clazz) {
		if (clazz == TurtleDuck.class || clazz == SimpleTurtle.class || clazz == TurtleDuckImpl.class)
			return (T) this;
		else
			return null;
	}

	@Override
	public double distanceTo(double x, double y) {
		return position.distanceTo(Point.point(x, y));
	}

	@Override
	public double x() {
		return position.getX();
	}

	@Override
	public double y() {
		return position.getX();
	}

	@Override
	public TurtleDuck curveTo(Point to, double startControl, double endAngle, double endControl) {
		return this;
	}

	@Override
	public void debugTurtle() {
	}

	@Override
	public Direction directionTo(Point point) {
		return position.directionTo(point);
	}

	@Override
	public double distanceTo(Point point) {
		return position.distanceTo(point);
	}

	@Override
	public TurtleDuck draw() {
		return draw(moveStep);
	}

	@Override
	public TurtleDuck draw(double dist) {
		return drawTo(position.move(heading, dist));
	}

	@Override
	public TurtleDuck draw(double angle, double dist) {
		return turn(angle).draw(dist);
	}

	@Override
	public TurtleDuck draw(Point relPos) {
		return drawTo(position.move(relPos));
	}

	@Override
	public TurtleDuck drawTo(double x, double y) {
		return draw(Point.point(x, y));
	}

	@Override
	public TurtleDuck drawTo(Point to) {
		Pen p = pen();
		canvas.line(p, p, position, to);
		position = to;
		return this;
	}

	@Override
	public Direction heading() {
		return heading;
	}

	@Override
	public TurtleDuck left() {
		return turn(angleStep);
	}

	@Override
	public TurtleDuck move() {
		return move(moveStep);
	}

	@Override
	public TurtleDuck move(double dist) {
		position = position.move(heading, dist);
		return this;
	}

	@Override
	public TurtleDuck move(Point relPos) {
		position = position.move(relPos);
		return this;
	}

	@Override
	public TurtleDuck moveTo(double x, double y) {
		position = Point.point(x, y);
		return this;
	}

	@Override
	public TurtleDuck moveTo(Point to) {
		position = to;
		return this;
	}

	@Override
	public Orientation orientation() {
		return null;
	}

	@Override
	public TurtleDuck orientation(Orientation orient) {
		return this;
	}

	@Override
	public TurtleDuck pitch(double angle) {
		return this;
	}

	@Override
	public TurtleDuck subTurtle() {
		return new TurtleDuckImpl(this);
	}

	@Override
	public TurtlePathBuilder path() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point position() {
		return position;
	}

	@Override
	public TurtleDuck right() {
		return turn(-angleStep);
	}

	@Override
	public TurtleDuck roll(double angle) {
		return this;
	}

	@Override
	public IShape shape() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TurtleDuck stepSize(double moving, double turning) {
		if (moving < 0 || turning < 0)
			throw new IllegalArgumentException("Argument must not be negative");
		moveStep = moving;
		angleStep = turning;
		return this;
	}

	@Override
	public TurtleDuck turn(double a) {
		angle += a * angleScale;
		heading = heading.rotZ(a * angleScale);
		return this;
	}

	@Override
	public TurtleDuck turnAround() {
		heading = heading.turnBack();
		angle = heading.toRadians();
		return this;
	}

	@Override
	public TurtleDuck turnTo(Direction dir) {
		heading = dir;
		angle = dir.toRadians();
		return this;
	}

	@Override
	public TurtleDuck turnTo(double a) {
		angle = a * angleScale;
		heading = Direction.fromRadians(angle);
		return this;
	}

	@Override
	public TurtleDuck turnTowards(Point to) {
		return turnTo(position.directionTo(to));
	}

	@Override
	public TurtleDuck useDegrees() {
		circle = 360;
		angleScale = 2.0*Math.PI/360.0;
		return this;
	}

	@Override
	public TurtleDuck useRadians() {
		circle = 2*Math.PI;
		angleScale = 1;
		return this;
	}

	@Override
	public TurtleDuck trace(boolean enabled) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Pen pen() {
		if (pen == null) {
			pen = penBuilder.done();
			penBuilder = null;
		}
		return pen;
	}

	@Override
	public PenBuilder<TurtleDuck> changePen() {
		penBuilder = pen().change();
		pen = null;
		return new TurtlePenBuilder<TurtleDuck>(penBuilder, (TurtleDuck) this);
	}

	@Override
	public TurtleDuck pen(Pen newPen) {
		if (newPen == null)
			throw new IllegalArgumentException("Argument must not be null");
		pen = newPen;
		return this;
	}

}
