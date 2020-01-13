package turtleduck.gl.objects;
import static org.lwjgl.opengl.GL40.*;

import java.nio.ByteBuffer;

import org.joml.*;
import org.lwjgl.BufferUtils;

public class Variables {
	static abstract class AbstractUniform<T> implements Uniform<T> {
		static final ByteBuffer tmpBuf = BufferUtils.createByteBuffer(4 * 4 * 8);
		protected ShaderProgram program;
		protected int loc;
		protected String name;

		@Override
		public String toString() {
			return typeName() + "("+ program + "::" + name + ", location=" + loc+ ")";
		}
	}


	// ### BEGIN GENERATED CONTENT ###
	static class Uniform1i extends AbstractUniform<Integer> {

		@Override
		public Integer get(Integer unused) {
			return glGetUniformi(program.id(), loc);
		}
		@Override
		public Integer get() {
			return glGetUniformi(program.id(), loc);
		}
		@Override
		public void set(Integer val) {
			program.bind();
			glUniform1i(loc, val);
		}

		@Override
		public String typeName() {
			return "int";
		}

		@Override
		public int typeId() {
			return GL_INT;
		}

		@Override
		public int size() {
			return 4;
		}

	}

	static class UniformVec2i extends AbstractUniform<Vector2i> {

		@Override
		public Vector2i get(Vector2i dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		@Override
		public Vector2i get() {
			tmpBuf.rewind().limit(size());
			glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector2i(tmpBuf.asIntBuffer());
		}

		@Override
		public void set(Vector2i val) {
			program.bind();
			glUniform2i(loc, val.x, val.y);
		}

		@Override
		public String typeName() {
			return "ivec2";
		}

		@Override
		public int typeId() {
			return GL_INT_VEC2;
		}

		@Override
		public int size() {
			return 8;
		}

	}

	static class UniformVec3i extends AbstractUniform<Vector3i> {

		@Override
		public Vector3i get(Vector3i dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		@Override
		public Vector3i get() {
			tmpBuf.rewind().limit(size());
			glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector3i(tmpBuf.asIntBuffer());
		}

		@Override
		public void set(Vector3i val) {
			program.bind();
			glUniform3i(loc, val.x, val.y, val.z);
		}

		@Override
		public String typeName() {
			return "ivec3";
		}

		@Override
		public int typeId() {
			return GL_INT_VEC3;
		}

		@Override
		public int size() {
			return 12;
		}

	}

	static class UniformVec4i extends AbstractUniform<Vector4i> {

		@Override
		public Vector4i get(Vector4i dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		@Override
		public Vector4i get() {
			tmpBuf.rewind().limit(size());
			glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector4i(tmpBuf.asIntBuffer());
		}

		@Override
		public void set(Vector4i val) {
			program.bind();
			glUniform4i(loc, val.x, val.y, val.z, val.w);
		}

		@Override
		public String typeName() {
			return "ivec4";
		}

		@Override
		public int typeId() {
			return GL_INT_VEC4;
		}

		@Override
		public int size() {
			return 16;
		}

	}

	static class Uniform1ui extends AbstractUniform<Integer> {

		@Override
		public Integer get(Integer unused) {
			return glGetUniformui(program.id(), loc);
		}
		@Override
		public Integer get() {
			return glGetUniformui(program.id(), loc);
		}
		@Override
		public void set(Integer val) {
			program.bind();
			glUniform1ui(loc, val);
		}

		@Override
		public String typeName() {
			return "uint";
		}

		@Override
		public int typeId() {
			return GL_UNSIGNED_INT;
		}

		@Override
		public int size() {
			return 4;
		}

	}

	static class UniformVec2ui extends AbstractUniform<Vector2i> {

		@Override
		public Vector2i get(Vector2i dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		@Override
		public Vector2i get() {
			tmpBuf.rewind().limit(size());
			glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector2i(tmpBuf.asIntBuffer());
		}

		@Override
		public void set(Vector2i val) {
			program.bind();
			glUniform2ui(loc, val.x, val.y);
		}

		@Override
		public String typeName() {
			return "uvec2";
		}

		@Override
		public int typeId() {
			return GL_UNSIGNED_INT_VEC2;
		}

		@Override
		public int size() {
			return 8;
		}

	}

	static class UniformVec3ui extends AbstractUniform<Vector3i> {

		@Override
		public Vector3i get(Vector3i dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		@Override
		public Vector3i get() {
			tmpBuf.rewind().limit(size());
			glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector3i(tmpBuf.asIntBuffer());
		}

		@Override
		public void set(Vector3i val) {
			program.bind();
			glUniform3ui(loc, val.x, val.y, val.z);
		}

		@Override
		public String typeName() {
			return "uvec3";
		}

		@Override
		public int typeId() {
			return GL_UNSIGNED_INT_VEC3;
		}

		@Override
		public int size() {
			return 12;
		}

	}

	static class UniformVec4ui extends AbstractUniform<Vector4i> {

		@Override
		public Vector4i get(Vector4i dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		@Override
		public Vector4i get() {
			tmpBuf.rewind().limit(size());
			glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector4i(tmpBuf.asIntBuffer());
		}

		@Override
		public void set(Vector4i val) {
			program.bind();
			glUniform4ui(loc, val.x, val.y, val.z, val.w);
		}

		@Override
		public String typeName() {
			return "uvec4";
		}

		@Override
		public int typeId() {
			return GL_UNSIGNED_INT_VEC4;
		}

		@Override
		public int size() {
			return 16;
		}

	}

	static class Uniform1f extends AbstractUniform<Float> {

		@Override
		public Float get(Float unused) {
			return glGetUniformf(program.id(), loc);
		}
		@Override
		public Float get() {
			return glGetUniformf(program.id(), loc);
		}
		@Override
		public void set(Float val) {
			program.bind();
			glUniform1f(loc, val);
		}

		@Override
		public String typeName() {
			return "float";
		}

		@Override
		public int typeId() {
			return GL_FLOAT;
		}

		@Override
		public int size() {
			return 4;
		}

	}

	static class UniformVec2f extends AbstractUniform<Vector2f> {

		@Override
		public Vector2f get(Vector2f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Vector2f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Vector2f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Vector2f val) {
			program.bind();
			glUniform2f(loc, val.x, val.y);
		}

		@Override
		public String typeName() {
			return "vec2";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_VEC2;
		}

		@Override
		public int size() {
			return 8;
		}

	}

	static class UniformVec3f extends AbstractUniform<Vector3f> {

		@Override
		public Vector3f get(Vector3f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Vector3f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Vector3f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Vector3f val) {
			program.bind();
			glUniform3f(loc, val.x, val.y, val.z);
		}

		@Override
		public String typeName() {
			return "vec3";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_VEC3;
		}

		@Override
		public int size() {
			return 12;
		}

	}

	static class UniformMat3x2f extends AbstractUniform<Matrix3x2f> {

		@Override
		public Matrix3x2f get(Matrix3x2f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Matrix3x2f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix3x2f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Matrix3x2f val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix3fv(loc, false, tmpBuf.asFloatBuffer());
		}

		@Override
		public String typeName() {
			return "mat3x2";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_MAT3x2;
		}

		@Override
		public int size() {
			return 24;
		}

	}

	static class UniformMat3f extends AbstractUniform<Matrix3f> {

		@Override
		public Matrix3f get(Matrix3f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Matrix3f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix3f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Matrix3f val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix3fv(loc, false, tmpBuf.asFloatBuffer());
		}

		@Override
		public String typeName() {
			return "mat3";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_MAT3;
		}

		@Override
		public int size() {
			return 36;
		}

	}

	static class UniformVec4f extends AbstractUniform<Vector4f> {

		@Override
		public Vector4f get(Vector4f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Vector4f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Vector4f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Vector4f val) {
			program.bind();
			glUniform4f(loc, val.x, val.y, val.z, val.w);
		}

		@Override
		public String typeName() {
			return "vec4";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_VEC4;
		}

		@Override
		public int size() {
			return 16;
		}

	}

	static class UniformMat4x3f extends AbstractUniform<Matrix4x3f> {

		@Override
		public Matrix4x3f get(Matrix4x3f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Matrix4x3f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix4x3f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Matrix4x3f val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix4fv(loc, false, tmpBuf.asFloatBuffer());
		}

		@Override
		public String typeName() {
			return "mat4x3";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_MAT4x3;
		}

		@Override
		public int size() {
			return 48;
		}

	}

	static class UniformMat4f extends AbstractUniform<Matrix4f> {

		@Override
		public Matrix4f get(Matrix4f dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		@Override
		public Matrix4f get() {
			tmpBuf.rewind().limit(size());
			glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix4f(tmpBuf.asFloatBuffer());
		}

		@Override
		public void set(Matrix4f val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix4fv(loc, false, tmpBuf.asFloatBuffer());
		}

		@Override
		public String typeName() {
			return "mat4";
		}

		@Override
		public int typeId() {
			return GL_FLOAT_MAT4;
		}

		@Override
		public int size() {
			return 64;
		}

	}

	static class Uniform1d extends AbstractUniform<Double> {

		@Override
		public Double get(Double unused) {
			return glGetUniformd(program.id(), loc);
		}
		@Override
		public Double get() {
			return glGetUniformd(program.id(), loc);
		}
		@Override
		public void set(Double val) {
			program.bind();
			glUniform1d(loc, val);
		}

		@Override
		public String typeName() {
			return "double";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE;
		}

		@Override
		public int size() {
			return 8;
		}

	}

	static class UniformVec2d extends AbstractUniform<Vector2d> {

		@Override
		public Vector2d get(Vector2d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Vector2d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Vector2d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Vector2d val) {
			program.bind();
			glUniform2d(loc, val.x, val.y);
		}

		@Override
		public String typeName() {
			return "dvec2";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_VEC2;
		}

		@Override
		public int size() {
			return 16;
		}

	}

	static class UniformVec3d extends AbstractUniform<Vector3d> {

		@Override
		public Vector3d get(Vector3d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Vector3d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Vector3d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Vector3d val) {
			program.bind();
			glUniform3d(loc, val.x, val.y, val.z);
		}

		@Override
		public String typeName() {
			return "dvec3";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_VEC3;
		}

		@Override
		public int size() {
			return 24;
		}

	}

	static class UniformMat3x2d extends AbstractUniform<Matrix3x2d> {

		@Override
		public Matrix3x2d get(Matrix3x2d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Matrix3x2d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix3x2d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Matrix3x2d val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix3dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		@Override
		public String typeName() {
			return "dmat3x2";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_MAT3x2;
		}

		@Override
		public int size() {
			return 48;
		}

	}

	static class UniformMat3d extends AbstractUniform<Matrix3d> {

		@Override
		public Matrix3d get(Matrix3d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Matrix3d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix3d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Matrix3d val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix3dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		@Override
		public String typeName() {
			return "dmat3";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_MAT3;
		}

		@Override
		public int size() {
			return 72;
		}

	}

	static class UniformVec4d extends AbstractUniform<Vector4d> {

		@Override
		public Vector4d get(Vector4d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Vector4d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Vector4d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Vector4d val) {
			program.bind();
			glUniform4d(loc, val.x, val.y, val.z, val.w);
		}

		@Override
		public String typeName() {
			return "dvec4";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_VEC4;
		}

		@Override
		public int size() {
			return 32;
		}

	}

	static class UniformMat4x3d extends AbstractUniform<Matrix4x3d> {

		@Override
		public Matrix4x3d get(Matrix4x3d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Matrix4x3d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix4x3d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Matrix4x3d val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix4dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		@Override
		public String typeName() {
			return "dmat4x3";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_MAT4x3;
		}

		@Override
		public int size() {
			return 96;
		}

	}

	static class UniformMat4d extends AbstractUniform<Matrix4d> {

		@Override
		public Matrix4d get(Matrix4d dest) {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		@Override
		public Matrix4d get() {
			tmpBuf.rewind().limit(size());
			glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix4d(tmpBuf.asDoubleBuffer());
		}

		@Override
		public void set(Matrix4d val)
		{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			glUniformMatrix4dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		@Override
		public String typeName() {
			return "dmat4";
		}

		@Override
		public int typeId() {
			return GL_DOUBLE_MAT4;
		}

		@Override
		public int size() {
			return 128;
		}

	}

	@SuppressWarnings("unchecked")
	protected static <T> AbstractUniform<T> createVariable(String typeName) {
		switch(typeName) {
		case "mat3":
			return (AbstractUniform<T>) new UniformMat3f();
		case "mat3x2":
			return (AbstractUniform<T>) new UniformMat3x2f();
		case "mat4x3":
			return (AbstractUniform<T>) new UniformMat4x3f();
		case "dmat3":
			return (AbstractUniform<T>) new UniformMat3d();
		case "double":
			return (AbstractUniform<T>) new Uniform1d();
		case "mat4":
			return (AbstractUniform<T>) new UniformMat4f();
		case "vec3":
			return (AbstractUniform<T>) new UniformVec3f();
		case "dmat4x3":
			return (AbstractUniform<T>) new UniformMat4x3d();
		case "vec2":
			return (AbstractUniform<T>) new UniformVec2f();
		case "dmat3x2":
			return (AbstractUniform<T>) new UniformMat3x2d();
		case "uint":
			return (AbstractUniform<T>) new Uniform1ui();
		case "float":
			return (AbstractUniform<T>) new Uniform1f();
		case "dmat4":
			return (AbstractUniform<T>) new UniformMat4d();
		case "int":
			return (AbstractUniform<T>) new Uniform1i();
		case "uvec4":
			return (AbstractUniform<T>) new UniformVec4ui();
		case "uvec3":
			return (AbstractUniform<T>) new UniformVec3ui();
		case "uvec2":
			return (AbstractUniform<T>) new UniformVec2ui();
		case "ivec3":
			return (AbstractUniform<T>) new UniformVec3i();
		case "ivec4":
			return (AbstractUniform<T>) new UniformVec4i();
		case "vec4":
			return (AbstractUniform<T>) new UniformVec4f();
		case "ivec2":
			return (AbstractUniform<T>) new UniformVec2i();
		case "dvec2":
			return (AbstractUniform<T>) new UniformVec2d();
		case "dvec3":
			return (AbstractUniform<T>) new UniformVec3d();
		case "dvec4":
			return (AbstractUniform<T>) new UniformVec4d();
		default:
			throw new IllegalArgumentException(typeName);
		}
	}

	@SuppressWarnings("unchecked")
	protected static <T> AbstractUniform<T> createVariable(int type) {
		switch(type) {
		case GL_FLOAT:
			return (AbstractUniform<T>) new Uniform1f();
		case GL_DOUBLE:
			return (AbstractUniform<T>) new Uniform1d();
		case GL_FLOAT_MAT4x3:
			return (AbstractUniform<T>) new UniformMat4x3f();
		case GL_DOUBLE_VEC4:
			return (AbstractUniform<T>) new UniformVec4d();
		case GL_FLOAT_MAT3x2:
			return (AbstractUniform<T>) new UniformMat3x2f();
		case GL_DOUBLE_VEC3:
			return (AbstractUniform<T>) new UniformVec3d();
		case GL_UNSIGNED_INT_VEC4:
			return (AbstractUniform<T>) new UniformVec4ui();
		case GL_DOUBLE_VEC2:
			return (AbstractUniform<T>) new UniformVec2d();
		case GL_UNSIGNED_INT:
			return (AbstractUniform<T>) new Uniform1ui();
		case GL_FLOAT_MAT4:
			return (AbstractUniform<T>) new UniformMat4f();
		case GL_FLOAT_MAT3:
			return (AbstractUniform<T>) new UniformMat3f();
		case GL_UNSIGNED_INT_VEC3:
			return (AbstractUniform<T>) new UniformVec3ui();
		case GL_UNSIGNED_INT_VEC2:
			return (AbstractUniform<T>) new UniformVec2ui();
		case GL_DOUBLE_MAT4:
			return (AbstractUniform<T>) new UniformMat4d();
		case GL_DOUBLE_MAT4x3:
			return (AbstractUniform<T>) new UniformMat4x3d();
		case GL_INT:
			return (AbstractUniform<T>) new Uniform1i();
		case GL_DOUBLE_MAT3x2:
			return (AbstractUniform<T>) new UniformMat3x2d();
		case GL_FLOAT_VEC2:
			return (AbstractUniform<T>) new UniformVec2f();
		case GL_INT_VEC4:
			return (AbstractUniform<T>) new UniformVec4i();
		case GL_DOUBLE_MAT3:
			return (AbstractUniform<T>) new UniformMat3d();
		case GL_INT_VEC2:
			return (AbstractUniform<T>) new UniformVec2i();
		case GL_FLOAT_VEC4:
			return (AbstractUniform<T>) new UniformVec4f();
		case GL_INT_VEC3:
			return (AbstractUniform<T>) new UniformVec3i();
		case GL_FLOAT_VEC3:
			return (AbstractUniform<T>) new UniformVec3f();
		default:
			throw new IllegalArgumentException(String.valueOf(type));
		}
	}

	@SuppressWarnings("unchecked")
	protected static <T> AbstractUniform<T> createVariable(Class<T> clazz) {
		if(clazz == null) {
			throw new IllegalArgumentException();
		} else if(clazz == Matrix4x3d.class) {
			return (AbstractUniform<T>) new UniformMat4x3d();
		} else if(clazz == Matrix3x2d.class) {
			return (AbstractUniform<T>) new UniformMat3x2d();
		} else if(clazz == Matrix4x3f.class) {
			return (AbstractUniform<T>) new UniformMat4x3f();
		} else if(clazz == Double.class) {
			return (AbstractUniform<T>) new Uniform1d();
		} else if(clazz == Matrix3x2f.class) {
			return (AbstractUniform<T>) new UniformMat3x2f();
		} else if(clazz == Integer.class) {
			return (AbstractUniform<T>) new Uniform1ui();
		} else if(clazz == Matrix4d.class) {
			return (AbstractUniform<T>) new UniformMat4d();
		} else if(clazz == Float.class) {
			return (AbstractUniform<T>) new Uniform1f();
		} else if(clazz == Matrix4f.class) {
			return (AbstractUniform<T>) new UniformMat4f();
		} else if(clazz == Vector4d.class) {
			return (AbstractUniform<T>) new UniformVec4d();
		} else if(clazz == Matrix3d.class) {
			return (AbstractUniform<T>) new UniformMat3d();
		} else if(clazz == Vector4f.class) {
			return (AbstractUniform<T>) new UniformVec4f();
		} else if(clazz == Vector2d.class) {
			return (AbstractUniform<T>) new UniformVec2d();
		} else if(clazz == Matrix3f.class) {
			return (AbstractUniform<T>) new UniformMat3f();
		} else if(clazz == Vector3d.class) {
			return (AbstractUniform<T>) new UniformVec3d();
		} else if(clazz == Vector2f.class) {
			return (AbstractUniform<T>) new UniformVec2f();
		} else if(clazz == Vector3f.class) {
			return (AbstractUniform<T>) new UniformVec3f();
		} else if(clazz == Vector3i.class) {
			return (AbstractUniform<T>) new UniformVec3ui();
		} else if(clazz == Vector4i.class) {
			return (AbstractUniform<T>) new UniformVec4ui();
		} else if(clazz == Vector2i.class) {
			return (AbstractUniform<T>) new UniformVec2ui();
		} else {
			throw new IllegalArgumentException(clazz.getName());
		}
	}

	// ### END GENERATED CONTENT ###
}
