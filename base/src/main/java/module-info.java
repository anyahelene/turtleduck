module turtleduck.base {
	exports turtleduck;
	exports turtleduck.buffer;
	exports turtleduck.colors;
	exports turtleduck.objects;
	exports turtleduck.display;
	exports turtleduck.display.impl;
	exports turtleduck.events;
	exports turtleduck.image;
	exports turtleduck.drawing;
	exports turtleduck.geometry;
	exports turtleduck.comms;
	exports turtleduck.sprites;
	exports turtleduck.grid;
	exports turtleduck.scene.impl;
	exports turtleduck.scene;
	exports turtleduck.sim;
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
	uses turtleduck.image.ImageFactory;
	requires java.logging;
	requires java.xml;
	requires transitive org.joml;
	requires java.desktop;
}
