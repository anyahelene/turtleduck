package turtleduck.shapes;

import turtleduck.canvas.Canvas;
import turtleduck.canvas.PenContext;
import turtleduck.geometry.Point;

public interface Shape {
	Point position();

	public interface Builder<T> extends PenContext<T> {
		Canvas stroke();

		Canvas fill();

		Canvas strokeAndFill();

		T at(Point p);

		T at(double x, double y);
	}

	public interface WxHShape {
		double width();

		double height();
	}

	public interface WxHBuilder<T> extends Builder<T> {
		T width(double width);

		T height(double height);
	}
}
