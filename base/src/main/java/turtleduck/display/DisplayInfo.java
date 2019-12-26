package turtleduck.display;

public interface DisplayInfo {
	/**
	 * Get the resolution of this screen, in DPI (pixels per inch).
	 * 
	 * @return The primary display's DPI
	 * @see javafx.stage.Screen#getDpi()
	 */
	double getDisplayDpi();

	/**
	 * Get the native physical width of the screen, in pixels.
	 * 
	 * <p>
	 * This will not include such things as toolbars, menus and such (on a desktop),
	 * or take pixel density into account (e.g., on high resolution mobile devices).
	 * 
	 * @return Raw width of the display
	 * @see javafx.stage.Screen#getBounds()
	 */
	double getRawDisplayWidth();

	/**
	 * Get the native physical height of the screen, in pixels.
	 * 
	 * <p>
	 * This will not include such things as toolbars, menus and such (on a desktop),
	 * or take pixel density into account (e.g., on high resolution mobile devices).
	 * 
	 * @return Raw width of the display
	 * @see javafx.stage.Screen#getBounds()
	 */
	double getRawDisplayHeight();

	/**
	 * Get the width of the display, in pixels.
	 * 
	 * <p>
	 * This takes into account such things as toolbars, menus and such (on a
	 * desktop), and pixel density (e.g., on high resolution mobile devices).
	 * 
	 * @return Width of the display
	 * @see javafx.stage.Screen#getVisualBounds()
	 */
	double getDisplayWidth();

	/**
	 * Get the height of the display, in pixels.
	 * 
	 * <p>
	 * This takes into account such things as toolbars, menus and such (on a
	 * desktop), and pixel density (e.g., on high resolution mobile devices).
	 * 
	 * @return Height of the display
	 * @see javafx.stage.Screen#getVisualBounds()
	 */
	double getDisplayHeight();

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
	Screen startPaintScene(Object stage);

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
	Screen startPaintScene(Object stage, int configuration);

}
