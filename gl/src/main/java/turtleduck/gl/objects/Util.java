package turtleduck.gl.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.BufferUtils;

/**
 * From {@link org.lwjgl.demo.opengl.util.DemoUtils}.
 * 
 * <p>Copyright © 2012-present Lightweight Java Game Library. All rights
 * reserved. License terms: <a href="https://www.lwjgl.org/license">https://www.lwjgl.org/license</a>
 * 
 * @author <a href="https://www.lwjgl.org/">LWJGL</a>
 * 
 *
 */
public class Util {
	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * 
	 * @param resource   the resource to read
	 * @param bufferSize the initial buffer size
	 *
	 * @return the resource data
	 *
	 * @throws IOException if an IO error occurs
	 * 
	 */
	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;
		URL url = Util.class.getResource(resource);
		if (url == null) {
			throw new FileNotFoundException(resource);
		}
		File file = new File(url.getFile());
		if (file.isFile() && file.length() > bufferSize) {
			try (FileInputStream fis = new FileInputStream(file)) {
				try (FileChannel fc = fis.getChannel()) {
					buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				}
			}
		} else {
			buffer = BufferUtils.createByteBuffer(bufferSize);
			try (InputStream source = url.openStream()) {
				if (source == null) {
					throw new FileNotFoundException(resource);
				}
				byte[] buf = new byte[8192];
				while (true) {
					int bytes = source.read(buf, 0, buf.length);
					if (bytes == -1) {
						break;
					}
					if (buffer.remaining() < bytes) {
						buffer = resizeBuffer(buffer, buffer.capacity() * 2);
					}
					buffer.put(buf, 0, bytes);
				}
				buffer.flip();
			}
		}
		return buffer;
	}

	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}
}
