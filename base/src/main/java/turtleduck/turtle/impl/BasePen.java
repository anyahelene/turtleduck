package turtleduck.turtle.impl;

import turtleduck.colors.Colors;

import java.util.function.Function;

import turtleduck.colors.Color;
import turtleduck.geometry.Projection;
import turtleduck.geometry.impl.OrthographicProjection;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;

public class BasePen implements Pen, PenBuilder<Pen> {
	protected static final Function<Color, Color> IDENTITY = c -> c;
	protected static final Function<Color, Color> NONE = c -> Colors.TRANSPARENT;
	protected double strokeWidth;
	protected Projection projection;
	protected Color pen;
	protected Function<Color, Color> strokeFun, fillFun;
	protected boolean frozen = false;
	protected boolean stroke = true, fill = false;
	protected SmoothType smoothType;
	protected double smoothAmount;

	public BasePen() {
		strokeWidth = 1;
		projection = new OrthographicProjection(100, 100);
		pen = Color.color(1, 1, 1);
	}

	public BasePen(BasePen original) {
		strokeWidth = original.strokeWidth;
		projection = original.projection;
		pen = original.pen;
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
				return strokeFun.apply(pen);
			else
				return pen;
		} else {
			return Colors.TRANSPARENT;
		}
	}

	@Override
	public Color fillColor() {
		if (fill) {
			if (fillFun != null)
				return fillFun.apply(pen);
			else
				return pen;
		} else {
			return Colors.TRANSPARENT;
		}
	}

	@Override
	public PenBuilder<Pen> change() {
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
	public PenBuilder<Pen> strokePaint(Color ink) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		if (ink == null) {
			strokeFun = NONE;
			stroke = false;
		} else {
			strokeFun = c -> ink; 
			stroke = true;
		}
		return this;
	}

	@Override
	public PenBuilder<Pen> strokeOpacity(double opacity) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
//		stroke = stroke.opacity(opacity);
		return this;
	}

	@Override
	public PenBuilder<Pen> fillPaint(Color ink) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		if (ink == null) {
			fillFun = NONE;
			fill = false;
		} else {
			fillFun = c -> ink;
			fill = true;
		}
		return this;
	}

	@Override
	public PenBuilder<Pen> fillOpacity(double opacity) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
//		fill = fill.opacity(opacity);
		return this;
	}

	@Override
	public PenBuilder<Pen> projection(Projection proj) {
		if (frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		if (proj == null)
			throw new IllegalArgumentException("Argument must not be null");
		projection = proj;
		return this;
	}

	@Override
	public Pen done() {
		frozen = true;
		return this;
	}

	@Override
	public PenBuilder<Pen> smooth(SmoothType smooth) {
		smoothType = smooth;
		return this;
	}

	@Override
	public PenBuilder<Pen> smooth(SmoothType smooth, double amount) {
		smoothType = smooth;
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
		return String.format("Pen(color=%s, stroke=%s, fill=%s)", pen, stroke, fill);
	}

	@Override
	public PenBuilder<Pen> color(Color ink) {
		if (ink == null)
			pen = Colors.TRANSPARENT;
		else
			pen = ink;
		strokeFun = IDENTITY;
		fillFun = IDENTITY;
		return this;
	}

	@Override
	public PenBuilder<Pen> color(Function<Color, Color> colorOp) {
		pen = colorOp.apply(pen);
		return this;
	}

	@Override
	public PenBuilder<Pen> stroke(Function<Color, Color> colorOp) {
		strokeFun = colorOp;
		return this;
	}

	@Override
	public PenBuilder<Pen> fill(Function<Color, Color> colorOp) {
		fillFun = colorOp;
		return this;
	}

	@Override
	public PenBuilder<Pen> stroke(boolean enable) {
		stroke = enable;
		return this;
	}

	@Override
	public PenBuilder<Pen> fill(boolean enable) {
		fill = enable;
		return this;
	}

}
