package turtleduck.drawing;

import turtleduck.geometry.BoundingBox;
import turtleduck.geometry.Point;
import turtleduck.paths.Path;

public interface Drawing {

	BoundingBox boundingBox();

	Point position();
	
	Drawing draw(Path path);
	Drawing draw(Drawing subDrawing);
}
