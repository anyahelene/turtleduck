package turtleduck.canvas;

import turtleduck.colors.Color;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.Pen.SmoothType;

public interface PenContext<T> {
	PenBuilder<? extends T> penChange();

	Pen pen();

	T pen(Pen newPen);

	/**
	 * Set the size of the turtle's pen
	 *
	 * @param pixels Line width, in pixels
	 * @return {@code this}, for sending more draw commands
	 * @requires pixels >= 0
	 */
	T strokeWidth(double pixels);

	/**
	 * Set colour used for drawing strokes.
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more draw commands
	 */
	T strokePaint(Color ink);

	T strokeOpacity(double opacity);

	/**
	 * Set colour used for filling.
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more draw commands
	 */
	T fillPaint(Color ink);

	T fillOpacity(double opacity);

	T smooth(SmoothType smooth);

	T smooth(SmoothType smooth, double amount);
}
