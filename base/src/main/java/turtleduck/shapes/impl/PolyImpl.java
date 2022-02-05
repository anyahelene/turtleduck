package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.shapes.Poly;
import turtleduck.shapes.Poly.LineBuilder;
import turtleduck.turtle.impl.PathPointImpl;

public class PolyImpl extends BaseShapeImpl<Poly.LineBuilder> implements Poly.LineBuilder {
	protected boolean closed;
	protected PathStroke stroke;

	public PolyImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen, boolean closed) {
		super(canvas, matrix, pw, pos, pen);
		this.closed = closed;
	}

	@Override
	public LineBuilder to(Point next) {
		if (stroke == null) {
			PathPointImpl pp = new PathPointImpl(position, pen);
			stroke = pathWriter.addStroke();
			stroke.addPoint(pp);
		}
		stroke.addPoint(next);
		return this;
	}

	@Override
	public LineBuilder to(double x, double y) {
		return to(Point.point(x, y));
	}

	@Override
	public LineBuilder close() {
		closed = true;
		return this;
	}

	@Override
	protected String writePath(PathWriter writer, Pen pen) {
		if (closed)
			this.stroke.addPoint(position);

		String id = String.format("poly@%x", System.identityHashCode(this));
		this.stroke.group(id);
		this.stroke.endPath();
		return id;
	}

}