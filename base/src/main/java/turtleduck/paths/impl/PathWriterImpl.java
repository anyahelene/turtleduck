package turtleduck.paths.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import turtleduck.annotations.Internal;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.shapes.Particles;

@Internal
public class PathWriterImpl implements PathWriter {
	protected boolean use3d = false;
	protected Queue<PathStrokeImpl> strokes = new LinkedList<>();
	protected int depth = 0;
	protected double x0, y0, x1, y1;

	public PathWriterImpl() {
		clear();
	}

	public boolean hasNextStroke() {
		return !strokes.isEmpty();
	}

	public PathStroke nextStroke() {
		if (strokes.isEmpty())
			return null;
		else {
			PathStrokeImpl stroke = strokes.remove();
			stroke.added = false;
			return stroke;
		}
	}

	public int depth() {
		return depth++;
	}

	public PathStroke addStroke() {
		PathStrokeImpl stroke = new PathStrokeImpl();
		stroke.added = true;
		stroke.depth = depth();
		strokes.add(stroke);
		return stroke;
	}

	class PathStrokeImpl implements PathStroke {
		protected List<PathPoint> points = new ArrayList<>();
		protected boolean finished = false;
		protected boolean added = false;
		protected int read = 0;
		protected int lengthSq = 0;
		protected int options = 0;
		protected PathPoint current;
		protected int depth = 0;
		String text;
		protected String group = null;

		@Override
		public void addPoint(PathPoint point) {
			if (current == null) {
				current = point;
			} else {
				addLine(current, point);
			}
		}

		@Override
		public void addPoint(Point point) {
			if (current == null) {
				throw new IllegalStateException();
			} else {
				PathPointImpl from = (PathPointImpl) current;
				PathPointImpl to = from.copy();
				to.point = point;

				addLine(from, to);
			}
		}

		@Override
		public void addLine(PathPoint from, PathPoint to) {
			if (from.pen() == null || to.pen() == null)
				throw new IllegalArgumentException();
			lengthSq += from.point().distanceToSq(to.point());
			if (points.isEmpty()) {
				points.add(from);
				addBounds(from);
			}

			points.add(to);
			addBounds(to);
			current = to;

			if (!added) {
				added = true;
				strokes.add(this);
			}
		}

		public void endPath() {
			if (points.size() > 1) {
				if (!added) {
					added = true;
					strokes.add(this);
				}
			}
		}

		public void closePath() {
			options |= PathStroke.CLOSED;
			if (points.size() > 1) {
				if (!added) {
					added = true;
					strokes.add(this);
				}
			}
		}

		@Override
		public int depth() {
			return depth;
		}

		@Override
		public int options() {
			return options;
		}

		@Override
		public void options(int options) {
			this.options = options;
		}

		@Override
		public void move(PathPoint point) {
			current = point;
		}

		@Override
		public Point currentPoint() {
			return current.point();
		}

//		@Override
//		public Direction currentDirection() {
//			return current.bearing();
//		}

		@Override
		public void clear() {
			points.clear();
			added = false;
			read = 0;
		}

		@Override
		public List<PathPoint> points() {
			List<PathPoint> list = new ArrayList<>(points.subList(read, points.size()));
			read += list.size() - 1;
			return list;
		}

		public String text() {
			return text;
		}

		@Override
		public void addText(PathPoint at, String text) {
			if (at.pen() == null)
				throw new IllegalArgumentException();
			current = at;
			points.add(at);

			this.text = text;
			if (!added) {
				added = true;
				strokes.add(this);
			}
		}

		public void text(String text) {
			if (points.isEmpty()) {
				if (current == null)
					throw new IllegalStateException();
				points.add(current);
			}
			this.text = text;
			if (!added) {
				added = true;
				strokes.add(this);
			}
		}

		@Override
		public void group(String group) {
			this.group = group;
		}

		public String group() {
			return group;
		}
	}

	public void drawBounds(Pen pen) {
		double x0tmp = x0, y0tmp = y0, x1tmp = x1, y1tmp = y1;

		PathStroke ps = addStroke();

		PathPointImpl start = new PathPointImpl(Point.point(x0tmp, y0tmp), pen);
		String id = String.format("rect@%x", System.identityHashCode(this));
		ps.group(id);
		ps.addPoint(start);
		ps.addPoint(Point.point(x0tmp, y1tmp));
		ps.addPoint(Point.point(x1tmp, y1tmp));
		ps.addPoint(Point.point(x1tmp, y0tmp));
		ps.addPoint(Point.point(x0tmp, y0tmp));
		ps.endPath();
		x0 = y0 = Double.POSITIVE_INFINITY;
		x1 = y1 = Double.NEGATIVE_INFINITY;
	}

	public void addBounds(PathPoint point) {
		Point p = point.point();
		double x = p.x(), y = p.y();
		x0 = Math.min(x0, x);
		y0 = Math.min(y0, y);
		x1 = Math.max(x1, x);
		y1 = Math.max(y1, y);
	}

	public void clear() {
		for (PathStrokeImpl stroke = strokes.poll(); stroke != null; stroke = strokes.poll()) {
			stroke.clear();
		}
		x0 = y0 = Double.POSITIVE_INFINITY;
		x1 = y1 = Double.NEGATIVE_INFINITY;

	}

	@Override
	public Particles addParticles() {
		throw new UnsupportedOperationException();
	}

}
