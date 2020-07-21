package turtleduck.turtle;

import turtleduck.colors.Color;
import turtleduck.turtle.Pen.SmoothType;

public interface Stroke {
	double strokeWidth();

	Color strokePaint();
	
	SmoothType smoothType();

	double smoothAmount();
}
