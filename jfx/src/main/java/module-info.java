module turtleduck.jfx {
	exports turtleduck.jfx;
	requires transitive turtleduck.base;
	requires javafx.graphics;
	provides turtleduck.colors.Xlate with turtleduck.jfx.FxUtil;
}
