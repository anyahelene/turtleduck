package turtleduck.geometry.impl;

import java.util.logging.Logger;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.DirectionVector;

@Deprecated
public class DoubleBearing implements DirectionVector, Bearing {
	static final Bearing DUE_NORTH = new DoubleBearing(0, true, true),//
			DUE_EAST = new DoubleBearing(Math.PI/2, true, true),//
			DUE_SOUTH = new DoubleBearing(Math.PI, true, true),//
			DUE_WEST = new DoubleBearing(3*Math.PI/2, true, true);
	static final int ARCSEC_NORTH = 360 * 3600, ARCSEC_EAST = 90 * 3600, ARCSEC_SOUTH = 180 * 3600,
			ARCSEC_WEST = 270 * 3600;
	static final double SIGN_LEFT = -1, SIGN_RIGHT = 1;
	public static final double HALF_PI = Math.PI / 2;
	public static final double TWO_PI = 2 * Math.PI;
	private final double radians;
	private final int arcsecs;
	private final boolean absolute;

	protected DoubleBearing(double radians, boolean absolute, boolean normalized) {
		this.absolute = absolute;
		if (!normalized) {
			radians = absolute ? normalizeAbsAngle(radians) : normalizeRelAngle(radians);
		}
		this.radians = radians;
		this.arcsecs = (int) Math.round(180 * 60 * 60 * this.radians / Math.PI);
//		System.out.printf("input: %f, output: %f\n", radians, this.radians);
	}


	@Override
	public double azimuth() {
		return Math.toDegrees(radians);
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
		return radians;
	}

	@Override
	public DoubleBearing add(Bearing o) {
		DoubleBearing other = (DoubleBearing) o;
		if (!absolute)
			return new DoubleBearing(radians + other.radians, other.absolute, false);
		else if (!other.absolute) {
			return new DoubleBearing(radians + other.radians, true, false);
		} else {
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);
			return new DoubleBearing(radians + other.radians, false, false);
		}
	}

	@Override
	public DoubleBearing sub(Bearing o) {
		DoubleBearing other = (DoubleBearing) o;
		if (!absolute)
			return new DoubleBearing(radians - other.radians, other.absolute, false);
		else if (!other.absolute) {
			return new DoubleBearing(radians - other.radians, true, false);
		} else {
			return new DoubleBearing(radians - other.radians, false, false);
		}
	}

	@Override
	public Bearing interpolate(Bearing o, double t) {
		DoubleBearing other = (DoubleBearing) o;
		if (t <= 0.0)
			return this;
		else if (t >= 1.0)
			return other;
		double a = radians, b = other.radians;
		if (a + Math.PI < b)
			a += 2 * Math.PI;
		else if (b + Math.PI < a)
			b += 2 * Math.PI;
		return new DoubleBearing(a + (b - a) * t, absolute, false);
	}

	@Override
	public String toNavString() {
//		int deg = arcsecs / 3600;

		double d = arcsecs / 3600.0; // Math.toDegrees(radians);
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
			else if (Math.signum(radians) == SIGN_LEFT)
				return "R" + (SIGN_LEFT * d) + "°";
			else if (Math.signum(radians) == SIGN_RIGHT)
				return "G" + (SIGN_RIGHT * d) + "°";
			else
				return "0°";
		}
	}

	@Override
	public String toArrow() {
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
			if (Math.abs(radians) == 0)
				return "↑";
			else if (Math.abs(radians) == Math.PI)
				return "⟳";
			else if (radians <= -Math.PI / 2)
				return "⤹";
			else if (radians < 0)
				return "↶";
			else if (radians <= Math.PI / 2)
				return "↷";
			else // if(radians <= 3*Math.PI/4)
				return "⤸";
		}
	}

	@Override
	public String toString() {
		return toArrow() + (absolute || radians < 0 ? "" : "+") + Math.toDegrees(radians) + "°";
//		if (absolute)
//			return String.format("%s%06.2f°%+.2f%+.2f", toArrow(), Math.toDegrees(radians), dirX(), dirY());
//		else
//			return String.format("%s%+08.3f°", toArrow(), Math.toDegrees(radians));
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
		return Math.sin(radians);
	}

	@Override
	public double dirY() {
		return -Math.cos(radians);
	}

	@Override
	public double dirZ() {
		return 0;
	}
}
