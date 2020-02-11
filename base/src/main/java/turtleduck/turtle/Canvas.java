package turtleduck.turtle;

import turtleduck.geometry.Point;
import turtleduck.objects.IdentifiedObject;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleDuckImpl;

public interface Canvas extends IdentifiedObject {
	/**
	 * Draw a dot
	 *
	 * @param point Center point of the dot
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas dot(Stroke pen, Geometry geom, Point point);

	/**
	 * Draw a line
	 *
	 * @param from Start point of the line
	 * @param to   End point of the line
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas line(Stroke pen, Geometry geom, Point from, Point to);

//	LineBuilder lines(Stroke pen, Geometry geom, Point from);

	/**
	 * Draw lines
	 *
	 * @param points A list of points
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas polyline(Stroke pen, Fill fill, Geometry geom, Point... points);

	/**
	 * Fill a polygon
	 *
	 * @param points A list of points
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas polygon(Stroke pen, Fill fill, Geometry geom, Point... points);

	/**
	 * Fill a strip of triangles
	 *
	 * @param points A list of points
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas triangles(Stroke pen, Fill fill, Geometry geom, Point... points);

	Canvas shape(Stroke pen, Fill fill, Geometry geom, IShape shape);

	Canvas path(Stroke pen, Fill fill, Geometry geom, PathBuilder path);

	Canvas clear();

	Canvas clear(Fill fill);

	TurtleControl createControl();
	
	Pen createPen();

	SimpleTurtle createSimpleTurtle();

	TurtleDuck createTurtleDuck();
	
	void flush();

}
