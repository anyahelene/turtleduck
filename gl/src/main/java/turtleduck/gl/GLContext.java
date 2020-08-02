package turtleduck.gl;

import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import turtleduck.gl.objects.ShaderProgram;

public interface GLContext {

	void perspective(float fovDegrees, float width, float height, float zNear, float zFar);

	default void orthographic(float width, float height) {
		orthographic(width, height, -1, 1);
	}

	void orthographic(float width, float height, float zNear, float zFar);

	Matrix4fc projMatrix();

	Matrix4fc viewMatrix();

	Quaternionf cameraOrientation();

	void updateProjection();

	void updateView();

	ShaderProgram shader();

	void depthTest(boolean enabled);

	void faceCulling(boolean enabled);

	void faceCulling(int faces, int winding);

	void blending(boolean enabled);

	void clear();

	Vector4f cameraPosition();
}
