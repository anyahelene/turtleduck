package turtleduck.display;

import java.util.function.Predicate;

import turtleduck.annotations.Icon;
import turtleduck.canvas.Canvas;
import turtleduck.colors.Color;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.objects.IdentifiedObject;
import turtleduck.scene.SceneWorld;
import turtleduck.text.TextWindow;

@Icon("📺")
public interface Screen extends IdentifiedObject {

	/** 16:9 */
	public static final int ASPECT_WIDE = 0;
	/** 16:10 */
	int ASPECT_MEDIUM = 1;
	/** 4:3 */
	int ASPECT_CLASSIC = 2;
	int ASPECT_NATIVE = 2;
	int _CONFIG_ASPECT_SHIFT = 0;
	int _CONFIG_ASPECT_MASK = 3 << _CONFIG_ASPECT_SHIFT;
	int _CONFIG_SCREEN_SHIFT = 2;
	int _CONFIG_SCREEN_MASK = 7 << _CONFIG_SCREEN_SHIFT;
	int _CONFIG_PIXELS_SHIFT = 5;
	int _CONFIG_PIXELS_MASK = 7 << _CONFIG_PIXELS_SHIFT;
	int _CONFIG_COORDS_SHIFT = 8;
	int _CONFIG_COORDS_MASK = 1 << _CONFIG_COORDS_SHIFT;
	int _CONFIG_FLAG_SHIFT = 9;
	int _CONFIG_FLAG_MASK = 7 << _CONFIG_FLAG_SHIFT;

	/** Screen's initial aspect ratio should be 16:9 */
	int CONFIG_ASPECT_WIDE = 0 << _CONFIG_ASPECT_SHIFT;
	/** Screen's initial aspect ratio should be 16:10 */
	int CONFIG_ASPECT_MEDIUM = 1 << _CONFIG_ASPECT_SHIFT;
	/** Screen's initial aspect ratio should be 4:3 */
	int CONFIG_ASPECT_CLASSIC = 2 << _CONFIG_ASPECT_SHIFT;
	/** Screen's initial aspect ratio should be the same as the device display. */
	int CONFIG_ASPECT_DEVICE = 3 << _CONFIG_ASPECT_SHIFT;
	/** Screen should start in a window. */
	int CONFIG_SCREEN_WINDOWED = 0 << _CONFIG_SCREEN_SHIFT;
	/** Screen should start in a borderless window. */
	int CONFIG_SCREEN_BORDERLESS = 1 << _CONFIG_SCREEN_SHIFT;
	/** Screen should start in a transparent window. */
	int CONFIG_SCREEN_TRANSPARENT = 2 << _CONFIG_SCREEN_SHIFT;
	/** Screen should start fullscreen. */
	int CONFIG_SCREEN_FULLSCREEN = 3 << _CONFIG_SCREEN_SHIFT;
	/**
	 * Screen should start fullscreen, without showing a "Press ESC to exit
	 * fullscreen" hint.
	 */
	int CONFIG_SCREEN_FULLSCREEN_NO_HINT = 4 << _CONFIG_SCREEN_SHIFT;

	/**
	 * Canvas size / number of pixels should be determined the default way.
	 * 
	 * The default is {@link #CONFIG_PIXELS_DEVICE} for
	 * {@link #CONFIG_SCREEN_FULLSCREEN} and {@link #CONFIG_COORDS_DEVICE}, and
	 * {@link #CONFIG_PIXELS_STEP_SCALED} otherwise.
	 */
	int CONFIG_PIXELS_DEFAULT = 0 << _CONFIG_PIXELS_SHIFT;
	/**
	 * Canvas size / number of pixels will be an integer multiple or fraction of the
	 * logical canvas size that fits the native display size.
	 * 
	 * Scaling by whole integers makes it less likely that we get artifacts from
	 * rounding errors or JavaFX's antialiasing (e.g., fuzzy lines).
	 */
	int CONFIG_PIXELS_STEP_SCALED = 1 << _CONFIG_PIXELS_SHIFT;
	/** Canvas size / number of pixels will the same as the native display size. */
	int CONFIG_PIXELS_DEVICE = 2 << _CONFIG_PIXELS_SHIFT;
	/**
	 * Canvas size / number of pixels will the same as the logical canvas size
	 * (typically 1280x960).
	 */
	int CONFIG_PIXELS_LOGICAL = 3 << _CONFIG_PIXELS_SHIFT;
	/**
	 * Canvas size / number of pixels will be scaled to fit the native display size.
	 */
	int CONFIG_PIXELS_SCALED = 4 << _CONFIG_PIXELS_SHIFT;

	/**
	 * The logical canvas coordinate system will be in logical units (i.e., 1280
	 * pixels wide regardless of how many pixels wide the screen actually is)
	 */
	int CONFIG_COORDS_LOGICAL = 0 << _CONFIG_COORDS_SHIFT;
	/** The logical canvas coordinate system will match the display. */
	int CONFIG_COORDS_DEVICE = 1 << _CONFIG_COORDS_SHIFT;
	int CONFIG_FLAG_HIDE_MOUSE = 1 << _CONFIG_FLAG_SHIFT;
	int CONFIG_FLAG_NO_AUTOHIDE_MOUSE = 2 << _CONFIG_FLAG_SHIFT;
	int CONFIG_FLAG_DEBUG = 4 << _CONFIG_FLAG_SHIFT;

	void clearBackground();

	/**
	 * Clear the screen.
	 * 
	 * <p>
	 * Everything on all layers is removed, leaving only transparency.
	 */
	Screen clear();

	Canvas createCanvas();

	TextWindow createTextWindow();

	Layer getBackgroundPainter();

	/**
	 * Return an input control for the given key/button/stick code.
	 * 
	 * @param type       the type of value that should be provided, usually
	 *                   Boolean.class or Float.class
	 * @param code       a keycode, usually from
	 *                   {@link turtleduck.events.KeyCodes.GamePad}
	 * @param controller which gamepad/controller to use (0 for the first, 1 for the
	 *                   second, -1 for
	 * @return
	 */
	<T> InputControl<T> inputControl(Class<T> type, int code, int controller);

	/** @return the keyOverride */
	Predicate<KeyEvent> getKeyOverride();

	/** @return the keyHandler */
	Predicate<KeyEvent> getKeyPressedHandler();

	/** @return the keyReleasedHandler */
	Predicate<KeyEvent> getKeyReleasedHandler();

	/** @return the keyTypedHandler */
	Predicate<KeyEvent> getKeyTypedHandler();

	double frameBufferHeight();

	double frameBufferWidth();

	/**
	 * Width of the virtual screen.
	 * 
	 * Virtual screen coordinates are used for drawing, and are usually independent
	 * of the actual display size, resolution and window size. The default width is
	 * 1280, with the height scaled according to aspect ratio.
	 * 
	 * @return Screen width, in virtual coordinates
	 */
	double width();

	/**
	 * Height of the virtual screen.
	 * 
	 * Virtual screen coordinates are used for drawing, and are usually independent
	 * of the actual display size, resolution and window size. The default width is
	 * 1280, with the height scaled according to aspect ratio.
	 * 
	 * @return Screen height, in virtual coordinates
	 */
	double height();

	/**
	 * Hide the mouse cursor.
	 */
	void hideMouseCursor();

	/**
	 * @return True if display is full screen
	 */
	boolean isFullScreen();

	void moveToBack(Layer layer);

	void moveToFront(Layer layer);

	void setBackground(Color bgColor);

	void setFullScreen(boolean fullScreen);

	void setHideFullScreenMouseCursor(boolean hideIt);

	/**
	 * Set a "key pressed" handler that will be called <em>before</em> other
	 * handlers
	 * 
	 * @param keyOverride the handler, should return true if the event is consumed
	 */
	void setKeyOverride(Predicate<KeyEvent> keyOverride);

	/**
	 * Set handler for "key pressed" events
	 * 
	 * @param keyHandler the handler, should return true if the event is consumed
	 */
	void setKeyPressedHandler(Predicate<KeyEvent> keyHandler);

	/**
	 * Set handler for "key released" events
	 * 
	 * @param keyReleasedHandler the handler, should return true if the event is
	 *                           consumed
	 */
	void setKeyReleasedHandler(Predicate<KeyEvent> keyReleasedHandler);

	/**
	 * Set handler for "key typed" events
	 * 
	 * @param keyTypedHandler the handler, should return true if the event is
	 *                        consumed
	 */
	void setKeyTypedHandler(Predicate<KeyEvent> keyTypedHandler);

	/**
	 * Set the mouse cursor shape.
	 * 
	 * @param cursor A cursor shape constant
	 */
	void setMouseCursor(MouseCursor cursor);

	/**
	 * Make the mouse cursor visible.
	 */
	void showMouseCursor();

	/**
	 * @return A Canvas that can be used for drawing debug information
	 */
	Canvas debugCanvas();

	/**
	 * Make sure all graphics changes are displayed
	 */
	void flush();

	/**
	 * Whether to use the "alternate" shortcut key combination; i.e., Ctrl-Shift
	 * instead of Ctrl.
	 * 
	 * This can be used if the Ctrl key with conflict with other keybindings (e.g.,
	 * Ctrl-C and such for a shell). On Mac, the shortcut key is always Cmd,
	 * regardless of this setting.
	 * 
	 * @param useAlternate
	 */
	void useAlternateShortcut(boolean useAlternate);

	/**
	 * Set a handler for paste events.
	 * 
	 * @param pasteHandler gets called whenever the user uses the "paste" key
	 *                     (Ctrl-V/Cmd-V)
	 * 
	 *                     // TODO: what is the return value from pasteHandler used
	 *                     for?
	 */
	void setPasteHandler(Predicate<String> pasteHandler);

	void clipboardPut(String copied);

	SceneWorld createScene3();

	/**
	 * @return Object for setting screen scaling, aspect etc.
	 */
	ScreenControls controls();

	interface ScreenControls {
		/**
		 * Switch to the next available zoom level
		 */
		void zoomCycle();

		/**
		 * Zoom so that the whole virtual screen is visible
		 */
		void zoomFit();

		/**
		 * Zoom in / increase scale
		 */
		void zoomIn();

		/**
		 * Set scale = 1
		 */
		void zoomOne();

		/**
		 * Zoom out / decrease scale
		 */
		void zoomOut();

		/**
		 * Move to the next available standard aspect ratio.
		 */
		void cycleAspect();

		void fitScaling();

		/**
		 * @param aspect Aspect ratio index, obtained from {@link #getAspect()}
		 */
		void setAspect(int aspect);

		/**
		 * @return current aspect ratio index
		 */
		int getAspect();

	}
}