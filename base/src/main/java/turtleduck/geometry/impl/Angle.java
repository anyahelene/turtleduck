package turtleduck.geometry.impl;

import java.util.logging.Logger;

import turtleduck.geometry.Direction;
import turtleduck.geometry.DirectionVector;

/**
 * 
 * Implements angles using milliarcseconds
 * @author anya
 *
 */
/**
 * @author anya
 *
 */
public class Angle implements DirectionVector, Direction {

	static final double SIGN_LEFT = -1, SIGN_RIGHT = 1;
	public static final double HALF_PI = Math.PI / 2;
	public static final double TWO_PI = 2 * Math.PI;
	public static final double THREE_PI = 2 * Math.PI;

	/**
	 * There are 3600 arc seconds in a degree
	 */
	public static final int ARCSEC = 3600;
	/**
	 * Number of milliarcsecs in a dregree
	 */
	public static final int MARCSEC = ARCSEC * 1000;
	/**
	 * π in milliarcsecs
	 */
	public static final int MI = 180 * MARCSEC;
	/**
	 * 2π in milliarcsecs
	 */
	public static final int TWO_MI = 360 * MARCSEC;
	static final int ARCSEC_NORTH = 0 * MARCSEC, ARCSEC_EAST = 90 * MARCSEC, //
			ARCSEC_SOUTH = 180 * MARCSEC, ARCSEC_WEST = -90 * MARCSEC;
	public static final double HALF_SQRT_2 = Math.sqrt(2) / 2, HALF_SQRT_3 = Math.sqrt(3) / 2;
	private final int angle;
	private final boolean absolute;

	protected Angle(int mArcSecs, boolean absolute) {
		mArcSecs = mArcSecs % TWO_MI;
		if (mArcSecs > MI) {
			mArcSecs -= TWO_MI;
		} else if (mArcSecs <= -MI) {
			mArcSecs += TWO_MI;
		}
		this.angle = mArcSecs;
		this.absolute = absolute;
//		assert angle > -MI && angle <= MI;
	}

	public static int degreesToMilliArcSec(double degrees) {
		degrees = degrees % 360;
		int m = (int) Math.round(degrees * MARCSEC); // ;
		return m; // degrees < 0 ? -m : m;
	}

	public static int radiansToMilliArcSec(double radians) {
		radians = radians % TWO_PI;
		int m = (int) Math.round(radians * MI / Math.PI);
		return m;
	}

	public static double milliArcSecToDegrees(int mArcSecs) {
		return ((double) mArcSecs) / MARCSEC;
	}

	public static double milliArcSecToRadians(int angle) {
//		if (angle == Short.MIN_VALUE)
//			return Math.PI;
//		else
		return ((double) angle) * Math.PI / MI;
	}

	public static Angle absolute(double a) {
		return new Angle(degreesToMilliArcSec(a), true);
	}

	public static Angle relative(double a) {
		return new Angle(degreesToMilliArcSec(a), false);
	}

	public static Angle absolute(double x, double y) {
		return new Angle(atan2(x, y), true);
	}

	public static Direction relative(double x, double y) {
		return new Angle(atan2(x, y), false);
	}

	@Override
	public double degrees() {
		double degs = milliArcSecToDegrees(angle);
		if (absolute && degs < 0)
			degs += 360.0;
		return degs;
	}

	/*
	 * default double degrees() { return Math.toDegrees(azimuth()); }
	 * 
	 * default double degreesTo(Bearing other) { return
	 * Math.toDegrees(angleTo(other)); }
	 */

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public double radians() {
		double rads = milliArcSecToRadians(angle);
		if (absolute && rads < 0)
			rads += TWO_PI;
		return rads;
	}

	@Override
	public Direction add(Direction other) {
		int b = ((Angle) other).angle;
		assert ((long) angle + (long) b) == angle + b;
		if (!absolute)
			return new Angle(angle + b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new Angle(angle + b, true);
		} else {
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);
			return new Angle(angle + b, false);
		}
	}

	@Override
	public Direction sub(Direction other) {
		int b = ((Angle) other).angle;
		assert ((long) angle - (long) b) == angle - b;
		if (!absolute)
			return new Angle(angle - b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new Angle(angle - b, true);
		} else {
			return new Angle(angle - b, false);
		}
	}

	@Override
	public Direction interpolate(Direction other, double t) {
		if (t <= 0.0)
			return this;
		else if (t >= 1.0)
			return other;
		double x0 = dirX(), x1 = other.dirX();
		double y0 = dirY(), y1 = other.dirY();
		double x = x0 + (x1 - x0) * t;
		double y = y0 + (y1 - y0) * t;
		return new Angle(atan2(x, y), absolute);
	}

	@Override
	public String toNavString() {
		double d = milliArcSecToDegrees(angle);
		if (absolute) {
			if (d < 0)
				d += MI;
			assert angle >= 0 && angle < ARCSEC_NORTH : String.format("0 <= %d < %d", angle, ARCSEC_NORTH);
			if (angle == 0) {
				return "N";
			} else if (angle == ARCSEC_EAST) {
				return "E";
			} else if (angle == ARCSEC_SOUTH) {
				return "S";
			} else if (angle == ARCSEC_WEST) {
				return "W";
			} else if (angle < ARCSEC_EAST) {
				return "N" + (d) + "°E";
			} else if (angle < ARCSEC_SOUTH) {
				return "S" + (180 - d) + "°E";
			} else if (angle < ARCSEC_WEST) {
				return "S" + (d - 180) + "°W";
			} else if (angle < ARCSEC_NORTH) {
				return "N" + (360 - d) + "°W";
			}
			return String.format("%06.2f°", d);
		} else {
//			assert mas > -180 * MARCSEC && mas <= 180 * MARCSEC : String.format("%d <= %d < %d", -180 * MARCSEC, mas,
//					180 * MARCSEC);
			if (angle == 0)
				return "0°";
			else if (angle < 0)
				return "R" + (360 - d) + "°";
			else if (angle == 180 * MARCSEC)
				return "180°";
			else // if (angle < 180 * MARCSEC)
				return "G" + d + "°";
		}
	}

	@Override
	public String toArrow() {
		if (angle == 0)
			return "↑";
		else if (angle < ARCSEC_WEST)
			return "↙";
		else if (angle == ARCSEC_WEST)
			return "←";
		else if (angle < 0)
			return "↖";
		else if (angle < ARCSEC_EAST)
			return "↗";
		else if (angle == ARCSEC_EAST)
			return "→";
		else if (angle < ARCSEC_SOUTH)
			return "↘";
		else if (angle == ARCSEC_SOUTH)
			return "↓";
		else
			return ""; // throw new IllegalStateException();
	}

	@Override
	public String toString() {
		double degs = milliArcSecToDegrees(angle);
		String format = absolute ? "%s%.6f°" : "%s%+.6f°";
		if (absolute && degs < 0) {
			degs += 360;
		}
		return String.format(format, toArrow(), degs);
	}

	public static double sin(int a) {
		double sign = -1;
		if (a < 0)
			a += TWO_MI;
		else if (a >= TWO_MI)
			a -= TWO_MI;
		if (a > MI) // reflect horiz
			a = TWO_MI - a;
		if (a > MI / 2) { // reflect vertical
			a = MI - a;
			sign = 1;
		}

		switch (a) {
		case 0:
			return sign;
		case MI / 2:
			return 0;
		case MI / 3:
			return sign * 0.5;
		case MI / 4:
			return sign * HALF_SQRT_2;
		case MI / 6:
			return sign * HALF_SQRT_3;
		default:
			return sign * Math.cos(Math.PI * a / MI);
		}
	}

	static double cos(int a) {
		double sign = 1;
		if (a < 0)
			a += TWO_MI;
		else if (a >= TWO_MI)
			a -= TWO_MI;
		if (a > MI) { // reflect horiz
			a = TWO_MI - a;
			sign = -1;
		}
		if (a > MI / 2) { // reflect vertical
			a = MI - a;
		}

		switch (a) {
		case 0:
			return 0;
		case MI / 2:
			return sign;
		case MI / 3:
			return sign * HALF_SQRT_3;
		case MI / 4:
			return sign * HALF_SQRT_2;
		case MI / 6:
			return sign * 0.5;
		default:
			return sign * Math.sin(Math.PI * a / MI);
		}
	}

	public static int atan2(double x, double y) {
		double absX = Math.abs(x), absY = Math.abs(y);
		if (absX < 1e-10) {
			if (absY < 1e-10)
				throw new ArithmeticException(String.format("atan2(%g,%g", x, y));
			else
				return y > 0 ? MI : 0;
		} else if (absY < 1e-10) {
			if (x > 0)
				return MI / 2;
			else
				return -MI / 2;
		} else {
			int m = degreesToMilliArcSec(Math.toDegrees(Math.atan(y / x)) + 90);
			if (x < 0)
				return m - MI;
			else
				return m;
		}
	}

	/*
	 * static double dirToRad(double x, double y) { return Math.atan2(x, -y); }
	 * 
	 * static double radToDirX(double a) { return Math.sin(a); }
	 * 
	 * static double radToDirY(double a) { return -Math.cos(a); }
	 */
	@Override
	public double dirX() {
		return cos(angle);
	}

	@Override
	public double dirY() {
		return sin(angle);
	}

	@Override
	public double dirZ() {
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (absolute ? 1231 : 1237);
		result = prime * result + angle;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Angle)) {
			return false;
		}
		Angle other = (Angle) obj;
		if (absolute != other.absolute) {
			return false;
		}
		if (angle != other.angle) {
			return false;
		}
		return true;
	}
}
