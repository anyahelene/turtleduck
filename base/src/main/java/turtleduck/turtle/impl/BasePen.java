package turtleduck.turtle.impl;

import turtleduck.colors.Colors;

import java.util.function.Function;

import turtleduck.annotations.Internal;
import turtleduck.colors.Color;
import turtleduck.geometry.Projection;
import turtleduck.geometry.impl.OrthographicProjection;
import turtleduck.paths.Pen;
import turtleduck.paths.PenBuilder;
import turtleduck.paths.SmoothType;

@Internal(to = { Pen.class, PenBuilder.class })
public class BasePen implements Pen, PenBuilder<Pen> {
	protected static final Color DEFAULT_COLOR = Colors.WHITE;
	protected double strokeWidth;
	protected Color penColor, strokeColor, fillColor;
	protected Function<Color, Color> strokeFun, fillFun;
	protected boolean frozen = false;
	protected boolean stroke = true, fill = false;
	protected SmoothType smoothType;
	protected double smoothAmount;

	public BasePen() {
		strokeWidth = 1;
		penColor = null;
	}

	public BasePen(BasePen original) {
		strokeWidth = original.strokeWidth;
		penColor = original.penColor;
		strokeColor = original.strokeColor;
		fillColor = original.fillColor;
		stroke = original.stroke;
		fill = original.fill;
		strokeFun = original.strokeFun;
		fillFun = original.fillFun;
	}

	@Override
	public double strokeWidth() {
		return strokeWidth;
	}

	@Override
	public Color strokeColor() {
		if (stroke) {
			if (strokeFun != null)
				return strokeFun.apply(color());
			else if (strokeColor != null)
				return strokeColor;
			else
				return color();
		} else {
			return Colors.TRANSPARENT;
		}
	}

	@Override
	public Color fillColor() {
		if (fill) {
			if (fillFun != null)
				return fillFun.apply(color());
			else if (fillColor != null)
				return fillColor;
			else
				return color();
		} else {
			return Colors.TRANSPARENT;
		}
	}

	@Override
	public Color color() {
		if (penColor != null)
			return penColor;
		else
			return DEFAULT_COLOR;
	}

	@Override
	public PenBuilder<Pen> penChange() {
		return new BasePen(this);
	}

	@Override
	public PenBuilder<Pen> strokeWidth(double pixels) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		if (pixels < 0)
			throw new IllegalArgumentException("Must be non-negative: " + pixels);
		strokeWidth = pixels;
		return this;
	}

	@Override
	public PenBuilder<Pen> stroke(Color ink) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		strokeFun = null;
		if (ink == null) {
			stroke = false;
		} else {
			if (penColor == null)
				penColor = ink;
			strokeColor = ink;
			stroke = true;
		}
		return this;
	}

	@Override
	public PenBuilder<Pen> stroke(boolean enable) {
		stroke = enable;
		return this;
	}

	@Override
	public PenBuilder<Pen> stroke(Color ink, double pixels) {
		strokeWidth = pixels;
		return stroke(ink);
	}

	@Override
	public PenBuilder<Pen> fill(Color ink) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		fillFun = null;
		if (ink == null) {
			fillColor = null;
			fill = false;
		} else {
			if (penColor == null)
				penColor = ink;
			fillColor = ink;
			fill = true;
		}
		return this;
	}

	@Override
	public PenBuilder<Pen> computedFill(Function<Color, Color> colorOp) {
		fillColor = null;
		fill = true;
		fillFun = colorOp;
		return this;
	}

	@Override
	public PenBuilder<Pen> fill(boolean enable) {
		fill = enable;
		return this;
	}

	@Override
	public Pen done() {
		frozen = true;
		return this;
	}

	@Override
	public PenBuilder<Pen> smooth(SmoothType smooth) {
		if (smooth == null)
			smoothType = SmoothType.CORNER;
		else
			smoothType = smooth;
		if (smoothAmount <= 0)
			smoothAmount = 1;
		return this;
	}

	@Override
	public PenBuilder<Pen> smooth(SmoothType smooth, double amount) {
		smooth(smooth);
		smoothAmount = amount;
		return this;
	}

	@Override
	public SmoothType smoothType() {
		return smoothType;
	}

	@Override
	public double smoothAmount() {
		return smoothAmount;
	}

	public String toString() {
		return String.format("Pen(color=%s, stroke=%s, fill=%s)", penColor, stroke, fill);
	}

	@Override
	public PenBuilder<Pen> color(Color ink) {
		penColor = ink;
		strokeColor = null;
		fillColor = null;
		return this;
	}

	@Override
	public PenBuilder<Pen> color(Function<Color, Color> colorOp) {
		penColor = colorOp.apply(color());
		strokeColor = null;
		fillColor = null;
		return this;
	}

	@Override
	public PenBuilder<Pen> computedStroke(Function<Color, Color> colorOp) {
		strokeFun = colorOp;
		strokeColor = null;
		stroke = true;
		return this;
	}

	@Override
	public boolean hasStroke() {
		return strokeColor != null;
	}

	@Override
	public boolean hasComputedStroke() {
		return strokeFun != null;
	}

	@Override
	public boolean stroking() {
		return stroke;
	}

	@Override
	public boolean hasFill() {
		return fillColor != null;
	}

	@Override
	public boolean hasComputedFill() {
		return fillFun != null;
	}

	@Override
	public boolean filling() {
		return fill;
	}

	@Override
	public PenBuilder<Pen> stroke(Function<Color, Color> colorOp) {
		if (strokeColor != null)
			stroke(colorOp.apply(strokeColor));
		else
			stroke(colorOp.apply(color()));
		return this;
	}

	@Override
	public PenBuilder<Pen> fill(Function<Color, Color> colorOp) {
		if (fillColor != null)
			fill(colorOp.apply(fillColor));
		else
			fill(colorOp.apply(color()));
		return this;
	}

}
