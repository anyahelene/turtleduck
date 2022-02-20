package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL32C.*;

import java.util.Arrays;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.buffer.DataField;

public class VertexArray {
	ArrayBuffer buffer;
	private VertexArrayFormat format;
	private int vbo = 0;
	private int[] vaos;
	private long offset = -1;
	private int nVertices = 0;
	private boolean ownsBuffer = false;

	public VertexArray(VertexArrayFormat format, ArrayBuffer buffer) {
		this.format = format;
		this.buffer = buffer;
		this.vaos = new int[buffer.numBuffers()];
		glGenVertexArrays(this.vaos);
	}

	public VertexArray(VertexArrayFormat format, int usage, int capacity) {
		this.format = format;
		buffer = new ArrayBuffer(usage, format.numBytes() * capacity);
		ownsBuffer = true;
//		System.out.println(buffer);
		this.vaos = new int[buffer.numBuffers()];
		glGenVertexArrays(this.vaos);
	}

	public void setFormat() {
		for (int i = 0; i < vaos.length; i++) {
			glBindVertexArray(vaos[i]);
			glBindBuffer(GL_ARRAY_BUFFER, buffer.bufferName(i));
			format.setVertexAttributes(0);

			if (false) {
				System.out.println("Array buffer: ");
				System.out.println("  number of vertices: " + nVertices);
				System.out.println("  stride:             " + format.numBytes());
				System.out.println("  buffer size:        " + buffer);
				System.out.println("  format:             " + format);
			}
		}
	}

	public VertexArray begin() {
		long off = buffer.begin(format);
		if (offset == -1)
			offset = off;
		return this;
	}

	public <T> VertexArray put(DataField<T> field, T data) {
		buffer.put(field, data);
		return this;
	}

	public <T> VertexArray put(DataField<T> field, float x) {
		buffer.put(field, x);
		return this;
	}

	public <T> VertexArray put(DataField<T> field, float x, float y) {
		buffer.put(field, x, y);
		return this;
	}

	public <T> VertexArray put(DataField<T> field, Vector2fc xy, float z) {
		buffer.put(field, xy.x(), xy.y(), z);
		return this;
	}

	public <T> VertexArray put(DataField<T> field, float x, float y, float z) {
		buffer.put(field, x, y, z);
		return this;
	}

	public <T> VertexArray put(DataField<T> field, float x, float y, float z, float w) {
		buffer.put(field, x, y, z, w);
		return this;
	}

	public VertexArray put(DataField<Vector4f> field, Vector3f data) {
		buffer.put(field, data.x, data.y, data.z, 0);
		return this;
	}

	public VertexArray put(DataField<Vector4f> field, Vector3f data, float w) {
		buffer.put(field, data.x, data.y, data.z, w);
		return this;
	}

	public VertexArray put(DataField<Vector4f> field, Vector2f data, float z, float w) {
		buffer.put(field, data.x, data.y, z, w);
		return this;
	}

	public void end() {
		buffer.end();
		nVertices++;
	}

	public void done() {
	}

	public void clear() {
		vbo = 0;
		nVertices = 0;
		offset = -1;
		if (ownsBuffer)
			buffer.clear();
	}

	public int bind() {
		buffer.done();
		int vao = vaos[buffer.currentBufferIndex()];
//		glBindBuffer(GL_ARRAY_BUFFER, buffer.buffers[buffer.currentBufferIndex()]);
		glBindVertexArray(vao);
		return vao;
	}

	public int nVertices() {
		return nVertices;
	}

	public void dispose() {
		vbo = 0;
		nVertices = 0;
		if (ownsBuffer) {
			buffer.dispose();
		}
		buffer = null;
		glDeleteVertexArrays(vaos);
	}

	public String toString() {
		return "VertexArray(vao=" + Arrays.toString(vaos) + ", buffer=" + buffer.toString() + ", offset=" + offset
				+ ")";
	}
}
