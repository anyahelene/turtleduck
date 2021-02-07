package turtleduck.shapes;


import turtleduck.canvas.Canvas;
import turtleduck.canvas.PenContext;

import turtleduck.geometry.Point;

public interface ShapeBuilder<T> extends PenContext<T> {
	Canvas stroke();

	Canvas fill();

	Canvas strokeAndFill();

	T at(Point p);

	T at(double x, double y);
}
