module turtleduck.jfx {
	exports turtleduck.gl;
	requires transitive turtleduck.base;
	requires org.lwjgl;
	provides turtleduck.display.MouseCursor with turtleduck.gl.GLCursor;
	provides turtleduck.Launcher with turtleduck.gl.GLLauncher;
	provides turtleduck.display.DisplayInfo with turtleduck.gl.GLDisplayInfo;
}
