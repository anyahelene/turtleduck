package turtleduck.paths;

import java.util.List;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface PathStroke {
	/**
	 * Flag value for closed paths.
	 * 
	 * A closed path has an extra line from the last point back to the first point.
	 */
	int PATH_CLOSED = 1;
	/**
	 * Flag for filled triangle strip paths.
	 * 
	 * Fills the triangle formed by each added point and the two previous points (minimum three points).  
	 * 
	 */
	int PATH_TRIANGLE_STRIP = 2;
	/**
	 * Flags for smooth paths.
	 * 
	 * @see {@link SmoothType#SMOOTH}
	 */
	int PATH_SMOOTH = 4;
	/**
	 * Flag for symmetric smooth paths.
	 * 
	 * No effect unless {@link #PATH_SMOOTH} is also set.
	 * @see {@link SmoothType#SYMMETRIC}
	 */
	int PATH_SYMMETRIC = 8;

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