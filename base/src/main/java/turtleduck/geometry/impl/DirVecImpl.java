package turtleduck.geometry.impl;

import org.joml.Vector3d;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;

public class DirVecImpl implements Direction {
	private final Vector3d vec;
	private final double rads;
	private final boolean absolute;

	private DirVecImpl(Vector3d v, boolean abs) {
		vec = v;
		rads = Math.atan2(v.y, v.x);
		absolute = abs;
	}

	public DirVecImpl(double a, boolean abs) {
		this(Math.cos(Math.toRadians(a)), Math.sin(Math.toRadians(a)), 0, abs);
	}

	public DirVecImpl(double x, double y, double z, boolean abs) {
		this((float) x, (float) y, (float) z, abs);
//		if (x == 0 && y == 0) {
//			throw new ArithmeticException("Direction from (0,0)");
//		}
//		vec = new Vector3d((float) x, (float) y, (float) z);
//		double r = 0;
//		if (x != 0 && y != 0) {
//			r = Math.atan2(y, x);
//			if (r < 0)
//				r += 2 * Math.PI;
//		}
//		rads = r;
//		absolute = abs;
	}

	public DirVecImpl(float x, float y, float z, boolean abs) {
		if (x == 0 && y == 0) {
			throw new ArithmeticException("Direction from (0,0)");
		}
		vec = new Vector3d(x, y, z);
		double r = 0;
		if (x != 0 && y != 0) {
			r = Math.atan2(y, x);
			if (r < 0)
				r += 2 * Math.PI;
		}
		rads = r;
		absolute = abs;
	}

	@Override
	public double degrees() {
		return Math.toDegrees(rads);
	}

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public double radians() {
		return rads;
	}

	@Override
	public Direction add(Direction other) {

		return new DirVecImpl(vec.rotateZ((float) other.radians(), new Vector3d()), absolute || other.isAbsolute());

	}

	@Override
	public Direction sub(Direction other) {
		boolean abs = absolute != other.isAbsolute();
		return new DirVecImpl(vec.rotateZ((float) -other.radians(), new Vector3d()), absolute || other.isAbsolute());
	}

	@Override
	public Direction interpolate(Direction other, double t) {
		if (t <= 0)
			return this;
		if (t >= 1)
			return other;
		float ft = (float) t;
		Vector3d tmp = other.directionVector(new Vector3d());
		return new DirVecImpl(vec.lerp(tmp, ft, tmp), absolute);

	}

	@Override
	public String toNavString() {
		return new AngleImpl(AngleImpl.radiansToMilliArcSec(rads), absolute).toNavString();
	}

	@Override
	public String toArrow() {
		return AngleImpl.toArrow(rads);
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
	public boolean is3d() {
		return vec.z != 0;
	}

	@Override
	public double altDegrees() {
		return Math.toDegrees(Math.acos(-vec.z) - Math.PI / 2);
	}

	@Override
	public Vector3d perpendicular(Vector3d dest) {
		return null;
	}

	@Override
	public Vector3d directionVector(Vector3d dest) {
		return vec.get(dest);
	}

	@Override
	public Vector3d normalVector(Vector3d dest) {
		dest.set(0, 0, 1);
		if (vec.z != 0)
			dest.rotateAxis((float) (Math.acos(-vec.z) - Math.PI / 2), -vec.y, vec.x, 0);
		return dest;
	}

	@Override
	public Direction yaw(double angle) {
		return new DirVecImpl(vec.rotateZ((float) Math.toRadians(angle), new Vector3d()), absolute);
	}

	@Override
	public Orientation pitch(double angle) {
		return new OrientImpl(vec, absolute).pitch(angle);
	}

	@Override
	public Orientation roll(double angle) {
		return new OrientImpl(vec, absolute).roll(angle);
	}

	@Override
	public boolean like(Direction other) {
		Vector3d otherVec = other.directionVector(new Vector3d());
		return Math.abs(otherVec.sub(vec).lengthSquared()) < 10e-6;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Direction(angle=").append(degrees()).append(", ").append(vec.x).append(", ").append(vec.y)
				.append(", ").append(vec.z).append(", absolute=").append(absolute).append(")");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (absolute ? 1231 : 1237);
		result = prime * result + ((vec == null) ? 0 : vec.hashCode());
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
		if (obj instanceof DirVecImpl) {
			DirVecImpl other = (DirVecImpl) obj;
			if (absolute != other.absolute) {
				return false;
			}
			if (vec == null) {
				if (other.vec != null) {
					return false;
				}
			} else if (!vec.equals(other.vec)) {
				return false;
			}
			return true;
		} else {
			return like((Direction) obj);
//			Direction other = (Direction) obj;
//			return absolute == other.isAbsolute() //
//					&& vec.x == (float) other.dirX()//
//					&& vec.y == (float) other.dirY()//
//					&& vec.z == (float) other.dirZ();
		}
	}

	@Override
	public Direction rotateTo(Direction other) {
		return other;
	}

	@Override
	public Orientation toOrientation() {
		return new OrientImpl(vec, absolute);
	}

	@Override
	public Direction rotateTo(double angle) {
		return new DirVecImpl(angle, absolute);
	}

}
