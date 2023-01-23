package turtleduck;

import turtleduck.display.Screen;

public interface TurtleDuckApp {
    void update(FrameInfo frameInfo);

	void start(Screen screen);
}
