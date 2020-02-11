package turtleduck.geometry;

import turtleduck.geometry.impl.BearingImpl;

public interface Bearing {
	static final Bearing DUE_NORTH = absolute(0), DUE_EAST = absolute(90), DUE_SOUTH = absolute(180),
			DUE_WEST = absolute(270);
	static final Bearing FORWARD = relative(0), RIGHT = relative(90), BACK = relative(180), LEFT = relative(270);
	public static Bearing absolute(double a) {
		return BearingImpl.absolute(a);
	}

	public static Bearing relative(double a) {
		return BearingImpl.relative(a);
	}

	public static Bearing absolute(double x, double y) {
		return BearingImpl.absolute(x, y);
	}

	public static Bearing relative(double x, double y) {
		return BearingImpl.relative(x, y);
	}

	double azimuth();

	boolean isAbsolute();

	double toRadians();

	Bearing add(Bearing other);

	Bearing sub(Bearing other);

	Bearing interpolate(Bearing other, double t);

	String toNavString();

	String toArrow();

	String toString();

	double dirX();

	double dirY();

	double dirZ();

}