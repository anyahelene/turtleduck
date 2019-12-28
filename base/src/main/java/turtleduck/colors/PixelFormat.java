package turtleduck.colors;

import java.nio.ByteBuffer;

public enum PixelFormat {
	GREY(0, 0, 0), RGB(0, 1, 2), BGR(2, 1, 0), ARGB(1, 2, 3, 0), BGRA(2, 1, 0, 3),
	RGBA(0, 1, 2, 3);

	private final int indexes[];
	private final int nBytes;

	private PixelFormat(int r, int g, int b) {
		indexes = new int[] { r, g, b, -1 };
		nBytes = r == g ? 1 : 3;
	}

	private PixelFormat(int r, int g, int b, int a) {
		indexes = new int[] { r, g, b, a };
		nBytes = 4;
	}

	public Paint decode(byte[] pixels, int offset) {
		if (pixels.length < offset + nBytes)
			throw new IllegalArgumentException(); // TODO
		float r = 0, g = 0, b = 0, a = 1;
		switch (nBytes) {
		case 1:
			r = g = b = Byte.toUnsignedInt(pixels[offset]) / 255f;
			break;
		case 4:
			a = Byte.toUnsignedInt(pixels[offset+indexes[3]]) / 255f;
		case 3:
			r = Byte.toUnsignedInt(pixels[offset+indexes[0]]) / 255f;
			g = Byte.toUnsignedInt(pixels[offset+indexes[1]]) / 255f;
			b = Byte.toUnsignedInt(pixels[offset+indexes[2]]) / 255f;
			break;
		}
		return new ColorRGB(r, g, b, a);
	}

	public Paint decode(ByteBuffer pixels, int offset) {
//		if(pixels.pixels.length < offset + nBytes)
//			throw new IllegalArgumentException(); // TODO
		if(nBytes == 1) {
			float g = Byte.toUnsignedInt(pixels.get()) / 255f;
			return new ColorRGB(g, g, g, 1f);
		} else {
			byte[] dst = new byte[nBytes];
			pixels.get(dst, 0, nBytes);
			float r = Byte.toUnsignedInt(dst[offset + indexes[0]]) / 255f;
			float g = Byte.toUnsignedInt(dst[offset + indexes[1]]) / 255f;
			float b = Byte.toUnsignedInt(dst[offset + indexes[2]]) / 255f;
			if(nBytes == 3)
				return new ColorRGB(r, g, b, 1f);
			else
				return new ColorRGB(r, g, b, Byte.toUnsignedInt(dst[offset + indexes[3]]) / 255f);
		}
	}
}
