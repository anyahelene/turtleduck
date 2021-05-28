package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.colors.Color;
import turtleduck.turtle.Pen.SmoothType;

@Icon("🖌️")
public interface Stroke {
	double strokeWidth();

	Color strokeColor();
	
	SmoothType smoothType();

	double smoothAmount();
}
