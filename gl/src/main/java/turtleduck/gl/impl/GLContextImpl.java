package turtleduck.gl.impl;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import turtleduck.gl.GLContext;
import turtleduck.gl.objects.ShaderProgram;
import static org.lwjgl.opengl.GL33C.*;
import static turtleduck.gl.Vectors.vec4;

public class GLContextImpl implements GLContext {
	private static final Vector4f defaultCameraPosition = vec4(0f, 0f, 5f, 1f);
	private static final Quaternionf defaultCameraOrientation = new Quaternionf(0, 0, 0, 1);
	private static final int OPT_PROJECTION = 0, OPT_VIEW = 1, OPT_CULLING = 2, OPT_BLENDING = 3, OPT_DEPTH = 4;

	private int changed = 0;
	private int cullFace = GL_BACK;
	private int frontFace = GL_CCW;
	private boolean faceCulling = false;
	private boolean depthTest = false;
	private Matrix4fc projMatrix = null, projMatrixInv = null;
	private Matrix4fc viewMatrix = null, viewMatrixInv = null;
	private final GLContextImpl parent;
	private ShaderProgram program = null;
	private Matrix4fc projViewMatrix;
	private Quaternionf cameraOrientation;
	private Vector4f cameraPosition;

	public GLContextImpl(GLContextImpl parent) {
		this.parent = parent;
	}

	@Override
	public Matrix4fc projMatrix() {
		if (projMatrix != null)
			return projMatrix;
		else
			return parent.projMatrix();
	}

	@Override
	public Matrix4fc viewMatrix() {
		if (viewMatrix != null)
			return viewMatrix;
		else
			return parent.viewMatrix();
	}

	@Override
	public ShaderProgram shader() {
		if (program != null)
			return program;
		else
			return parent.shader();
	}

	@Override
	public void depthTest(boolean enabled) {
		boolean change = enabled != depthTest;
		if (change)
			changed |= (1 << OPT_DEPTH);

		if (parent != null) {
			parent.depthTest(enabled);
		} else if (enabled != depthTest) {
			if (enabled)
				glEnable(GL_DEPTH_TEST);
			else
				glDisable(GL_DEPTH_TEST);
		}
		depthTest = enabled;
	}

	@Override
	public void faceCulling(boolean enabled) {
		if (enabled)
			faceCulling(cullFace, frontFace);
		boolean change = enabled != faceCulling;
		if (change)
			changed |= (1 << OPT_CULLING);

		if (parent != null) {
			parent.faceCulling(false);
		} else if (change) {
			glDisable(GL_CULL_FACE);
		}
		faceCulling = enabled;
	}

	@Override
	public void faceCulling(int faces, int winding) {
		boolean change = !faceCulling || faces != cullFace || winding != frontFace;
		if (change)
			changed |= (1 << OPT_CULLING);

		if (parent != null) {
			parent.faceCulling(faces, winding);
		} else {
			if (!faceCulling)
				glEnable(GL_CULL_FACE);
			if (faces != cullFace)
				glCullFace(faces);
			if (winding != frontFace)
				glFrontFace(winding);
		}
	}

	@Override
	public void blending(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void perspective(float fov, float width, float height, float zNear, float zFar) {
		projMatrix = new Matrix4f().setPerspective((float) Math.toRadians(fov), width / height, zNear, zFar);
		projMatrixInv = projMatrix.invertPerspective(new Matrix4f());
		projViewMatrix = null;
	}

	@Override
	public void orthographic(float width, float height, float zNear, float zFar) {
		projMatrix = new Matrix4f().setOrtho(0, width, height, 0, zNear, zFar);
		projMatrixInv = projMatrix.invertOrtho(new Matrix4f());
		projViewMatrix = null;
	}

	@Override
	public Quaternionf cameraOrientation() {
		if (cameraOrientation == null) {
			if (parent != null)
				cameraOrientation = new Quaternionf(parent.cameraOrientation());
			else
				cameraOrientation = new Quaternionf(defaultCameraOrientation);
		}
		return cameraOrientation;
	}

	@Override
	public Vector4f cameraPosition() {
		if (cameraPosition == null) {
			if (parent != null)
				cameraPosition = new Vector4f(parent.cameraPosition());
			else
				cameraPosition = new Vector4f(defaultCameraPosition);
		}
		return cameraPosition;
	}

	@Override
	public void updateProjection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateView() {
		viewMatrix = cameraOrientation().get(new Matrix4f()) //
				.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
		viewMatrixInv = viewMatrix.invertAffine(new Matrix4f());

		// TODO Auto-generated method stub

	}

}
