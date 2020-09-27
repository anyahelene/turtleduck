package turtleduck.scene;

import turtleduck.geometry.Box3;
import turtleduck.geometry.Direction;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

public interface SceneObject3<T extends SceneObject3<T>> extends SceneNode , PositionVector, DirectionVector{ 
	Point pivot();

	T pivot(Point p);

	T move(double dx, double dy, double dz);

	T moveTo(double x, double y, double z);
	
	T move(Direction dir, double dist);

	Point position();

	T yaw(double angle);
	T pitch(double angle);
	T roll(double angle);
	T yawTo(double angle);
	T pitchTo(double angle);
	T rollTo(double angle);
	T orient(Direction dir);

	T orientTo(Direction dir);


	default T scale(double scale) {
		return scale(scale, scale, scale);
	}

	T scale(double xScale, double yScale, double zScale);
	
	Direction orientation();
	
	Point scale();
	
	Box3 boundingBox();
}
