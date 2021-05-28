package turtleduck.geometry;

import org.joml.Vector3f;

import turtleduck.geometry.impl.AngleImpl;
import turtleduck.geometry.impl.OrientImpl;

public interface Direction {
	static final int DEGREES_NORTH = -90, DEGREES_WEST = 180, DEGREES_SOUTH = 90, DEGREES_EAST = 0;
	static final Direction DUE_NORTH = absolute(DEGREES_NORTH), DUE_WEST = absolute(DEGREES_WEST),
			DUE_SOUTH = absolute(DEGREES_SOUTH),
			DUE_EAST = absolute(DEGREES_EAST);
	static final Direction FORWARD = relative(0), RIGHT = relative(90), BACK = relative(180), LEFT = relative(-90);
	public static Direction absolute(double a) {
		return AngleImpl.absolute(a);
	}

	public static Direction relative(double a) {
		return AngleImpl.relative(a);
	}

	public static Direction absolute(double x, double y) {
		return AngleImpl.absolute(x, y);
	}

	public static Direction relative(double x, double y) {
		return AngleImpl.relative(x, y);
	}

	/**
	 * Return direction angle, measured in degrees
	 * 
	 * For {@link #isAbsolute()}, this is the angle between {@link #DUE_NORTH} and the current direction.
	 * 
	 * @return Angle of this direction
	 * @see #radians()
	 */
	double degrees();

	boolean isAbsolute();

	/**
	 * Return direction angle, measured in radians
	 * 
	 * For {@link #isAbsolute()}, this is the angle between {@link #DUE_NORTH} and the current direction.
	 * 
	 * @return Angle of this direction
	 * @see #degrees()
	 */
	double radians();

	Direction add(Direction other);

	Direction sub(Direction other);

	Direction interpolate(Direction other, double t);

	String toNavString();

	String toArrow();

	String toString();

	double dirX();

	double dirY();

	double dirZ();
	
	@Override
	int hashCode();
	
	@Override
	boolean equals(Object other);
	
	boolean is3d();

	double altDegrees();
	Vector3f perpendicular(Vector3f dest);

	Vector3f directionVector(Vector3f dest);
	Vector3f normalVector(Vector3f dest);

	Direction yaw(double angle);
	Direction pitch(double angle);
	Direction roll(double angle);

	boolean like(Direction other);


}