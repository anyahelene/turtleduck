package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.shapes.Rectangle;
import turtleduck.shapes.Rectangle.RectangleBuilder;
import turtleduck.turtle.impl.PathPointImpl;

public class RectangleImpl extends BaseShapeWxH<Rectangle.RectangleBuilder>
		implements Rectangle.RectangleBuilder {

	public RectangleImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		super(canvas, matrix, pw, pos, pen);
	}

	@Override
	protected String writePath(PathWriter writer, Pen pen) {
		Point p0 = position.add(-width / 2, -height / 2);
		PathStroke ps = writer.addStroke();

		PathPointImpl start = new PathPointImpl(p0, pen);
		String id = String.format("rect@%x", System.identityHashCode(this));
		ps.group(id);
		ps.addPoint(start);
		ps.addPoint(p0.add(0, height));
		ps.addPoint(p0.add(width, height));
		ps.addPoint(p0.add(width, 0));
		ps.addPoint(p0);
		ps.endPath();

		return id;
	}

}