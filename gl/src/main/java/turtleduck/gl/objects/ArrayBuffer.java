package turtleduck.gl.objects;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL43C;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;

import turtleduck.buffer.VertexAttribute;
import turtleduck.buffer.VertexLayout;
import turtleduck.gl.GLScreen;
import turtleduck.util.TextUtil;

public class ArrayBuffer {
	int[] buffers = { 0, 0 };
	long[] bufferSizes = { 0, 0 };
	boolean[] isMapped = { false, false };
	int usage = GL_STREAM_DRAW;
	int bufferIndex = 0;
	long pos = 0;
	ByteBuffer buffer;
	VertexLayout current;
	int currentField = 0;
	private int initCapacity;
	int mapBits = GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_RANGE_BIT | GL_MAP_UNSYNCHRONIZED_BIT; // GL_MAP_READ_BIT
	boolean debug = false;

	public ArrayBuffer(int usage, int capacity) {
		gl.glGenBuffers(buffers);
		this.usage = usage == 0 ? GL_STATIC_DRAW : usage;
		this.initCapacity = capacity == 0 ? 1024 : capacity;
	}

	public void dispose() {
	    gl.glDeleteBuffers(buffers);
		buffers[0] = 0;
		buffers[1] = 0;
	}

	public long begin(VertexLayout format) {
		current = format;
		currentField = 0;
		ensureSpaceFor(format.numBytes());
		if (!isMapped[bufferIndex]) {
			if (debug)
				System.out.print("map..." + buffers[bufferIndex]);
			gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
			buffer = gl.glMapBufferRange(GL_ARRAY_BUFFER, pos, bufferSizes[bufferIndex] - pos, mapBits, buffer);
			if (buffer == null) {
				throw new OutOfMemoryError("couldn't map buffer");
			} else {
				isMapped[bufferIndex] = true;
			}
		}
		return pos;
	}

	private void ensureSpaceFor(int numBytes) {
		long capacity = bufferSizes[bufferIndex];
		long newCap = pos + numBytes;
		if (capacity < newCap) {
//			System.out.println("Current capacity: " + capacity + ", pos=" + pos + ", needed=" + numBytes + ", newCap="
//					+ (newCap) + ", " + buffer);
			newCap = Math.max(initCapacity, newCap + (newCap >> 1));
			System.out.printf("Realloc %d at pos %d size %d ", buffers[bufferIndex], pos, capacity);

			int oldIndex = bufferIndex;
			if (pos > 0)
				bufferIndex = (bufferIndex + 1) % buffers.length;
			gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
			gl.glBufferData(GL_ARRAY_BUFFER, newCap, usage);
			bufferSizes[bufferIndex] = newCap;
			System.out.printf("to %d size %d (%sB)", buffers[bufferIndex], newCap,
					TextUtil.humanFriendlyBinary(newCap));
			if (pos > 0) {
				gl.glBindBuffer(GL_COPY_READ_BUFFER, buffers[oldIndex]);
				if (isMapped[oldIndex]) {
					gl.glUnmapBuffer(GL_COPY_READ_BUFFER);
					isMapped[oldIndex] = false;
				}
				gl.glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_ARRAY_BUFFER, 0, 0, pos);
				gl.glBufferData(GL_COPY_READ_BUFFER, newCap, usage);
				bufferSizes[oldIndex] = newCap;
				//if (GLScreen.glMajor >= 4 && GLScreen.glMinor >= 3)
				//    GL43C.glInvalidateBufferData(buffers[oldIndex]); // TODO
				gl.glBindBuffer(GL_COPY_READ_BUFFER, 0);
//				dump(current);
				System.out.printf(", %sB copied", TextUtil.humanFriendlyBinary(pos));
			}
			System.out.println();
			gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
			buffer = gl.glMapBufferRange(GL_ARRAY_BUFFER, pos, newCap - pos, mapBits, buffer);
			if (buffer == null) {
				throw new OutOfMemoryError("couldn't map buffer");
			} else {
				isMapped[bufferIndex] = true;
			}
			gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
		}
	}

	public <T> void put(VertexAttribute<T> field, T data) {
		checkWrite(field);
		field.write(buffer, data);
		pos += field.numBytes();
		currentField++;
	}

	private <T> void checkWrite(VertexAttribute<T> field) {
		if (!isMapped[bufferIndex]) {
			throw new IllegalStateException("Writing after done()");
		}
		if (field != current.attribute(currentField)) {
			if (!field.equals(current.attribute(currentField)))
				throw new IllegalStateException(
						"Expected data for " + current.attribute(currentField) + " but got " + field);
		}
	}

	public <T> void put(VertexAttribute<T> field, float x) {
		checkWrite(field);
		field.write(buffer, x);
		pos += field.numBytes();
		currentField++;
	}

	public <T> void put(VertexAttribute<T> field, float x, float y) {
		checkWrite(field);
		field.write(buffer, x, y);
		pos += field.numBytes();
		currentField++;
	}

	public <T> void put(VertexAttribute<T> field, float x, float y, float z) {
		checkWrite(field);
		field.write(buffer, x, y, z);
		pos += field.numBytes();
		currentField++;
	}

	public <T> void put(VertexAttribute<T> field, float x, float y, float z, float w) {
		checkWrite(field);
		field.write(buffer, x, y, z, w);
		pos += field.numBytes();
		currentField++;
	}

	public long end() {
		if (!isMapped[bufferIndex]) {
			throw new IllegalStateException("Writing after done()");
		}
		if (currentField != current.numAttributes()) {
		    System.err.println(current);
			throw new IllegalStateException(
					"Expected " + current.numAttributes() + " data fields, but got " + currentField);
		}
		current = null;
		return pos;
	}

	public int done() {
		if (isMapped[bufferIndex]) {
			gl.glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
			gl.glUnmapBuffer(GL_ARRAY_BUFFER);
			gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
			isMapped[bufferIndex] = false;
		}
		return buffers[bufferIndex];
	}

	public void dump(VertexLayout format, int maxVertices) {
		if (format == null)
			format = current;
		gl.glBindBuffer(GL_COPY_READ_BUFFER, buffers[bufferIndex]);
		ByteBuffer b = gl.glMapBufferRange(GL_COPY_READ_BUFFER, 0, bufferSizes[bufferIndex], GL_MAP_READ_BIT, null);
		int i = 0, j = 0;
		while (b.remaining() >= format.numBytes()) {
			VertexAttribute<?> field = format.attribute(i++);
			System.out.print(field.read(b) + " ");
			if (i >= format.numAttributes()) {
				i = 0;
				j++;
				System.out.println();
			}
			if (maxVertices > 0 && j > maxVertices)
				break;
		}
		gl.glUnmapBuffer(GL_COPY_READ_BUFFER);

	}

	public void clear() {
		done();
		if (debug)
			System.out.print("clear..." + buffers[bufferIndex]);
		gl.glBindBuffer(GL_COPY_READ_BUFFER, buffers[bufferIndex]);
		if (isMapped[bufferIndex]) {
			gl.glUnmapBuffer(GL_COPY_READ_BUFFER);
			isMapped[bufferIndex] = false;
		}
		//if (GLScreen.glMajor >= 4 && GLScreen.glMinor >= 3)
		//	GL43C.glInvalidateBufferData(buffers[bufferIndex]); //TODO
		gl.glBindBuffer(GL_COPY_READ_BUFFER, 0);
		pos = 0;
		current = null;
		bufferIndex = (bufferIndex + 1) % buffers.length;
		if (debug)
			System.out.println("done");
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ArrayBuffer(pos=").append(pos).append(", current=buf").append(bufferIndex).append(", ");
		for (int i = 0; i < buffers.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append("buf").append(i).append("(size=").append(bufferSizes[i]);
			sb.append(", globj=").append(buffers[i]);
			if (isMapped[i])
				sb.append(", mapped");
			sb.append(")");
		}
		sb.append(", write=");
		if (buffer != null) {
			sb.append(buffer);
		} else {
			sb.append("null");
		}
		sb.append(")");
		return sb.toString();
	}

	public int currentBufferIndex() {
		return bufferIndex;
	}

	public int bufferName(int index) {
		return buffers[index];
	}

	public int numBuffers() {
		return buffers.length;
	}
}
