package turtleduck.turtle;

import java.util.List;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface PathWriter {

	PathStroke addStroke();

	interface PathStroke {
		void addLine(PathPoint from);

		void addLine(PathPoint from, PathPoint to);

		void updateLine(PathPoint from, PathPoint to);

		void endPath();

		void move(PathPoint point);

		Point currentPoint();

		Direction currentDirection();

		void clear();
		
		List<PathPoint> points();
	}
}
