package turtleduck.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import turtleduck.AbstractEqualsTest;
import turtleduck.geometry.impl.AngleImpl;
import turtleduck.testutil.generators.DirectionGenerator;

public class AngleTest extends AbstractEqualsTest<Direction> {
	static final Vector3d UP = new Vector3d(0, 0, 1);
	Random random = new Random();

	public AngleTest() {
		super(new DirectionGenerator(), ToStringEqualsProperty.STRONG);
	}

	@CsvSource(value = { "0", "45", "90", "-45", "-90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absoluteAz({0}).degrees() == {0}")
	public void basicAngle2(double a) {
		Direction dir = Direction.absolute(a);
		if (a < 0)
			a += 360;
		assertEquals(a, dir.degrees(), 10e-6);
	}

	@CsvSource(value = { "0", "45", "90", "-45", "-90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absoluteAz({0}).degrees() == {0}")
	public void basicAngle3(double a) {
		Orientation dir = Orientation.absoluteAz(a);
		if (a < 0)
			a += 360;
		assertEquals(a, dir.degrees(), 10e-6);
	}
	@CsvSource(value = { "0,→", "45,↗", "90,↑", "-45,↘", "-90,↓", "127,↖", "179.9,←", "180,←", "180.1,←", "225,↙", "270,↓", "321,↘", "359.9,→" })
	@ParameterizedTest(name = "absolute({0}).toArrow() == {1}")
	public void angleArrow(double a, String s) {
		Direction dir = Direction.absolute(a);
		assertEquals(s, dir.toArrow());
	}
	@CsvSource(value = { "0,E", "45,N45.0°E", "90,N", "-45,S45.0°E", "-90,S", "127,N37.0°W", "179.9,N89.9°W", "180,W", "180.1,S89.9°W", "225,S45.0°W", "270,S", "321,S51.0°E", "359.9,S89.9°E" })
	@ParameterizedTest(name = "absolute({0}).toNavString() == {1}")
	public void angleNav(double a, String s) {
		Direction dir = Direction.absolute(a);
		assertEquals(s, dir.toNavString());
	}
	// @CsvSource(value = { "0", "45", "90", "127", "179.9", "180.1" })
	@CsvSource(value = { "0", "45", "90", "-45", "-90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absolute({0}).dirX_or_Y() == cos_or_sin({0})")
	public void basicVecXY(double a) {
		Direction dir = Direction.absolute(a);
		Vector3d vec = dir.directionVector(new Vector3d());
		double expected = Math.cos(Math.toRadians(a));
		assertEquals(expected, vec.x, 10e-6);
		assertEquals(expected, dir.dirX(), 10e-6);
		expected = Math.sin(Math.toRadians(a));
		assertEquals(expected, vec.y, 10e-6);
		assertEquals(expected, dir.dirY(), 10e-6);
	}

	@CsvSource(value = { "0", "45", "90", "-45", "-90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absoluteAz({0}).dirX() == cos({0})")
	public void basicVecXYZ(double a) {
		Orientation dir = Orientation.absoluteAz(a);
		Vector3d vec = dir.directionVector(new Vector3d());
		double expected = Math.cos(Math.toRadians(a));
		assertEquals(expected, dir.dirX(), 10e-6);
		assertEquals(expected, vec.x, 10e-6);
		expected = Math.sin(Math.toRadians(a));
		assertEquals(expected, dir.dirY(), 10e-6);
		assertEquals(expected, vec.y, 10e-6);
		assertEquals(0, dir.dirZ(), 10e-6);
		assertEquals(0, vec.z, 10e-6);
	}

	@CsvSource(value = { "0", "45", "90", "-45", "-90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absoluteAz({0}).normal() == [0,0,1]")
	public void basicUp(double a) {
		Orientation dir = Orientation.absoluteAz(a);
		Vector3d vec = dir.normalVector(new Vector3d());
		assertEquals(UP, vec);
	}

	@CsvSource(value = { "0", "45", "90", "-45", "-90", "127", "179.9", "180", "180.1", "225", "270", "321", "359.9" })
	@ParameterizedTest(name = "absoluteAz({0}).normal() == [x,y,1]")
	public void basicRoll(double a) {
		Orientation dir = Orientation.absoluteAz(0).roll(a).yaw(90);
		Vector3d vec = dir.normalVector(new Vector3d());
		assertEquals(0, vec.x);
		assertEquals(Math.sin(Math.toRadians(-a)), vec.y, 1e-6);
		assertEquals(Math.cos(Math.toRadians(-a)), vec.z, 1e-6);

	}

	@Test
	public void wrapAroundTest() {
//		System.out.println("" + Direction.absolute(-4949.788892744302) + "," + Direction.absolute(-6389.788892744302));

		assertEquals(Direction.absolute(-4949.788892744302), Direction.absolute(-6389.788892744302));

		for (int i = 0; i < 360; i++) {
			for (int j = -5; j < 6; j++) {
				assertLike(Direction.absolute(i), Direction.absolute(i + j * 360));
			}
		}
	}

	@Test
	public void conversionTest() {
		for (int i = -3600; i <= 3600; i++) {
			assertEquals(AngleImpl.degreesToMilliArcSec(i), AngleImpl.radiansToMilliArcSec(Math.toRadians(i)));
		}

		for (int i = -540; i <= 540; i++) {
			int marcsecs = i * 3600 * 1000;
			assertEquals(i, AngleImpl.milliArcSecToDegrees(marcsecs));
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
		assertEquals(1, Direction.DUE_EAST.dirX(), 1e-10);
		assertEquals(0, Direction.DUE_EAST.dirY(), 1e-10);
		assertEquals(-1, Direction.DUE_WEST.dirX(), 1e-10);
		assertEquals(0, Direction.DUE_WEST.dirY(), 1e-10);

		assertEquals(0, Orientation.DUE_NORTH.dirX(), 1e-10);
		assertEquals(1, Orientation.DUE_NORTH.dirY(), 1e-10);
		assertEquals(0, Orientation.DUE_SOUTH.dirX(), 1e-10);
		assertEquals(-1, Orientation.DUE_SOUTH.dirY(), 1e-10);
		assertEquals(1, Orientation.DUE_EAST.dirX(), 1e-10);
		assertEquals(0, Orientation.DUE_EAST.dirY(), 1e-10);
		assertEquals(-1, Orientation.DUE_WEST.dirX(), 1e-10);
		assertEquals(0, Orientation.DUE_WEST.dirY(), 1e-10);

		assertEquals(-1, Direction.DUE_NORTH.yaw(90).dirX(), 1e-10);
		assertEquals(0, Direction.DUE_NORTH.yaw(90).dirY(), 1e-10);
		assertEquals(1, Direction.DUE_NORTH.yaw(-90).dirX(), 1e-10);
		assertEquals(0, Direction.DUE_NORTH.yaw(-90).dirY(), 1e-10);
		assertEquals(-1, Orientation.DUE_NORTH.yaw(90).dirX(), 1e-10);
		assertEquals(0, Orientation.DUE_NORTH.yaw(90).dirY(), 1e-10);
		assertEquals(1, Orientation.DUE_NORTH.yaw(-90).dirX(), 1e-10);
		assertEquals(0, Orientation.DUE_NORTH.yaw(-90).dirY(), 1e-10);
	}

	@ParameterizedTest
	@CsvSource(value = { "0,90,.5,45", "45,35,.5,40", "90,-89,.5,.5", "90,-91,.5,179.5", "127,128,.5,127.5",
			"179.9,180.1,.5,180"
	// , "180,360", "180.1,360", "225,230", "270,300", "321,331", "359.9,0.1"
	})
	public void interpolateTest(double a, double b, double f, double r) {
		assertEquals(r, Direction.absolute(a).interpolate(Direction.absolute(b), f).degrees(), 1e-3);
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
