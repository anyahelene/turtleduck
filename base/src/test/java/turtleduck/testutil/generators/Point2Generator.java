package turtleduck.testutil.generators;

import java.util.Random;

import turtleduck.geometry.Point;
import turtleduck.geometry.impl.Point2;
import turtleduck.testutil.IGenerator;

public class Point2Generator extends AbstractGenerator<Point> {
	/**
	 * Generator for the x-coordinate
	 */
	private final IGenerator<Double> xGenerator;
	/**
	 * Generator for the y-coordinate
	 */
	private final IGenerator<Double> yGenerator;

	/**
	 * Generate random points between (-1000,-1000) and (1000,1000)
	 */
	public Point2Generator() {
		this.xGenerator = new DoubleGenerator(-1000, 1000);
		this.yGenerator = new DoubleGenerator(-1000, 1000);
	}

	/**
	 * Generate random Areas between (min,min) and (max,max)
	 *

	 */
	public Point2Generator(double min, double max) {

		this.xGenerator = new DoubleGenerator(min, max);
		this.yGenerator = new DoubleGenerator(min, max);
	}

	@Override
	public Point generate(Random r) {
		return new Point2(xGenerator.generate(r), yGenerator.generate(r));
	}
}
