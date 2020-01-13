package turtleduck.gl.objects;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.gl.GLScreen;

public interface IModel {

	void computeTransform();

	void renderStart(GLScreen glm);

	void renderBindBuffers(GLScreen glm);

	void scale(float factor);

	void scale(float sx, float sy, float sz);

	void scaleTo(Vector3f scale);

	void move(float dx, float dy, float dz);

	void moveTo(Vector3f xyz);

	void moveTo(Vector4f pos);

	void moveTo(float x, float y, float z);

	void rotate(float x, float y, float z);

	void rotation(float x, float y, float z);

	void orientTo(Quaternionf orient);

	void pitch(float d);

	void yaw(float d);

	void roll(float d);

	void transformDirection(Vector3f v);

	void transformPosition(Vector3f v);

	void transform(Vector4f v);

	Matrix4f getTransform();

	void setParent(IModel parent);

	IModel getParent();

	void render(GLScreen glm);

	Matrix4f getNormalTransform();

	void transformDirection(Vector4f v);

}