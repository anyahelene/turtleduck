package turtleduck.turtle;

import java.util.function.Function;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.geometry.Projection;
import turtleduck.turtle.Pen.SmoothType;

public interface PenBuilder<T> extends Pen {
	/**
	 * Set the width of the pen's stroke
	 *
	 * @param pixels Line width, in pixels
	 * @return {@code this}, for sending more pen commands
	 * @requires pixels >= 0
	 */
	PenBuilder<T> strokeWidth(double pixels);

	/**
	 * Enable stroking and set colour used for drawing strokes.
	 * <ul>
	 * <li>If the current pen color is unset, it will be updated to <code>ink</ink>
	 * <li>If stroking is currently disabled, it will be enabled
	 * <li>If <code>ink</code> is <code>null</code>, stroking is disabled and the
	 * stroke color is unset (and will fall back to the main pen colour)
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> stroke(Color ink);

	/**
	 * Enable stroking and set colour and width for drawing strokes.
	 * <ul>
	 * <li>If the current pen color is unset, it will be updated to <code>ink</ink>
	 * <li>If stroking is currently disabled, it will be enabled
	 * <li>If <code>ink</code> is <code>null</code>, stroking is disabled and the
	 * stroke color is unset (and will fall back to the main pen colour)
	 * <li>If <code>width</code> is <code>0</code>, no stroke will be drawn, but
	 * stroking is considered enabled and will be active again when the width is set
	 * to a positive value.
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> stroke(Color ink, double pixels);

	/**
	 * Set the main pen colour and forget the current stroke and fill color
	 * 
	 * <ul>
	 * <li>If a stroke or fill color has been previously set by
	 * {@link #stroke(Color)} or {@link #fillColor()}, these will be unset and
	 * default to the updated main pen color
	 * <li>The enable/disable status of stroking and filling is unchanged
	 * <li>If <code>ink</code> is <code>null</code>, the main color is unset along
	 * with the stroke and fill color. Any further drawing will be done with the
	 * system default color (probably {@link Colors.TRANSPARENT})
	 * 
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> color(Color ink);

	/**
	 * Modify the main pen color by applying the function to the current color
	 * 
	 * This is equivalent to <code>color(colorOp.apply(color()))</code>
	 * 
	 * @param colorOp A function that computes a new color
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> color(Function<Color, Color> colorOp);

	/**
	 * Modify stroke color by applying the function to the current stroke color
	 * 
	 * This is equivalent to <code>stroke(colorOp.apply(strokeColor()))</code>
	 * 
	 * <ul>
	 * <li>If the current stroke color is unset, the new color is computed based on
	 * the current pen color
	 * <li>If the current pen color is unset, it will be set based on the result
	 * <li>If stroking is currently disabled, it will be enabled
	 * </ul>
	 * 
	 * <code>colorOp</code> is called immediately to compute the color, unlike
	 * #computedStroke(Function) which continuously recomputes the stroke color
	 * based on the current pen color.
	 * 
	 * @param colorOp A function that computes a new color (not <code>null</null>)
	 * @return {@code this}, for sending more pen commands
	 * @see #computedStroke(Function)
	 */
	PenBuilder<T> stroke(Function<Color, Color> colorOp);

	/**
	 * Set the stroke color based on the main pen color
	 * 
	 * The stroke color will set to colorOp.apply(color()) change depending on the
	 * current main pen color.
	 * <p>
	 * For example, <code>pen.computedStroke(c -> c.darker())</code> will give a
	 * stroke color that's always one step darker than the current pen color.
	 * <p>
	 * Setting the stroke color explicitly using {@link #stroke(Color)} will remove
	 * the stroke color function.
	 * <p>
	 * <code>colorOp</code> is called whenever the main pen color changes, unlike
	 * {@link #stroke(Function)} which sets the stroke to a fixed value.
	 * 
	 * @param colorOp A function to apply to compute stroke color from main color
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> computedStroke(Function<Color, Color> colorOp);

	/**
	 * Set the fill color based on the main pen color
	 * 
	 * The fill color will set to colorOp.apply(color()) change depending on the
	 * current main pen color.
	 * <p>
	 * For example, <code>pen.computedFill(c -> c.darker())</code> will give a fill
	 * color that's always one step darker than the current pen color.
	 * <P>
	 * Setting the fill color explicitly using {@link #fill(Color)} will remove the
	 * fill color function.
	 * <p>
	 * <code>colorOp</code> is called whenever the main pen color changes, unlike
	 * {@link #fill(Function)} which sets the fill to a fixed value.
	 * 
	 * @param colorOp A function to apply to compute fill color from main color
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> computedFill(Function<Color, Color> colorOp);

	/**
	 * Enable or disable stroking.
	 * 
	 * The current colors are unchanged.
	 * 
	 * @param enable True to enable stroking, false to disable
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> stroke(boolean enable);

	/**
	 * Enable or disable filling.
	 * 
	 * The current colors are unchanged.
	 * 
	 * @param enable True to enable filling, false to disable
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> fill(boolean enable);

	/**
	 * Use <code>fill(c -> c.opacity(opacity))</code> instead.
	 * 
	 */
	@Deprecated
	PenBuilder<T> strokeOpacity(double opacity);

	/**
	 * Enable filling and set fill color.
	 * <ul>
	 * <li>If the current pen color is unset, it will be updated to <code>ink</ink>
	 * <li>If filling is currently disabled, it will be enabled
	 * <li>If <code>ink</code> is <code>null</code>, filling is disabled and the
	 * fill color is unset (and will fall back to the main pen colour)
	 * 
	 * @param ink A colour or paint
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> fill(Color ink);

	/**
	 * Enable filling and compute fill color.
	 * 
	 * This is equivalent to <code>fill(colorOp.apply(fillColor()))</code>
	 * 
	 * <ul>
	 * <li>If the current fill color is unset, the new color is computed based on
	 * the current pen color
	 * <li>If the current pen color is unset, it will be set based on the result
	 * <li>If filling is currently disabled, it will be enabled
	 * <p>
	 * <code>colorOp</code> is called immediately to compute the color, unlike
	 * #computedFill(Function) which continuously recomputes the fill color based on
	 * the current pen color.
	 * 
	 * 
	 * @param colorOp A function that computes a new color (not <code>null</null>)
	 * @return {@code this}, for sending more pen commands
	 * @see #computedFill(Function) which continuously recomputes the fill color
	 *      based on the current pen color
	 */
	PenBuilder<T> fill(Function<Color, Color> colorOp);

	/**
	 * Use <code>fill(c -> c.opacity(opacity))</code> instead.
	 * 
	 */
	@Deprecated
	PenBuilder<T> fillOpacity(double opacity);

	/**
	 * Set pen smoothing.
	 * 
	 * A ‘smooth’ pen will draw curves instead of straight lines.
	 * 
	 * The smooth type can be either {@link Pen.SmoothType.CORNER} (no smoothing),
	 * {@link Pen.SmoothType.SMOOTH} (incoming and outgoing line segments form a
	 * straight line), {@link Pen.SmoothType.SYMMETRIC} (smooth, with control points
	 * at the same distance).
	 * 
	 * @param smooth The smoothing type, or null for {@link Pen.SmoothType.CORNER}
	 *               (no smoothing)
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> smooth(SmoothType smooth);

	/**
	 * Set pen smoothing.
	 * 
	 * A ‘smooth’ pen will draw curves instead of straight lines.
	 * 
	 * The smooth type can be either {@link Pen.SmoothType.CORNER} (no smoothing),
	 * {@link Pen.SmoothType.SMOOTH} (incoming and outgoing line segments form a
	 * straight line), {@link Pen.SmoothType.SYMMETRIC} (smooth, with control points
	 * at the same distance).
	 * <p>
	 * The smooth amount controls the shape of the curve: smaller values (0 <=
	 * amount < 1) give a curve that's straighter in the middle (down to 0, which
	 * gives a straight line with control points at the start and end points), 1
	 * gives a nice, smooth curve (control points meeting), and larger values
	 * (amount > 1) make the curve straighter at the end and bendier in the middle
	 * (control points streched out).
	 * 
	 * @param smooth The smoothing type, or null for {@link Pen.SmoothType.CORNER}
	 *               (no smoothing)
	 * @param amount How much smoothing to apply, with 0 being no smoothing and 1
	 * @return {@code this}, for sending more pen commands
	 */
	PenBuilder<T> smooth(SmoothType smooth, double amount);

	/**
	 * Complete the pen change and produce an immutable pen.
	 * 
	 * @return A finished pen
	 */
	T done();
}
