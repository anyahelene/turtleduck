package turtleduck.geometry;

import turtleduck.AbstractEqualsTest;
import turtleduck.testutil.Generator;

public abstract class PointTest extends AbstractEqualsTest<Point> {

	public PointTest(Generator<Point> gen) {
		super(gen, ToStringEqualsProperty.STRONG);
	}

}
