package turtleduck.scene;

import org.joml.Matrix4f;

public interface RenderContext<C extends RenderContext<C>> {
	Matrix4f projectionMatrix();
	Matrix4f viewMatrix();
	Matrix4f projectionViewMatrix();
	Matrix4f matrix();
	C child();
}
