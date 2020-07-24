package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL40.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import turtleduck.colors.Color;
import turtleduck.util.TextUtil;

public class VertexArrayBuilder {
	enum Type {
		FLOAT(4, GL_FLOAT, false), NORM_BYTE(1, GL_UNSIGNED_BYTE, true), NORM_SHORT(2, GL_UNSIGNED_SHORT, true),
		UNSIGNED_BYTE(1, GL_UNSIGNED_BYTE, false), UNSIGNED_SHORT(2, GL_UNSIGNED_SHORT, false);

		protected int bytes;
		protected int glType;
		protected boolean norm;

		Type(int bytes, int glType, boolean norm) {
			this.bytes = bytes;
			this.glType = glType;
			this.norm = norm;
		}
	};

	// List<Float> floats = new ArrayList<>();
	List<String> names = new ArrayList<>();
	List<Integer> locations = new ArrayList<>();
	List<Integer> sizes = new ArrayList<>();
	List<Type> types = new ArrayList<>();
	Type type = null;
	ByteBuffer data;
	int pos = 0;
	int vertexSize = 0;
	int start = 0;
	int loc = 0;
	int nVertices = 0;
	private int vao;
	private int vbo;
	private int usage;
	boolean DEBUG = false;
	private final int BUFFER_PAGE_SIZE = 1024;
	private final int BUFFER_PAGE_MASK = BUFFER_PAGE_SIZE - 1;

	public VertexArrayBuilder(int vao, int vbo, int usage) {
		this.vao = vao;
		this.vbo = vbo;
		this.usage = usage == 0 ? GL_STATIC_DRAW : usage;
		data = BufferUtils.createByteBuffer(BUFFER_PAGE_SIZE);
	}

	public void layoutFloat(String name, int location, int numFloats) {
		names.add(name);
		locations.add(location);
		sizes.add(numFloats * 4);
		types.add(Type.FLOAT);
		vertexSize += numFloats * 4;
	}

	public void layoutNormByte(String name, int location, int numComponents) {
		names.add(name);
		locations.add(location);
		sizes.add(numComponents);
		types.add(Type.NORM_BYTE);
		vertexSize += numComponents;
	}

	public void layoutNormShort(String name, int location, int numComponents) {
		names.add(name);
		locations.add(location);
		sizes.add(numComponents * 2);
		types.add(Type.NORM_SHORT);
		vertexSize += numComponents * 2;
	}

	public int nVertices() {
		return nVertices;
	}

	public void nextVertex() {
		if (pos != start || loc != 0) {
			throw new IllegalStateException("Wrong vertex data at location " + loc + " (" + type + " " + names.get(loc)
					+ ") byte index " + pos);

		}
	}

	public void next() {
		int currentSize = pos - start;
		if (currentSize != sizes.get(loc)) {
			throw new IllegalStateException(
					"Expected " + sizes.get(loc) + " bytes of data for for " + names.get(loc) + ", got " + currentSize);
		}
		start = pos;
		loc = (loc + 1) % sizes.size();
		type = types.get(loc);
		if (loc == 0) {
			nVertices++;
		}
	}

	public VertexArrayBuilder flt(float x) {
		check(1);
		writeFloat(x);
		next();
		return this;
	}

	public VertexArrayBuilder uint8(int r) {
		check(1);
		writeByte(r);
		next();
		return this;
	}

	public VertexArrayBuilder uint8(int r, int g, int b) {
		check(3);
		writeByte(r);
		writeByte(b);
		writeByte(b);
		next();
		return this;
	}

	public VertexArrayBuilder uint8(int r, int g, int b, int a) {
		check(4);
		writeByte(r);
		writeByte(b);
		writeByte(b);
		writeByte(a);
		next();
		return this;
	}

	public VertexArrayBuilder color(Color color) {
		check(4 * 4);
		writeFloat(color.red());
		writeFloat(color.green());
		writeFloat(color.blue());
		writeFloat(color.opacity());
		next();
		return this;
	}

	private void writeFloat(float x) {
		switch (types.get(loc)) {
		case FLOAT:
			data.putFloat(pos, x);
			pos += 4;
			break;
		case NORM_BYTE:
			if (x < 0 || x > 1) {
				throw new IllegalArgumentException("Must be 0 <= x <= 1: " + x);
			}
			data.put(pos++, (byte) Math.round(x * 255));
			break;
		case NORM_SHORT:
			if (x < 0 || x > 1) {
				throw new IllegalArgumentException("Must be 0 <= x <= 1: " + x);
			}
			data.putShort(pos, (short) Math.round(x * 65535));
			pos += 2;
			break;
		case UNSIGNED_BYTE:
		case UNSIGNED_SHORT:
			throw new IllegalArgumentException("Writing byte, but expected " + types.get(loc));
		default:
			break;
		}

	}

	private void writeByte(int x) {
		if (x < 0 || x > 255) {
			throw new IllegalArgumentException("" + x);
		}
		switch (types.get(loc)) {
		case UNSIGNED_BYTE:
		case NORM_BYTE:
			data.put(pos++, (byte) x);
			break;
		case FLOAT:
			writeFloat(x / 255f);
			break;
		case NORM_SHORT:
		case UNSIGNED_SHORT:
			throw new IllegalArgumentException("Writing byte, but expected " + types.get(loc));
		default:
			break;
		}
	}

	public VertexArrayBuilder vec2(float x, float y) {
		check(2);
		writeFloat(x);
		writeFloat(y);
		next();
		return this;
	}

	public VertexArrayBuilder vec3(float x, float y, float z) {
		check(3);
		writeFloat(x);
		writeFloat(y);
		writeFloat(z);
		next();
		return this;
	}

	public VertexArrayBuilder vec4(float x, float y, float z, float w) {
		check(4);
		writeFloat(x);
		writeFloat(y);
		writeFloat(z);
		writeFloat(w);
		next();
		return this;
	}

	public VertexArrayBuilder vec2(Vector2f vec) {
		if (type == Type.FLOAT) {
			check(2);
			vec.get(pos, data);
			pos += 2 * 4;
			next();
			return this;
		} else {
			return vec2(vec.x, vec.y);
		}
	}

	public VertexArrayBuilder vec3(Vector2f vec, float z) {
		if (type == Type.FLOAT) {
			check(3);
			vec.get(pos, data);
			pos += 2 * 4;
			writeFloat(z);
			next();
			return this;
		} else {
			return vec3(vec.x, vec.y, z);
		}
	}

	public VertexArrayBuilder vec3(Vector3f vec) {
		if (type == Type.FLOAT) {
			check(3);
			vec.get(pos, data);
			pos += 3 * 4;
			next();
			return this;
		} else {
			return vec3(vec.x, vec.y, vec.z);
		}
	}

	public VertexArrayBuilder vec4(Vector4f vec) {
		if (type == Type.FLOAT) {
			check(4);
			vec.get(pos, data);
			pos += 4 * 4;
			next();
			return this;
		} else {
			return vec4(vec.x, vec.y, vec.z, vec.w);
		}
	}

	private void check(int n) {
		if (type == null)
			type = types.get(loc);
		n *= type.bytes;

		if (pos + n > data.capacity()) {
			int newCap = data.capacity();
			newCap = Math.max(pos + n, newCap + (newCap >> 1));
			if((newCap & BUFFER_PAGE_MASK) > 0)
				newCap = (newCap & ~BUFFER_PAGE_MASK) + BUFFER_PAGE_SIZE;
			
			System.out.printf("Realloc at %d to %d (%sB)%n", pos, newCap, TextUtil.humanFriendlyBinary(newCap));
			ByteBuffer newBuf = BufferUtils.createByteBuffer(newCap);
			data.rewind();
			newBuf.put(data);
			data = newBuf;
		}
	}

	public void trim() {
		if (pos != data.capacity()) {
			System.out.printf("Realloc at %d to %d (%sB)%n", data.capacity(), pos, TextUtil.humanFriendlyBinary(pos));
			ByteBuffer newBuf = BufferUtils.createByteBuffer(pos);
			data.rewind();
			newBuf.put(data);
			data = newBuf;
		}
	}

	public int bindArrayBuffer() {
		data.rewind();
		data.limit(pos);
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, data, usage);
		int stride = vertexSize;
		int offset = 0;
		if (DEBUG) {
			System.out.println("Array buffer: " + types + sizes + locations);
			System.out.println("  number of vertices: " + nVertices);
			System.out.println("  stride:             " + stride);
			System.out.println("  buffer size:        " + data.capacity() + " (" + data.limit() + " used)");
		}
		for (int i = 0; i < sizes.size(); i++) {
			int size = sizes.get(i);
			int loc = locations.get(i);
			Type type = types.get(i);
			if (DEBUG)
				System.out.printf("  location %d: %d %s at offset %d%n", loc, size / type.bytes, type, offset);
			glVertexAttribPointer(loc, size / type.bytes, type.glType, type.norm, stride, offset);
			glEnableVertexAttribArray(loc);
			offset += size;
		}
		return vbo;
	}

	public void clear() {
		data.clear();
		start = pos = loc = 0;
		nVertices = 0;
		type = types.get(loc);

	}
}