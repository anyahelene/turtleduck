package turtleduck.canvas;

import org.joml.Matrix4x3d;

public interface TransformContext3<T> extends TransformContext2<T> {
	/**
	 * Equivalent to {@link #scale(double, double, double) scale(scale, scale,
	 * scale)}.
	 */
	default T scale(double scale) {
		return scale(scale, scale, scale);
	}

	/**
	 * Equivalent to {@link #scale(double, double, double) scale(scaleX, scaleY,
	 * 1)}.
	 */
	default T scale(double scaleX, double scaleY) {
		return scale(scaleX, scaleY, 1);
	}

	/**
	 * Scale the current graphics context.
	 * 
	 * @param scaleX X-axis scaling factor (1 for no scaling, negative to
	 *               reflect/flip around origin)
	 * @param scaleY Y-axis scaling factor (1 for no scaling, negative to
	 *               reflect/flip around origin)
	 * @param scaleZ Z-axis scaling factor (1 for no scaling, negative to
	 *               reflect/flip around origin)
	 * @return <code>this</code>
	 */
	T scale(double scaleX, double scaleY, double scaleZ);

	/**
	 * Translate (move the origin of) the current graphics context.
	 * 
	 * @param deltaX Horizontal translation
	 * @param deltaY Vertical translation
	 * @param deltaZ Depth translation
	 * @return <code>this</code>
	 */
	T translate(double deltaX, double deltaY, double deltaZ);

	T getMatrix(Matrix4x3d matrix);

	T setMatrix(Matrix4x3d matrix);

	T multiply(Matrix4x3d matrix);

}
