package turtleduck.canvas;

import turtleduck.annotations.Icon;
import turtleduck.display.Layer;
import turtleduck.geometry.Point;
import turtleduck.messaging.CanvasService;
import turtleduck.shapes.Ellipse;
import turtleduck.shapes.Image;
import turtleduck.shapes.Path;
import turtleduck.shapes.Poly;
import turtleduck.shapes.Rectangle;
import turtleduck.shapes.Shape;
import turtleduck.shapes.Text;
import turtleduck.turtle.Turtle;
import turtleduck.turtle.Turtle3;

/**
 * @author anya
 *
 */
@Icon("🖼️")
public interface Canvas extends PenContext<Canvas>, TransformContext2<Canvas> {

	/**
	 * Create a new sub-canvas with its own drawing context.
	 * 
	 * Changing the style of the sub-canvas won't affect its parent and vice verse.
	 * Call {@link #done()} to retrieve the parent and continue drawing on it.
	 * 
	 * @return
	 */
	Canvas spawn();

	/**
	 * End drawing to a sub-canvas.
	 * 
	 * @return The parent of the sub-canvas, for continued drawing.
	 */
	Canvas done();

	Path.PathBuilder path();

	Rectangle.RectangleBuilder rectangle();

	Ellipse.EllipseBuilder ellipse();

	Text.TextBuilder text();

	default Text.TextBuilder text(String text) {
		return text().text(text);
	}

	Image.ImageBuilder image();

	Poly.LineBuilder polygon();

	Poly.LineBuilder polyline();

	Canvas drawRectangle(Point p0, Point p1);

	Canvas drawRectangle(Point center, double width, double height);

	Canvas drawCircle(Point center, double radius);

	Canvas drawEllipse(Point center, double width, double height);

	Turtle turtle();

	Turtle3 turtle3();

	/**
	 * Draw a dot
	 *
	 * @param center Center point of the dot
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas drawPoint(Point center);

	/**
	 * Draw a line
	 *
	 * @param from Start point of the line
	 * @param to   End point of the line
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas drawLine(Point from, Point to);

	/**
	 * Draw a polyline
	 *
	 * A polyline is an open polygon, i.e., there is no final line back to the
	 * starting point.
	 *
	 * @param from   Start point of the polyline (may be null to, to draw an array
	 *               of points)
	 * @param points Subsequent points of the polyline
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas drawPolyline(Point first, Point... points);

	/**
	 * Draw a polygon
	 * 
	 * A polygon is a closed polyline, i.e., it automatically adds a line from the
	 * final point to the starting point.
	 *
	 * @param from   Start point of the polygon (may be null to, to draw an array of
	 *               points)
	 * @param points Subsequent points of the polyline
	 * @return {@code this}, for sending more draw commands
	 */
	Canvas drawPolygon(Point first, Point... points);

	Canvas drawShape(Point position, Shape shape);
	
	Transformation<Canvas> transform(String id);
	
	CanvasService service();

	void onKeyPress(String javaScript);

	void setText(String id, String newText);

	void evalScript(String javaScript);
}
