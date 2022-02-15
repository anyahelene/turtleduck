package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.shapes.Path;
import turtleduck.shapes.Path.PathBuilder;

public class PathImpl extends BaseShapeImpl<Path.PathBuilder> implements Path.PathBuilder {
	protected boolean closed;
	protected PathStroke stroke;
	protected Point current;
	private String id;

	public PathImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		super(canvas, matrix, pw, pos, pen);
		this.closed = false;
		this.id = String.format("path@%x", System.identityHashCode(this));
	}

	@Override
	public Path.PathBuilder moveTo(Point next) {
		if (position == null)
			position = next;
		finishStroke();
		current = next;
		return this;
	}

	protected void finishStroke() {
		if (stroke != null) {
			if (closed && current != null) {
				stroke.addPoint(current);
				closed = false;
			}
			stroke.endPath();
			stroke = null;
		}
	}

	@Override
	public Path.PathBuilder moveTo(double x, double y) {
		return moveTo(Point.point(x, y));
	}

	@Override
	public Path.PathBuilder drawTo(Point next) {
		if (current == null)
			current = position;
		if (stroke == null) {
			PathPointImpl pp = new PathPointImpl(current, pen);
			stroke = pathWriter.addStroke();
			stroke.group(id);
			stroke.addPoint(pp);
		}
		stroke.addPoint(next);
		return this;
	}

	@Override
	public Path.PathBuilder drawTo(double x, double y) {
		return drawTo(Point.point(x, y));
	}

	@Override
	public Path.PathBuilder close() {
		closed = true;
		return this;
	}

	@Override
	protected String writePath(PathWriter writer, Pen pen) {
		finishStroke();
		return id;
	}

}