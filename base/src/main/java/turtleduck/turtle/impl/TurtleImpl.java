package turtleduck.turtle.impl;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.joml.Vector3f;

import turtleduck.colors.Color;
import turtleduck.display.Canvas;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Direction3;
import turtleduck.geometry.Point;
import turtleduck.turtle.Chelonian;
import turtleduck.turtle.DrawingBuilder;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.PathWriter.PathStroke;

import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.SpriteBuilder;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.Turtle;

public class TurtleImpl<THIS extends Chelonian<THIS, RESULT>, RESULT> extends NavigatorImpl<THIS>
		implements Chelonian<THIS, RESULT> {
	public static class SpecificTurtle extends TurtleImpl<Turtle, Turtle> implements Turtle {

		public SpecificTurtle(Point p, Direction b, Pen pen) {
			super(p, b, pen);
		}

		public SpecificTurtle(SpecificTurtle parent) {
			super(parent);
		}

		protected SpecificTurtle copy() {
			return new SpecificTurtle(this);
		}
	}

	/** True if pen is down and next go commmand will add to the path. */
	protected boolean penDown = false;
	/**
	 * True if the last movement added to the path, and the pen has not been raised
	 * since then.
	 * 
	 * Set to true by penDown() and all draw() commands, set to false by penUp() and
	 * all jump() commands.
	 */
	protected boolean drawing = false;
	/**
	 * The current pen.
	 */
	protected Pen pen;
	/**
	 * The list of points we have moved through.
	 */
//	protected List<PathPointImpl> points = new ArrayList<>();
	protected PathPointImpl last;
//	protected List<TurtleImpl<THIS, RESULT>> children = new ArrayList<>();
	protected TurtleImpl<THIS, RESULT> parent = null;
	protected PenBuilder<Pen> penBuilder;
	private PathWriter writer;
	private PathStroke currentStroke;
	private double stepSize = 0;
	private BiConsumer<PathPoint, PathPoint> drawAction;
	private Consumer<PathPoint> moveAction;

	public TurtleImpl(TurtleImpl<THIS, RESULT> parent) {
		super(parent);
		this.pen = parent.pen;
//		this.points = new ArrayList<>();
//		this.points.add(parent.current);
		this.drawAction = parent.drawAction;
		this.moveAction = parent.moveAction;
		this.parent = parent;
		this.writer = parent.writer;
	}

	public TurtleImpl(Point p, Direction b, Pen pen) {
		super(p, b);
		this.pen = pen;
	}

	protected void addPoint(PathPointImpl point) {
		last = current;
	}

	@Override
	public void beginPath() {
		last = null;
//		points = new ArrayList<>();
//		points.add(current);
//		throw new UnsupportedOperationException();
	}

	@Override
	public Path endPath() {
		last = null;
//		Path path = Path.fromList(points);
		return null;
	}

	/*
	 * public Bearing previousBearing(int index) { if (index < 0) index =
	 * points.size() + index; return points.get(index).bearing; }
	 * 
	 * public Point previousPosition(int index) { if (index < 0) index =
	 * points.size() + index; return points.get(index).point; }
	 */
	public THIS pen(Pen newPen) {
		pen = newPen;
		penBuilder = null;
		return (THIS) this;
	}

	public Pen pen() {
		if (penBuilder != null) {
			pen = penBuilder.done();
			penBuilder = null;
		}
		return pen;
	}

	@Override
	public String id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> U as(Class<U> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public THIS curveTo(Point to, double startControl, double endAngle, double endControl) {
		return drawTo(to);
	}

	@Override
	public THIS drawTo(Point to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public THIS draw(double dist) {
		drawing = true;
		boolean penStatus = penDown;
		penDown = true;
		current.pen = pen();
		forward(dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@Override
	public THIS jump(double dist) {
		drawing = false;
		boolean penStatus = penDown;
		penDown = false;
		forward(dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@Override
	public THIS goTo(Point newPos) {
		super.goTo(newPos);
		triggerActions();
		return (THIS) this;
	}

	@Override
	public THIS jumpTo(Point newPos) {
		drawing = false;
		boolean penStatus = penDown;
		penDown = false;
		goTo(newPos);
		penDown = penStatus;
		return (THIS) this;
	}

	@Override
	public THIS turn(double angle) {
		current.bearing = current.bearing.yaw(angle);
		return (THIS) this;
	}

	@Override
	public THIS turnTo(double angle) {
		current.bearing = Direction3.absoluteAz(angle);
		return (THIS) this;
	}

	@Override
	public PenBuilder<? extends THIS> penChange() {
		penBuilder = pen.change();
		return new TurtlePenBuilder<THIS, RESULT>(penBuilder, (THIS) this);
	}

	@Override
	public THIS pen(Stroke newPen) {
		pen = pen.stroke(newPen);
		return (THIS) this;
	}

	@Override
	public THIS pen(Fill newPen) {
		pen = pen.fill(newPen);
		return (THIS) this;
	}

	@Override
	public THIS penColor(Color color) {
		pen = pen.change().strokePaint(color).done();
		return (THIS) this;
	}

	@Override
	public THIS penWidth(double width) {
		pen = pen.change().strokeWidth(width).done();
		return (THIS) this;
	}

	@Override
	public THIS stroke() { // TODO?
		beginPath();
		return (THIS) this;
	}

	@Override
	public RESULT done() {
		if (parent != null) {
			return (RESULT) parent;
		} else {
			return (RESULT) this;
		}
	}

	@Override
	public THIS spawn() {
		var child = copy();
		return (THIS) child;
	}

	protected TurtleImpl<THIS, RESULT> copy() {
		return new TurtleImpl<THIS, RESULT>(this);
	}

	@Override
	public THIS child(Canvas canvas) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isChild() { // TODO
		return parent != null;
	}

	@Override
	public THIS parent() {
		return (THIS) parent;
	}

	@Override
	public SpriteBuilder sprite() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DrawingBuilder drawing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public THIS go(Direction bearing, double dist) {
		super.go(bearing, dist);
		triggerActions();
		return (THIS) this;
	}

	private void triggerActions() {
		if (last != null && last.point.equals(current.point)) {
			System.out.println("" + last + " == " + current);
		}
		if (writer != null) {
			if (drawing) {
				if (currentStroke == null)
					currentStroke = writer.addStroke();
				if (stepSize > 0) {
					double len = last.point().distanceTo(current.point());
					currentStroke.addLine(last);
					for (double d = stepSize; d < len; d++) {
						Point p = last.point().interpolate(current.point(), d / len);
						PathPointImpl tmp = current.copy();
						tmp.point = p;
						currentStroke.updateLine(last, current);
					}
				}
		
				currentStroke.addLine(last, current);
			} else if (currentStroke != null) {
				currentStroke.endPath();
				currentStroke = null;
			}
		}
	}

	@Override
	public THIS jump(Direction bearing, double dist) {
		drawing = false;
		return super.go(bearing, dist);
	}

	@Override
	public THIS draw(Direction bearing, double dist) {
		drawing = true;
		boolean penStatus = penDown;
		penDown = true;
		current.pen = pen;
		super.go(bearing, dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@Override
	public THIS onDraw(BiConsumer<PathPoint, PathPoint> action) {
		drawAction = action;
		return (THIS) this;
	}

	@Override
	public THIS onMove(Consumer<PathPoint> action) {
		moveAction = action;
		return (THIS) this;
	}

	public String toString() {
		return "turtle(at=" + at() + ", bearing=" + bearing() + ")";
	}

	@Override
	public THIS writePathsTo(PathWriter pathWriter) {
		this.writer = pathWriter;
		return (THIS) this;
	}

	@Override
	public THIS spawn(Consumer<THIS> f) {
		THIS child = spawn();
		f.accept(child);
		child.done();
		return (THIS) this;
	}

	@Override
	public THIS spawn(int n, BiConsumer<THIS, Integer> f) {
		for (int i = 0; i < n; i++) {
			THIS child = spawn();
			f.accept(child, i);
			child.done();
		}
		return (THIS) this;
	}

	@Override
	public <U> THIS spawn(Iterable<U> elts, BiConsumer<THIS, U> f) {
		for (U elt : elts) {
			THIS child = spawn();
			f.accept(child, elt);
			child.done();
		}
		return (THIS) this;
	}

	@Override
	public <U> THIS spawn(Stream<U> elts, BiConsumer<THIS, U> f) {
		elts.forEach((elt) -> {
			THIS child = spawn();
			f.accept(child, elt);
			child.done();
		});
		return (THIS) this;

	}

	@Override
	public THIS repeat(int n, BiConsumer<THIS, Integer> f) {
		for (int i = 0; i < n; i++) {
			f.accept((THIS) this, i);
		}
		return (THIS) this;
	}

	@Override
	public <U> THIS repeat(Iterable<U> elts, BiConsumer<THIS, U> f) {
		for (U elt : elts) {
			f.accept((THIS) this, elt);
		}
		return (THIS) this;
	}

	@Override
	public <U> THIS repeat(Stream<U> elts, BiConsumer<THIS, U> f) {
		elts.forEachOrdered((elt) -> f.accept((THIS) this, elt));
		return (THIS) this;

	}

	@Override
	public THIS apply(Consumer<THIS> f) {
		f.accept((THIS) this);
		return (THIS) this;

	}

	@Override
	public <U> THIS apply(U arg, BiConsumer<THIS, U> f) {
		f.accept((THIS) this, arg);
		return (THIS) this;

	}
}
