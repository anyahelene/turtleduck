module turtleduck.jfx {
	exports turtleduck.jfx;
	requires transitive turtleduck.base;
	requires javafx.graphics;
	provides turtleduck.display.MouseCursor with turtleduck.jfx.JfxCursor;
	provides turtleduck.Launcher with turtleduck.jfx.JfxLauncher;
	provides turtleduck.display.DisplayInfo with turtleduck.jfx.JfxDisplayInfo;
}
