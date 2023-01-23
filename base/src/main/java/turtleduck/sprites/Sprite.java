package turtleduck.sprites;

import turtleduck.annotations.Icon;
import turtleduck.turtle.Navigator;

@Icon("👾")
public interface Sprite extends Navigator<Sprite> {
	
	Sprite update();
	
	Sprite transition(String spec);
	
	default Sprite move(double x, double y) {
		return goTo(offsetAxisAligned(x, y));
	}
	
	String id();
}
