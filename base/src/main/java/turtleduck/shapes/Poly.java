package turtleduck.shapes;

import turtleduck.geometry.Point;

public interface Poly extends Shape {
	public interface LineBuilder extends Shape.Builder<LineBuilder> {
		LineBuilder to(Point next);

		LineBuilder to(double x, double y);

		LineBuilder close();
	}
}
