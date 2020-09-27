package turtleduck.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import turtleduck.AbstractEqualsTest;
import turtleduck.geometry.impl.Angle;
import turtleduck.testutil.generators.DirectionGenerator;

public class AngleTest extends AbstractEqualsTest<Direction> {
	static final Vector3f UP = new Vector3f(0, 0, 1);
	Random random = new Random();

	public AngleTest() {
		super(new DirectionGenerator(), ToStringEqualsProperty.STRONG);
	}

	@CsvSource(value = { "0", "45", "90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absoluteAz({0}).degrees() == {0}")
	public void basicAngle(double a) {
		Direction3 dir = Direction3.absoluteAz(a);

		assertEquals(a, dir.degrees(), 10e-6);
	}

	@CsvSource(value = { "0", "45", "90", "127", "179.9", "180.1" })
	@ParameterizedTest(name = "absoluteAz({0}).dirX() == cos({0})")
	public void basicX(double a) {
		Direction3 dir = Direction3.absoluteAz(a);
		Vector3f vec = dir.directionVector(new Vector3f());
		double expected = Math.cos(Math.toRadians(a));
		assertEquals(dir.dirX(), vec.x, 10e-6);
//		System.out.println("a=" +a + ", expect "+ expected + " == " + vec);
		assertEquals(expected, vec.x, 10e-6);
	}

	@CsvSource(value = { "0", "45", "90", "127", "179.9", "180.1" })
	@ParameterizedTest(name = "absoluteAz({0}).dirX() == sin({0})")
	public void basicY(double a) {
		Direction3 dir = Direction3.absoluteAz(a);
		Vector3f vec = dir.directionVector(new Vector3f());
		double expected = Math.sin(Math.toRadians(a));
		assertEquals(dir.dirY(), vec.y, 10e-6);
//		System.out.println("a=" +a + ", expect "+ expected + " == " + vec);
		assertEquals(expected, vec.y, 10e-6);
	}

	@CsvSource(value = { "0", "45", "90", "127", "179.9", "180.1" })
	@ParameterizedTest(name = "absoluteAz({0}).normal() == [0,0,1]")
	public void basicUp(double a) {
		Direction3 dir = Direction3.absoluteAz(a);
		Vector3f vec = dir.normalVector(new Vector3f());
		assertEquals(UP, vec);
	}

	@Test
	public void wrapAroundTest() {
//		System.out.println("" + Direction.absolute(-4949.788892744302) + "," + Direction.absolute(-6389.788892744302));

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

	@Test
	public void xyTest() {
		for (int i = 0; i <= 360; i++) {
			Direction dir = Direction.absolute(i);
			double x = dir.dirX();
			double y = dir.dirY();
			int a = i;
			assertEquals(dir, Direction.absolute(x, y), () -> ("angle(" + x + "," + y + ") == " + a));
		}
	}

	@Test
	public void xyTest2() {
		assertEquals(0, Direction.DUE_NORTH.dirX(), 1e-10);
		assertEquals(1, Direction.DUE_NORTH.dirY(), 1e-10);
		assertEquals(0, Direction.DUE_SOUTH.dirX(), 1e-10);
		assertEquals(-1, Direction.DUE_SOUTH.dirY(), 1e-10);
		assertEquals(-1, Direction.DUE_EAST.dirX(), 1e-10);
		assertEquals(0, Direction.DUE_EAST.dirY(), 1e-10);
		assertEquals(1, Direction.DUE_WEST.dirX(), 1e-10);
		assertEquals(0, Direction.DUE_WEST.dirY(), 1e-10);
		
		assertEquals(0, Direction3.DUE_NORTH.dirX(), 1e-10);
		assertEquals(1, Direction3.DUE_NORTH.dirY(), 1e-10);
		assertEquals(0, Direction3.DUE_SOUTH.dirX(), 1e-10);
		assertEquals(-1, Direction3.DUE_SOUTH.dirY(), 1e-10);
		assertEquals(-1, Direction3.DUE_EAST.dirX(), 1e-10);
		assertEquals(0, Direction3.DUE_EAST.dirY(), 1e-10);
		assertEquals(1, Direction3.DUE_WEST.dirX(), 1e-10);
		assertEquals(0, Direction3.DUE_WEST.dirY(), 1e-10);
		
		assertEquals(-1, Direction.DUE_NORTH.yaw(90).dirX(), 1e-10);
		assertEquals(0, Direction.DUE_NORTH.yaw(90).dirY(), 1e-10);
		assertEquals(1, Direction.DUE_NORTH.yaw(-90).dirX(), 1e-10);
		assertEquals(0, Direction.DUE_NORTH.yaw(-90).dirY(), 1e-10);
		assertEquals(-1, Direction3.DUE_NORTH.yaw(90).dirX(), 1e-10);
		assertEquals(0, Direction3.DUE_NORTH.yaw(90).dirY(), 1e-10);
		assertEquals(1, Direction3.DUE_NORTH.yaw(-90).dirX(), 1e-10);
		assertEquals(0, Direction3.DUE_NORTH.yaw(-90).dirY(), 1e-10);
	}

	public void sinCosProperty(double a) {
		a = Math.round(a * 3600) / 3600;
		assertEquals(Math.cos(Math.toRadians(a)), Direction.absolute(a).dirX(), 1e-6);
		assertEquals(Math.sin(Math.toRadians(a)), Direction.absolute(a).dirY(), 1e-6);
	}

	public void addProperty(double a, double b) {
		a = Math.round(a * 3600) / 3600;
		b = Math.round(b * 3600) / 3600;
		assertLike(Direction.absolute(a + b), Direction.absolute(a).add(Direction.relative(b)));
	}

	public void subProperty(double a, double b) {
		a = Math.round(a * 3600) / 3600;
		b = Math.round(b * 3600) / 3600;
		assertLike(Direction.absolute(a - b), Direction.absolute(a).sub(Direction.relative(b)));
	}

	public void assertLike(Direction expected, Direction value) {
		assertTrue(expected.like(value), () -> String.format("expected: <%s> but was: <%s>", expected, value));
	}
}
