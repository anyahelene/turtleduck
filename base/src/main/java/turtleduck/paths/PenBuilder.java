package turtleduck.paths;

import turtleduck.annotations.Builder;
import turtleduck.paths.impl.PenSettings;

@Builder(Pen.class)
public interface PenBuilder<T> extends Pen, PenSettings<PenBuilder<T>> {

	/**
	 * Complete the pen change and produce an immutable pen.
	 * 
	 * @return A finished pen
	 */
	T done();
}
