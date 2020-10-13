package turtleduck.testutil.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.testutil.Generator;

public class DirectionGenerator extends AbstractGenerator<Direction> {
	/**
	 * Generator for the angle
	 */
	private final Generator<Integer> aGenerator;
	private final long MILLIARCSEC = 60 * 60 * 1000;

	public DirectionGenerator() {
		this(0, 360);
	}

	/**
	 * Generate random Areas between (min,min) and (max,max)
	 *
	 * 
	 */
	public DirectionGenerator(double min, double max) {

		this.aGenerator = new IntGenerator((int) (min * MILLIARCSEC), (int) (max * MILLIARCSEC));
	}

	@Override
	public Direction generate(Random r) {
		double angle = aGenerator.generate(r);
		return r.nextBoolean() ? Direction.absolute(angle) : Direction.relative(angle);
	}

	@Override
	public List<Direction> generateEquals(Random r, int n) {
		long angle = aGenerator.generate(r);
		List<Direction> list = new ArrayList<>();
		boolean absolute = r.nextBoolean();
		boolean threedee = r.nextBoolean();

		for (int i = 0; i < n; i++) {
			long offset = (r.nextInt(33) - 16) * 360 * MILLIARCSEC;
			double a = (double)(angle + offset) / MILLIARCSEC;
			if (threedee) {
				list.add(absolute ? Orientation.absoluteAz(a) : Orientation.relativeAz(a));
			} else {
				list.add(absolute ? Direction.absolute(a) : Direction.relative(a));
			}
//			System.out.printf("%12d * %12d = %25.15f => %20.18f\n", angle, offset, a, list.get(list.size() - 1).degrees());
		}
		return list;
	}
}
