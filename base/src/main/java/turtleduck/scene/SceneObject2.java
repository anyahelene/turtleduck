package turtleduck.scene;

import turtleduck.geometry.Box3;
import turtleduck.geometry.Direction;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

public interface SceneObject2<T extends SceneObject2<T>> extends SceneNode, PositionVector, DirectionVector {
	Point pivot();

	T pivot(Point p);

	T move(double dx, double dy);

	T moveTo(double x, double y);

	T move(Direction dir, double dist);

	Point position();

	T rotate(double angle);

	T rotateTo(double angle);

	T orientTo(Direction dir);

	T orient(Direction dir);

	default T scale(double scale) {
		return scale(scale, scale);
	}

	T scale(double xScale, double yScale);

	Direction orientation();

	Point scale();

	Box3 boundingBox();
}
