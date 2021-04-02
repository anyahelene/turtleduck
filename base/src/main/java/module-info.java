module turtleduck.base {
	exports turtleduck;
	exports turtleduck.async;
	exports turtleduck.buffer;
	exports turtleduck.colors;
	exports turtleduck.objects;
	exports turtleduck.display;
	exports turtleduck.display.impl;
	exports turtleduck.events;
	exports turtleduck.image;
	exports turtleduck.drawing;
	exports turtleduck.geometry;
	exports turtleduck.messaging;
	exports turtleduck.messaging.generated;
	exports turtleduck.canvas;
	exports turtleduck.sprites;
	exports turtleduck.shapes;
	exports turtleduck.grid;
	exports turtleduck.scene.impl;
	exports turtleduck.scene;
	exports turtleduck.sim;
	exports turtleduck.terminal;
	exports turtleduck.text;
	exports turtleduck.text.impl;
	exports turtleduck.turtle;
	exports turtleduck.turtle.impl;

	exports turtleduck.util;

	uses turtleduck.display.DisplayInfo;
	uses turtleduck.display.MouseCursor;
	uses turtleduck.events.KeyEvent;
	uses turtleduck.Launcher;
	uses turtleduck.image.ImageFactory;

	requires turtleduck.anno;
	requires java.logging;
	requires java.xml;
	requires transitive org.joml;
	requires java.desktop;
	requires java.base;
	requires transitive org.slf4j;
	requires transitive com.github.spotbugs.annotations;
	requires java.compiler;
	
}
