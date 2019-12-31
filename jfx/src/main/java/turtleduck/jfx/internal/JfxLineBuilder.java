package turtleduck.jfx.internal;

import java.util.ArrayList;
import java.util.List;

import turtleduck.geometry.Point;
import turtleduck.jfx.JfxCanvas;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Stroke;

public class JfxLineBuilder implements LineBuilder {
	private JfxCanvas canvas;
	private List<Segment> segments = new ArrayList<>();
	private boolean polygon = false;
	private Segment segment;
	private Point first, last;
	private Stroke stroke;
	private Geometry geom;

	public JfxLineBuilder(Stroke stroke, Geometry geom, Point first, JfxCanvas canvas) {
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
		segment.points = new PointList();
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
	public Canvas done() {
		if (polygon && segment != null) {
			canvas.strokePolygon(segment.stroke, segment.geom, segment.points);
		} else if (segments.size() == 1) {
			canvas.strokePolyline(segment.stroke, segment.geom, segment.points);
		} else {
			Object o = canvas.beginLines();
			try {
				for (Segment s : segments) {
					canvas.strokePolyline(s.stroke, s.geom, s.points);
				}
			} finally {
				canvas.endLines(o);
			}
		}
		segments.clear();
		segment = null;
		return canvas;
	}

	protected static class Segment {
		PointList points;
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
