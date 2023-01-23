package turtleduck.display;

import turtleduck.geometry.Box;
import turtleduck.geometry.Point;
import turtleduck.display.impl.ViewportBuilderImpl;

/**
 * Represents a rectangular viewing region
 * 
 * The viewport maps virtual screen coordinates (as seen by application code) to
 * actual screen coordinates (as seen by the computer / underlying graphics
 * system).
 * 
 * For example, the actual screen size may vary from device to device depending
 * on resolution and physical display, and may change at runtime, e.g., as the
 * user resizes a window or moves between windowed and fullscreen mode – the
 * viewport creates an abstraction over this, so the application can deal with a
 * consistent coordinate system.
 * 
 * @author Anya Helene Bagge
 *
 */
public interface Viewport {
	static ViewportBuilder create(DisplayInfo info) {
		return new ViewportBuilderImpl(info);
	}

	/**
	 * Observe physical characteristics of display.
	 * 
	 * The returned information may not be accurate if the viewport is entirely
	 * virtual (e.g., rendering to a framebuffer or just discarding output).
	 * 
	 * @return Information about the display device this viewport is connected to
	 */
	DisplayInfo displayInfo();

	/**
	 * @return Actual bounds in screen coordinates of the full viewport
	 * @ensures screenArea().contains(viewArea())
	 */
	int screenX();

	int screenY();

	/**
	 * The view area is the part of the screen area that is actually being rendered
	 * to.
	 * 
	 * This may be less than {@link #screenArea()} in order to preserve the desired
	 * aspect ratio.
	 * 
	 * @return Viewable bounds in screen coordinates of the viewport
	 * @ensures screenArea().contains(viewArea())
	 */
	double viewX();

	double viewY();

	/**
	 * @return Actual width in screen coordinates
	 */
	int screenWidth();

	/**
	 * @return Actual height in screen coordinates
	 */
	int screenHeight();

	/**
	 * @return Usable width in screen coordinates
	 */
	int viewWidth();

	/**
	 * @return Usable height in screen coordinates
	 */
	int viewHeight();

	/**
	 * @return Width in virtual coordinates
	 */
	int width();

	/**
	 * @return Height in virtual coordinates
	 */
	int height();

	double aspect();

	ViewportBuilder change();

	Camera create2dCamera();
	Camera create3dCamera();
	
	interface ViewportBuilder {
		ViewportBuilder screenArea(int x, int y, int width, int height);

		/**
		 * @param width The desired world width
		 * @return this
		 */
		ViewportBuilder width(int width);

		/**
		 * @param height The desired world height
		 * @return this
		 */
		ViewportBuilder height(int height);

		/**
		 * Viewport will be scaled to fit display area, without regard for aspect.
		 * 
		 * @return this
		 */
		ViewportBuilder stretch();

		/**
		 * Viewport will be scaled so that at least one dimension fills the display
		 * area. The other dimension may be smaller than the display area
		 * ("letterboxing").
		 * 
		 * @return this
		 */
		ViewportBuilder fit();

		/**
		 * Viewport will be scaled so that at least one dimension fills the display
		 * area. If the other dimension is smaller than the display area, it will be
		 * extended to fit (i.e., world size is extended).
		 * 
		 * @return this
		 */
		ViewportBuilder extend();

		/**
		 * Viewport will be scaled so that both dimensions fill the display area. If the
		 * other dimension is larger than the display area, the excess is cut.
		 * 
		 * @return this
		 */
		ViewportBuilder clip();

		/**
		 * Attempt pixel-perfect scaling, multiplying or dividing by whole integers
		 * only.
		 * 
		 * @return this
		 */
		ViewportBuilder perfect();

		ViewportBuilder aspect(int w, int h);

		ViewportBuilder aspect(double ratio);

		ViewportBuilder aspectNative();

		ViewportBuilder aspectUnset();

		/**
		 * Classic 16:9 widescreen, as seen in HD TV
		 * 
		 * @return this
		 */
		default ViewportBuilder aspectWide() {
			return aspect(16, 9);
		}

		/**
		 * Wide 16:10 aspect, common on some computer displays
		 * 
		 * @return this
		 */
		default ViewportBuilder aspectGolden() {
			return aspect(16, 10);
		}

		/**
		 * Classic 4:3 none-wide television aspect
		 * 
		 * @return this
		 */
		default ViewportBuilder aspectClassic() {
			return aspect(4, 3);
		}

		Viewport done();
	}

	double viewAspect();

	double screenAspect();
}
