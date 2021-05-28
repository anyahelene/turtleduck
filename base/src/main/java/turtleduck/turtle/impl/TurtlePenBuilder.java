package turtleduck.turtle.impl;

import java.util.function.Function;

import turtleduck.colors.Color;
import turtleduck.geometry.Projection;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.Pen.SmoothType;
import turtleduck.turtle.Chelonian;

public class TurtlePenBuilder<T extends Chelonian<T, C>, C> implements PenBuilder<T> {
	protected PenBuilder<Pen> pen;
	protected T obj;

	public TurtlePenBuilder(PenBuilder<Pen> pen, T obj) {
		this.pen = pen;
		this.obj = obj;
	}

	public double strokeWidth() {
		return pen.strokeWidth();
	}

	public PenBuilder<T> strokeWidth(double pixels) {
		pen.strokeWidth(pixels);
		return this;
	}

	public Color strokeColor() {
		return pen.strokeColor();
	}

	public Color fillColor() {
		return pen.fillColor();
	}

	public PenBuilder<T> strokePaint(Color ink) {
		pen.strokePaint(ink);
		return this;
	}

	public PenBuilder<T> strokeOpacity(double opacity) {
		pen.strokeOpacity(opacity);
		return this;
	}

	public PenBuilder<T> fillPaint(Color ink) {
		pen.fillPaint(ink);
		return this;
	}

	public PenBuilder<T> fillOpacity(double opacity) {
		pen.fillOpacity(opacity);
		return this;
	}

	public PenBuilder<T> projection(Projection proj) {
		pen.projection(proj);
		return this;
	}

	public T done() {
		obj.pen();
		return obj;
	}

	public PenBuilder<T> smooth(SmoothType smooth) {
		pen.smooth(smooth);
		return this;
	}

	public PenBuilder<T> smooth(SmoothType smooth, double amount) {
		pen.smooth(smooth, amount);
		return this;
	}

	@Override
	public SmoothType smoothType() {
		return pen.smoothType();
	}

	@Override
	public double smoothAmount() {
		return pen.smoothAmount();
	}

	@Override
	public PenBuilder<T> color(Color ink) {
		pen.color(ink);
		return this;
	}

	@Override
	public PenBuilder<T> color(Function<Color, Color> colorOp) {
		pen.color(colorOp);
		return this;
	}

	@Override
	public PenBuilder<T> stroke(Function<Color, Color> colorOp) {
		pen.stroke(colorOp);
		return this;
	}

	@Override
	public PenBuilder<T> fill(Function<Color, Color> colorOp) {
		pen.fill(colorOp);
		return this;
	}

	@Override
	public PenBuilder<T> stroke(boolean enable) {
		pen.stroke(enable);
		return this;
	}

	@Override
	public PenBuilder<T> fill(boolean enable) {
		pen.fill(enable);
		return this;
	}
}
