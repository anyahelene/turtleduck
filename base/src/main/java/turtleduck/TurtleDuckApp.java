package turtleduck;

import turtleduck.display.Screen;

public interface TurtleDuckApp {
	void start(Screen screen);
	
	void smallStep(double deltaTime);
	
	void bigStep(double deltaTime);
}