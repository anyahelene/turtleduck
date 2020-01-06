package turtleduck.geometry;

import turtleduck.geometry.impl.BearingImpl;

public interface Bearing {
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