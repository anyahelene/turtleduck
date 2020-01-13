package turtleduck.gl.objects;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class FloatMath {
	public static final float PI = (float) Math.PI;

	private FloatMath() {
	}

	public static float pi(double factor) {
		return (float) (Math.PI*factor);
	}

	public static float normalizeAngle(float a) {
		while(a < 0) {
			a += 2*PI;
		}
		while(a >= 2*PI) {
			a -= 2*PI;
		}
		return a;
	}

	public static double normalizeAngle(double a) {
		while(a < 0) {
			a += 2*Math.PI;
		}
		while(a >= 2*Math.PI) {
			a -= 2*Math.PI;
		}
		return a;
	}
	public static float max(float... values) {
		float max = values[0];
		for(float f : values) {
			max = f > max ? f : max;
		}
		return max;
	}
	public static float min(float... values) {
		float min = values[0];
		for(float f : values) {
			min = f < min ? f : min;
		}
		return min;
	}

	/**
	 * Cubic Bézier curve interpolation.
	 *
	 * <code>dest</code> may be the same object as <code>p0</code> or <code>p3</code>,
	 * but must be distinct from  <code>p1</code> and <code>p2</code>.
	 *
	 * @param p0 The start point of the curve
	 * @param p1 The first control point
	 * @param p2 The second control point
	 * @param p3 The end point of the curve
	 * @param t The point to compute, 0 <= t <= 1
	 * @param dest Destination vector
	 * @return dest – the result
	 */
	public static Vector3f bezier3(Vector3fc p0, Vector3fc p1, Vector3fc p2, Vector3fc p3, float t, Vector3f dest) {
		if(p1 == dest || p2 == dest) {
			throw new IllegalArgumentException("dest must not same object as p1 or p2");
		}
		Vector3f tmp = new Vector3f();
		p0.mul((1-t)*(1-t)*(1-t), tmp);
		p3.mul(t*t*t, dest);
		dest.add(tmp);
		p1.mul(3*(1-t)*(1-t)*t, tmp);
		dest.add(tmp);
		p2.mul(3*(1-t)*t*t, tmp);
		dest.add(tmp);

		return dest;
	}

	/**
	 * Cubic Bézier curve interpolation.
	 *
	 *
	 * @param p0 The start point of the curve
	 * @param p1 The first control point
	 * @param p2 The second control point
	 * @param p3 The end point of the curve
	 * @param t The point to compute, 0 <= t <= 1
	 * @return The result, in a new vector
	 */
	public static Vector3f bezier3(Vector3fc p0, Vector3fc p1, Vector3fc p2, Vector3fc p3, float h) {
		Vector3f v = new Vector3f();
		bezier3(p0, p1, p2, p3, h, v);
		return v;
	}
}
