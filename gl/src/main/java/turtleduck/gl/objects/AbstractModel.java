package turtleduck.gl.objects;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.buffer.VertexLayout;
import turtleduck.gl.GLScreen;

public abstract class AbstractModel implements IModel {
	protected static final Vector3f ORIGIN = new Vector3f();
	protected Vector3f position = new Vector3f();
	protected Quaternionf orientation = new Quaternionf();
	protected Vector3f scale = new Vector3f(1, 1, 1);
	protected ShaderProgram shader;
	protected IModel parent;
	protected boolean transformNeeded = true;
	protected boolean scaleIsNonUniform = false;

	// protected Matrix4f preTransform = new Matrix4f();
	// private Matrix4f transform = new Matrix4f();
	private Matrix4f normalTransform = new Matrix4f();
	private Matrix4f transform = new Matrix4f();
	protected Matrix4f innerTransform = null;
	protected Matrix4f innerNormalTransform = null;
	private Uniform<Matrix4f> uModel;
	private Uniform<Matrix4f> uNormal;
	private Uniform<Matrix4f> uProjection;
	private Uniform<Matrix4f> uView;
	private Uniform<Vector4f> uLightPos;
	private Uniform<Vector4f> uViewPos;

	protected int vbo = 0;
	protected int ebo = 0;
	protected int vao = 0;
	private Uniform<Matrix4f> uProjView;
	private Uniform<Matrix4f> uProjViewModel;

	protected void setShader(ShaderProgram prog) {
		shader = prog;
		uModel = shader.uniform("uModel", Matrix4f.class);
		uNormal = shader.uniform("uNormal", Matrix4f.class);
		uProjection = shader.uniform("uProjection", Matrix4f.class);
		uView = shader.uniform("uView", Matrix4f.class);
		uProjView = shader.uniform("uProjView", Matrix4f.class);
		uProjViewModel = shader.uniform("uProjViewModel", Matrix4f.class);
		uLightPos = shader.uniform("uLightPos", Vector4f.class);
		uViewPos = shader.uniform("uViewPos", Vector4f.class);
		if (uView == null && uProjView == null && uProjViewModel == null) {
			System.err.println("Warning: shader program should probably have a view matrix uniform: " + prog);
		}
		if (uProjection == null && uProjView == null && uProjViewModel == null) {
			System.err.println("Warning: shader program should probably have a projection matrix uniform: " + prog);
		}
		if (uModel == null && uProjViewModel == null) {
			System.err.println("Warning: shader program should probably have a model matrix uniform: " + prog);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#computeTransform()
	 */
	@Override
	public void computeTransform() {
		transform.translationRotateScale(position, orientation, scale);
		normalTransform.translationRotateScale(ORIGIN, orientation, scale);

		if (innerTransform != null) {
			transform.mul(innerTransform);
			if (innerNormalTransform != null) {
				normalTransform.mul(innerNormalTransform);
			}
		}

		if (parent != null) {
			transform.mulLocal(parent.getTransform());
			normalTransform.mulLocal(parent.getNormalTransform());
		}

		if (scaleIsNonUniform) {
			normalTransform.invert().transpose();
		}
		transformNeeded = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#renderStart(gltest.GLMain)
	 */
	@Override
	public void renderStart(GLScreen glm) {
		if (shader == null) {
			setShader(glm.shader3d);
		}
		if (transformNeeded) {
			computeTransform();
		}
		if (uModel != null) {
			uModel.set(transform);
		}
		if (uNormal != null) {
			uNormal.set(normalTransform);
		}
		if (uProjection != null) {
			uProjection.set(glm.camera3.projectionMatrix);
		}
		if (uView != null) {
			uView.set(glm.camera3.viewMatrix);
		}
		if (uProjView != null) {
			uProjView.set(new Matrix4f(glm.camera3.projectionMatrix).mul(glm.camera3.viewMatrix));
		}
		if (uProjViewModel != null) {
			uProjViewModel.set(new Matrix4f(glm.camera3.projectionMatrix).mul(glm.camera3.viewMatrix).mul(transform));
		}
		if (uLightPos != null) {
			glm.lightPosition.w = 1;
			uLightPos.set(glm.lightPosition);
		}
		if (uViewPos != null) {
			glm.camera3.position.w = 1;
			uViewPos.set(glm.camera3.position);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#renderBindBuffers(gltest.GLMain)
	 */
	@Override
	public void renderBindBuffers(GLScreen glm) {
		if (vao != 0) {
			gl.glBindVertexArray(vao);
		} else if (vbo != 0) {
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#scale(float)
	 */
	@Override
	public void scale(float factor) {
		scale.set(factor, factor, factor);
		transformNeeded = true;
		scaleIsNonUniform = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#scale(float, float, float)
	 */
	@Override
	public void scale(float sx, float sy, float sz) {
		scale.set(sx, sy, sz);
		transformNeeded = true;
		scaleIsNonUniform = sx != sy || sx != sz || sy != sz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#scaleTo(org.joml.Vector3f)
	 */
	@Override
	public void scaleTo(Vector3f newScale) {
		scale.set(newScale);
		transformNeeded = true;
		scaleIsNonUniform = scale.x != scale.y || scale.y != scale.z || scale.z != scale.x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#move(float, float, float)
	 */
	@Override
	public void move(float dx, float dy, float dz) {
		position.add(dx, dy, dz);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#moveTo(org.joml.Vector3f)
	 */
	@Override
	public void moveTo(Vector3f xyz) {
		position.set(xyz);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#moveTo(org.joml.Vector4f)
	 */
	@Override
	public void moveTo(Vector4f pos) {
		position.set(pos.x, pos.y, pos.z);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#moveTo(float, float, float)
	 */
	@Override
	public void moveTo(float x, float y, float z) {
		position.set(x, y, z);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#rotate(float, float, float)
	 */
	@Override
	public void rotate(float x, float y, float z) {
		orientation.rotateXYZ(x, y, z);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#rotation(float, float, float)
	 */
	@Override
	public void rotation(float x, float y, float z) {
		orientation.rotationXYZ(x, y, z);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#orientTo(org.joml.Quaternionf)
	 */
	@Override
	public void orientTo(Quaternionf orient) {
		orientation.set(orient);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#pitch(float)
	 */
	@Override
	public void pitch(float d) {
		orientation.rotateX(d);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#yaw(float)
	 */
	@Override
	public void yaw(float d) {
		orientation.rotateY(d);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#roll(float)
	 */
	@Override
	public void roll(float d) {
		orientation.rotateZ(d);
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#transformDirection(org.joml.Vector3f)
	 */
	@Override
	public void transformDirection(Vector3f v) {
		if (transformNeeded) {
			computeTransform();
		}
		transform.transformDirection(v);
	}

	@Override
	public void transformDirection(Vector4f v) {
		if (transformNeeded) {
			computeTransform();
		}
		v.w = 0;
		transform.transform(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#transformPosition(org.joml.Vector3f)
	 */
	@Override
	public void transformPosition(Vector3f v) {
		if (transformNeeded) {
			computeTransform();
		}
		transform.transformPosition(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#transform(org.joml.Vector4f)
	 */
	@Override
	public void transform(Vector4f v) {
		if (transformNeeded) {
			computeTransform();
		}
		transform.transform(v);
	}

	protected VertexArrayBuilder buildVertexArray(VertexLayout format) {
		VertexArrayBuilder builder = new VertexArrayBuilder(format, GL_STATIC_DRAW);
		vao = builder.vao();
		return builder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#getTransform()
	 */
	@Override
	public Matrix4f getTransform() {
		if (transformNeeded) {
			computeTransform();
		}
		return transform;
	}

	@Override
	public Matrix4f getNormalTransform() {
		if (transformNeeded) {
			computeTransform();
		}
		return normalTransform;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#setParent(gltest.IModel)
	 */
	@Override
	public void setParent(IModel parent) {
		this.parent = parent;
		transformNeeded = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gltest.IModel#getParent()
	 */
	@Override
	public IModel getParent() {
		return parent;
	}

}
