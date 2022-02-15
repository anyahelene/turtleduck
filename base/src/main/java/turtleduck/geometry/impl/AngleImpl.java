package turtleduck.geometry.impl;

import java.util.logging.Logger;

import org.joml.AxisAngle4d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import turtleduck.geometry.Direction;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.Orientation;

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
public class AngleImpl implements DirectionVector, Orientation {
	public static final Vector3dc POSITIVE_Z = new Vector3d(0, 0, 1);
	static final double EPSILON = 10e-6;
	static final double SIGN_LEFT = -1, SIGN_RIGHT = 1;
	public static final double HALF_PI = Math.PI / 2;
	public static final double TWO_PI = 2 * Math.PI;
	public static final double THREE_PI = 2 * Math.PI;
	static final String CW_ARROWS = "→↘↘↘↓↙↙↙←↖↖↖↑↗↗↗→";
	static final String CCW_ARROWS = "→↗↗↗↑↖↖↖←↙↙↙↓↘↘↘→";

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
	static final int ARCSEC_NORTH = Direction.DEGREES_NORTH * ARCSEC, //
			ARCSEC_EAST = Direction.DEGREES_EAST * ARCSEC, //
			ARCSEC_SOUTH = (360 + Direction.DEGREES_SOUTH) * ARCSEC, //
			ARCSEC_WEST = Direction.DEGREES_WEST * ARCSEC;
	static final int MARCSEC_NORTH = 1000 * ARCSEC_NORTH, MARCSEC_EAST = 1000 * ARCSEC_EAST, //
			MARCSEC_SOUTH = 1000 * ARCSEC_SOUTH, MARCSEC_WEST = 1000 * ARCSEC_WEST;
	public static final double HALF_SQRT_2 = Math.sqrt(2) / 2, HALF_SQRT_3 = Math.sqrt(3) / 2;
	private final int angle;
	private final double x, y;
	private final boolean absolute;

	protected AngleImpl(int mArcSecs, boolean absolute) {
		mArcSecs = mArcSecs % TWO_MI;
		if (mArcSecs > MI) {
			mArcSecs -= TWO_MI;
		} else if (mArcSecs <= -MI) {
			mArcSecs += TWO_MI;
		}
		this.angle = mArcSecs;
		this.absolute = absolute;
		this.x = cos(mArcSecs);
		this.y = sin(mArcSecs);
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

	public static AngleImpl absolute(double a) {
		return new AngleImpl(degreesToMilliArcSec(a), true);
	}

	public static AngleImpl relative(double a) {
		return new AngleImpl(degreesToMilliArcSec(a), false);
	}

	public static AngleImpl absolute(double x, double y) {
		return new AngleImpl(atan2(y, x), true);
	}

	public static Direction relative(double x, double y) {
		return new AngleImpl(atan2(y, x), false);
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
	public Orientation add(Direction other) {
		if (other.is3d())
			return other.toOrientation().add(this);
		// TODO
		int b = ((AngleImpl) other).angle;
		assert ((long) angle + (long) b) == angle + b;
		if (!absolute)
			return new AngleImpl(angle + b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new AngleImpl(angle + b, true);
		} else {
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);
			return new AngleImpl(angle + b, false);
		}
	}

	@Override
	public Orientation sub(Direction other) {
		if (other.is3d())
			return new OrientImpl(this).sub(other);
		int b = ((AngleImpl) other).angle;
		assert ((long) angle - (long) b) == angle - b;
		return new AngleImpl(angle - b, absolute != other.isAbsolute());
	}

	@Override
	public Orientation interpolate(Direction other, double t) {
		if (t <= 0.0)
			return this;
		else if (t >= 1.0)
			return other.toOrientation();
//		Vector3d thisVec = this.directionVector(null);
//		Vector3d otherVec = other.directionVector(null);
//		thisVec.lerp(otherVec, (float)t);
		double x0 = x, x1 = other.dirX();
		double y0 = y, y1 = other.dirY();
		double x = x0 + (x1 - x0) * t;
		double y = y0 + (y1 - y0) * t;
		return new AngleImpl(atan2(y, x), absolute);
	}

	@Override
	public String toNavString() {
		double d = milliArcSecToDegrees(angle);
		int a = angle;
		if (absolute) {
			if (d < 0)
				d += 360;
			if (a < 0)
				a += TWO_MI;
//			assert angle >= 0 && angle < MARCSEC_NORTH : String.format("0 <= %d < %d", angle, MARCSEC_NORTH);
			if (a == MARCSEC_NORTH) {
				return "N";
			} else if (a == MARCSEC_EAST || angle == TWO_MI) {
				return "E";
			} else if (a == MARCSEC_SOUTH) {
				return "S";
			} else if (a == MARCSEC_WEST) {
				return "W";
			} else if (a > MARCSEC_SOUTH) {
				return String.format("S%.1f°E", d - 270);
			} else if (a > MARCSEC_WEST) {
				return String.format("S%.1f°W", 270 - d);
			} else if (a > MARCSEC_NORTH) {
				return String.format("N%.1f°W", d - 90);
			} else if (a > MARCSEC_EAST) {
				return String.format("N%.1f°E", d);
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
		return toArrow(angle);
	}

	public static String toArrow(int angle) {
		if (angle < 0)
			angle += TWO_MI;
		angle = (int) Math.round((16.0 * angle) / TWO_MI);
		return CCW_ARROWS.substring(angle, angle + 1);
	}

	public static String toArrow(double radians) {
		if (radians < 0)
			radians += TWO_PI;
		int index = (int) Math.round((16.0 * radians) / TWO_PI);
		return CCW_ARROWS.substring(index, index + 1);
	}

	@Override
	public String toString() {
		double degs = milliArcSecToDegrees(angle);
		String format = absolute ? "%s%.1f°" : "%s%+.1f°";
		if (absolute && degs < 0) {
			degs += 360;
		}
//		return (absolute ? "=" : "±") + angle; 
		return String.format(format, toArrow(), degs);
	}

	public static String toString(boolean absolute, double degrees) {
		String format = absolute ? "%s%.1f°" : "%s%+.1f°";

//		if (degrees > 180) { //(absolute && degrees < 0) {
//			degrees -= 360;
//		}
		return String.format(format, toArrow(degreesToMilliArcSec(degrees)), degrees);
	}

	public static double cos(int a) {
		double sign = 1;
		if (a < 0)
			a += TWO_MI;
		else if (a >= TWO_MI)
			a -= TWO_MI;
		if (a > MI) // reflect horiz
			a = TWO_MI - a;
		if (a > MI / 2) { // reflect vertical
			a = MI - a;
			sign = -1;
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

	static double sin(int a) {
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

	public static int atan2(double y, double x) {
		double absX = Math.abs(x), absY = Math.abs(y);
		double e = 1e-10;
		if (absX < e || absY < e) { // special case when close to zero
			if (absX == 0 && absY == 0) {
				throw new ArithmeticException(String.format("atan2(%s,%s)", Double.toString(x), Double.toString(y)));
			} else if (absX >= absY) {
				return x > 0 ? 0 : -MI;
			} else {
				return y > 0 ? MI / 2 : -MI / 2;
			}
		} else {
			int m = degreesToMilliArcSec(Math.toDegrees(Math.atan(y / x)));
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
		return x; // cos(angle);
	}

	@Override
	public double dirY() {
		return y; // sin(angle);
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
		if (!(obj instanceof Direction)) {
			return false;
		}
		Direction other = (Direction) obj;

		if (other.is3d())
			return other.equals(this);

		if (absolute != other.isAbsolute())
			return false;

		if (other instanceof AngleImpl)
			return angle == ((AngleImpl) other).angle;
		else
			return degrees() == other.degrees();
	}

	@Override
	public boolean is3d() {
		return false;
	}

	@Override
	public double altDegrees() {
		return 0;
	}

	@Override
	public Vector3d perpendicular(Vector3d dest) {
		return dest.set(x, y, 0).rotateZ((float) Math.PI / 2);
	}

	@Override
	public Vector3d normalVector(Vector3d dest) {
		return dest.set(0, 0, 1);
	}

	@Override
	public Vector3d directionVector(Vector3d dest) {
		return dest.set(x, y, 0);
	}

	@Override
	public Orientation yaw(double degrees) {
		return new AngleImpl(angle + degreesToMilliArcSec(degrees), absolute);
	}

	@Override
	public Orientation pitch(double degrees) {
		return new OrientImpl(this).pitch(degrees);
	}

	@Override
	public Orientation roll(double degrees) {
		return new OrientImpl(this).roll(degrees);
	}

	@Override
	public boolean like(Direction other) {
		if (other instanceof AngleImpl) {
			AngleImpl o = (AngleImpl) other;
			return Math.abs(angle - o.angle) < EPSILON;
		} else if (other instanceof OrientImpl)
			return other.equals(this);
		return false;
	}

	@Override
	public Orientation rotateTo(Direction other) {
		return other.toOrientation();
	}

	@Override
	public Orientation toOrientation() {
		return this;
	}

	@Override
	public double altRadians() {
		return 0;
	}

	@Override
	public Quaterniond toQuaternion(Quaterniond dest) {
		return dest.set(new AxisAngle4d(radians(), OrientImpl.UP_VEC));
	}

	@Override
	public Quaternionf toQuaternion(Quaternionf dest) {
		return dest.set(new AxisAngle4d(radians(), OrientImpl.UP_VEC));
	}

	@Override
	public Matrix4f toMatrix(Matrix4f dest) {
		return dest.set(new AxisAngle4d(radians(), OrientImpl.UP_VEC));
	}

	@Override
	public Orientation rotateTo(double angle) {
		return absolute(angle);
	}
}
