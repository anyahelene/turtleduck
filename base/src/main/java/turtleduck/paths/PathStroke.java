package turtleduck.paths;

import java.util.List;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface PathStroke {
	int CLOSED = 1;
	int TRIANGLE_STRIP = 2;

	/**
	 * Add a line
	 * 
	 * Afterwards, the current point will be <code>to</code>
	 * 
	 * @param from Start point of line
	 * @param to   End point of line
	 */
	void addLine(PathPoint from, PathPoint to);

	/**
	 * Finish the current path
	 */
	void endPath();

	/**
	 * Move current point
	 * 
	 * @param point New current point
	 */
	void move(PathPoint point);

	/**
	 * @return Position of the current point, or null
	 */
	Point currentPoint();

	/**
	 * @return Direction of the current point, or null
	 */
//	Direction currentDirection();

	/**
	 * Clear the stored path
	 */
	void clear();

	/**
	 * @return A list of all points in the path
	 */
	List<PathPoint> points();

	void addPoint(PathPoint point);

	void addPoint(Point point);

	String text();

	void addText(PathPoint at, String text);

	void group(String group);

	String group();

	int depth();

	void closePath();

	int options();

	void options(int newOptions);

}