package turtleduck.canvas;

import java.util.function.Function;

import org.joml.Matrix3x2d;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.util.Dict;

public class TransformationImpl<T> implements Transformation<T> {
	protected boolean matrixShared = false;
	protected Matrix3x2d matrix;
	private double transition = 0;
	private Function<Transformation<T>, T> fun;

	public TransformationImpl(Matrix3x2d m, Function<Transformation<T>, T> fun) {
		this.matrix = new Matrix3x2d(m);
		this.fun = fun;
	}

	public TransformationImpl(Function<Transformation<T>, T> fun) {
		this.fun = fun;
	}

	public Transformation<T> reflect(Direction axis) {
		throw new UnsupportedOperationException();
	}

	public Transformation<T> shear(double shearX, double shearY) {
		throw new UnsupportedOperationException();
	}

	public Transformation<T> rotate(double degrees) {
		matrix().rotate(degrees);
		return this;
	}

	@Override
	public Transformation<T> transition(double secs) {
		transition = secs;
		return this;
	}

	public Transformation<T> scale(double scale) {
		matrix().scale(scale);
		return this;
	}

	public Transformation<T> scale(double scaleX, double scaleY) {
		matrix().scale(scaleX, scaleY);
		return this;
	}

	public Transformation<T> translate(double deltaX, double deltaY) {
		matrix().translate(deltaX, deltaY);
		return this;
	}

	public Transformation<T> translate(Point delta) {
		matrix().translate(delta.x(), delta.y());
		return this;
	}

	public Transformation<T> getMatrix(Matrix3x2d destMatrix) {
		if (destMatrix == null)
			throw new NullPointerException();
		matrix.get(destMatrix);
		return this;
	}

	public Transformation<T> multiply(Matrix3x2d rightMatrix) {
		if (rightMatrix == null)
			return this;
		matrix().mul(rightMatrix);
		return this;
	}

	public Transformation<T> multiply(TransformContext2<?> rightTransform) {
		if (rightTransform == null)
			return this;
		Matrix3x2d src = new Matrix3x2d();
		rightTransform.getMatrix(src);
		matrix().mul(src);
		return this;
	}

	public Transformation<T> setMatrix(TransformContext2<?> sourceTransform) {
		if (sourceTransform == null)
			matrix().identity();
		else
			sourceTransform.getMatrix(matrix());
		return this;
	}

	public Transformation<T> setMatrix(Matrix3x2d sourceMatrix) {
		if (sourceMatrix == null)
			matrix().identity();
		else
			matrix().set(sourceMatrix);
		return this;
	}

	public Transformation<T> clearMatrix() {
		matrix().identity();
		return this;
	}

	protected Matrix3x2d matrix() {
		if (matrixShared) {
			matrix = new Matrix3x2d(matrix);
			matrixShared = false;
		}
		if (matrix == null)
			matrix = new Matrix3x2d();

		return matrix;
	}

	@Override
	public T done() {
		return fun.apply(this);
	}

	public Dict toCSS() {
		Dict dict = Dict.create();
		Matrix3x2d m = matrix();
		dict.put("transform",
				String.format("matrix(%f, %f, %f, %f, %f, %f)", m.m00, m.m01, m.m10, m.m11, m.m20, m.m21));
		dict.put("transition", String.format("%fs", transition));
		return dict;
	}
}
