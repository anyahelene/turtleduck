package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.colors.Color;

@Icon("🖌️")
 interface Fill {
	/**
	 * Return the current fill color.
	 * 
	 * <ul>
	 * <li>If a fill color has been set (with {@link PenBuilder.fill(Color)}), it is
	 * returned.
	 * <li>Otherwise, if the pen has a fill color function, the result is computed
	 * from the main pen color
	 * <li>Otherwise, the main pen color is returned, or the default color if no
	 * color has been set
	 * </ul>
	 * 
	 * @return Color to use for fill operations
	 */
	Color fillColor();

	/**
	 * @return True if an explicit fill color has been set.
	 */
	boolean hasFill();

	/**
	 * @return True if fill color is derived from current pen color using a
	 *         function.
	 */
	boolean hasComputedFill();

	/**
	 * @return True if filling is enabled
	 */
	boolean filling();
}
