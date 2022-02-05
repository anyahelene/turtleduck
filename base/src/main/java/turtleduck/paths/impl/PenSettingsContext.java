package turtleduck.paths.impl;

import turtleduck.annotations.Internal;
import turtleduck.paths.Pen;
import turtleduck.paths.PenBuilder;

@Internal
public interface PenSettingsContext<T> extends PenContext, PenSettings<T> {
	/**
	 * Start changing the current pen.
	 * 
	 * Call done() once the pen changes are finished, and you'll get your original
	 * canvas/turtle back.
	 * 
	 * @return A PenBuilder that returns <code>this</code> when you call
	 *         <code>done()</code>
	 */
	PenBuilder<T> penChange();

	/**
	 * Change pen
	 * 
	 * @param newPen The new pen
	 * @return <code>this</code>
	 */
	T pen(Pen newPen);
}
