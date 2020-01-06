package turtleduck.geometry.unused;

import java.util.function.BiFunction;
import java.util.function.Function;


public interface Vec2 extends Vec<Vec2> {
	double x();

	double y();

	default Vec2 x(double x) {
		return xy(x, y());
	}

	default Vec2 y(double y) {
		return xy(x(), y);
	}

	Vec2 xy(double x, double y);

	default Vec2 neg() {
		return xy(-x(), -y());
	}

	default Vec2 abs() {
		return xy(Math.abs(x()), Math.abs(y()));
	}

	default Vec2 interpolate(Vec2 v, double t) {
		return xy(x() + (v.x() - x()) * t, y() + (v.y() - y()) * t);
	}

	default Vec2 add(double x, double y) {
		return xy(x() + x, y() + y);
	}

	default Vec2 sub(double x, double y) {
		return xy(x() - x, y() - y);
	}

	default Vec2 mul(double x, double y) {
		return xy(x() * x, y() * y);
	}

	default Vec2 div(double x, double y) {
		return xy(x() / x, y() / y);
	}

	default Vec2 min(double x, double y) {
		return xy(Math.min(x(), x), Math.min(y(), y));
	}

	default Vec2 max(double x, double y) {
		return xy(Math.max(x(), x), Math.max(y(), y));
	}

	default Vec2 apply(Function<Double, Double> f) {
		return xy(f.apply(x()), f.apply(y()));
	}

	default Vec2 apply(BiFunction<Double, Double, Double> f, Vec2 v) {
		return xy(f.apply(x(), v.x()), f.apply(y(), v.y()));
	}

	default double dot(double x, double y) {
		return x() * x + y() * y;
	}

	default Vec2 addScaled(double a, double x, double y) {
		return xy(Math.fma(a, x, x()), Math.fma(a, y, y()));
	}

	default Vec2 xy(Vec2 xy) {
		return xy(xy.x(), xy.y());
	}

	default Vec2 add(Vec2 v) {
		return xy(x() + v.x(), y() + v.y());
	}

	default Vec2 sub(Vec2 v) {
		return xy(x() - v.x(), y() - v.y());
	}

	default Vec2 mul(Vec2 v) {
		return xy(x() * v.x(), y() * v.y());
	}

	default Vec2 div(Vec2 v) {
		return xy(x() / v.x(), y() / v.y());
	}

	default Vec2 min(Vec2 v) {
		return xy(Math.min(x(), v.x()), Math.min(y(), v.y()));
	}

	default Vec2 max(Vec2 v) {
		return xy(Math.min(x(), v.x()), Math.min(y(), v.y()));
	}

	default double dot(Vec2 v) {
		return x() * v.x() + y() * v.y();
	}

	default Vec2 scale(double a) {
		return xy(a * x(), a * y());
	}

	default Vec2 addScaled(double a, Vec2 v) {
		return xy(Math.fma(a, v.x(), x()), Math.fma(a, v.y(), y()));
	}

	default double distanceTo(Vec2 v) {
		return Math.hypot(x() - v.x(), y() - v.y());
	}

	default double distanceToSq(Vec2 v) {
		double dx = x() - v.x();
		double dy = y() - v.y();
		return dx * dx + dy * dy;
	}

	default double length() {
		return Math.hypot(x(), y());
	}

	default double lengthSq() {
		double x = x(), y = y();
		return x * x + y * y;
	}

	default int dimensions() {
		return 2;
	}

	static class VecRef2 implements Vec2 {
		private double[] refArray;
		private int offset;

		public VecRef2(double[] refArray, int offset) {
			if (refArray.length < offset + 2)
				throw new ArrayIndexOutOfBoundsException("" + offset + "+1");

			this.refArray = refArray;
			this.offset = offset;
		}

		@Override
		public double get(int i) {
			switch (i) {
			case 0:
				return refArray[offset];
			case 1:
				return refArray[offset + 1];
			default:
				throw new ArrayIndexOutOfBoundsException("" + i);
			}
		}

		@Override
		public double x() {
			return refArray[offset];
		}

		@Override
		public double y() {
			return refArray[offset + 1];
		}

		@Override
		public Vec2 xy(double x, double y) {
			refArray[offset] = x;
			refArray[offset] = y;
			return this;
		}
		@Override
		public void write(double[] dest, int off) {
			double x = refArray[offset], y = refArray[offset+1];
			dest[off] = x;
			dest[off + 1] = y;
		}

	}

	static class SimpleVec2 implements Vec2 {
		private final double x, y;

		public SimpleVec2(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public double get(int i) {
			switch (i) {
			case 0:
				return x;
			case 1:
				return y;
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
		public Vec2 xy(double x, double y) {
			if (x == this.x && y == this.y)
				return this;
			return new SimpleVec2(x, y);
		}

		@Override
		public void write(double[] dest, int offset) {
			dest[offset] = x;
			dest[offset + 1] = y;
		}

	}
}
