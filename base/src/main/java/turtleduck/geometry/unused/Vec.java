package turtleduck.geometry.unused;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Vec<T extends Vec<T>> {
	static Vec2 vec(double x, double y) {
		return new Vec2.SimpleVec2(x, y);
	}
	static Vec3 vec(double x, double y, double z) {
		return new Vec3.SimpleVec3(x, y, z);
	}
	T scale(double a);
	T add(T v);
	T sub(T v);
	T mul(T v);
	T div(T v);
	T min(T v);
	T max(T v);
	T apply(Function<Double, Double> f);
	T apply(BiFunction<Double, Double, Double> f, T v);
	double dot(T v);
	T addScaled(double a, T v);
	T interpolate(T v, double t);
	T neg();
	T abs();
	double distanceTo(T v);
	double distanceToSq(T v);
	double get(int i);
	double length();
	double lengthSq();
	int dimensions();
	void write(double[] dest, int offset);

	
}
