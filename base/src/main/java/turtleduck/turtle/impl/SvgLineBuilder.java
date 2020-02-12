package turtleduck.turtle.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.base.SvgCanvas;

public class SvgLineBuilder implements LineBuilder {
	private SvgCanvas canvas;
	private List<Segment> segments = new ArrayList<>();
	private boolean polygon = false;
	private Segment segment;
	private Point first, last;
	private Stroke stroke;

	public SvgLineBuilder(Stroke stroke, Point first, SvgCanvas canvas) {
		this.canvas = canvas;
		this.first = first;
		this.last = first;
		this.stroke = stroke;
		segment = null;
	}

	@Override
	public LineBuilder to(Point next) {
		if (segment == null)
			newSeg(stroke, last);
		segment.points.add(next);
		last = next;
		return null;
	}

	protected void newSeg(Stroke s, Point p) {
		segment = new Segment();
		segment.stroke = s;
		segment.points = new ArrayList<>();
		segment.points.add(p);
		segments.add(segment);
	}

	@Override
	public LineBuilder to(Stroke stroke, Point next) {
		if (segment == null || stroke != segment.stroke)
			newSeg(stroke,last);
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
			canvas.polygon(segment.stroke, null,segment.points.toArray(new Point[segment.points.size()]));
		} else if (segments.size() == 1) {
			canvas.polyline(segment.stroke, null,  segment.points.toArray(new Point[segment.points.size()]));
		} else {
				for (Segment s : segments) {
					canvas.polyline(s.stroke, null, s.points.toArray(new Point[s.points.size()]));
				}
		}
		segments.clear();
		segment = null;
		return canvas;
	}

	protected static class Segment {
		List<Point> points;
		Stroke stroke;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Segment [points=").append(points).append(", stroke=").append(stroke).append(", geom=")
					.append("]");
			return builder.toString();
		}
	}

	@Override
	public Canvas fill(Fill fill, boolean andStroke) {
		// TODO Auto-generated method stub
		return null;
	}
}
