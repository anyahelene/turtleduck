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

	public PenBuilder<T> stroke(Color ink) {
		pen.stroke(ink);
		return this;
	}

	public PenBuilder<T> strokeOpacity(double opacity) {
		pen.strokeOpacity(opacity);
		return this;
	}

	public PenBuilder<T> fill(Color ink) {
		pen.fill(ink);
		return this;
	}

	public PenBuilder<T> fillOpacity(double opacity) {
		pen.fillOpacity(opacity);
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
	public PenBuilder<T> computedStroke(Function<Color, Color> colorOp) {
		pen.computedStroke(colorOp);
		return this;
	}

	@Override
	public PenBuilder<T> computedFill(Function<Color, Color> colorOp) {
		pen.computedFill(colorOp);
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

	@Override
	public boolean hasStroke() {
		return pen.hasStroke();
	}

	@Override
	public boolean hasComputedStroke() {
		return pen.hasComputedStroke();
	}

	@Override
	public boolean stroking() {
		return pen.stroking();
	}

	@Override
	public boolean hasFill() {
		return hasFill();
	}

	@Override
	public boolean hasComputedFill() {
		return hasComputedFill();
	}

	@Override
	public boolean filling() {
		return pen.filling();
	}

	@Override
	public PenBuilder<T> stroke(Color ink, double pixels) {
		pen.stroke(ink, pixels);
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
	public PenBuilder<Pen> change() {
		return pen;
	}

	@Override
	public Color color() {
		return pen.color();
	}
}
