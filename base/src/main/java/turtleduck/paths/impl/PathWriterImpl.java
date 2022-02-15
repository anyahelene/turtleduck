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

@Internal
public class PathWriterImpl implements PathWriter {
	protected boolean use3d = false;
	protected Queue<PathStrokeImpl> strokes = new LinkedList<>();

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

	public PathStroke addStroke() {
		PathStrokeImpl stroke = new PathStrokeImpl();
		stroke.added = true;
		strokes.add(stroke);
		return stroke;
	}

	class PathStrokeImpl implements PathStroke {
		protected List<PathPoint> points = new ArrayList<>();
		protected boolean finished = false;
		protected boolean added = false;
		protected int read = 0;
		protected int lengthSq = 0;
		protected PathPoint current;
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
			if (points.isEmpty())
				points.add(from);

			points.add(to);
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

	public void clear() {
		for (PathStrokeImpl stroke = strokes.poll(); stroke != null; stroke = strokes.poll()) {
			stroke.clear();
		}
	}

}
