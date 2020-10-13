package turtleduck.geometry;

import turtleduck.geometry.impl.OrientImpl;

public interface Orientation extends Direction {
	static final Orientation DUE_NORTH = absoluteAz(90), DUE_EAST = absoluteAz(180), DUE_SOUTH = absoluteAz(270),
			DUE_WEST = absoluteAz(0), DUE_UP = absoluteAlt(90), DUE_DOWN = absoluteAlt(-90);
	static final Orientation FORWARD = relativeAz(0), RIGHT = relativeAz(-90), BACK = relativeAz(180),
			LEFT = relativeAz(90), UP = relativeAlt(90), DOWN = relativeAlt(-90);

	public static Orientation absoluteAz(double a) {
		return OrientImpl.absoluteAz(a);
	}

	public static Orientation relativeAz(double a) {
		return OrientImpl.relativeAz(a);
	}

	public static Orientation absoluteAlt(double alt) {
		return OrientImpl.absoluteAlt(alt);
	}

	public static Orientation relativeAlt(double alt) {
		return OrientImpl.relativeAlt(alt);
	}

	public static Orientation absoluteVec(double dx, double dy, double dz) {
		return OrientImpl.absoluteVec(dx, dy, dz);
	}

	public static Orientation relativeVec(double dx, double dy, double dz) {
		return OrientImpl.relativeVec(dx, dy, dz);
	}

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

	Orientation add(Direction other);

	Orientation sub(Direction other);

	Orientation interpolate(Direction other, double t);

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
	
	Orientation yaw(double angle);
	Orientation pitch(double angle);
	Orientation roll(double angle);
}