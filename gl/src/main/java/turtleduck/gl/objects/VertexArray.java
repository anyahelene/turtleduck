package turtleduck.gl.objects;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import java.util.Arrays;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.buffer.VertexAttribute;
import turtleduck.buffer.VertexLayout;

public class VertexArray {
	ArrayBuffer buffer;
	private VertexLayout format;
	private int vbo = 0;
	private int[] vaos;
	private long offset = -1;
	private int nVertices = 0;
	private boolean ownsBuffer = false;

	public VertexArray(VertexLayout format, ArrayBuffer buffer) {
		this.format = format;
		this.buffer = buffer;
		this.vaos = new int[buffer.numBuffers()];
		gl.glGenVertexArrays(this.vaos);
	}

	public VertexArray(VertexLayout format, int usage, int capacity) {
		this.format = format;
		buffer = new ArrayBuffer(usage, format.numBytes() * capacity);
		ownsBuffer = true;
//		System.out.println(buffer);
		this.vaos = new int[buffer.numBuffers()];
		gl.glGenVertexArrays(this.vaos);
	}

	public void setFormat() {
		for (int i = 0; i < vaos.length; i++) {
			gl.glBindVertexArray(vaos[i]);
			gl.glBindBuffer(GL_ARRAY_BUFFER, buffer.bufferName(i));
			setVertexAttributes(format, 0, false);

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

	public <T> VertexArray put(VertexAttribute<T> field, T data) {
		buffer.put(field, data);
		return this;
	}

	public <T> VertexArray put(VertexAttribute<T> field, float x) {
		buffer.put(field, x);
		return this;
	}

	public <T> VertexArray put(VertexAttribute<T> field, float x, float y) {
		buffer.put(field, x, y);
		return this;
	}

	public <T> VertexArray put(VertexAttribute<T> field, Vector2fc xy, float z) {
		buffer.put(field, xy.x(), xy.y(), z);
		return this;
	}

	public <T> VertexArray put(VertexAttribute<T> field, float x, float y, float z) {
		buffer.put(field, x, y, z);
		return this;
	}

	public <T> VertexArray put(VertexAttribute<T> field, float x, float y, float z, float w) {
		buffer.put(field, x, y, z, w);
		return this;
	}

	public VertexArray put(VertexAttribute<Vector4f> field, Vector3f data) {
		buffer.put(field, data.x, data.y, data.z, 0);
		return this;
	}

	public VertexArray put(VertexAttribute<Vector4f> field, Vector3f data, float w) {
		buffer.put(field, data.x, data.y, data.z, w);
		return this;
	}

	public VertexArray put(VertexAttribute<Vector4f> field, Vector2f data, float z, float w) {
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
//		gl.glBindBuffer(GL_ARRAY_BUFFER, buffer.buffers[buffer.currentBufferIndex()]);
		gl.glBindVertexArray(vao);
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
		gl.glDeleteVertexArrays(vaos);
	}

	public String toString() {
		return "VertexArray(vao=" + Arrays.toString(vaos) + ", buffer=" + buffer.toString() + ", offset=" + offset
				+ ")";
	}
	
    public static void setVertexAttributes(VertexLayout format, long offset, boolean DEBUG) {
        int n = format.numAttributes();
        for(int i = 0; i < n; i++) {
            VertexAttribute<?> attr = format.attribute(i);
            if (DEBUG)
                System.out.println(attr.toLine());
            gl.glVertexAttribPointer(attr.location(), attr.numElements(), attr.glType(), attr.isNormalized(),
                    format.numBytes(), offset + attr.offset());
            gl.glEnableVertexAttribArray(attr.location());
        }
    }
}
