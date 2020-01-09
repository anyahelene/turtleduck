module turtleduck.base {
	exports turtleduck;
	exports turtleduck.colors;
	exports turtleduck.objects;
	exports turtleduck.display;
	exports turtleduck.display.impl;
	exports turtleduck.events;
	exports turtleduck.geometry;
	exports turtleduck.text;
	exports turtleduck.text.impl;
	exports turtleduck.turtle;
	exports turtleduck.turtle.base;
	uses turtleduck.display.DisplayInfo;
	uses turtleduck.display.MouseCursor;
	uses turtleduck.events.KeyEvent;
	uses turtleduck.Launcher;
	requires java.logging;
	requires java.xml;
	requires org.joml;
}
