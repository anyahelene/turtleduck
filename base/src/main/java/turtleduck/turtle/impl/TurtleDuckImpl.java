package turtleduck.turtle.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.geometry.unused.Orientation;
import turtleduck.objects.IdentifiedObject;
import turtleduck.turtle.Fill;
import turtleduck.turtle.IShape;
import turtleduck.turtle.Navigator;
import turtleduck.turtle.Path;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.ShapeImpl;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.TurtleMark;

public class TurtleDuckImpl implements TurtleDuck {
	protected Navigator nav;
	protected Pen pen;
	protected PenBuilder<Pen> penBuilder;
//	protected TurtleControl journal;
//	protected final TurtleControl mainJournal;
	protected final Canvas canvas;
	protected final TurtleDuckImpl parent;
	protected final List<Path> paths = new ArrayList<>();
	private boolean drawing = false, moving = false;
	private String id;
	private int nSpawns = 0;

	public TurtleDuckImpl(String id, Canvas canvas) {
		this.canvas = canvas;
		this.id = id;
		this.pen = canvas.createPen();
//		this.mainJournal = journal;
//		this.journal = journal;
		parent = null;
		this.nav = new Navigator.DefaultNavigator(Point.point(0, 0), Bearing.DUE_NORTH, pen);
	}

	public TurtleDuckImpl(TurtleDuckImpl td) {
		this(td, td.canvas);
	}

	public TurtleDuckImpl(TurtleDuckImpl td, Canvas c) {
		parent = td;
		id = td.id + "." + td.nSpawns++;
		nav = td.nav.copy();
		canvas = c;
		pen = td.pen();
		penBuilder = null;
		drawing = false;
		moving = true;
//		mainJournal = td.mainJournal.child();
//		journal = td.journal == td.mainJournal ? mainJournal : td.journal.child();
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
		penDown();
		if (dist != 0) {
			nav.forward(dist);
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

	protected void penDown() {
		if (moving) {
			moving = false;
//				paths.add(nav.endPath());

		}
		if (!drawing) {
			drawing = true;
			nav.beginPath();
		}
		nav.pen(pen());
	}

	protected void penUp() {
		if (drawing) {
			drawing = false;
			Path path = nav.endPath();
			canvas.draw(path);
//				paths.add(path);

		}
		if (!moving) {
			moving = true;
			nav.beginPath();
			nav.pen(null);
		}
	}

	@Override
	public TurtleDuck drawTo(Point to) {
		return absMove(to, true);
	}

	protected TurtleDuck absMove(Point to, boolean draw) {
		if (!nav.isAt(to)) {
			turnTowards(to);
			if (draw)
				return draw(nav.distanceTo(to));
			else {
				penUp();
				return move(nav.distanceTo(to));
			}
		} else if (draw) {
			draw(0);
		} else {
			penUp();
		}
		return this;
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
		return this;
	}

	@Override
	public TurtleDuck fillAndStroke() {
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
	public TurtleDuck child(Canvas canvas) {
		return new TurtleDuckImpl(this, canvas);
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

			if (drawing) {
				nav.pen(pen);
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
		if (drawing) {
			nav.pen(pen);
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
	public TurtleDuck pen(Stroke newPen) {
		if (newPen instanceof Pen)
			pen((Pen) newPen);
		return this;
	}

	@Override
	public TurtleDuck pen(Fill newPen) {
		if (newPen instanceof Pen)
			pen((Pen) newPen);
		return this;
	}

	@Override
	public String id() {
		return id;
	}
}
