package turtleduck.display;

import java.util.function.Predicate;

import turtleduck.turtle.Pen;
import turtleduck.colors.Color;
import turtleduck.events.KeyEvent;
import turtleduck.objects.IdentifiedObject;
import turtleduck.text.TextWindow;

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

	Canvas createCanvas();

	TextWindow createTextWindow();

	void cycleAspect();

	void fitScaling();

	int getAspect();

	Layer getBackgroundPainter();

	double getHeight();

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

	double width();

	void hideMouseCursor();

	boolean isFullScreen();

	boolean minimalKeyHandler(KeyEvent event);

	void moveToBack(Layer layer);

	void moveToFront(Layer layer);

	void setAspect(int aspect);

	void setBackground(Color bgColor);

	void setFullScreen(boolean fullScreen);

	void setHideFullScreenMouseCursor(boolean hideIt);

	/**
	 * @param keyOverride
	 *            the keyOverride to set
	 */
	void setKeyOverride(Predicate<KeyEvent> keyOverride);

	/**
	 * @param keyHandler
	 *            the keyHandler to set
	 */
	void setKeyPressedHandler(Predicate<KeyEvent> keyHandler);

	/**
	 * @param keyReleasedHandler
	 *            the keyReleasedHandler to set
	 */
	void setKeyReleasedHandler(Predicate<KeyEvent> keyReleasedHandler);

	/**
	 * @param keyTypedHandler
	 *            the keyTypedHandler to set
	 */
	void setKeyTypedHandler(Predicate<KeyEvent> keyTypedHandler);

	void setMouseCursor(MouseCursor cursor);

	void showMouseCursor();

	void zoomCycle();

	void zoomFit();

	void zoomIn();

	void zoomOne();

	void zoomOut();

	Canvas debugCanvas();

	void flush();

	void useAlternateShortcut(boolean useAlternate);

	void setPasteHandler(Predicate<String> pasteHandler);

	void clipboardPut(String copied);
}