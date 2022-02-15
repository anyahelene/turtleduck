package turtleduck.geometry.impl;

import org.joml.AxisAngle4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3d;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.IPoint3;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

/**
 * A 3D version of {@link Point2}
 *
 */
public class PointImpl extends Vector3d implements IPoint3, Direction {
	public static final PointImpl ZERO = new PointImpl(0, 0, 0);

	public static PointImpl create(double x, double y, double z) {
		if (x == 0 && y == 0 && z == 0)
			return ZERO;
		else
			return new PointImpl(x, y, z);
	}

	protected PointImpl(double x, double y, double z) {
		super(x, y, z);
	}

	protected PointImpl(Vector3d point) {
		super(point);
	}

	protected PointImpl() {
		super();
	}

	@Override
	public double distanceTo(PositionVector otherPoint) {
		double x = otherPoint.x() - x();
		double y = otherPoint.y() - y();
		double z = otherPoint.z() - z();
		if (z == 0)
			return Math.hypot(x, y);
		else
			return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public double distanceToSq(PositionVector otherPoint) {
		if (otherPoint instanceof PointImpl)
			return distanceSquared((Vector3dc) otherPoint);
		else
			return distanceSquared(otherPoint.x(), otherPoint.y(), otherPoint.z());
	}

	@Override
	public double asLength() {
		return length();
	}

	@Override
	public Point scale(double factor) {
		PointImpl p = new PointImpl();
		mul(factor, p);
		return p;
	}

	@Override
	public Point xyz(double newX, double newY, double newZ) {
		return new PointImpl(newX, newY, newZ);
	}

	@Override
	public double angleTo(PositionVector otherPoint) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLeftOf(PositionVector otherPoint) {
		return x < otherPoint.x();
	}

	@Override
	public boolean isAbove(PositionVector otherPoint) {
		return y < otherPoint.y();
	}

	public boolean like(Point other) {
		double dx = Math.abs(x - other.x());
		double dy = Math.abs(y - other.y());
		double dz = Math.abs(z() - other.z());
		return dx < 10e-6 && dy < 10e-6 && dz < 10e-6;
	}

	@Override
	public Point add(double deltaX, double deltaY) {
		return new PointImpl(x + deltaX, y + deltaY, z);
	}

	@Override
	public Point add(Direction dir, double distance) {
		if (distance == 0)
			return this;
		if (dir instanceof PointImpl) {
			PointImpl p = new PointImpl(this);
			p.fma(distance, (PointImpl) dir, new Vector3d());
			return p;
		}
		Vector3d vec = dir.directionVector(new Vector3d()).mul((float) distance);
		return new PointImpl(x + vec.x, y + vec.y, z + vec.z);
	}

	@Override
	public Point add(PositionVector deltaPos) {
		return new PointImpl(x + deltaPos.x(), y + deltaPos.y(), z + deltaPos.z());
	}

	@Override
	public Point sub(PositionVector deltaPos) {
		return new PointImpl(x - deltaPos.x(), y - deltaPos.y(), z - deltaPos.z());
	}

	@Override
	public IPoint3 xy(double newX, double newY) {
		return new PointImpl(newX, newY, z);
	}

	@Override
	public String toString() {
		return String.format("(%.1f,%.1f,%.1f)", x, y, z);
	}

	@Override
	public Point interpolate(Point otherPoint, double fraction) {
		if (fraction <= 0.0)
			return this;
		else if (fraction >= 1.0)
			return otherPoint;
		else
			return new PointImpl(x + (otherPoint.x() - x) * fraction, y + (otherPoint.y() - y) * fraction,
					z + (otherPoint.z() - z) * fraction);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else
			return super.equals(obj);
	}

	@Override
	public Orientation asBearing() {
		return Orientation.absoluteVec(x(), y(), z());
	}

	protected double invLength() {
		return 1.0 / Math.sqrt(Math.fma(x, x, Math.fma(y, y, z * z)));
	}

	@Override
	public Direction directionTo(PositionVector otherPoint, Direction dflt) {
		double x1 = otherPoint.x() - x;
		double y1 = otherPoint.y() - y;
		double z1 = otherPoint.z() - z;

		double l = Math.sqrt(Math.fma(x1, x1, Math.fma(y1, y1, z1 * z1)));
		double invLength = 1.0 / l;
		if (l > 0) {
			return new PointImpl(x1 * invLength, y1 * invLength, z1 * invLength);
		} else {
			return dflt;
		}
	}

	@Override
	public Vector3d toVector(Vector3d dest) {
		return dest.set(x, y, z);
	}

	@Override
	public Vector3f toVector(Vector3f dest) {
		return dest.set(x, y, z);
	}

	@Override
	public double degrees() {
		return Math.toDegrees(radians());
	}

	@Override
	public boolean isAbsolute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double radians() {
		double r = 0;
		if (x != 0 && y != 0) {
			r = Math.atan2(y, x);
			if (r < 0)
				r += 2 * Math.PI;
		}
		return r;
	}

	@Override
	public Direction add(Direction other) {
		if (other == ZERO)
			return this;
		else if (other.is3d())
			return other.add(this);
		else
			return toOrientation().add(other);
	}

	@Override
	public Direction sub(Direction other) {
		if (other == ZERO)
			return this;
		else
			return toOrientation().sub(other);
	}

	public Orientation toOrientation() {
		return new OrientImpl(new Quaterniond(new AxisAngle4d(0, x, y, z)), isAbsolute());
	}

	@Override
	public Direction interpolate(Direction other, double t) {
		if (t <= 0) {
			return this;
		} else if (t >= 1) {
			return other;
		} else if (other instanceof PointImpl) {
			PointImpl p = new PointImpl(this);
			p.lerp((Vector3d) other, t);
			return p;
		} else {
			return toOrientation().interpolate(other, t);
		}
	}

	@Override
	public String toNavString() {
		return new AngleImpl(AngleImpl.radiansToMilliArcSec(radians()), false).toNavString();

	}

	@Override
	public String toArrow() {
		return AngleImpl.toArrow(radians());

	}

	@Override
	public double dirX() {
		return x;
	}

	@Override
	public double dirY() {
		return y;
	}

	@Override
	public double dirZ() {
		return z;
	}

	@Override
	public boolean is3d() {
		return false;
	}

	@Override
	public double altDegrees() {
		return Math.toDegrees(Math.acos(-z) - Math.PI / 2);
	}

	@Override
	public Vector3d perpendicular(Vector3d dest) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3d directionVector(Vector3d dest) {
		return get(dest);
	}

	@Override
	public Vector3d normalVector(Vector3d dest) {
		dest.set(0, 0, 1);
		if (z != 0)
			dest.rotateAxis((float) (Math.acos(-z) - Math.PI / 2), (float) -y, (float) x, 0);
		return dest;
	}

	@Override
	public Direction yaw(double angle) {
		if (angle == 0)
			return this;
		return new PointImpl(rotateZ(Math.toRadians(angle), new Vector3d()));
	}

	@Override
	public Orientation pitch(double angle) {
		return toOrientation().pitch(angle);
	}

	@Override
	public Orientation roll(double angle) {
		return toOrientation().roll(angle);
	}

	@Override
	public boolean like(Direction other) {
		Vector3d otherVec = new Vector3d(other.dirX(), other.dirY(), other.dirZ());
		return Math.abs(otherVec.sub(this).lengthSquared()) < 10e-6;
	}

	@Override
	public Direction rotateTo(Direction other) {
		return other;
	}

	@Override
	public Direction rotateTo(double angle) {
		// TODO Auto-generated method stub
		return null;
	}

}
