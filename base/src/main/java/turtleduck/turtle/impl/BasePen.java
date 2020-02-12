package turtleduck.turtle.impl;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.geometry.Projection;
import turtleduck.geometry.impl.OrthographicProjection;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;

public class BasePen implements Pen, PenBuilder<Pen> {
	protected double strokeWidth;
	protected Projection projection;
	protected Paint stroke, fill;
	protected boolean frozen = false;
	protected SmoothType smoothType;
	protected double smoothAmount;
	
	public BasePen() {
		strokeWidth = 1;
		projection = new OrthographicProjection(100, 100);
		stroke = Paint.color(0,0,0);
		fill = Colors.TRANSPARENT;
	}

	public BasePen(BasePen original) {
		strokeWidth = original.strokeWidth;
		projection = original.projection;
		stroke = original.stroke;
		fill = original.fill;
	}

	@Override
	public double strokeWidth() {
		return strokeWidth;
	}


	@Override
	public Paint strokePaint() {
		return stroke;
	}

	@Override
	public Paint fillPaint() {
		return fill;
	}


	@Override
	public PenBuilder<Pen> change() {
		return new BasePen(this);
	}
	
	@Override
	public PenBuilder<Pen> strokeWidth(double pixels) {
		if(frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		if (pixels < 0)
			throw new IllegalArgumentException("Must be non-negative: " + pixels);
		strokeWidth = pixels;
		return this;
	}

	@Override
	public PenBuilder<Pen> strokePaint(Paint ink) {
		if(frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		if (ink == null)
			stroke = Colors.TRANSPARENT;
		else
			stroke = ink;
		return this;
	}

	@Override
	public PenBuilder<Pen> strokeOpacity(double opacity) {
		if(frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		stroke = stroke.opacity(opacity);
		return this;
	}

	@Override
	public PenBuilder<Pen> fillPaint(Paint ink) {
		if (ink == null)
			fill = Colors.TRANSPARENT;
		else
			fill = ink;
		return this;
	}

	@Override
	public PenBuilder<Pen> fillOpacity(double opacity) {
		if(frozen)
			throw new IllegalStateException("Changing pen properties after done()");
		fill = fill.opacity(opacity);
		return this;
	}

	@Override
	public PenBuilder<Pen> projection(Projection proj) {
		if(frozen)
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


}
