package turtleduck.scene;

import org.joml.Matrix4f;

public interface RenderContext {
	Matrix4f projectionMatrix();
	Matrix4f viewMatrix();
	Matrix4f projectionViewMatrix();
	Matrix4f modelMatrix();
}
