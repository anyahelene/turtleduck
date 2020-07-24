package turtleduck.display;

import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.image.Image;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Turtle;

public interface Canvas extends Layer {
	/**
	 * Draw a dot
	 *
	 * @param point Center point of the dot
	 * @return {@code this}, for sending more draw commands
	 */
//	Canvas dot(Stroke pen, Point point);

	/**
	 * Draw a line
	 *
	 * @param from Start point of the line
	 * @param to   End point of the line
	 * @return {@code this}, for sending more draw commands
	 */
//	Canvas line(Stroke pen, Point from, Point to);

//	LineBuilder lines(Stroke pen, Point from);

	/**
	 * Draw lines
	 *
	 * @param points A list of points
	 * @return {@code this}, for sending more draw commands
	 */
//	Canvas polyline(Stroke pen, Fill fill, Point... points);

	/**
	 * Fill a polygon
	 *
	 * @param points A list of points
	 * @return {@code this}, for sending more draw commands
	 */
//	Canvas polygon(Stroke pen, Fill fill, Point... points);

	/**
	 * Fill a strip of triangles
	 *
	 * @param points A list of points
	 * @return {@code this}, for sending more draw commands
	 */
//	Canvas triangles(Stroke pen, Fill fill, Point... points);

//	Canvas shape(Stroke pen, Fill fill, IShape shape);

	Canvas clear();

	Canvas clear(Fill fill);
	
	Pen createPen();

	Turtle createTurtle();
	
	Canvas flush();
	Canvas draw(Path path);
	Canvas draw(Drawing drawing);

	void drawImage(Point at, Image img);
}
