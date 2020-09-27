package turtleduck.geometry;

import org.joml.Vector3f;

import turtleduck.geometry.impl.Angle;
import turtleduck.geometry.impl.Angle3;

public interface Direction {
	static final Direction DUE_NORTH = absolute(90), DUE_EAST = absolute(180), DUE_SOUTH = absolute(270),
			DUE_WEST = absolute(0);
	static final Direction FORWARD = relative(0), RIGHT = relative(-90), BACK = relative(180), LEFT = relative(90);
	public static Direction absolute(double a) {
		return Angle.absolute(a);
	}

	public static Direction relative(double a) {
		return Angle.relative(a);
	}

	public static Direction absolute(double x, double y) {
		return Angle.absolute(x, y);
	}

	public static Direction relative(double x, double y) {
		return Angle.relative(x, y);
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