package turtleduck.terminal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TerminalInputStream extends InputStream {
	private Charset charset;
	private byte[] buf = null;
	private int count = 0;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public TerminalInputStream() {
		charset = Charset.forName("UTF-8");
	}

	public void write(String s) {
		ByteBuffer encoded = charset.encode(s);
		if (encoded.hasArray()) {
			write(encoded.array(), encoded.arrayOffset(), encoded.remaining());
		} else {
			byte[] dst = new byte[encoded.remaining()];
			encoded.get(dst);
			write(dst, 0, dst.length);
		}
	}

	/**
	 * Writes the specified byte to the input buffer.
	 *
	 * The input buffer will resize to automatically, so this method will never
	 * block.
	 * 
	 * @param b the byte to be written, value 0x00–0xff (upper bits are dropped)
	 */
	public void write(int b) {
		synchronized (baos) {
			baos.write(b);
		}
	}

	/**
	 * Writes the specified bytes to the input buffer.
	 *
	 * The input buffer will resize to automatically, so this method will never
	 * block.
	 * 
	 * @param b   an array of bytes to be written
	 * @param off an offset into the array
	 * @param len number of bytes to write
	 * @throws NullPointerException      if {@code b} is {@code null}.
	 * @throws IndexOutOfBoundsException if {@code off} is negative, {@code len} is
	 *                                   negative, or {@code len} is greater than
	 *                                   {@code b.length - off}
	 */
	public void write(byte b[], int off, int len) {
		synchronized (baos) {
			baos.write(b, off, len);
		}
	}

	@Override
	public int read() {
		synchronized (this) {
			if (buf == null || count >= buf.length) {
				if (!flush())
					return -1;
			}
			return buf[count++] & 0xff;
		}
	}

	private boolean flush() {
		synchronized (baos) {
			if (baos.size() > 0) {
				buf = baos.toByteArray();
				count = 0;
				baos.reset();
				return true;
			} else {
				return false;
			}
		}
	}

}
