package turtleduck.turtle;

import java.util.List;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface PathStroke {
	/**
	 * Add starting point for a line
	 * 
	 * The end point may be updated with {@link #updateLine(PathPoint, PathPoint)},
	 * and finalised with a call to {@link #addLine(PathPoint, PathPoint)}.
	 * 
	 * Afterwards, the current point will be <code>from</code>
	 * 
	 * @param from Start point of line
	 */
	void addLine(PathPoint from);

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
	 * Update the end point of a line previously added by
	 * {@link #addLine(PathPoint)}.
	 * 
	 * Multiple updates may be made, call {@link #addLine(PathPoint, PathPoint)} to
	 * finalise.
	 * 
	 * Afterwards, the current point will be <code>from</code>
	 * 
	 * @param from Start point of line
	 * @param to   New end point of line
	 */
	void updateLine(PathPoint from, PathPoint to);

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
	Direction currentDirection();

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

}