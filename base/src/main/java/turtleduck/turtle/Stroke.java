package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.colors.Color;
import turtleduck.turtle.Pen.SmoothType;

@Icon("🖌️")
 interface Stroke {
	double strokeWidth();

	/**
	 * Return the current stroke color.
	 * 
	 * <ul>
	 * <li>If a stroke color has been set (with {@link PenBuilder.stroke(Color)}),
	 * it is returned.
	 * <li>Otherwise, if the pen has a stroke color function, the result is computed
	 * from the main pen color
	 * <li>Otherwise, the main pen color is returned, or the default color if no
	 * color has been set
	 * </ul>
	 * 
	 * @return Color to use for stroke operations
	 */
	Color strokeColor();

	/**
	 * @return The current smooth type ({@link Pen.SmoothType.CORNER}) when no
	 *         smooth is in effect
	 */
	SmoothType smoothType();

	/**
	 * @return The current smooth amount.
	 */
	double smoothAmount();

	/**
	 * @return True if an explicit stroke color has been set.
	 */
	boolean hasStroke();

	/**
	 * @return True if stroke color is derived from current pen color using a
	 *         function.
	 */
	boolean hasComputedStroke();
	
	/**
	 * @return True if stroking is enabled
	 */
	boolean stroking();	

}
