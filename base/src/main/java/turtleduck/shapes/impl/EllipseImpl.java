package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.shapes.Ellipse;
import turtleduck.shapes.Ellipse.EllipseBuilder;

public class EllipseImpl extends BaseShapeWxH<Ellipse.EllipseBuilder> implements Ellipse.EllipseBuilder {

	public EllipseImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		super(canvas, matrix, pw, pos, pen);
	}

	@Override
	public Ellipse.EllipseBuilder radius(double widthAndHeight) {
		this.width = 2 * widthAndHeight;
		this.height = 2 * widthAndHeight;
		return this;
	}

	@Override
	protected String writePath(PathWriter writer, Pen pen) {
		PathStroke ps = writer.addStroke();
		double w = width / 2, h = height / 2;
		double circ = 2 * width + 2 * height;
		PathPointImpl start = new PathPointImpl(position.add(w, 0), pen);
		ps.addPoint(start);
		int steps = Math.max(6, Math.min((int) circ / 3, 36));
		while (360 % steps > 0) {
			steps--;
		}
		int step = 360 / steps;
		for (int i = step; i <= 360; i += step) {
			double a = (2.0 * Math.PI * i) / 360.0;
			ps.addPoint(position.add(Math.cos(a) * w, Math.sin(a) * h));
		}
		String id = String.format("ellipse@%x", System.identityHashCode(this));
		ps.group(id);
		ps.endPath();
		return id;
	}

}