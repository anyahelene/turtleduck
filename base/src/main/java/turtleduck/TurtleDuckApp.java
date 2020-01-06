package turtleduck;

import turtleduck.display.Screen;

public interface TurtleDuckApp {
	void bigStep(double deltaTime);

	void smallStep(double deltaTime);

	void start(Screen screen);
}
