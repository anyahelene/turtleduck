package turtleduck.paths;

import turtleduck.shapes.Particles;

public interface PathWriter {

	/**
	 * Start painting a new stroke
	 * 
	 * @return A stroke object to write the next sub-path to
	 */
	PathStroke addStroke();
	
	Particles addParticles();

}
