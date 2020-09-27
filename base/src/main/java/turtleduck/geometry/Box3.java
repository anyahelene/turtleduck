package turtleduck.geometry;

public interface Box3 {

	double width();

	double height();

	double depth();

	double x();

	double y();

	double z();

	default Box3 x(double newX) {
		return position(Point.point(newX, y(), z()));
	}

	default Box3 y(double newY) {
		return position(Point.point(x(), newY, z()));
	}

	default Box3 z(double newZ) {
		return position(Point.point(x(), y(), newZ));
	}

	default Box3 width(double newWidth) {
		return size(newWidth, height(), depth());
	}

	default Box3 height(double newHeight) {
		return size(width(), newHeight, depth());
	}

	default Box3 depth(double newDepth) {
		return size(width(), height(), newDepth);
	}

	Box3 position(Point newPos);

	Point position();

	default Box3 position(double newX, double newY, double newZ) {
		return position(Point.point(newX, newY, newZ));
	}

	default Box3 size(double newWidth, double newHeight, double newDepth) {
		return width(newWidth).height(newHeight).depth(newDepth);
	}

	boolean contains(Point point);

	boolean contains(Box3 other);

	boolean overlaps(Box3 other);
}
