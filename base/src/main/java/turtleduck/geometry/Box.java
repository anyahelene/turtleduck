package turtleduck.geometry;

public interface Box {

	double width();

	double height();

	double x();

	double y();

	default Box x(double newX) {
		return position(Point.point(newX, y()));
	}

	default Box y(double newY) {
		return position(Point.point(x(), newY));
	}

	default Box width(double newWidth) {
		return size(newWidth, height());
	}

	default Box height(double newHeight) {
		return size(width(), newHeight);
	}

	Box position(Point newPos);
	Point position();

	default Box position(double newX, double newY) {
		return position(Point.point(newX, newY));
	}

	default Box size(double newWidth, double newHeight) {
		return width(newWidth).height(newHeight);
	}
	
	boolean contains(Point point);
	
	boolean contains(Box other);
	
	boolean overlaps(Box other);
}
