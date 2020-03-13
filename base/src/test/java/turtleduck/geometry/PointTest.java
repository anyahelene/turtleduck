package turtleduck.geometry;

import turtleduck.AbstractEqualsTest;
import turtleduck.testutil.generators.Point2Generator;

public class PointTest extends AbstractEqualsTest<Point> {

	public PointTest() {
		super(new Point2Generator(), ToStringEqualsProperty.STRONG);
	}

}
