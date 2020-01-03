package turtleduck.turtle.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class LineBuilderImpl implements LineBuilder {

	private Stroke stroke;
	private Geometry geom;
	private Point first;
	private Canvas canvas;
	private List<Point> points = new ArrayList<>();
	private List<Stroke> strokes = new ArrayList<>();
	private boolean closed = false;

	public LineBuilderImpl(Stroke stroke, Geometry geom, Point first, Canvas canvas) {
		this.stroke = stroke;
		this.geom = geom;
		this.first = first;
		this.canvas = canvas;
		points.add(first);
	}

	@Override
	public LineBuilder to(Point next) {
		strokes.add(stroke);
		points.add(next);

		return this;
	}

	@Override
	public LineBuilder to(Stroke stroke, Point next) {
		this.stroke = stroke;
		strokes.add(stroke);
		points.add(next);

		return this;
	}

	@Override
	public LineBuilder close() {
//		strokes.add(stroke);
//		points.add(first);
		closed = true;
		return this;
	}

	@Override
	public Canvas fill(Fill fill, boolean andStroke) {
		canvas.polygon(null, fill, geom, points.toArray(new Point[points.size()]));
		if (andStroke)
			return done();
		else
			return canvas;
	}

	@Override
	public Canvas done() {
		strokes.add(stroke);
		if (closed)
			canvas.polygon(stroke, null, geom, points.toArray(new Point[points.size()]));
		else
			canvas.polyline(stroke, null, geom, points.toArray(new Point[points.size()]));
		return canvas;
	}

}
