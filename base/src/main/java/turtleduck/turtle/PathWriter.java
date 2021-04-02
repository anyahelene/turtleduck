package turtleduck.turtle;

public interface PathWriter {

	/**
	 * Start painting a new stroke
	 * 
	 * @return A stroke object to write the next sub-path to
	 */
	PathStroke addStroke();

}
