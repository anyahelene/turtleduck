package turtleduck.geometry;

public interface Bearing {
	public static Bearing absolute(double a) {
		return MasBearing.absolute(a);
	}

	public static Bearing relative(double a) {
		return MasBearing.relative(a);
	}

	public static Bearing absolute(double x, double y) {
		return MasBearing.absolute(x, y);
	}

	public static Bearing relative(double x, double y) {
		return MasBearing.relative(x, y);
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