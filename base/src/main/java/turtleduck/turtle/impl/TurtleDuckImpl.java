package turtleduck.turtle.impl;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Navigator;
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
	protected Navigator nav;
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
		this.nav = new Navigator.DefaultNavigator();
	}

	public TurtleDuckImpl(TurtleDuckImpl td) {
		parent = td;
		nav = td.nav.copy();
		canvas = td.canvas;
		pen = td.pen();
		penBuilder = null;
		if (td.recorder != null) {
			recorder = td.recorder.child();
			recorder.init(pen, nav.position(), nav.bearing().toRadians());
			nav.recordTo(recorder);
		} else {
			nav.recordTo(null);
		}
	}

	@Override
	public double angle() {
		return nav.bearing().azimuth();
	}

	@Override
	public <T> T as(Class<T> clazz) {
		if (clazz == TurtleDuck.class || clazz == SimpleTurtle.class || clazz == TurtleDuckImpl.class)
			return (T) this;
		else
			return null;
	}

	@Override
	public double x() {
		return nav.x();
	}

	@Override
	public double y() {
		return nav.y();
	}

	@Override
	public TurtleDuck curveTo(Point to, double startControl, double endAngle, double endControl) {
		return drawTo(to);
	}

	@Override
	public void debugTurtle() {
	}

	@Override
	public TurtleDuck draw(double dist) {
		if (dist != 0) {
			penDown();
			nav.forward(dist);
			if (recorder != null) {
				recorder.draw();
			} else {
				lines.to(pen, nav.position());
			}
		}
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
			lines = canvas.lines(p, p, nav.position());
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
		turnTowards(to);
		if (draw)
			return draw(nav.distanceTo(to));
		else {
			penUp();
			return move(nav.distanceTo(to));
		}
	}

	@Override
	public Bearing heading() {
		return nav.bearing();
	}

	@Override
	public TurtleDuck move(double dist) {
		penUp();
		nav.forward(dist);
		return this;
	}

	@Override
	public TurtleDuck fill() {
		if(lines != null) {
			lines.fill(pen, false);
			lines = null;
		}
		return this;
	}
	@Override
	public TurtleDuck fillAndStroke() {
		if(lines != null) {
			lines.fill(pen, true);
			lines = null;
		}
		return this;
	}
	
	protected TurtleDuck relMove(Point relPos, boolean draw) {
		turnTo(Bearing.absolute(relPos.x(), relPos.y()));
		if (draw)
			return draw(relPos.asLength());
		else {
			penUp();
			return move(relPos.asLength());
		}

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
		return nav.position();
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
	public TurtleDuck turn(double a) {
		nav.right(a);

		return this;
	}

	@Override
	public TurtleDuck turnTo(Bearing dir) {
		nav.face(dir);

		return this;
	}

	@Override
	public TurtleDuck turnTo(double a) {
		return turnTo(Bearing.absolute(a));
	}

	@Override
	public TurtleDuck turnTowards(Point to) {
		nav.face(to);
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
		return new TurtleMark(nav.position(), nav.bearing(), name);
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
		recorder.init(pen, nav.position(), nav.bearing().azimuth());
		nav.recordTo(recorder);
		return this;
	}

	@Override
	public CommandRecorder endRecording() {
		if (recorder == null)
			throw new IllegalStateException("Not currently recording commands!");
		CommandRecorder r = recorder;
		recorder = null;
		nav.recordTo(null);
		return r;
	}
}
