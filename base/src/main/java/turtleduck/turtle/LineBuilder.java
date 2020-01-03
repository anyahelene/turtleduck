package turtleduck.turtle;

import turtleduck.geometry.Point;

public interface LineBuilder {

	LineBuilder to(Point next);
	
	LineBuilder to(Stroke stroke, Point next);
	
	LineBuilder close();
	
	Canvas done();

	Canvas fill(Fill fill, boolean andStroke);
}
