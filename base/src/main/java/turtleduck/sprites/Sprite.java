package turtleduck.sprites;

import turtleduck.canvas.Canvas;
import turtleduck.turtle.Navigator;

public interface Sprite extends Navigator<Sprite> {

	void draw(Canvas canvas);
}
