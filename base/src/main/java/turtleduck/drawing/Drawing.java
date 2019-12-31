package turtleduck.drawing;

import turtleduck.geometry.BoundingBox;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;

public interface Drawing {

	BoundingBox boundingBox();

	Point position();
	
	void draw(Canvas canvas);
}
