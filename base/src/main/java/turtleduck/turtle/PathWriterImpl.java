package turtleduck.turtle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

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

	class PathStrokeImpl implements PathWriter.PathStroke {
		protected List<PathPoint> points = new ArrayList<>();
		protected boolean updating = false;
		protected boolean finished = false;
		protected boolean added = false;
		protected int read = 0;
		protected int lengthSq = 0;
		protected PathPoint current;

		public void addLine(PathPoint from) {
			addLine(from, from);
			updating = true;
		}

		public void addLine(PathPoint from, PathPoint to) {
			if (from.pen() == null || to.pen() == null)
				throw new IllegalArgumentException();
			lengthSq += from.point().distanceToSq(to.point());
			if (!updating) {
				if (points.isEmpty())
					points.add(from);
//				else if (points.get(points.size() - 2) != from)
//					throw new IllegalArgumentException("Invalid update: addLine(" + from + ", " + to + ")");

				points.add(to);
				current = to;
			} else {
				updating = false;
			}
//			if (currentStroke.size() >= 3) {
//				PathPoint from2 = points.get(currentStroke.get(currentStroke.size() - 3));
//				tangents.set(currentStroke.get(currentStroke.size() - 2),
//						from2.bearing().interpolate(from.bearing(), 0.5));
//			}
			if (!added) {
				added = true;
				strokes.add(this);
			}
		}

		public void updateLine(PathPoint from, PathPoint to) {
			if (!updating)
				throw new IllegalStateException("updateLine(p1,p2) without previous addLine(p)");
			if (points.get(points.size() - 2) != from)
				throw new IllegalArgumentException("Invalid update: updateLine(" + from + ", " + to + ")");
			points.set(points.size() - 1, to);
			current = to;
			if (!added) {
				added = true;
				strokes.add(this);
			}
		}

		public void endPath() {
			PathPoint first = points.get(0);
			if (first.point().distanceToSq(current.point()) < 0.1) {
			}
			if (!added) {
				added = true;
				strokes.add(this);
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

		@Override
		public Direction currentDirection() {
			return current.bearing();
		}

		@Override
		public void clear() {

		}

		@Override
		public List<PathPoint> points() {
			return points;
		}
	}
}
