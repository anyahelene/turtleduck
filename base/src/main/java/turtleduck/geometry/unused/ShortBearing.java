package turtleduck.geometry.unused;

import java.util.logging.Logger;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.DirectionVector;

@Deprecated
public class ShortBearing implements DirectionVector, Bearing {
	static final Bearing DUE_NORTH = absolute(0), DUE_EAST = absolute(90), DUE_SOUTH = absolute(180),
			DUE_WEST = absolute(270);
	static final int ARCSEC_NORTH = 360 * 3600, ARCSEC_EAST = 90 * 3600, ARCSEC_SOUTH = 180 * 3600,
			ARCSEC_WEST = 270 * 3600;
	static final double SIGN_LEFT = -1, SIGN_RIGHT = 1;
	public static final double HALF_PI = Math.PI / 2;
	public static final double TWO_PI = 2 * Math.PI;
	public static final double THREE_PI = 2 * Math.PI;
	public static final double BI = 32768;
	public static final double TWO_BI = 2 * BI;
	private final short brad;
	private final boolean absolute;

	protected ShortBearing(int angle, boolean absolute) {
		brad = (short) angle;
		this.absolute = absolute;
	}

	public static short degreesToBrad(double angle) {
		return (short) Math.round(angle * BI / 180.0);
	}

	public static double bradToDegrees(short angle) {
		if (angle == Short.MIN_VALUE)
			return 180.0;
		else
			return ((double) angle) * 180.0 / BI;
	}

	public static double bradToRadians(short angle) {
		if (angle == Short.MIN_VALUE)
			return Math.PI;
		else
			return ((double) angle) * Math.PI / BI;
	}

	public static ShortBearing absolute(double a) {
		return new ShortBearing(degreesToBrad(a), true);
	}

	public static ShortBearing relative(double a) {
		return new ShortBearing(degreesToBrad(a), false);
	}

	public static ShortBearing absolute(double x, double y) {
		return new ShortBearing(degreesToBrad(Math.toDegrees(dirToRad(x, y))), true);
	}

	public static Bearing relative(double x, double y) {
		return new ShortBearing(degreesToBrad(Math.toDegrees(dirToRad(x, y))), false);
	}

	@Override
	public double azimuth() {
		return bradToDegrees(brad);
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
	public double toRadians() {
		return bradToRadians(brad);
	}

	@Override
	public Bearing add(Bearing other) {
		short b = ((ShortBearing) other).brad;
		if (!absolute)
			return new ShortBearing(brad + b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new ShortBearing(brad + b, true);
		} else {
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);
			return new ShortBearing(brad + b, false);
		}
	}

	@Override
	public Bearing sub(Bearing other) {
		short b = ((ShortBearing) other).brad;
		if (!absolute)
			return new ShortBearing(brad - b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new ShortBearing(brad - b, true);
		} else {
			return new ShortBearing(brad - b, false);
		}
	}

	@Override
	public Bearing interpolate(Bearing other, double t) {
		if (t <= 0.0)
			return this;
		else if (t >= 1.0)
			return other;
		int a = brad, b = ((ShortBearing) other).brad;
		if (a + Short.MAX_VALUE < b)
			a += 65536;
		else if (b + Short.MAX_VALUE < a)
			b += 65536;
		return new ShortBearing((short) Math.round(a + (b - a) * t), absolute);
	}

	@Override
	public String toNavString() {
//		int deg = arcsecs / 3600;

		double d = brad / 3600.0; // Math.toDegrees(brad);
		int s = (int) Math.round(d * 3600);
		if (absolute) {
			assert s >= 0 && s < ARCSEC_NORTH : String.format("0 <= %d < %d", s, ARCSEC_NORTH);
			if (s == 0) {
				return "N";
			} else if (s == ARCSEC_EAST) {
				return "E";
			} else if (s == ARCSEC_SOUTH) {
				return "S";
			} else if (s == ARCSEC_WEST) {
				return "W";
			} else if (s < ARCSEC_EAST) {
				return "N" + (d) + "°E";
			} else if (s < ARCSEC_SOUTH) {
				return "S" + (180 - d) + "°E";
			} else if (s < ARCSEC_WEST) {
				return "S" + (d - 180) + "°W";
			} else if (s < ARCSEC_NORTH) {
				return "N" + (360 - d) + "°W";
			}
			return String.format("%06.2f°", d);
		} else {
			assert s > -180 * 3600 && s <= 180 * 3600 : String.format("%d <= %d < %d", -180 * 3600, s, -180 * 3600);
			if (s == 180 * 3600)
				return "180°";
			else if (Math.signum(brad) == SIGN_LEFT)
				return "R" + (SIGN_LEFT * d) + "°";
			else if (Math.signum(brad) == SIGN_RIGHT)
				return "G" + (SIGN_RIGHT * d) + "°";
			else
				return "0°";
		}
	}

	@Override
	public String toArrow() {
		int arcsecs = brad;
		int deg = arcsecs / 3600;
		if (absolute) {
			if (deg == 0)
				return "↑";
			else if (arcsecs < ARCSEC_EAST)
				return "↗";
			else if (deg == 90)
				return "→";
			else if (arcsecs < ARCSEC_SOUTH)
				return "↘";
			else if (deg == 180)
				return "↓";
			else if (arcsecs < ARCSEC_WEST)
				return "↙";
			else if (deg == 270)
				return "←";
			else
				return "↖";
		} else {
			if (Math.abs(brad) == 0)
				return "↑";
			else if (Math.abs(brad) == Math.PI)
				return "⟳";
			else if (brad <= -Math.PI / 2)
				return "⤹";
			else if (brad < 0)
				return "↶";
			else if (brad <= Math.PI / 2)
				return "↷";
			else // if(brad <= 3*Math.PI/4)
				return "⤸";
		}
	}

	@Override
	public String toString() {
		return toArrow() + (absolute || brad < 0 ? "" : "+") + Math.toDegrees(brad) + "°";
//		if (absolute)
//			return String.format("%s%06.2f°%+.2f%+.2f", toArrow(), Math.toDegrees(brad), dirX(), dirY());
//		else
//			return String.format("%s%+08.3f°", toArrow(), Math.toDegrees(brad));
	}

	static double normalizeRelAngle(double angle) {
		if (angle == Math.PI)
			return angle;
		if (!Double.isFinite(angle))
			throw new IllegalArgumentException("Angle must be finite: " + angle);
		return angle - (TWO_PI * Math.floor((angle + Math.PI) / TWO_PI));
	}

	static double normalizeAbsAngle(double angle) {
		if (!Double.isFinite(angle))
			throw new IllegalArgumentException("Angle must be finite: " + angle);
		return angle - TWO_PI * Math.floor(angle / TWO_PI);
	}

	static double normalizeAngleAround(double angle, double center) {
		double b = angle - center;
		if (!Double.isFinite(b))
			throw new IllegalArgumentException("Angle must be finite: " + angle + ", " + center);
		return angle - TWO_PI * Math.floor((b + Math.PI) / TWO_PI);
	}

	static double dirToRad(double x, double y) {
		return Math.atan2(x, -y);
	}

	static double radToDirX(double a) {
		return Math.sin(a);
	}

	static double radToDirY(double a) {
		return -Math.cos(a);
	}

	@Override
	public double dirX() {
		return Math.sin(0);
	}

	@Override
	public double dirY() {
		return -Math.cos(0);
	}

	@Override
	public double dirZ() {
		return 0;
	}
}
