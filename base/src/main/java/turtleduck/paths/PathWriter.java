package turtleduck.paths;


import org.joml.Matrix4f;

import turtleduck.shapes.Particles;

public interface PathWriter {

	/**
	 * Start painting a new stroke
	 * 
	 * @return A stroke object to write the next sub-path to
	 */
	PathStroke addStroke();
	
	Particles addParticles(Matrix4f matrix);
	
	default boolean is3d() {
	    return false;
	}

}
