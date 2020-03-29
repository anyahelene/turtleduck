package turtleduck.testutil.generators;

import java.util.Random;

import turtleduck.geometry.Point;
import turtleduck.geometry.impl.Point2;
import turtleduck.testutil.Generator;

public class Point3Generator extends AbstractGenerator<Point> {
	/**
	 * Generator for the x-coordinate
	 */
	private final Generator<Double> xGenerator;
	/**
	 * Generator for the y-coordinate
	 */
	private final Generator<Double> yGenerator;
	/**
	 * Generator for the z-coordinate
	 */
	private final Generator<Double> zGenerator;

	/**
	 * Generate random points between (-1000,-1000) and (1000,1000)
	 */
	public Point3Generator() {
		this.xGenerator = new DoubleGenerator(-1000, 1000);
		this.yGenerator = new DoubleGenerator(-1000, 1000);
		this.zGenerator = new DoubleGenerator(-1000, 1000);
	}

	/**
	 * Generate random Areas between (min,min) and (max,max)
	 *
	 * 
	 */
	public Point3Generator(double min, double max) {

		this.xGenerator = new DoubleGenerator(min, max);
		this.yGenerator = new DoubleGenerator(min, max);
		this.zGenerator = new DoubleGenerator(min, max);
	}

	@Override
	public Point generate(Random r) {
		if (r.nextInt(10) < 8)
			return Point.point(xGenerator.generate(r), yGenerator.generate(r), zGenerator.generate(r));
		else
			return Point.point(xGenerator.generate(r), yGenerator.generate(r));
	}
}
