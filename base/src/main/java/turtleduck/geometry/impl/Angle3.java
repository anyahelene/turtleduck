package turtleduck.geometry.impl;

import java.util.logging.Logger;

import org.joml.AxisAngle4d;
import org.joml.Math;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3f;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Direction3;

public class Angle3 implements Direction3 {
	public static final Vector3f FORWARD_VEC = new Vector3f(1, 0, 0);
	public static final Vector3f UP_VEC = new Vector3f(0, 0, 1);
	public static final Vector3f LEFT_VEC = new Vector3f(0, 0, 1);

	private final Quaterniondc q;
	private final boolean absolute;

	private static double normalizeDegrees(double deg) {
//		System.out.print("+" + deg + "~= ");
		long a = Math.round(deg * 60 * 60 * 1000);
		a %= 360 * 60 * 60 * 1000;
		a += (a < 0 ? 360 * 60 * 60 * 1000 : 0);
		deg = a / (60.0 * 60.0 * 1000.0);
//		System.out.println(deg);
		return deg;
	}

	public static Direction3 absoluteAz(double az) {
		return new Angle3(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(az)), 0, 0, 1)), true);
	}

	public static Direction3 relativeAz(double az) {
		return new Angle3(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(az)), 0, 0, 1)), false);
	}

	public static Direction3 absoluteAlt(double alt) {
		return new Angle3(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(alt)), 0, 1, 0)), true);
	}

	public static Direction3 relativeAlt(double alt) {
		return new Angle3(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(alt)), 0, 1, 0)), false);
	}

//	public static Direction3 absoluteVec(double dx, double dy, double dz) {
//		return new Angle3(new Vector3d(dx, dy, dz).normalize(), true);
//	}
//
//	public static Direction3 relativeVec(double dx, double dy, double dz) {
//		return new Angle3(new Vector3d(dx, dy, dz).normalize(), false);
//	}

	/*
	 * public Angle3(double yaw, double pitch, double roll, boolean absolute) { q =
	 * new Quaterniond().lookAlong(0, 0, 1, 0, 1, 0)
	 * .rotateZYX(Math.toRadians(roll), Math.toRadians(pitch),
	 * Math.toRadians(yaw)).normalize(); this.absolute = absolute; assert
	 * !(Double.isNaN(q.x()) || Double.isNaN(q.y()) || Double.isNaN(q.z()) ||
	 * Double.isNaN(q.w())); assert Math.round(10e8 * q.lengthSquared()) == 10e8; }
	 */
	public Angle3(Quaterniond newQ, boolean b) {
		this.q = newQ.normalize();
		this.absolute = b;
		newQ.normalize();
		assert !(Double.isNaN(newQ.x) || Double.isNaN(newQ.y) || Double.isNaN(newQ.z) || Double.isNaN(newQ.w));
		assert Math.round(10e8 * q.lengthSquared()) == 10e8;
	}

	public Angle3(Angle angle) {
		this(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(angle.degrees())), 0, 0, 1)),
				angle.isAbsolute());
	}

	@Override
	public double degrees() {
//		double x = q.x(), y = q.y(), z = q.z(), w = q.w();
//        eulerAngles.x = Math.atan2(2.0 * (x*w - y*z), 1.0 - 2.0 * (x*x + y*y));
//        eulerAngles.y = Math.safeAsin(2.0 * (x*z + y*w));
//        eulerAngles.z = Math.atan2(2.0 * (z*w - x*y), 1.0 - 2.0 * (y*y + z*z));
		Vector3d xyz = q.getEulerAnglesXYZ(new Vector3d());
		if (xyz.z >= 0)
			return Math.toDegrees(xyz.z);
		else
			return Math.toDegrees(xyz.z) + 360.0;
	}

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public double radians() {
		Vector3d xyz = q.getEulerAnglesXYZ(new Vector3d());
		return xyz.z;
	}

	@Override
	public double altDegrees() {
		return Math.toDegrees(altRadians());
	}

	@Override
	public double altRadians() {
		Vector3d xyz = q.getEulerAnglesXYZ(new Vector3d());
		return xyz.y;
	}

	@Override
	public Direction3 add(Direction other) {
		if (absolute && other.isAbsolute())
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);

		if (other.is3d()) {
			Angle3 o = ((Angle3) other);
			return new Angle3(q.mul(o.q, new Quaterniond()), absolute || other.isAbsolute());
		} else {
			return new Angle3(new Quaterniond(q).rotateLocalY(other.radians()), absolute || other.isAbsolute());
		}
	}

	@Override
	public Direction3 sub(Direction other) {
//		Vector3d result = new Vector3d(other.dirX(), other.dirY(), other.dirZ());

//		vec.sub(result, result);
		if (other.is3d()) {
			Angle3 o = ((Angle3) other);
			return new Angle3(new Quaterniond(q).mul(new Quaterniond(o.q).conjugate()),
					absolute && !other.isAbsolute());
		} else {
			return new Angle3(new Quaterniond(q).rotateLocalY(-other.radians()), absolute && !other.isAbsolute());
		}
	}

	@Override
	public Direction3 interpolate(Direction other, double t) {
		if (other.is3d()) {
			Angle3 o = ((Angle3) other);
			return new Angle3(new Quaterniond(q).nlerp(o.q, t), absolute);
		} else {
			return interpolate((Angle) other, t);
		}
	}

	@Override
	public String toNavString() {
		return toString();
	}

	@Override
	public String toArrow() {
		return "";
	}

	@Override
	public double dirX() {
		return q.transform(new Vector3d(1, 0, 0)).x;
	}

	@Override
	public double dirY() {
		return q.transform(new Vector3d(1, 0, 0)).y;
	}

	@Override
	public double dirZ() {
		return q.transform(new Vector3d(1, 0, 0)).z;
	}

	@Override
	public Vector3f perpendicular(Vector3f dest) {
		q.transform(dest.set(0, 1, 0));
		return dest;
	}

	@Override
	public Vector3f directionVector(Vector3f dest) {
		q.transform(dest.set(1, 0, 0));
		return dest; // .set(dest.y, dest.z, dest.x);
	}

	@Override
	public Vector3f normalVector(Vector3f dest) {
		q.transform(dest.set(0, 0, 1));
		return dest; // .set(dest.y, dest.z, dest.x);
	}

	@Override
	public boolean is3d() {
		return true;
	}

	@Override
	public String toString() {
		double degs = degrees();
		Vector3d euler = q.getEulerAnglesXYZ(new Vector3d());
//		double x = q.x(), y = q.y(), z = q.z(), w = q.w();
//        euler.x = Math.atan2(2.0 * (x*w - y*z), 1.0 - 2.0 * (x*x + y*y));
//        euler.y = Math.safeAsin(2.0 * (x*z + y*w));
//        euler.y = Math.atan2(2.0 * (y*w - x*x), 1.0 - 2.0 * (z*z + x*x));
//        euler.z = Math.atan2(2.0 * (z*w - x*y), 1.0 - 2.0 * (y*y + z*z));
		String format = absolute ? "%s%.2f°,%.2f°,%.2f° %s" : "%s%+.2f°,%+.2f°,%+.2f° %s";
		if (absolute && degs < 0) {
			degs += 360;
		}
		return String.format(format, toArrow(), Math.toDegrees(euler.x), Math.toDegrees(euler.y),
				Math.toDegrees(euler.z), q.toString());
	}

	@Override
	public Direction3 yaw(double degrees) {
		return new Angle3(q.rotateLocalZ(Math.toRadians(degrees), new Quaterniond()), absolute);
	}

	@Override
	public Direction3 pitch(double degrees) {
		return new Angle3(q.rotateLocalY(Math.toRadians(degrees), new Quaterniond()), absolute);
	}

	@Override
	public Direction3 roll(double degrees) {
		return new Angle3(q.rotateLocalX(Math.toRadians(degrees), new Quaterniond()), absolute);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (absolute ? 1231 : 1237);
		result = prime * result + ((q == null) ? 0 : q.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Angle3)) {
			return false;
		}
		Angle3 other = (Angle3) obj;
		if (absolute != other.absolute) {
			return false;
		}
		if (q == null) {
			if (other.q != null) {
				return false;
			}
		}
		return Math.abs(q.dot(other.q)) > 1-10e-12;
	}

	@Override
	public boolean like(Direction other) {
		if (other instanceof Angle3) {
			Angle3 o = (Angle3) other;
			// dot product will be 1 or -1 when equal
			return Math.abs(q.dot(o.q)) > 1 - 10e-6;
//			return Math.abs(q.x() - o.q.x()) < 10e-6 //
//					&& Math.abs(q.y() - o.q.y()) < 10e-6 //
//					&& Math.abs(q.z() - o.q.z()) < 10e-6 //
//					&& Math.abs(q.w() - o.q.w()) < 10e-6;
		}
		return false;
	}
}
