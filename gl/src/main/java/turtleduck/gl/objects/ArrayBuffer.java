package turtleduck.gl.objects;

import static org.lwjgl.opengl.GL43C.*;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL33C;

import turtleduck.buffer.DataField;
import turtleduck.buffer.DataFormat;
import turtleduck.util.TextUtil;

public class ArrayBuffer {
	int[] buffers = { 0, 0 };
	long[] bufferSizes = { 0, 0 };
	boolean[] isMapped = { false, false };
	int usage = GL_STREAM_DRAW;
	int bufferIndex = 0;
	long pos = 0;
	ByteBuffer buffer;
	DataFormat current;
	int currentField = 0;
	private int initCapacity;

	public ArrayBuffer(int usage, int capacity) {
		GL33C.glGenBuffers(buffers);
		this.usage = usage == 0 ? GL_STATIC_DRAW : usage;
		this.initCapacity = capacity == 0 ? 1024 : capacity;
	}

	public void dispose() {
		GL33C.glDeleteBuffers(buffers);
		buffers[0] = 0;
		buffers[1] = 0;
	}

	public long begin(DataFormat format) {
		current = format;
		currentField = 0;
		ensureSpaceFor(format.numBytes());
		if (!isMapped[bufferIndex]) {
			glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
			buffer = glMapBufferRange(GL_ARRAY_BUFFER, pos, bufferSizes[bufferIndex] - pos, GL_MAP_WRITE_BIT|GL_MAP_INVALIDATE_RANGE_BIT, buffer);
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
			glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
			glBufferData(GL_ARRAY_BUFFER, newCap, usage);
			bufferSizes[bufferIndex] = newCap;
			System.out.printf("to %d size %d (%sB)%n", buffers[bufferIndex], newCap,
					TextUtil.humanFriendlyBinary(newCap));
			if (pos > 0) {
				glBindBuffer(GL_COPY_READ_BUFFER, buffers[oldIndex]);
				if (isMapped[oldIndex]) {
					glUnmapBuffer(GL_COPY_READ_BUFFER);
					isMapped[oldIndex] = false;
				}
				glCopyBufferSubData(GL_COPY_READ_BUFFER, GL_ARRAY_BUFFER, 0, 0, buffer.position());
				glBufferData(GL_COPY_READ_BUFFER, newCap, usage);
				bufferSizes[oldIndex] = newCap;
//			glInvalidateBufferData(buffers[oldIndex]);
				glBindBuffer(GL_COPY_READ_BUFFER, 0);
			}
			buffer = glMapBufferRange(GL_ARRAY_BUFFER, pos, newCap - pos, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT,
					buffer);
			if (buffer == null) {
				throw new OutOfMemoryError("couldn't map buffer");
			} else {
				isMapped[bufferIndex] = true;
			}
		}
	}

	public <T> void put(DataField<T> field, T data) {
		checkWrite(field);
		field.write(buffer, data);
		pos += field.numBytes();
		currentField++;
	}

	private <T> void checkWrite(DataField<T> field) {
		if (!isMapped[bufferIndex]) {
			throw new IllegalStateException("Writing after done()");
		}
		if (field != current.field(currentField)) {
			throw new IllegalStateException("Expected data for " + current.field(currentField) + " but got " + field);
		}
	}

	public <T> void put(DataField<T> field, float x) {
		checkWrite(field);
		field.write(buffer, x);
		pos += field.numBytes();
		currentField++;
	}

	public <T> void put(DataField<T> field, float x, float y) {
		checkWrite(field);
		field.write(buffer, x, y);
		pos += field.numBytes();
		currentField++;
	}

	public <T> void put(DataField<T> field, float x, float y, float z) {
		checkWrite(field);
		field.write(buffer, x, y, z);
		pos += field.numBytes();
		currentField++;
	}

	public <T> void put(DataField<T> field, float x, float y, float z, float w) {
		checkWrite(field);
		field.write(buffer, x, y, z, w);
		pos += field.numBytes();
		currentField++;
	}

	public long end() {
		if (!isMapped[bufferIndex]) {
			throw new IllegalStateException("Writing after done()");
		}
		if (currentField != current.numFields())
			throw new IllegalStateException(
					"Expected " + current.numFields() + " data fields, but got " + currentField);
		current = null;
		return pos;
	}

	public int done() {
		if (isMapped[bufferIndex]) {
				glBindBuffer(GL_ARRAY_BUFFER, buffers[bufferIndex]);
				glUnmapBuffer(GL_ARRAY_BUFFER);
				glBindBuffer(GL_ARRAY_BUFFER, 0);
				isMapped[bufferIndex] = false;
		}
		return buffers[bufferIndex];
	}

	public void dump(DataFormat format) {
		glBindBuffer(GL_COPY_READ_BUFFER, buffers[bufferIndex]);
		ByteBuffer b = glMapBufferRange(GL_COPY_READ_BUFFER, 0, bufferSizes[bufferIndex], GL_MAP_READ_BIT, null);
		int i = 0;
		while (b.remaining() >= format.numBytes()) {
			DataField<?> field = format.field(i++);
			System.out.print(field.read(b) + " ");
			if (i >= format.numFields()) {
				i = 0;
				System.out.println();
			}
		}
		glUnmapBuffer(GL_COPY_READ_BUFFER);

	}

	public void clear() {
		done();
		glBindBuffer(GL_COPY_READ_BUFFER, buffers[bufferIndex]);
		if (isMapped[bufferIndex]) {
			glUnmapBuffer(GL_COPY_READ_BUFFER);
			isMapped[bufferIndex] = false;
		}
		glInvalidateBufferData(buffers[bufferIndex]);
		glBindBuffer(GL_COPY_READ_BUFFER, 0);
		pos = 0;
		current = null;
		bufferIndex = (bufferIndex + 1) % buffers.length;
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
