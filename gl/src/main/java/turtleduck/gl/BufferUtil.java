package turtleduck.gl;

import java.nio.IntBuffer;

public class BufferUtil {
	public static IntBuffer sliceBuffer(IntBuffer buf, int pos, int size) {
		if (buf == null || pos < 0 || pos >= buf.limit() || pos + size > buf.limit()) {
			throw new IllegalArgumentException();
		}
		int oldPos = buf.position();
		buf.position(pos);
		IntBuffer slice = buf.slice();
		slice.limit(size);
		buf.position(oldPos);
		return slice;
	}

	public static IntBuffer[] sliceBuffer(IntBuffer buf, int n) {
		if (buf == null || n <= 0 || n > buf.limit()) {
			throw new IllegalArgumentException();
		}
		int oldPos = buf.position();
		IntBuffer[] bufs = new IntBuffer[n];
		for (int i = 0; i < n; i++) {
			buf.position(i);
			bufs[i] = buf.slice();
			bufs[i].limit(1);
		}
		buf.position(oldPos);
		return bufs;
	}
}
