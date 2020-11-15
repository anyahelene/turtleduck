package turtleduck.gl;

import org.joml.Matrix4f;

import turtleduck.scene.RenderContext;

public class GLRenderContext implements RenderContext<GLRenderContext> {
	public GLRenderContext() {

	}

	public GLRenderContext(GLRenderContext parent) {
		matrix.set(parent.matrix);
	}

	Matrix4f proj;
	Matrix4f view;
	Matrix4f projView;
	Matrix4f matrix = new Matrix4f();

	@Override
	public Matrix4f projectionMatrix() {
		return proj;
	}

	@Override
	public Matrix4f viewMatrix() {
		return view;
	}

	@Override
	public Matrix4f projectionViewMatrix() {
		return projView;
	}

	@Override
	public Matrix4f matrix() {
		return matrix;
	}

	@Override
	public GLRenderContext child() {
		return new GLRenderContext(this);
	}

}
