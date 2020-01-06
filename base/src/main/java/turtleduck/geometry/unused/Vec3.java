package turtleduck.geometry.unused;

import java.util.function.BiFunction;
import java.util.function.Function;


public interface Vec3 extends Vec<Vec3> {
	double x();

	double y();

	double z();

	default Vec3 x(double x) {
		return xyz(x, y(), z());
	}
	default Vec3 y(double y) {
		return xyz(x(), y, z());
	}
	default Vec3 z(double z) {
		return xyz(x(), y(), z);
	}

	Vec3 xyz(double x, double y, double z);

	default Vec3 neg() {
		return xyz(-x(), -y(), -z());
	}

	default Vec3 abs() {
		return xyz(Math.abs(x()), Math.abs(y()), Math.abs(z()));
	}

	default Vec3 interpolate(Vec3 v, double t) {
		return xyz(x() + (v.x() - x()) * t, y() + (v.y() - y()) * t, z() + (v.z() - z()) * t);
	}

	default Vec3 add(double x, double y, double z) {
		return xyz(x() + x, y() + y, z() + z);
	}

	default Vec3 sub(double x, double y, double z) {
		return xyz(x() - x, y() - y, z() - z);
	}

	default Vec3 mul(double x, double y, double z) {
		return xyz(x() * x, y() * y, z() * z);
	}

	default Vec3 div(double x, double y, double z) {
		return xyz(x() / x, y() / y, z() / z);
	}

	default Vec3 min(double x, double y, double z) {
		return xyz(Math.min(x(), x), Math.min(y(), y), Math.min(z(), z));
	}

	default Vec3 max(double x, double y, double z) {
		return xyz(Math.max(x(), x), Math.max(y(), y), Math.max(z(), z));
	}

	default double dot(double x, double y, double z) {
		return x() * x + y() * y + z() * z;
	}

	default Vec3 addScaled(double a, double x, double y, double z) {
		return xyz(Math.fma(a, x, x()), Math.fma(a, y, y()), Math.fma(a, z, z()));
	}

	default Vec3 xy0(Vec2 xy) {
		return xyz(xy.x(), xy.y(), 0);
	}

	default Vec3 xyz(Vec2 xy, double z) {
		return xyz(xy.x(), xy.y(), z);
	}

	default Vec3 xyz(Vec3 xyz) {
		return xyz(xyz.x(), xyz.y(), xyz.z());
	}

	default Vec3 add(Vec3 v) {
		return xyz(x() + v.x(), y() + v.y(), z() + v.z());
	}

	default Vec3 sub(Vec3 v) {
		return xyz(x() - v.x(), y() - v.y(), z() + v.z());
	}

	default Vec3 mul(Vec3 v) {
		return xyz(x() * v.x(), y() * v.y(), z() + v.z());
	}

	default Vec3 div(Vec3 v) {
		return xyz(x() / v.x(), y() / v.y(), z() + v.z());
	}

	default Vec3 min(Vec3 v) {
		return xyz(Math.min(x(), v.x()), Math.min(y(), v.y()), Math.max(z(), v.z()));
	}

	default Vec3 max(Vec3 v) {
		return xyz(Math.max(x(), v.x()), Math.max(y(), v.y()), Math.max(z(), v.z()));
	}

	default Vec3 apply(Function<Double, Double> f) {
		return xyz(f.apply(x()), f.apply(y()), f.apply(z()));
	}

	default Vec3 apply(BiFunction<Double, Double, Double> f, Vec3 v) {
		return xyz(f.apply(x(), v.x()), f.apply(y(), v.y()), f.apply(z(), v.z()));
	}

	default double dot(Vec3 v) {
		return x() * v.x() + y() * v.y() + z() * v.z();
	}

	default Vec3 scale(double a) {
		return xyz(a * x(), a * y(), a * z());
	}

	default Vec3 addScaled(double a, Vec3 v) {
		return xyz(Math.fma(a, v.x(), x()), Math.fma(a, v.y(), y()), Math.fma(a, v.z(), z()));
	}

	default double distanceTo(Vec3 v) {
		return Math.sqrt(distanceToSq(v));
	}

	default double distanceToSq(Vec3 v) {
		double dx = x() - v.x();
		double dy = y() - v.y();
		double dz = z() - v.z();
		return dx * dx + dy * dy + dz * dz;
	}

	default double length() {
		return Math.sqrt(lengthSq());
	}

	default double lengthSq() {
		double x = x(), y = y(), z = z();
		return x * x + y * y + z * z;
	}

	default int dimensions() {
		return 3;
	}

	static class SimpleVec3 implements Vec3 {
		private final double x, y, z;

		public SimpleVec3(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public double get(int i) {
			switch (i) {
			case 0:
				return x;
			case 1:
				return y;
			case 2:
				return z;
			default:
				throw new IndexOutOfBoundsException("" + i);
			}
		}

		@Override
		public double x() {
			return x;
		}

		@Override
		public double y() {
			return y;
		}

		@Override
		public double z() {
			return z;
		}

		@Override
		public Vec3 xyz(double x, double y, double z) {
			if (x == this.x && y == this.y && z == this.z)
				return this;
			return new SimpleVec3(x, y, z);
		}
		@Override
		public void write(double[] dest, int offset) {
			dest[offset] = x;
			dest[offset + 1] = y;
			dest[offset + 2] = y;
		}

	}

}
