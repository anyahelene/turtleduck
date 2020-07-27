package turtleduck.sprites;

import turtleduck.display.Canvas;
import turtleduck.turtle.Navigator;

public interface Sprite extends Navigator<Sprite> {

	void draw(Canvas canvas);
}
