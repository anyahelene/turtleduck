package turtleduck.testutil.generators;

import java.util.List;

import turtleduck.geometry.Gravity;

public class GravityGenerator extends ElementGenerator<Gravity> {
	/**
	 * New DirectionGenerator, will generate directions between 0° and 360°
	 */
	public GravityGenerator() {
		super(Gravity.values());
	}

	/**
	 * New DirectionGenerator, will generate directions between minValue and maxVaue
	 */
	public GravityGenerator(List<Gravity> dirs) {
		super(dirs);
	}
}
