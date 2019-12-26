package turtleduck.jfx;

import javafx.stage.Stage;
import turtleduck.display.DisplayInfo;
import turtleduck.display.Screen;

public class JfxDisplayInfo implements DisplayInfo {
	public static final DisplayInfo INSTANCE = new JfxDisplayInfo();

	private JfxDisplayInfo() {

	}

	public static DisplayInfo provider() {
		return INSTANCE;
	}

	/**
	 * Start the paint display system.
	 * 
	 * This will open a window on the screen, and set up background, text and paint
	 * layers, and listener to handle keyboard input.
	 * 
	 * @param stage A JavaFX {@link javafx.stage.Stage}, typically obtained from the
	 *              {@link javafx.application.Application#start(Stage)} method
	 * @return A screen for drawing on
	 */
	@Override
	public Screen startPaintScene(Object stage) {
		return startPaintScene(stage, Screen.CONFIG_SCREEN_FULLSCREEN_NO_HINT);
	}

	/**
	 * Start the paint display system.
	 * 
	 * This will open a window on the screen, and set up background, text and paint
	 * layers, and listener to handle keyboard input.
	 * 
	 * @param stage A JavaFX {@link javafx.stage.Stage}, typically obtained from the
	 *              {@link javafx.application.Application#start(Stage)} method
	 * @return A screen for drawing on
	 */
	@Override
	public Screen startPaintScene(Object stage, int configuration) {
		return JfxScreen.startPaintScene((Stage) stage, configuration);
	}

	@Override
	public double getDisplayDpi() {
		return javafx.stage.Screen.getPrimary().getDpi();
	}

	@Override
	public double getDisplayHeight() {
		return javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
	}

	@Override
	public double getDisplayWidth() {
		return javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
	}

	@Override
	public double getRawDisplayHeight() {
		return javafx.stage.Screen.getPrimary().getBounds().getHeight();
	}

	@Override
	public double getRawDisplayWidth() {
		return javafx.stage.Screen.getPrimary().getBounds().getWidth();
	}

}
