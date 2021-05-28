package turtleduck.turtle;

import java.util.function.Function;

import turtleduck.colors.Color;
import turtleduck.geometry.Projection;
import turtleduck.turtle.Pen.SmoothType;

public interface PenBuilder<T> extends Stroke, Fill {
	/**
	 * Set the size of the turtle's pen
	 *
	 * @param pixels Line width, in pixels
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
	@Deprecated
	PenBuilder<T> strokePaint(Color ink);

	PenBuilder<T> color(Color ink);

	PenBuilder<T> color(Function<Color, Color> colorOp);

	PenBuilder<T> stroke(Function<Color, Color> colorOp);

	PenBuilder<T> fill(Function<Color, Color> colorOp);

	PenBuilder<T> stroke(boolean enable);

	PenBuilder<T> fill(boolean enable);

	@Deprecated
	PenBuilder<T> strokeOpacity(double opacity);

	/**
	 * Set colour used for filling.
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more draw commands
	 */
	@Deprecated
	PenBuilder<T> fillPaint(Color ink);

	@Deprecated
	PenBuilder<T> fillOpacity(double opacity);

	@Deprecated
	PenBuilder<T> projection(Projection proj);

	PenBuilder<T> smooth(SmoothType smooth);

	PenBuilder<T> smooth(SmoothType smooth, double amount);

	T done();
}
