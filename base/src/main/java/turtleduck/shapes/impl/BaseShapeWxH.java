package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.shapes.Shape;
import turtleduck.shapes.Shape.WxHBuilder;

public abstract class BaseShapeWxH<T extends Shape.Builder<T>> extends BaseShapeImpl<T> implements WxHBuilder<T> {
	protected double width = 1, height = 1;

	protected BaseShapeWxH(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		super(canvas, matrix, pw, pos, pen);
	}

	@SuppressWarnings("unchecked")
	public T width(double width) {
		this.width = width;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T height(double height) {
		this.height = height;
		return (T) this;
	}
}