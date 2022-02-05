package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.annotations.Internal;
import turtleduck.canvas.Canvas;

import turtleduck.colors.Color;
import turtleduck.geometry.Point;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.PenBuilder;
import turtleduck.shapes.Shape;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.PenBuilderDelegate;

/**
 * @author Anya Helene Bagge
 *
 * @param <T> The type of <code>this</code>
 */
@Internal
public abstract class BaseShapeImpl<T extends Shape.Builder<T>> implements Shape, Shape.Builder<T> {
	protected Point position;
	protected Matrix3x2dc matrix;
	protected Pen pen;
	protected PenBuilder<Pen> penBuilder = null;
	protected Canvas canvas;
	protected PathWriter pathWriter;

	protected BaseShapeImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		if (pos == null)
			throw new NullPointerException();
		if (pen == null)
			pen = new BasePen();
		this.canvas = canvas;
		this.position = pos;
		this.pen = pen;
		this.matrix = matrix;
		this.pathWriter = pw;
	}

	@Override
	public Point position() {
		return position;
	}

	@SuppressWarnings("unchecked")
	public T at(Point p) {
		if (p == null)
			throw new NullPointerException();
		position = p;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T at(double x, double y) {
		position = Point.point(x, y);
		return (T) this;
	}

	@Override
	public String stroke() {
		Pen p = pen;
		if (!p.stroking() || p.filling()) {
			p = pen.penChange().stroke(true).fill(false).done();
		}
		return writePath(pathWriter, p);
	}

	@Override
	public String fill() {
		Pen p = pen;
		if (p.stroking() || !p.filling()) {
			p = pen.penChange().stroke(false).fill(true).done();
		}
		return writePath(pathWriter, p);
	}

	@Override
	public String strokeAndFill() {
		Pen p = pen;
		if (!p.stroking() || !p.filling()) {
			p = pen.penChange().stroke(true).fill(true).done();
		}
		return writePath(pathWriter, p);
	}

	@Override
	public String done() {
		return writePath(pathWriter, pen);
	}

	protected abstract String writePath(PathWriter writer, Pen pen);
	@SuppressWarnings("unchecked")
	@Override
	public PenBuilder<T> penChange() {
		penBuilder = pen.penChange();
		return new PenBuilderDelegate<T>(penBuilder, (T) this);
	}


	@Override
	public Pen pen() {
		if (penBuilder != null) {
			pen = penBuilder.done();
			penBuilder = null;
		}
		return pen;
	}

	@SuppressWarnings("unchecked")
	public T pen(Pen newPen) {
		pen = newPen;
		penBuilder = null;
		return (T) this;
	}

}
