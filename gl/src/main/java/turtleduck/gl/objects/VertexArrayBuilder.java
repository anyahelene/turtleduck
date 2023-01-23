package turtleduck.gl.objects;
import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import java.nio.ByteBuffer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import turtleduck.buffer.DataFormat;

import turtleduck.buffer.DataField;
import turtleduck.colors.Color;
import turtleduck.util.TextUtil;

public class VertexArrayBuilder {
	private VertexArrayFormat format;
	private DataField<?> currentField;
	private ByteBuffer data;
	private int pos = 0;
	private int start = 0;
	private int loc = 0;
	private int nVertices = 0, vertexCap = 0;
	private int vao;
	private int[] buffers = { 0, 0 }, allocated = { 0, 0 };
	private int currentBuffer = 0;
	private boolean formatted = false;
	private int allocated1 = 0, allocated2 = 0;
	private boolean ownsGlObjects = false;
	private int usage;
	private boolean DEBUG = false;

	public VertexArrayBuilder(VertexArrayFormat format, int usage, int capacity) {
		this.format = format;
		this.vao = gl.glGenVertexArrays();
		this.buffers[0] = gl.glGenBuffers();
		this.buffers[1] = gl.glGenBuffers();
		this.usage = usage == 0 ? GL_STATIC_DRAW : usage;
		ensureCapacity(capacity);
		this.ownsGlObjects = true;
		currentField = format.field(0);
	}

	public VertexArrayBuilder(VertexArrayFormat format, int usage) {
		this.format = format;
		this.vao = gl.glGenVertexArrays();
		this.buffers[0] = gl.glGenBuffers();
		this.buffers[1] = gl.glGenBuffers();
		this.usage = usage == 0 ? GL_STATIC_DRAW : usage;
		ensureCapacity(16);
		this.ownsGlObjects = true;
		currentField = format.field(0);
	}

	public void ensureCapacity(int minVertexCap) {
		if (minVertexCap > vertexCap) {
			minVertexCap = Math.max(minVertexCap, nVertices + (nVertices >> 1));
			int newCap = minVertexCap * format.numBytes();
			System.out.printf("Realloc at %d (%d vertices) to %d (%sB, %d vertices)%n", pos, vertexCap, newCap,
					TextUtil.humanFriendlyBinary(newCap), minVertexCap);
			ByteBuffer newBuf = BufferUtils.createByteBuffer(newCap);
			if (data != null) {
				data.rewind();
				newBuf.put(data);
			}
			data = newBuf;
			vertexCap = minVertexCap;
		}
	}

	public int nVertices() {
		return nVertices;
	}

	public void nextVertex() {
		if (pos != start || loc != 0) {
			throw new IllegalStateException(
					"Wrong vertex data at location " + loc + " (" + currentField + ") byte index " + pos);

		}
	}

	public void next() {
		int currentSize = pos - start;
		if (currentSize != currentField.numBytes()) {
			throw new IllegalStateException("Expected " + currentField.numBytes() + " bytes of data for for "
					+ currentField + ", got " + currentSize);
		}
		start = pos;
		loc = (loc + 1) % format.numFields();
		if (loc == 0) {
			nVertices++;
		}
		currentField = format.field(loc);
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
		writeFloat(color.alpha());
		next();
		return this;
	}

	private void writeFloat(float x) {
		switch (currentField.type()) {
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
			throw new IllegalArgumentException("Writing byte, but expected " + currentField);
		default:
			break;
		}

	}

	private void writeByte(int x) {
		if (x < 0 || x > 255) {
			throw new IllegalArgumentException("" + x);
		}
		switch (currentField.type()) {
		case UNSIGNED_BYTE:
		case NORM_BYTE:
			data.put(pos++, (byte) x);
			break;
		case FLOAT:
			writeFloat(x / 255f);
			break;
		case NORM_SHORT:
		case UNSIGNED_SHORT:
			throw new IllegalArgumentException("Writing byte, but expected " + currentField);
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
		if (currentField.type() == DataFormat.Type.FLOAT) {
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
		if (currentField.type() == DataFormat.Type.FLOAT) {
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
		if (currentField.type() == DataFormat.Type.FLOAT) {
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
		if (currentField.type() == DataFormat.Type.FLOAT) {
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
		if (currentField == null)
			currentField = format.field(loc);
		n *= currentField.numBytes();

		if (pos + n > data.capacity()) {
			ensureCapacity(nVertices + 1);
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
		int vbo = buffers[currentBuffer];
		gl.glBindVertexArray(vao);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo);
		if (!formatted) {
			format.setVertexAttributes(0);
			formatted = true;
		}
		if (pos > allocated[currentBuffer]) {
			gl.glBufferData(GL_ARRAY_BUFFER, data, usage);
			allocated[currentBuffer] = pos;
		} else {
			gl.glBufferSubData(GL_ARRAY_BUFFER, 0, data);
		}
		if (DEBUG) {
			System.out.println("Array buffer: ");
			System.out.println("  number of vertices: " + nVertices);
			System.out.println("  stride:             " + format.numBytes());
			System.out.println("  buffer:             " + data.capacity() + " (" + data.limit() + " used)");
			System.out.println("  format:             " + format);
		}
//		currentBuffer = (currentBuffer + 1) % buffers.length;
		return vbo;
	}

	public void clear() {
		data.clear();
		start = pos = loc = 0;
		nVertices = 0;
		currentField = format.field(0);
	}

	public void dispose() {
		clear();
		if (ownsGlObjects) {
			gl.glDeleteBuffers(buffers);
			gl.glDeleteVertexArrays(vao);
		}
		data = null;
	}

	public int vao() {
		return vao;
	}
}