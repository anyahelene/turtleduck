package turtleduck.turtle;

import turtleduck.colors.Paint;
import turtleduck.geometry.Projection;

public interface PenBuilder<T> extends Stroke, Fill, Geometry {
	/**
	 * Set the size of the turtle's pen
	 *
	 * @param pixels
	 *            Line width, in pixels
	 * @return {@code this}, for sending more draw commands
	 * @requires pixels >= 0
	 */
	PenBuilder<T> strokeWidth(double pixels);
	/**
	 * Set colour used for drawing strokes.
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more draw commands
	 */
	PenBuilder<T> strokePaint(Paint ink);
	PenBuilder<T> strokeOpacity(double opacity);
	/**
	 * Set colour used for filling.
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more draw commands
	 */
	PenBuilder<T> fillPaint(Paint ink);
	PenBuilder<T> fillOpacity(double opacity);
	PenBuilder<T> projection(Projection proj);

	T done();
}
