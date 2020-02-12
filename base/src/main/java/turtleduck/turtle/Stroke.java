package turtleduck.turtle;

import turtleduck.colors.Paint;
import turtleduck.turtle.Pen.SmoothType;

public interface Stroke {
	double strokeWidth();

	Paint strokePaint();
	
	SmoothType smoothType();

	double smoothAmount();
}
