package turtleduck.turtle;

import turtleduck.geometry.Point;

public interface Pen extends Stroke, Fill, Geometry {
	/**
	 * Draw a dot
	 *
	 * @param point
	 *            Center point of the dot
	 * @return {@code this}, for sending more draw commands
	 */
	Pen dot(Point point);
	
	/**
	 * Draw a line
	 *
	 * @param from
	 *            Start point of the line
	 * @param to
	 *            End point of the line
	 * @return {@code this}, for sending more draw commands
	 */
	Pen line(Point from, Point to);
	/**
	 * Draw lines
	 *
	 * @param points
	 *            A list of points
	 * @return {@code this}, for sending more draw commands
	 */
	Pen polyline(Point ...points);
	/**
	 * Fill a polygon
	 *
	 * @param points
	 *            A list of points
	 * @return {@code this}, for sending more draw commands
	 */
	Pen polygon(Point ...points);
	/**
	 * Fill a strip of triangles
	 *
	 * @param points
	 *            A list of points
	 * @return {@code this}, for sending more draw commands
	 */
	Pen triangles(Point ...points);

	Pen stroke(IShape shape);
	
	Pen fill(IShape shape);
	
	Pen strokeAndFill(IShape shape);
	

	PenBuilder<Pen> change();
}
