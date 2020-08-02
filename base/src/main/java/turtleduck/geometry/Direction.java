package turtleduck.geometry;

import turtleduck.geometry.impl.Angle;

public interface Direction {
	static final Direction DUE_NORTH = absolute(0), DUE_EAST = absolute(90), DUE_SOUTH = absolute(180),
			DUE_WEST = absolute(270);
	static final Direction FORWARD = relative(0), RIGHT = relative(90), BACK = relative(180), LEFT = relative(270);
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
	

}