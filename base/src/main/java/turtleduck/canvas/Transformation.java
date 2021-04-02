package turtleduck.canvas;

import turtleduck.util.Dict;

public interface Transformation<T> extends TransformContext2<Transformation<T>> {

	T done();
	
	Dict toCSS();

	Transformation<T> transition(double secs);
}
