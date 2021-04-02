package turtleduck.sprites;

import turtleduck.annotations.Icon;
import turtleduck.canvas.Transformation;
import turtleduck.geometry.Point;
import turtleduck.messaging.CanvasService;
import turtleduck.turtle.Navigator;

@Icon("👾")
public interface Sprite extends Navigator<Sprite> {
	
	Sprite update();
	
	Sprite transition(String spec);
	
	default Sprite move(double x, double y) {
		return go(Point.point(x, y), RelativeTo.POSITION);
	}
	
	String id();
}
