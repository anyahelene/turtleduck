package turtleduck.turtle.impl;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.CommandRecorder;
import turtleduck.turtle.IShape;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.ShapeImpl;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.TurtleMark;
import turtleduck.turtle.TurtlePathBuilder;

public class TurtleDuckImpl implements TurtleDuck {
	protected Point position = Point.point(0, 0);
	protected Direction heading = Direction.fromDegrees(0);
	protected double circle = 360;
	protected double angleScale = 2.0 * Math.PI / 360.0;
	protected double moveStep = 10, angleStep = 90;
	protected Pen pen;
	protected PenBuilder<Pen> penBuilder;
	protected LineBuilder lines;
	protected final Canvas canvas;
	protected final TurtleDuckImpl parent;
	protected CommandRecorder recorder;

	public TurtleDuckImpl(Canvas canvas) {
		this.canvas = canvas;
		this.pen = canvas.createPen();
		parent = null;
	}

	public TurtleDuckImpl(TurtleDuckImpl td) {
		parent = td;
		position = td.position;
		heading = td.heading;
		angleScale = td.angleScale;
		circle = td.circle;
		moveStep = td.moveStep;
		angleStep = td.angleStep;
		canvas = td.canvas;
		pen = td.pen();
		penBuilder = null;
		if (td.recorder != null) {
			recorder = td.recorder.child();
			recorder.init(pen, position, heading.toRadians());
		}
	}

	@Override
	public double angle() {
		return heading.toRadians() / angleScale;
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
		return drawTo(to);
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
//		System.out.println("draw(" + dist + ")");
		Point to = position.move(heading, dist);
		if (recorder != null) {
			recorder.draw(dist);
		} else {
			penDown().to(pen, to);
		}
		position = to;
		return this;
	}

	@Override
	public TurtleDuck draw(double angle, double dist) {
		return turn(angle).draw(dist);
	}

	@Override
	public TurtleDuck draw(Point relPos) {
		return relMove(relPos, true);
	}

	@Override
	public TurtleDuck drawTo(double x, double y) {
		return absMove(Point.point(x, y), true);
	}

	protected LineBuilder penDown() {
		Pen p = pen();
		if (lines == null) {
			lines = canvas.lines(p, p, position);
		}
		return lines;
	}

	protected void penUp() {
		if (lines != null) {
			lines.done();
			lines = null;
		}
	}

	@Override
	public TurtleDuck drawTo(Point to) {
		return absMove(to, true);
	}

	protected TurtleDuck absMove(Point to, boolean draw) {
		turnTo(position.directionTo(to));
		if (draw)
			return draw(position.distanceTo(to));
		else
			return move(position.distanceTo(to));
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
		if (recorder != null) {
			recorder.move(dist);
		} else {
			penUp();
		}
		return this;
	}

	protected TurtleDuck relMove(Point relPos, boolean draw) {
		turnTo(relPos.asDirection());
		if (draw)
			return draw(relPos.asLength());
		else
			return move(relPos.asLength());

	}

	@Override
	public TurtleDuck move(Point relPos) {
		return relMove(relPos, false);
	}

	@Override
	public TurtleDuck moveTo(double x, double y) {
		return absMove(Point.point(x, y), false);
	}

	@Override
	public TurtleDuck moveTo(Point to) {
		return absMove(to, false);
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
	public TurtleDuck child() {
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
		return new ShapeImpl();
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
		heading = heading.rotZ(a * angleScale);
		if (recorder != null) {
			recorder.turn(a * angleScale);
		}
		return this;
	}


	@Override
	public TurtleDuck turnAround() {
		heading = heading.turnBack();
		if (recorder != null) {
			recorder.turn(Math.PI);
		}
		return this;
	}

	@Override
	public TurtleDuck turnTo(Direction dir) {
		if (!heading.equals(dir)) {
			Direction old = heading;
			heading = dir;
			if (recorder != null) {
				recorder.turn(heading.relativeTo(old).toRadians());
			}
		}
		return this;
	}

	@Override
	public TurtleDuck turnTo(double a) {
		return turnTo(Direction.fromRadians(a * angleScale));
	}

	@Override
	public TurtleDuck turnTowards(Point to) {
		return turnTo(position.directionTo(to));
	}

	@Override
	public TurtleDuck useDegrees() {
		circle = 360;
		angleScale = 2.0 * Math.PI / 360.0;
		return this;
	}

	@Override
	public TurtleDuck useRadians() {
		circle = 2 * Math.PI;
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
			if (recorder != null) {
				recorder.pen(pen);
			}
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
		if (recorder != null) {
			recorder.pen(pen);
		}

		return this;
	}

	@Override
	public void done() {
		penUp();
	}

	@Override
	public TurtleMark mark(String name) {
		return new TurtleMark(position, heading, name);
	}

	@Override
	public boolean isChild() {
		return parent != null;
	}

	@Override
	public TurtleDuck parent() {
		return parent;
	}

	@Override
	public SimpleTurtle startRecording() {
		if (recorder != null)
			throw new IllegalStateException("Alreadyy recording commands!");
		penUp();
		recorder = new CommandRecorder();
		recorder.init(pen, position, heading.toRadians());
		return this;
	}

	@Override
	public CommandRecorder endRecording() {
		if (recorder == null)
			throw new IllegalStateException("Not currently recording commands!");
		CommandRecorder r = recorder;
		recorder = null;
		return r;
	}
}
