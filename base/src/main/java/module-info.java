module turtleduck.base {
	exports turtleduck;
	exports turtleduck.colors;
	exports turtleduck.objects;
	exports turtleduck.display;
	exports turtleduck.display.impl;
	exports turtleduck.events;
	exports turtleduck.drawing;
	exports turtleduck.geometry;
	exports turtleduck.comms;
	exports turtleduck.terminal;
	exports turtleduck.text;
	exports turtleduck.text.impl;
	exports turtleduck.turtle;
	exports turtleduck.util;
	uses turtleduck.display.DisplayInfo;
	uses turtleduck.display.MouseCursor;
	uses turtleduck.events.KeyEvent;
	uses turtleduck.Launcher;
	uses turtleduck.comms.MessageData;
	requires java.logging;
	requires java.xml;
	requires org.joml;
}
