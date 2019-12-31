package turtleduck.drawing;

import java.util.ArrayList;
import java.util.List;

import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.base.SvgCanvas;

public class DrawingLineBuilder implements LineBuilder {
	private DrawCanvas canvas;
	private List<Segment> segments = new ArrayList<>();
	private boolean polygon = false;
	private Segment segment;
	private Point first, last;
	private Stroke stroke;
	private Geometry geom;

	public DrawingLineBuilder(Stroke stroke, Geometry geom, Point first, DrawCanvas canvas) {
		this.canvas = canvas;
		this.first = first;
		this.last = first;
		this.stroke = stroke;
		this.geom = geom;
		segment = null;
	}

	@Override
	public LineBuilder to(Point next) {
		if (segment == null)
			newSeg(stroke, geom, last);
		segment.points.add(next);
		last = next;
		return null;
	}

	protected void newSeg(Stroke s, Geometry g, Point p) {
		segment = new Segment();
		segment.stroke = s;
		segment.geom = g;
		segment.points = new ArrayList<>();
		segment.points.add(p);
		segments.add(segment);
	}

	@Override
	public LineBuilder to(Stroke stroke, Point next) {
		if (segment == null || stroke != segment.stroke)
			newSeg(stroke, geom, last);
		segment.points.add(next);
		last = next;
		return this;
	}

	@Override
	public LineBuilder close() {
		if (segments.size() > 1) {
			return to(first);
		}
		polygon = true;
		return null;
	}

	@Override
	public DrawCanvas done() {
		if (polygon && segment != null) {
			canvas.polygon(segment.stroke, null, geom, segment.points.toArray(new Point[segment.points.size()]));
		} else if (segments.size() == 1) {
			canvas.polyline(segment.stroke, null, segment.geom, segment.points.toArray(new Point[segment.points.size()]));
		} else {
				for (Segment s : segments) {
					canvas.polyline(s.stroke, null, s.geom, s.points.toArray(new Point[s.points.size()]));
				}
		}
		segments.clear();
		segment = null;
		return canvas;
	}

	protected static class Segment {
		List<Point> points;
		Stroke stroke;
		Geometry geom;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Segment [points=").append(points).append(", stroke=").append(stroke).append(", geom=")
					.append(geom).append("]");
			return builder.toString();
		}
	}
}
