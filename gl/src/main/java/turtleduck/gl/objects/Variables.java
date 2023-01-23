package turtleduck.gl.objects;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43C;

import static turtleduck.gl.GLScreen.gl;
import turtleduck.gl.compat.GLA;

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
		
		@Override
		public int location() {
		    return loc;
		}
		
		@Override
		public boolean isDeclared() {
		    return loc >= 0;
		}
	}
	public static class TypeDesc {
		public final int id;
		public final String name;
		public final String baseType;
		public final Class<?> jomlClass;
		public final int rows;
		public final int cols;

		public TypeDesc(int id, String name, String baseType, Class<?> jomlClass, int m, int n) {
			this.id = id;
			this.name = name;
			this.baseType = baseType;
			this.jomlClass = jomlClass;
			this.rows = m;
			this.cols = n;
		}
	}

	static public final Map<Integer, TypeDesc> GL_TYPES = new HashMap<>();
	static public final Map<String, TypeDesc> GLSL_TYPES = new HashMap<>();
	static public final Map<Class<?>, TypeDesc> JOML_TYPES = new HashMap<>();

	public static String nameOf(int glTypeId) {
		TypeDesc typeDesc = GL_TYPES.get(glTypeId);
		if(typeDesc == null)
			return "void";
		else
			return typeDesc.name;
	}

	// ### BEGIN GENERATED CONTENT ###

	static {
		TypeDesc type;

		type = new TypeDesc(GLA.GL_INT, "int", "int", Integer.class, 1, 1);
		GL_TYPES.put(GLA.GL_INT, type);
		GLSL_TYPES.put("int", type);
		JOML_TYPES.put(Integer.class, type);

		type = new TypeDesc(GLA.GL_INT_VEC2, "ivec2", "int", Vector2i.class, 2, 1);
		GL_TYPES.put(GLA.GL_INT_VEC2, type);
		GLSL_TYPES.put("ivec2", type);
		JOML_TYPES.put(Vector2i.class, type);

		type = new TypeDesc(GLA.GL_INT_VEC3, "ivec3", "int", Vector3i.class, 3, 1);
		GL_TYPES.put(GLA.GL_INT_VEC3, type);
		GLSL_TYPES.put("ivec3", type);
		JOML_TYPES.put(Vector3i.class, type);

		type = new TypeDesc(GLA.GL_INT_VEC4, "ivec4", "int", Vector4i.class, 4, 1);
		GL_TYPES.put(GLA.GL_INT_VEC4, type);
		GLSL_TYPES.put("ivec4", type);
		JOML_TYPES.put(Vector4i.class, type);

		type = new TypeDesc(GLA.GL_UNSIGNED_INT, "uint", "uint", Integer.class, 1, 1);
		GL_TYPES.put(GLA.GL_UNSIGNED_INT, type);
		GLSL_TYPES.put("uint", type);
		JOML_TYPES.put(Integer.class, type);

		type = new TypeDesc(GLA.GL_UNSIGNED_INT_VEC2, "uvec2", "uint", Vector2i.class, 2, 1);
		GL_TYPES.put(GLA.GL_UNSIGNED_INT_VEC2, type);
		GLSL_TYPES.put("uvec2", type);
		JOML_TYPES.put(Vector2i.class, type);

		type = new TypeDesc(GLA.GL_UNSIGNED_INT_VEC3, "uvec3", "uint", Vector3i.class, 3, 1);
		GL_TYPES.put(GLA.GL_UNSIGNED_INT_VEC3, type);
		GLSL_TYPES.put("uvec3", type);
		JOML_TYPES.put(Vector3i.class, type);

		type = new TypeDesc(GLA.GL_UNSIGNED_INT_VEC4, "uvec4", "uint", Vector4i.class, 4, 1);
		GL_TYPES.put(GLA.GL_UNSIGNED_INT_VEC4, type);
		GLSL_TYPES.put("uvec4", type);
		JOML_TYPES.put(Vector4i.class, type);

		type = new TypeDesc(GLA.GL_FLOAT, "float", "float", Float.class, 1, 1);
		GL_TYPES.put(GLA.GL_FLOAT, type);
		GLSL_TYPES.put("float", type);
		JOML_TYPES.put(Float.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_VEC2, "vec2", "float", Vector2f.class, 2, 1);
		GL_TYPES.put(GLA.GL_FLOAT_VEC2, type);
		GLSL_TYPES.put("vec2", type);
		JOML_TYPES.put(Vector2f.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_VEC3, "vec3", "float", Vector3f.class, 3, 1);
		GL_TYPES.put(GLA.GL_FLOAT_VEC3, type);
		GLSL_TYPES.put("vec3", type);
		JOML_TYPES.put(Vector3f.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_MAT3x2, "mat3x2", "float", Matrix3x2f.class, 3, 2);
		GL_TYPES.put(GLA.GL_FLOAT_MAT3x2, type);
		GLSL_TYPES.put("mat3x2", type);
		JOML_TYPES.put(Matrix3x2f.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_MAT3, "mat3", "float", Matrix3f.class, 3, 3);
		GL_TYPES.put(GLA.GL_FLOAT_MAT3, type);
		GLSL_TYPES.put("mat3", type);
		JOML_TYPES.put(Matrix3f.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_VEC4, "vec4", "float", Vector4f.class, 4, 1);
		GL_TYPES.put(GLA.GL_FLOAT_VEC4, type);
		GLSL_TYPES.put("vec4", type);
		JOML_TYPES.put(Vector4f.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_MAT4x3, "mat4x3", "float", Matrix4x3f.class, 4, 3);
		GL_TYPES.put(GLA.GL_FLOAT_MAT4x3, type);
		GLSL_TYPES.put("mat4x3", type);
		JOML_TYPES.put(Matrix4x3f.class, type);

		type = new TypeDesc(GLA.GL_FLOAT_MAT4, "mat4", "float", Matrix4f.class, 4, 4);
		GL_TYPES.put(GLA.GL_FLOAT_MAT4, type);
		GLSL_TYPES.put("mat4", type);
		JOML_TYPES.put(Matrix4f.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE, "double", "double", Double.class, 1, 1);
		GL_TYPES.put(GL43C.GL_DOUBLE, type);
		GLSL_TYPES.put("double", type);
		JOML_TYPES.put(Double.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_VEC2, "dvec2", "double", Vector2d.class, 2, 1);
		GL_TYPES.put(GL43C.GL_DOUBLE_VEC2, type);
		GLSL_TYPES.put("dvec2", type);
		JOML_TYPES.put(Vector2d.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_VEC3, "dvec3", "double", Vector3d.class, 3, 1);
		GL_TYPES.put(GL43C.GL_DOUBLE_VEC3, type);
		GLSL_TYPES.put("dvec3", type);
		JOML_TYPES.put(Vector3d.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_MAT3x2, "dmat3x2", "double", Matrix3x2d.class, 3, 2);
		GL_TYPES.put(GL43C.GL_DOUBLE_MAT3x2, type);
		GLSL_TYPES.put("dmat3x2", type);
		JOML_TYPES.put(Matrix3x2d.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_MAT3, "dmat3", "double", Matrix3d.class, 3, 3);
		GL_TYPES.put(GL43C.GL_DOUBLE_MAT3, type);
		GLSL_TYPES.put("dmat3", type);
		JOML_TYPES.put(Matrix3d.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_VEC4, "dvec4", "double", Vector4d.class, 4, 1);
		GL_TYPES.put(GL43C.GL_DOUBLE_VEC4, type);
		GLSL_TYPES.put("dvec4", type);
		JOML_TYPES.put(Vector4d.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_MAT4x3, "dmat4x3", "double", Matrix4x3d.class, 4, 3);
		GL_TYPES.put(GL43C.GL_DOUBLE_MAT4x3, type);
		GLSL_TYPES.put("dmat4x3", type);
		JOML_TYPES.put(Matrix4x3d.class, type);

		type = new TypeDesc(GL43C.GL_DOUBLE_MAT4, "dmat4", "double", Matrix4d.class, 4, 4);
		GL_TYPES.put(GL43C.GL_DOUBLE_MAT4, type);
		GLSL_TYPES.put("dmat4", type);
		JOML_TYPES.put(Matrix4d.class, type);
	}

	static class Uniform1i extends AbstractUniform<Integer> {

		public Integer get(Integer unused) {
			return gl.glGetUniformi(program.id(), loc);
		}
		public Integer get() {
			return gl.glGetUniformi(program.id(), loc);
		}
		public void set(Integer val) {
			program.bind();
			gl.glUniform1i(loc, val);
		}

		public String typeName() {
			return "int";
		}

		public int typeId() {
			return GLA.GL_INT;
		}

		public int size() {
			return 4;
		}

	}

	static class UniformVec2i extends AbstractUniform<Vector2i> {

		public Vector2i get(Vector2i dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		public Vector2i get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector2i(tmpBuf.asIntBuffer());
		}

		public void set(Vector2i val) {
			program.bind();
			gl.glUniform2i(loc, val.x, val.y);
		}

		public String typeName() {
			return "ivec2";
		}

		public int typeId() {
			return GLA.GL_INT_VEC2;
		}

		public int size() {
			return 8;
		}

	}

	static class UniformVec3i extends AbstractUniform<Vector3i> {

		public Vector3i get(Vector3i dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		public Vector3i get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector3i(tmpBuf.asIntBuffer());
		}

		public void set(Vector3i val) {
			program.bind();
			gl.glUniform3i(loc, val.x, val.y, val.z);
		}

		public String typeName() {
			return "ivec3";
		}

		public int typeId() {
			return GLA.GL_INT_VEC3;
		}

		public int size() {
			return 12;
		}

	}

	static class UniformVec4i extends AbstractUniform<Vector4i> {

		public Vector4i get(Vector4i dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		public Vector4i get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector4i(tmpBuf.asIntBuffer());
		}

		public void set(Vector4i val) {
			program.bind();
			gl.glUniform4i(loc, val.x, val.y, val.z, val.w);
		}

		public String typeName() {
			return "ivec4";
		}

		public int typeId() {
			return GLA.GL_INT_VEC4;
		}

		public int size() {
			return 16;
		}

	}

	static class Uniform1ui extends AbstractUniform<Integer> {

		public Integer get(Integer unused) {
			return gl.glGetUniformui(program.id(), loc);
		}
		public Integer get() {
			return gl.glGetUniformui(program.id(), loc);
		}
		public void set(Integer val) {
			program.bind();
			gl.glUniform1ui(loc, val);
		}

		public String typeName() {
			return "uint";
		}

		public int typeId() {
			return GLA.GL_UNSIGNED_INT;
		}

		public int size() {
			return 4;
		}

	}

	static class UniformVec2ui extends AbstractUniform<Vector2i> {

		public Vector2i get(Vector2i dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		public Vector2i get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector2i(tmpBuf.asIntBuffer());
		}

		public void set(Vector2i val) {
			program.bind();
			gl.glUniform2ui(loc, val.x, val.y);
		}

		public String typeName() {
			return "uvec2";
		}

		public int typeId() {
			return GLA.GL_UNSIGNED_INT_VEC2;
		}

		public int size() {
			return 8;
		}

	}

	static class UniformVec3ui extends AbstractUniform<Vector3i> {

		public Vector3i get(Vector3i dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		public Vector3i get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector3i(tmpBuf.asIntBuffer());
		}

		public void set(Vector3i val) {
			program.bind();
			gl.glUniform3ui(loc, val.x, val.y, val.z);
		}

		public String typeName() {
			return "uvec3";
		}

		public int typeId() {
			return GLA.GL_UNSIGNED_INT_VEC3;
		}

		public int size() {
			return 12;
		}

	}

	static class UniformVec4ui extends AbstractUniform<Vector4i> {

		public Vector4i get(Vector4i dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return dest.set(tmpBuf.asIntBuffer());
		}

		public Vector4i get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformuiv(program.id(), loc, tmpBuf.asIntBuffer());
			return new Vector4i(tmpBuf.asIntBuffer());
		}

		public void set(Vector4i val) {
			program.bind();
			gl.glUniform4ui(loc, val.x, val.y, val.z, val.w);
		}

		public String typeName() {
			return "uvec4";
		}

		public int typeId() {
			return GLA.GL_UNSIGNED_INT_VEC4;
		}

		public int size() {
			return 16;
		}

	}

	static class Uniform1f extends AbstractUniform<Float> {

		public Float get(Float unused) {
			return gl.glGetUniformf(program.id(), loc);
		}
		public Float get() {
			return gl.glGetUniformf(program.id(), loc);
		}
		public void set(Float val) {
			program.bind();
			gl.glUniform1f(loc, val);
		}

		public String typeName() {
			return "float";
		}

		public int typeId() {
			return GLA.GL_FLOAT;
		}

		public int size() {
			return 4;
		}

	}

	static class UniformVec2f extends AbstractUniform<Vector2f> {

		public Vector2f get(Vector2f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Vector2f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Vector2f(tmpBuf.asFloatBuffer());
		}

		public void set(Vector2f val) {
			program.bind();
			gl.glUniform2f(loc, val.x, val.y);
		}

		public String typeName() {
			return "vec2";
		}

		public int typeId() {
			return GLA.GL_FLOAT_VEC2;
		}

		public int size() {
			return 8;
		}

	}

	static class UniformVec3f extends AbstractUniform<Vector3f> {

		public Vector3f get(Vector3f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Vector3f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Vector3f(tmpBuf.asFloatBuffer());
		}

		public void set(Vector3f val) {
			program.bind();
			gl.glUniform3f(loc, val.x, val.y, val.z);
		}

		public String typeName() {
			return "vec3";
		}

		public int typeId() {
			return GLA.GL_FLOAT_VEC3;
		}

		public int size() {
			return 12;
		}

	}

	static class UniformMat3x2f extends AbstractUniform<Matrix3x2f> {

		public Matrix3x2f get(Matrix3x2f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Matrix3x2f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix3x2f(tmpBuf.asFloatBuffer());
		}

		public void set(Matrix3x2f val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			gl.glUniformMatrix3fv(loc, false, tmpBuf.asFloatBuffer());
		}

		public String typeName() {
			return "mat3x2";
		}

		public int typeId() {
			return GLA.GL_FLOAT_MAT3x2;
		}

		public int size() {
			return 24;
		}

	}

	static class UniformMat3f extends AbstractUniform<Matrix3f> {

		public Matrix3f get(Matrix3f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Matrix3f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix3f(tmpBuf.asFloatBuffer());
		}

		public void set(Matrix3f val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			gl.glUniformMatrix3fv(loc, false, tmpBuf.asFloatBuffer());
		}

		public String typeName() {
			return "mat3";
		}

		public int typeId() {
			return GLA.GL_FLOAT_MAT3;
		}

		public int size() {
			return 36;
		}

	}

	static class UniformVec4f extends AbstractUniform<Vector4f> {

		public Vector4f get(Vector4f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Vector4f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Vector4f(tmpBuf.asFloatBuffer());
		}

		public void set(Vector4f val) {
			program.bind();
			gl.glUniform4f(loc, val.x, val.y, val.z, val.w);
		}

		public String typeName() {
			return "vec4";
		}

		public int typeId() {
			return GLA.GL_FLOAT_VEC4;
		}

		public int size() {
			return 16;
		}

	}

	static class UniformMat4x3f extends AbstractUniform<Matrix4x3f> {

		public Matrix4x3f get(Matrix4x3f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Matrix4x3f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix4x3f(tmpBuf.asFloatBuffer());
		}

		public void set(Matrix4x3f val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			gl.glUniformMatrix4fv(loc, false, tmpBuf.asFloatBuffer());
		}

		public String typeName() {
			return "mat4x3";
		}

		public int typeId() {
			return GLA.GL_FLOAT_MAT4x3;
		}

		public int size() {
			return 48;
		}

	}

	static class UniformMat4f extends AbstractUniform<Matrix4f> {

		public Matrix4f get(Matrix4f dest) {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return dest.set(tmpBuf.asFloatBuffer());
		}

		public Matrix4f get() {
			tmpBuf.rewind().limit(size());
			gl.glGetUniformfv(program.id(), loc, tmpBuf.asFloatBuffer());
			return new Matrix4f(tmpBuf.asFloatBuffer());
		}

		public void set(Matrix4f val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			gl.glUniformMatrix4fv(loc, false, tmpBuf.asFloatBuffer());
		}

		public String typeName() {
			return "mat4";
		}

		public int typeId() {
			return GLA.GL_FLOAT_MAT4;
		}

		public int size() {
			return 64;
		}

	}

	static class Uniform1d extends AbstractUniform<Double> {

		public Double get(Double unused) {
			return GL43C.glGetUniformd(program.id(), loc);
		}
		public Double get() {
			return GL43C.glGetUniformd(program.id(), loc);
		}
		public void set(Double val) {
			program.bind();
			GL43C.glUniform1d(loc, val);
		}

		public String typeName() {
			return "double";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE;
		}

		public int size() {
			return 8;
		}

	}

	static class UniformVec2d extends AbstractUniform<Vector2d> {

		public Vector2d get(Vector2d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Vector2d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Vector2d(tmpBuf.asDoubleBuffer());
		}

		public void set(Vector2d val) {
			program.bind();
			GL43C.glUniform2d(loc, val.x, val.y);
		}

		public String typeName() {
			return "dvec2";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_VEC2;
		}

		public int size() {
			return 16;
		}

	}

	static class UniformVec3d extends AbstractUniform<Vector3d> {

		public Vector3d get(Vector3d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Vector3d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Vector3d(tmpBuf.asDoubleBuffer());
		}

		public void set(Vector3d val) {
			program.bind();
			GL43C.glUniform3d(loc, val.x, val.y, val.z);
		}

		public String typeName() {
			return "dvec3";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_VEC3;
		}

		public int size() {
			return 24;
		}

	}

	static class UniformMat3x2d extends AbstractUniform<Matrix3x2d> {

		public Matrix3x2d get(Matrix3x2d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Matrix3x2d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix3x2d(tmpBuf.asDoubleBuffer());
		}

		public void set(Matrix3x2d val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			GL43C.glUniformMatrix3dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		public String typeName() {
			return "dmat3x2";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_MAT3x2;
		}

		public int size() {
			return 48;
		}

	}

	static class UniformMat3d extends AbstractUniform<Matrix3d> {

		public Matrix3d get(Matrix3d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Matrix3d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix3d(tmpBuf.asDoubleBuffer());
		}

		public void set(Matrix3d val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			GL43C.glUniformMatrix3dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		public String typeName() {
			return "dmat3";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_MAT3;
		}

		public int size() {
			return 72;
		}

	}

	static class UniformVec4d extends AbstractUniform<Vector4d> {

		public Vector4d get(Vector4d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Vector4d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Vector4d(tmpBuf.asDoubleBuffer());
		}

		public void set(Vector4d val) {
			program.bind();
			GL43C.glUniform4d(loc, val.x, val.y, val.z, val.w);
		}

		public String typeName() {
			return "dvec4";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_VEC4;
		}

		public int size() {
			return 32;
		}

	}

	static class UniformMat4x3d extends AbstractUniform<Matrix4x3d> {

		public Matrix4x3d get(Matrix4x3d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Matrix4x3d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix4x3d(tmpBuf.asDoubleBuffer());
		}

		public void set(Matrix4x3d val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			GL43C.glUniformMatrix4dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		public String typeName() {
			return "dmat4x3";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_MAT4x3;
		}

		public int size() {
			return 96;
		}

	}

	static class UniformMat4d extends AbstractUniform<Matrix4d> {

		public Matrix4d get(Matrix4d dest) {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return dest.set(tmpBuf.asDoubleBuffer());
		}

		public Matrix4d get() {
			tmpBuf.rewind().limit(size());
			GL43C.glGetUniformdv(program.id(), loc, tmpBuf.asDoubleBuffer());
			return new Matrix4d(tmpBuf.asDoubleBuffer());
		}

		public void set(Matrix4d val)
	{
			val.get(tmpBuf);
			tmpBuf.limit(size());
			program.bind();
			GL43C.glUniformMatrix4dv(loc, false, tmpBuf.asDoubleBuffer());
		}

		public String typeName() {
			return "dmat4";
		}

		public int typeId() {
			return GL43C.GL_DOUBLE_MAT4;
		}

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
		case GLA.GL_INT_VEC4:
			return (AbstractUniform<T>) new UniformVec4i();
		case GLA.GL_FLOAT_MAT3:
			return (AbstractUniform<T>) new UniformMat3f();
		case GLA.GL_FLOAT_MAT4:
			return (AbstractUniform<T>) new UniformMat4f();
		case GL43C.GL_DOUBLE_VEC3:
			return (AbstractUniform<T>) new UniformVec3d();
		case GL43C.GL_DOUBLE_VEC4:
			return (AbstractUniform<T>) new UniformVec4d();
		case GL43C.GL_DOUBLE_MAT4x3:
			return (AbstractUniform<T>) new UniformMat4x3d();
		case GL43C.GL_DOUBLE_VEC2:
			return (AbstractUniform<T>) new UniformVec2d();
		case GL43C.GL_DOUBLE_MAT3x2:
			return (AbstractUniform<T>) new UniformMat3x2d();
		case GLA.GL_INT_VEC2:
			return (AbstractUniform<T>) new UniformVec2i();
		case GLA.GL_INT_VEC3:
			return (AbstractUniform<T>) new UniformVec3i();
		case GL43C.GL_DOUBLE_MAT3:
			return (AbstractUniform<T>) new UniformMat3d();
		case GL43C.GL_DOUBLE_MAT4:
			return (AbstractUniform<T>) new UniformMat4d();
		case GL43C.GL_DOUBLE:
			return (AbstractUniform<T>) new Uniform1d();
		case GLA.GL_UNSIGNED_INT:
			return (AbstractUniform<T>) new Uniform1ui();
		case GLA.GL_UNSIGNED_INT_VEC3:
			return (AbstractUniform<T>) new UniformVec3ui();
		case GLA.GL_FLOAT_VEC3:
			return (AbstractUniform<T>) new UniformVec3f();
		case GLA.GL_FLOAT_MAT4x3:
			return (AbstractUniform<T>) new UniformMat4x3f();
		case GLA.GL_UNSIGNED_INT_VEC2:
			return (AbstractUniform<T>) new UniformVec2ui();
		case GLA.GL_FLOAT_VEC4:
			return (AbstractUniform<T>) new UniformVec4f();
		case GLA.GL_FLOAT_MAT3x2:
			return (AbstractUniform<T>) new UniformMat3x2f();
		case GLA.GL_UNSIGNED_INT_VEC4:
			return (AbstractUniform<T>) new UniformVec4ui();
		case GLA.GL_FLOAT:
			return (AbstractUniform<T>) new Uniform1f();
		case GLA.GL_FLOAT_VEC2:
			return (AbstractUniform<T>) new UniformVec2f();
		case GLA.GL_INT:
			return (AbstractUniform<T>) new Uniform1i();
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
