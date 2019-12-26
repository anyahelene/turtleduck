module turtleduck.base {
	exports turtleduck;
	exports turtleduck.colors;
	exports turtleduck.display;
	exports turtleduck.events;
	exports turtleduck.text;
	exports turtleduck.turtle;
	uses turtleduck.display.MouseCursor;
	uses turtleduck.display.DisplayInfo;
	uses turtleduck.events.KeyEvent;
	uses turtleduck.events.KeyCode;
	uses turtleduck.Launcher;
	requires java.logging;
	requires java.xml;
}
