package turtleduck.display;

import turtleduck.objects.IdentifiedObject;
import turtleduck.text.TextWindow;

public interface Layer extends IdentifiedObject {
	/**
	 * Clear the layer.
	 * 
	 * <p>
	 * Everything on the layer is removed, leaving only transparency.
	 */
	Layer clear();

	/**
	 * Send this layer to the back, so it will be drawn behind any other layers.
	 * 
	 * <p>
	 * There will still be background behind this layer. You may clear it or draw to
	 * it using {@link Screen#clearBackground()},
	 * {@link Screen#setBackground(javafx.scene.paint.Color)} and
	 * {@link Screen#getBackgroundContext()}.
	 */
	Layer layerToBack();

	/**
	 * Send this layer to the front, so it will be drawn on top of any other layers.
	 */
	Layer layerToFront();

	/**
	 * @return Width (in pixels) of graphics layer
	 */
	double width();

	/**
	 * @return Height (in pixels) of graphics layer
	 */
	double height();
	


	Layer show();
	
	Layer hide();

	Layer flush();
	

}
