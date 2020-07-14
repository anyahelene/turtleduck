package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public class VertexArrayBuilder {
	//		List<Float> floats = new ArrayList<>();
	List<String> names = new ArrayList<>();
	List<Integer> locations = new ArrayList<>();
	List<Integer> sizes = new ArrayList<>();
	FloatBuffer floats;
	int pos = 0;
	int vertexSize = 0;
	int start = 0;
	int loc = 0;
	int nVertices = 0;
	private int vao;
	private int vbo;
	private int usage;
boolean DEBUG = false;
	public VertexArrayBuilder(int vao, int vbo, int usage) {
		this.vao = vao;
		this.vbo = vbo;
		this.usage = usage == 0 ? GL_STATIC_DRAW : usage;
		floats = BufferUtils.createFloatBuffer(1024);
	}

	public void layout(String name, int location, int numFloats) {
		names.add(name);
		locations.add(location);
		sizes.add(numFloats);
		vertexSize += numFloats;
	}

	public int nVertices() {
		return nVertices;
	}
	public void next() {
		int currentSize = pos - start;
		if(currentSize != sizes.get(loc)) {
			throw new IllegalStateException("Expected " + sizes.get(loc) + " floats for " + names.get(loc) + ", got " + currentSize);
		}
		start = pos;
		loc = (loc + 1) % sizes.size();
		if(loc == 0) {
			nVertices++;
		}
	}

	public VertexArrayBuilder add(float x) {
		check(1);
		floats.put(pos++, x);
		return this;
	}

	public VertexArrayBuilder flt(float x) {
		check(1);
		floats.put(pos++, x);
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

	private void writeByte(int x) {
		if(x < 0 || x > 255) {
			throw new IllegalArgumentException(""+x);
		}
		floats.put(pos++, x/255f);
	}
	public VertexArrayBuilder vec2(float x, float y) {
		check(2);
		floats.put(pos++, x);
		floats.put(pos++, y);
		next();
		return this;
	}

	public VertexArrayBuilder vec3(float x, float y, float z) {
		check(3);
		floats.put(pos++, x);
		floats.put(pos++, y);
		floats.put(pos++, z);
		next();
		return this;
	}

	public VertexArrayBuilder vec4(float x, float y, float z, float w) {
		check(4);
		floats.put(pos++, x);
		floats.put(pos++, y);
		floats.put(pos++, z);
		floats.put(pos++, w);
		next();
		return this;
	}

	public VertexArrayBuilder vec2(Vector2f vec) {
		check(2);
		vec.get(pos,floats);
		pos += 2;
		next();
		return this;
	}

	public VertexArrayBuilder vec3(Vector3f vec) {
		check(3);
		vec.get(pos,floats);
		pos += 3;
		next();
		return this;
	}

	public VertexArrayBuilder vec4(Vector4f vec) {
		check(4);
		vec.get(pos,floats);
		pos += 4;
		next();
		return this;
	}

	private void check(int n) {
		if(pos + n > floats.capacity()) {
			System.out.println("Realloc at " + pos);
			FloatBuffer newBuf = BufferUtils.createFloatBuffer((int) (floats.capacity()*1.85));
			floats.rewind();
			newBuf.put(floats);
			floats = newBuf;

		}
	}
	public int bindArrayBuffer() {
		floats.rewind();
		floats.limit(pos);
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, floats, usage);
		int stride = vertexSize * 4;
		int offset = 0;
		if(DEBUG) {
		System.out.println("Array buffer: ");
		System.out.println("  floats per vertex:  " + vertexSize);
		System.out.println("  number of vertices: " + nVertices);
		System.out.println("  stride:             " + stride);
		}
		for(int i = 0; i < sizes.size(); i++) {
			int size = sizes.get(i);
			int loc = locations.get(i);
		if(DEBUG)	System.out.println("  location " + loc + ": " + size + " floats at offset " + offset);
			glVertexAttribPointer(loc, size, GL_FLOAT, false, stride, offset);
			glEnableVertexAttribArray(loc);
			offset += size*4;
		}
		return vbo;
	}
	
	public void clear() {
		floats.clear();
		start = pos = loc = 0;
		nVertices = 0;
		
	}
}