package turtleduck.scene;

import org.joml.Matrix3x2fc;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public interface SceneTransform2<T extends SceneTransform2<T>>
		extends SceneContainer<T> {
	T rotate(double angle);

	T rotate(Direction dir);

	default T rotateAround(double angle, Point p) {
		return translate(p).rotate(angle).translate(p.scale(-1));
	}

	default T rotateAround(Direction dir, Point p) {
		return translate(p).rotate(dir).translate(p.scale(-1));
	}

//	T rotation(Direction dir);

	T translate(Point delta);

	T translate(double dx, double dy);

//	T translation(Point point);
//	T translation(double x, double y);
	T scale(double xScale, double yScale);

	default T scaleAround(double xScale, double yScale, Point p) {
		return translate(p).scale(xScale, yScale).translate(p.scale(-1));
	}

	default T scale(double scale) {
		return scale(scale, scale);
	}

	default T scaleAround(double scale, Point p) {
		return translate(p).scale(scale, scale).translate(p.scale(-1));
	}

//	T scaling(double xScale, double yScale);
//	default T scaling(double scale) { return scale(scale, scale); }
	Point transform(Point p);

	Direction transform(Direction dir);

	Point untransform(Point p);

	Direction untransform(Direction dir);

	Matrix3x2fc matrix();

	T matrix(Matrix3x2fc mat);

	/**
	 * Multiply the transformation matrix by <code>right</code>.
	 * 
	 * This will do something like <code>this = this * right</code>, with the effect
	 * that the transformation in <code>right</code> will be applied first.
	 * 
	 * 
	 * @param right a transformation matrix to multiply with this transformation's
	 *              matrix
	 * @return <code>this</code>
	 */
	T mul(Matrix3x2fc right);

	/**
	 * Pre-multiply the transformation matrix by <code>left</code> (i.e.,
	 * <code>this = left * this</code>).
	 * 
	 * This will do something like <code>this = left  * this</code>, with the effect
	 * that the transformation in <code>this</code> will be applied first.
	 * 
	 * @param left a transformation matrix to pre-multiply with this
	 *             transformation's matrix
	 * @return <code>this</code>
	 */
	T premul(Matrix3x2fc left);

	/**
	 * Set the transformation matrix to the identity matrix (i.e., no
	 * scaling/rotation/translation).
	 * 
	 * @return <code>this</code>
	 */
	T identity();

	/**
	 * Invert the current transformation.
	 * 
	 * @return <code>this</code>
	 */
	T invert();
}
