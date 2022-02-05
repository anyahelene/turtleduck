package turtleduck.paths;

import turtleduck.annotations.Icon;
import turtleduck.colors.Color;
import turtleduck.turtle.impl.BasePen;

@Icon("🖋️")
public interface Pen {
	static PenBuilder<Pen> create() {
		return new BasePen();
	}

	/**
	 * Start changing the pen settings
	 * 
	 * @return A PenBuilder; call {@link PenBuilder.done()} on it when done to get
	 *         the new pen object
	 */
	PenBuilder<Pen> penChange();

	@Override
	int hashCode();

	@Override
	boolean equals(Object other);

	/**
	 * @return The current main pen color.
	 */
	Color color();

	/**
	 * @return The current stroke width
	 */
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
	 * @return The current smooth type ({@link SmoothType.CORNER}) when no smooth is
	 *         in effect
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
