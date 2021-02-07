package turtleduck.canvas;

import org.joml.Matrix3x2d;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface TransformContext2<T> {
	/**
	 * Scale the current graphics context uniformly.
	 * 
	 * @param scale scaling factor (1 for no scaling, negative to reflect/flip
	 *              around origin)
	 * @return <code>this</code>
	 */
	T scale(double scale);

	/**
	 * Scale the current graphics context.
	 * 
	 * @param scaleX Horizontal scaling factor (1 for no scaling, negative to
	 *               reflect/flip around origin)
	 * @param scaleY Vertical scaling factor (1 for no scaling, negative to
	 *               reflect/flip around origin)
	 * @return <code>this</code>
	 */
	T scale(double scaleX, double scaleY);

	/**
	 * Translate (move the origin of) the current graphics context.
	 * 
	 * @param deltaX Horizontal translation
	 * @param deltaY Vertical translation
	 * @return <code>this</code>
	 */
	T translate(double deltaX, double deltaY);
	/**
	 * Translate (move the origin of) the current graphics context.
	 * 
	 * @param delta Horizontal/vertical translation
	 * @return <code>this</code>
	 */
	T translate(Point delta);

	/**
	 * Rotate around origin
	 * 
	 * @param degrees Number of degrees to rotate (counter-clockwise)
	 * @return <code>this</code>
	 */
	T rotate(double degrees);

	/**
	 * Reflect around the given axis
	 * 
	 * @param axis Reflection axis (as a line going through origin in the given
	 *             direction)
	 * @return <code>this</code>
	 */
	T reflect(Direction axis);

	/**
	 * Shear the current graphics context.
	 * 
	 * Shearing “skews” or “slants” the coordinate system. For example, for a
	 * horizontal shear, a point <i>(x,y)</i> will be mapped to <i>(x + shearX * y,
	 * y)</i>. With a positive <i>shearX</i>, the figure is slanted to the right,
	 * and negative slants to the left; 0 means no shearing.
	 * 
	 * @param shearX Horizontal shearing factor
	 * @param shearY Vertical shearing factor
	 * @return <code>this</code>
	 */
	T shear(double shearX, double shearY);

	T getMatrix(Matrix3x2d matrix);

	T multiply(Matrix3x2d matrix);

	T multiply(TransformContext2<?> matrix);

	T setMatrix(TransformContext2<?> matrix);

	T setMatrix(Matrix3x2d matrix);

	T clearMatrix();

}
