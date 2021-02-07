package turtleduck.geometry.impl;

import java.util.logging.Logger;

import org.joml.AxisAngle4d;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;

public class OrientImpl implements Orientation {
	public static final Vector3f FORWARD_VEC = new Vector3f(1, 0, 0);
	public static final Vector3f UP_VEC = new Vector3f(0, 0, 1);
	public static final Vector3f LEFT_VEC = new Vector3f(1, 0, 0);
	private static final Quaterniondc ROTATE_YZ = new Quaterniond(new AxisAngle4d(-Math.PI / 2, 1, 0, 0));
	private final Quaterniondc q;
	private final Vector3f vec;
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

	public static Orientation absoluteVec(double dx, double dy, double dz) {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(0, dx, dy, dz)), true);
	}

	public static Orientation relativeVec(double dx, double dy, double dz) {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(0, dx, dy, dz)), false);
	}

	public static Orientation absoluteAz(double az) {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(az)), UP_VEC)), true);
	}

	public static Orientation relativeAz(double az) {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(az)), UP_VEC)), false);
	}

	public static Orientation absoluteAlt(double alt) {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(alt)), 0, -1, 0)), true);
	}

	public static Orientation relativeAlt(double alt) {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(Math.toRadians(normalizeDegrees(alt)), 0, -1, 0)), false);
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
	public OrientImpl(Quaterniond newQ, boolean b) {
		newQ.normalize();
//		if (newQ.w < 0) {
//			System.out.println(newQ);
//			this.q = newQ.set(-newQ.x, -newQ.y, -newQ.z, -newQ.w);
//			System.out.println(" => " + newQ);
//		} else {
		this.q = newQ;
//		}
		this.absolute = b;
		newQ.normalize();
		Vector3f tmp = q.transform(new Vector3f(FORWARD_VEC)).normalize();
//		vec = tmp.set(tmp.x, -tmp.z, -tmp.y);
		vec = tmp;
		assert !(Double.isNaN(newQ.x) || Double.isNaN(newQ.y) || Double.isNaN(newQ.z) || Double.isNaN(newQ.w));
		assert Math.round(10e8 * q.lengthSquared()) == 10e8;
	}

	public OrientImpl(AngleImpl angle) {
		this(new Quaterniond(new AxisAngle4d(Math.toRadians((angle.degrees())), 0, 0, 1)), angle.isAbsolute());
	}

	@Override
	public double degrees() {
		Vector3d xyz = new Vector3d();
		double x = q.x(), y = q.y(), z = q.z(), w = q.w();
		xyz.y = Math.atan2(2.0 * (y * z + w * x), w * w - x * x - y * y + z * z); // 1.0 - 2.0 * (x*x + y*y));
		xyz.x = Math.safeAsin(2.0 * (x * z + y * w));
//		xyz.z = 
		double yaw = Math.atan2(2.0 * (z * w - x * y), 1.0 - 2.0 * (y * y + z * z));
//		Vector3d xyz = q.getEulerAnglesXYZ(new Vector3d());
		return Math.toDegrees(yaw < 0 ? 2 * Math.PI + yaw : yaw);
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
	public Orientation add(Direction other) {
		if (absolute && other.isAbsolute())
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);

		if (other.is3d()) {
			OrientImpl o = ((OrientImpl) other);
			return new OrientImpl(q.mul(o.q, new Quaterniond()), absolute || other.isAbsolute());
		} else {
			return new OrientImpl(new Quaterniond(q).rotateLocalY(other.radians()), absolute || other.isAbsolute());
		}
	}

	@Override
	public Orientation sub(Direction other) {
//		Vector3d result = new Vector3d(other.dirX(), other.dirY(), other.dirZ());

//		vec.sub(result, result);
		if (other.is3d()) {
			OrientImpl o = ((OrientImpl) other);
			return new OrientImpl(new Quaterniond(q).conjugate().mul(new Quaterniond(o.q)).conjugate(),
					absolute && !other.isAbsolute());
		} else {
			return new OrientImpl(new Quaterniond(q).rotateLocalY(-other.radians()), absolute && !other.isAbsolute());
		}
	}

	@Override
	public Orientation interpolate(Direction other, double t) {
		if (other.is3d()) {
			OrientImpl o = ((OrientImpl) other);
			return new OrientImpl(new Quaterniond(q).nlerp(o.q, t), absolute);
		} else {
			return interpolate((AngleImpl) other, t);
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
		return vec.x;
	}

	@Override
	public double dirY() {
		return vec.y;
	}

	@Override
	public double dirZ() {
		return vec.z;
	}

	@Override
	public Vector3f perpendicular(Vector3f dest) {
		q.transform(dest.set(LEFT_VEC));
//		ROTATE_YZ.transform(dest);
		return dest;
	}

	@Override
	public Vector3f directionVector(Vector3f dest) {
		dest.set(vec);
		return dest; // .set(dest.y, dest.z, dest.x);
	}

	@Override
	public Vector3f normalVector(Vector3f dest) {
		q.transform(dest.set(UP_VEC));
//		ROTATE_YZ.transform(dest);
		return dest; // .set(dest.y, dest.z, dest.x);
	}

	@Override
	public Quaternionf toQuaternion(Quaternionf dest) {
		q.get(dest);
		return dest;
	}

	@Override
	public Quaterniond toQuaternion(Quaterniond dest) {
		q.get(dest);
		return dest;
	}

	@Override
	public Matrix4f toMatrix(Matrix4f dest) {
		q.get(dest);
		return dest;
	}

	@Override
	public boolean is3d() {
		return true;
	}

	@Override
	public String toString() {
		double degs = degrees();
		Vector3d euler = q.getEulerAnglesXYZ(new Vector3d());
		Vector3d xyz = new Vector3d();
		double x = q.x(), y = q.y(), z = q.z(), w = q.w();
		xyz.y = Math.atan2(2.0 * (y * z + w * x), w * w - x * x - y * y + z * z); // 1.0 - 2.0 * (x*x + y*y));
//		xyz.y = Math.atan2(2*(z*w +x*y), -1 + 2*(w*w+x*x));
		xyz.x = Math.safeAsin(-2.0 * (x * z - w * y));
		xyz.z = Math.atan2(2.0 * (z * w + x * y), w * w + x * x - y * y - z * z); // 1.0 - 2.0 * (y * y + z * z));

//		double x = q.x(), y = q.y(), z = q.z(), w = q.w();
//        euler.x = Math.atan2(2.0 * (x*w - y*z), 1.0 - 2.0 * (x*x + y*y));
//        euler.y = Math.safeAsin(2.0 * (x*z + y*w));
//        euler.y = Math.atan2(2.0 * (y*w - x*x), 1.0 - 2.0 * (z*z + x*x));
//        euler.z = Math.atan2(2.0 * (z*w - x*y), 1.0 - 2.0 * (y*y + z*z));
	// format = absolute ? "%s%.2f°,%.2f°,%.2f° %s" : "%s%+.2f°,%+.2f°,%+.2f° %s";
		if (absolute && degs < 0) {
			degs += 360;
		}
		return String.format("%.2f°,%.2f°,%.2f° / %.2f°,%.2f°,%.2f° %s %s", Math.toDegrees(euler.x),
				Math.toDegrees(euler.y), Math.toDegrees(euler.z), Math.toDegrees(xyz.x), Math.toDegrees(xyz.y),
				Math.toDegrees(xyz.z), vec, q);
//		return String.format(format, toArrow(), Math.toDegrees(euler.x), Math.toDegrees(euler.y),
//				Math.toDegrees(euler.z), euler.toString() + "/" + xyz.toString() + " vec " + vec.toString());
	}

	@Override
	public Orientation yaw(double degrees) {
		return new OrientImpl(q.rotateZ(Math.toRadians(degrees), new Quaterniond()), absolute);
	}

	@Override
	public Orientation pitch(double degrees) {
		return new OrientImpl(q.rotateY(Math.toRadians(degrees), new Quaterniond()), absolute);
	}

	@Override
	public Orientation roll(double degrees) {
		return new OrientImpl(q.rotateX(Math.toRadians(degrees), new Quaterniond()), absolute);
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
		if (!(obj instanceof OrientImpl)) {
			return false;
		}
		OrientImpl other = (OrientImpl) obj;
		if (absolute != other.absolute) {
			return false;
		}
		if (q == null) {
			if (other.q != null) {
				return false;
			}
		}
		return Math.abs(q.dot(other.q)) > 1 - 10e-12;
	}

	@Override
	public boolean like(Direction other) {
		if (other instanceof OrientImpl) {
			OrientImpl o = (OrientImpl) other;
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
