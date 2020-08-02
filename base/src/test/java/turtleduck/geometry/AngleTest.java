package turtleduck.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import turtleduck.AbstractEqualsTest;
import turtleduck.geometry.impl.Angle;
import turtleduck.testutil.generators.DirectionGenerator;

public class AngleTest extends AbstractEqualsTest<Direction> {
	Random random = new Random();

	public AngleTest() {
		super(new DirectionGenerator(), ToStringEqualsProperty.STRONG);
	}

	@Test
	public void wrapAroundTest() {
		System.out.println("" + Direction.absolute(-4949.788892744302) + "," + Direction.absolute(-6389.788892744302));

		assertEquals(Direction.absolute(-4949.788892744302), Direction.absolute(-6389.788892744302));

		for (int i = 0; i < 360; i++) {
			for (int j = -5; j < 6; j++) {
				assertEquals(Direction.absolute(i), Direction.absolute(i + j * 360));
			}
		}
	}

	@Test
	public void conversionTest() {
		for (int i = -3600; i <= 3600; i++) {
			assertEquals(Angle.degreesToMilliArcSec(i), Angle.radiansToMilliArcSec(Math.toRadians(i)));
		}

		for (int i = -540; i <= 540; i++) {
			int marcsecs = i * 3600 * 1000;
			assertEquals(i, Angle.milliArcSecToDegrees(marcsecs));
		}
	}

	@Test
	public void addSubTest() {
		for (int i = 0; i < 10000; i++) {
			double a = 720 * (Math.random() - 0.5);
			double b = 720 * (Math.random() - 0.5);

			addProperty(a, b);
			subProperty(a, b);
		}
	}
	@Test
	public void sinCosTest() {
		for (int i = 0; i < 10000; i++) {
			double a = 180 * (Math.random() - 0.5);

			sinCosProperty(a);
		}
	}
	public void sinCosProperty(double a) {
		a = Math.round(a * 3600) / 3600;
		assertEquals(-Math.cos(Math.toRadians(a)), Direction.absolute(a).dirY(), 1e-6);
		assertEquals(Math.sin(Math.toRadians(a)), Direction.absolute(a).dirX(), 1e-6);
	}
	public void addProperty(double a, double b) {
		a = Math.round(a * 3600) / 3600;
		b = Math.round(b * 3600) / 3600;
		assertEquals(Direction.absolute(a + b), Direction.absolute(a).add(Direction.relative(b)));
	}

	public void subProperty(double a, double b) {
		a = Math.round(a * 3600) / 3600;
		b = Math.round(b * 3600) / 3600;
		assertEquals(Direction.absolute(a - b), Direction.absolute(a).sub(Direction.relative(b)));
	}
}
