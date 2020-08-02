package turtleduck.testutil.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import turtleduck.geometry.Direction;
import turtleduck.testutil.Generator;

public class DirectionGenerator extends AbstractGenerator<Direction> {
	/**
	 * Generator for the angle
	 */
	private final Generator<Double> aGenerator;

	public DirectionGenerator() {
		this.aGenerator = new DoubleGenerator(-3600, 3600);
	}

	/**
	 * Generate random Areas between (min,min) and (max,max)
	 *
	 * 
	 */
	public DirectionGenerator(double min, double max) {

		this.aGenerator = new DoubleGenerator(min, max);
	}

	@Override
	public Direction generate(Random r) {
		double angle = aGenerator.generate(r);
		return r.nextBoolean() ? Direction.absolute(angle) : Direction.relative(angle);
	}

	@Override
	public List<Direction> generateEquals(Random r, int n) {
		double angle = aGenerator.generate(r);
		List<Direction> list = new ArrayList<>();
		boolean absolute = r.nextBoolean();

		for (int i = 0; i < n; i++) {
			int offset = (r.nextInt(33)-16) * 360;
			list.add(absolute ? Direction.absolute(angle+offset) : Direction.relative(angle+offset));
			System.out.println("" + (angle+offset) + ", " + list.get(list.size()-1));
		}
		return list;
	}
}
