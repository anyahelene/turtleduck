package turtleduck.turtle.impl;

import java.util.IdentityHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.paths.Path;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.PenBuilder;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.sprites.Sprite;
import turtleduck.sprites.SpriteImpl;
import turtleduck.turtle.Annotation;
import turtleduck.turtle.BaseTurtle;
import turtleduck.turtle.DrawingBuilder;
import turtleduck.turtle.SpriteBuilder;
import turtleduck.turtle.Turtle;

public class TurtleImpl<THIS extends BaseTurtle<THIS, RESULT>, RESULT> extends BaseNavigatorImpl<THIS>
		implements BaseTurtle<THIS, RESULT> {
	public static class SpecificTurtle extends TurtleImpl<Turtle, Turtle> implements Turtle {

		public SpecificTurtle(Point p, Direction b, Pen pen, Canvas canvas) {
			super(p, b, pen, canvas);
		}

		public SpecificTurtle(SpecificTurtle parent) {
			super(parent);
		}

		protected SpecificTurtle copy() {
			return new SpecificTurtle(this);
		}
	}

	public static class SpriteTurtle extends TurtleImpl<SpriteBuilder, Sprite> implements SpriteBuilder {

		public SpriteTurtle(Point p, Direction b, Pen pen, Canvas canvas) {
			super(p, b, pen, canvas);
		}

		public SpriteTurtle(SpriteTurtle parent) {
			super(parent);
		}

		public SpriteTurtle(TurtleImpl<?, ?> parent, Function<TurtleImpl<SpriteBuilder, Sprite>, Sprite> result) {
			super(parent, result);
		}

		protected SpriteTurtle copy() {
			return new SpriteTurtle(this);
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
	protected TurtleImpl<?, ?> parent = null;
	protected PenBuilder<Pen> penBuilder;
	private PathWriter writer;
	private PathStroke currentStroke;
	private double stepSize = 0;
	private BiConsumer<PathPoint, PathPoint> drawAction;
	private Consumer<PathPoint> moveAction;
	private Function<TurtleImpl<THIS, RESULT>, RESULT> result;
	private String objId = null;
	private Canvas canvas;

	public TurtleImpl(TurtleImpl<?, ?> parent) {
		super(parent);
		this.pen = parent.pen;
//		this.points = new ArrayList<>();
//		this.points.add(parent.current);
		this.drawAction = parent.drawAction;
		this.moveAction = parent.moveAction;
		this.parent = parent;
		this.writer = parent.writer;
		this.objId = parent.objId;
		this.canvas = parent.canvas;
	}

	public TurtleImpl(TurtleImpl<?, ?> parent, Function<TurtleImpl<THIS, RESULT>, RESULT> result) {
		super(parent);
		this.pen = parent.pen;
//		this.points = new ArrayList<>();
//		this.points.add(parent.current);
		this.drawAction = parent.drawAction;
		this.moveAction = parent.moveAction;
		this.parent = parent;
		this.writer = parent.writer;
		this.result = result;
		this.objId = "path@" + System.identityHashCode(this);
		this.canvas = parent.canvas;
	}

	public TurtleImpl(Point p, Direction b, Pen pen, Canvas canvas) {
		super(p, b);
		this.pen = pen;
		this.canvas = canvas;
	}

	protected void addPoint(PathPointImpl point) {
		last = current;
	}

	@Override
	public void beginPath() {
		last = null;
		if (currentStroke != null) {
			endPath(false);
		}
		currentStroke = writer.addStroke();
		currentStroke.group(objId);
	}


	@Override
	public Path closePath() {
		return endPath(true);
	}

	@Override
	public Path endPath() {
		return endPath(false);
	}

	protected Path endPath(boolean close) {
		if (currentStroke != null) {
			if (close)
				currentStroke.closePath();
			else
				currentStroke.endPath();
			currentStroke = null;
		}
		if (current != null) {
			current.pen = pen();
			PathPointImpl pp = current.copy();
			current = pp;
		}
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
	@SuppressWarnings("unchecked")
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

	protected boolean setPen(boolean down) {
		boolean penStatus = penDown;
		penDown = down;
		if (down) {
			current.pen = pen;
			if (last != null && last.pen == null)
				last.pen = pen;
		}
		return penStatus;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS drawTo(Point to) {
		drawing = true;
		boolean penStatus = setPen(true);
		super.go(to, RelativeTo.WORLD);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS draw(double dist) {
		drawing = true;
		boolean penStatus = setPen(true);
		go(dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS jump(double dist) {
		drawing = false;
		boolean penStatus = penDown;
		penDown = false;
		go(dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS goTo(Point newPos) {
		super.go(newPos, RelativeTo.WORLD);
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS jumpTo(Point newPos) {
		drawing = false;
		boolean penStatus = penDown;
		penDown = false;
		goTo(newPos);
		endPath();
		penDown = penStatus;
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS turn(double angle) {
//		System.out.printf("%s + %g = ", current.bearing, angle);
		direction = direction.yaw(angle);
//		System.out.println(current.bearing);
//		current.rotation += String.format("[yaw%+.1f°]", angle);
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS turn(Direction dir) {
		direction = direction.add(dir);
//		current.rotation += String.format("[+%s]", dir);
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS turnTo(Direction dir) {
		direction = direction.rotateTo(dir);
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS turnTo(double angle) {
		direction = direction.rotateTo(angle);

		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PenBuilder<THIS> penChange() {
		penBuilder = pen.penChange();
		return new PenBuilderDelegate<THIS>(penBuilder, (THIS) this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RESULT done() {
		if (current != null) {
			current.pen = pen();
		}
		if (currentStroke != null) {
			currentStroke.endPath();
			currentStroke = null;
			drawing = false;
		}
		if (result != null) {
			return result.apply(this);
		} else if (parent != null) {
			return (RESULT) parent;
		} else {
			return (RESULT) this;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> THIS annotate(Annotation<T> anno, T value) {
		if (current.annos == null) {
			current.annos = new IdentityHashMap<>();
		}
		current.annos.put(anno, value);
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T annotation(Annotation<T> anno) {
		if (current.annos == null) {
			return null;
		}
		return (T) current.annos.get(anno);
	}

	@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	@Override
	public THIS parent() {
		return (THIS) parent;
	}

	@Override
	public SpriteBuilder sprite() {
		Point here = current.point;
		Direction b = direction;
		SpriteBuilder spawn = new SpriteTurtle(this, t -> {
			return new SpriteImpl(here, b, t.objId, canvas);
		}).goTo(0, 0).turnTo(0);
		return spawn;
	}

	@Override
	public DrawingBuilder drawing() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS go(Direction bearing, double dist) {
		super.go(bearing, dist);
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS pathOptions(int newOptions) {
		if (currentStroke != null)
			currentStroke.options(newOptions);
		return (THIS) this;
	}

	private void triggerActions() {
		if (last != null && last.point.equals(current.point)) {
			System.out.println("" + last.point + " == " + current.point);
		}
		if (writer != null && last != null) {
			if (drawing) {
				if (currentStroke == null) {
					currentStroke = writer.addStroke();
					currentStroke.group(objId);
				}
				if (direction instanceof Orientation) {
					last.orient = (Orientation) direction;
					current.orient = (Orientation) direction;
				}
				currentStroke.addLine(last, current);
			} else if (currentStroke != null) {
				currentStroke.endPath();
				currentStroke = null;
			}
		}
	}

	@Override
	public THIS jump(Direction dir, double dist) {
		drawing = false;
		boolean penStatus = penDown;
		penDown = false;
		direction(dir);
		go(dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS draw(Direction dir, double dist) {
		drawing = true;
		boolean penStatus = penDown;
		penDown = true;
		current.pen = pen;
		direction(dir);
		go(dist);
		penDown = penStatus;
		triggerActions();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS onDraw(BiConsumer<PathPoint, PathPoint> action) {
		drawAction = action;
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS onMove(Consumer<PathPoint> action) {
		moveAction = action;
		return (THIS) this;
	}

	public String toString() {
		return "turtle(at=" + point() + ", bearing=" + direction() + ")";
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS writePathsTo(PathWriter pathWriter) {
		this.writer = pathWriter;
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS spawn(Consumer<THIS> f) {
		THIS child = spawn();
		f.accept(child);
		child.done();
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS spawn(int n, BiConsumer<THIS, Integer> f) {
		for (int i = 0; i < n; i++) {
			THIS child = spawn();
			f.accept(child, i);
			child.done();
		}
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> THIS spawn(Iterable<U> elts, BiConsumer<THIS, U> f) {
		for (U elt : elts) {
			THIS child = spawn();
			f.accept(child, elt);
			child.done();
		}
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> THIS spawn(Stream<U> elts, BiConsumer<THIS, U> f) {
		elts.forEach((elt) -> {
			THIS child = spawn();
			f.accept(child, elt);
			child.done();
		});
		return (THIS) this;

	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS repeat(int n, BiConsumer<THIS, Integer> f) {
		for (int i = 0; i < n; i++) {
			f.accept((THIS) this, i);
		}
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> THIS repeat(Iterable<U> elts, BiConsumer<THIS, U> f) {
		for (U elt : elts) {
			f.accept((THIS) this, elt);
		}
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> THIS repeat(Stream<U> elts, BiConsumer<THIS, U> f) {
		elts.forEachOrdered((elt) -> f.accept((THIS) this, elt));
		return (THIS) this;

	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS apply(Consumer<THIS> f) {
		f.accept((THIS) this);
		return (THIS) this;

	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> THIS apply(U arg, BiConsumer<THIS, U> f) {
		f.accept((THIS) this, arg);
		return (THIS) this;

	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS pitch(double angle) {
		direction = direction.pitch(angle);
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS roll(double angle) {
		direction = direction.roll(angle);
		return (THIS) this;
	}
}
