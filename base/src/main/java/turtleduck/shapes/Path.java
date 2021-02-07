package turtleduck.shapes;

import turtleduck.geometry.Point;

public interface Path extends Shape {

	public interface PathBuilder extends Shape.Builder<PathBuilder> {
		PathBuilder moveTo(Point next);

		PathBuilder moveTo(double x, double y);

		PathBuilder drawTo(Point next);

		PathBuilder drawTo(double x, double y);

		PathBuilder close();
	}
}
