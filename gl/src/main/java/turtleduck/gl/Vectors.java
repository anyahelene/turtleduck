package turtleduck.gl;

import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class Vectors {
	private Vectors() {
		
	}
	public static Vector3f vec3() {
		return new Vector3f();
	}

	public static Vector3f vec3(float x, float y, float z) {
		return new Vector3f(x, y, z);
	}
	public static Vector4f vec4(float x, float y, float z, float w) {
		return new Vector4f(x, y, z, w);
	}

	public static Vector3f vec3(Vector3fc v) {
		return new Vector3f(v);
	}
	public static Vector3f vec3(Vector2fc v, float z) {
		return new Vector3f(v, z);
	}
	public static Vector4f vec4(Vector4fc v) {
		return new Vector4f(v);
	}
	public static Vector4f vec4(Vector3fc v, float w) {
		return new Vector4f(v,w);
	}
	public static Vector3f xyz(Vector4fc v) {
		return new Vector3f(v.x(), v.y(), v.z());
	}


}
