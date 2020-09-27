package turtleduck.geometry;

import org.joml.Vector3f;

import turtleduck.geometry.impl.Angle;
import turtleduck.geometry.impl.Angle3;

public interface Direction3 extends Direction {
	static final Direction3 DUE_NORTH = absoluteAz(90), DUE_EAST = absoluteAz(180), DUE_SOUTH = absoluteAz(270),
			DUE_WEST = absoluteAz(0), DUE_UP = absoluteAlt(90), DUE_DOWN = absoluteAlt(-90);
	static final Direction3 FORWARD = relativeAz(0), RIGHT = relativeAz(-90), BACK = relativeAz(180),
			LEFT = relativeAz(90), UP = relativeAlt(90), DOWN = relativeAlt(-90);

	public static Direction3 absoluteAz(double a) {
		return Angle3.absoluteAz(a);
	}

	public static Direction3 relativeAz(double a) {
		return Angle3.relativeAz(a);
	}

	public static Direction3 absoluteAlt(double alt) {
		return Angle3.absoluteAlt(alt);
	}

	public static Direction3 relativeAlt(double alt) {
		return Angle3.relativeAlt(alt);
	}

//	public static Direction3 absoluteVec(double dx, double dy, double dz) {
//		return Angle3.absoluteVec(dx, dy, dz);
//	}
//
//	public static Direction3 relativeVec(double dx, double dy, double dz) {
//		return Angle3.relativeVec(dx, dy, dz);
//	}

	/**
	 * Return direction angle, measured in degrees
	 * 
	 * For {@link #isAbsolute()}, this is the angle between {@link #DUE_NORTH} and
	 * the current direction.
	 * 
	 * @return Angle of this direction
	 * @see #radians()
	 */
	double degrees();

	boolean isAbsolute();

	/**
	 * Return direction angle, measured in radians
	 * 
	 * For {@link #isAbsolute()}, this is the angle between {@link #DUE_NORTH} and
	 * the current direction.
	 * 
	 * @return Angle of this direction
	 * @see #degrees()
	 */
	double radians();

	Direction3 add(Direction other);

	Direction3 sub(Direction other);

	Direction3 interpolate(Direction other, double t);

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

	double altDegrees();

	double altRadians();
	
	Direction3 yaw(double angle);
	Direction3 pitch(double angle);
	Direction3 roll(double angle);
}