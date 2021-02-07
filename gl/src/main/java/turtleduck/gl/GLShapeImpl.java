package turtleduck.gl;

import turtleduck.geometry.Point;
import turtleduck.gl.GLLayer.DrawObject;
import turtleduck.shapes.Shape;

public class GLShapeImpl {
	protected DrawObject dobj;
	protected boolean immediate;

	void drawRectangle(Point p0, Point p1) {
	}

	void drawRectangle(Point center, double width, double height) {
	}

	void drawCircle(Point center, double radius) {
	}

	void drawEllipse(Point center, double width, double height) {
	}

	void drawPoint(Point center) {
	}

	void drawPolyline(Point first, Point... points) {
	}

	void drawPolygon(Point first, Point... points) {
	}

	void drawShape(Point position, Shape shape) {
	}

}
