package turtleduck.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import turtleduck.colors.Color;
import turtleduck.image.impl.AwtPixelData;

public interface Image {
	enum Transpose {
		FLIP_LEFT_RIGHT(-1, 0, 0, 1, 1, 0), //
		FLIP_TOP_BOTTOM(1, 0, 0, -1, 0, 1), //
		ROTATE_90(0, 1, -1, 0, 0, 1), //
		ROTATE_180(-1, 0, 0, -1, 1, 1), //
		ROTATE_270(0, -1, 1, 0, 1, 0);

		int m11, m21, m12, m22;
		int tx, ty;
		Transpose(int i, int j, int k, int l, int t0, int t1) {
			m11 = i;
			m21 = j;
			m12 = k;
			m22 = l;
			tx = t0;
			ty = t1;
		}

		public int[] transform(int x, int y, int[] out) {
			out[0] = x * m11 + y * m21;
			out[1] = x * m12 + y * m22;
			return out;
		}
		public int[] transformNormalized(int x, int y, int[] out) {
			out[0] = x * m11 + y * m21 + tx;
			out[1] = x * m12 + y * m22 + ty;
			return out;
		}

		public int transformX(int x, int y) {
			return x * m11 + y * m21;
		}

		public int transformY(int x, int y) {
			return x * m12 + y * m22;
		}
	}

	enum WrapMode {
		CLAMP, REPEAT, MIRROR, MIRROR_ONCE, BORDER;

		public int clamp(int value, int max) {
			if (value >= 0 && value < max)
				return value;
			else {
				int v = value % max;
				switch (this) {
				case BORDER:
					return -1;
				case CLAMP:
					return value < 0 ? 0 : max - 1;
				case MIRROR:
				case MIRROR_ONCE:
					return max - v - 1;
				case REPEAT:
					return v;
				default:
					throw new IllegalStateException();
				}
			}
		}
	}

	interface Visitor<T> {
		default T visitResourceUrl(URL url, int width, int height) { System.out.println(url); return null; }

		default T visitResourceName(String name, int width, int height) { System.out.println(name); return null; }

		default T visitData(Object data) {System.out.println(data);  return null; }

		default T visitTransposed(Transpose method, Image source) {System.out.println(source);  return source.visit(this); }

		default T visitCropped(int x, int y, int width, int height, Image source) {  return source.visit(this); }

		default T visitScaled(int width, int height, Image source) { return source.visit(this); }
	}

	static Image create(int width, int height, ImageMode mode) {
		return null;
	}

	static Image create(int width, int height, Color fill) {
		return null;
	}

	static Image create(URL url) throws IOException {
		return ImageFactory.defaultFactory().imageFromUrl(url, -1, -1, null);
	}

	static Image create(InputStream data) throws IOException {
		return new AwtPixelData(ImageIO.read(data));
	}

	static Image create(int width, int height, ImageMode mode, byte[] data) {
		return null;
	}

	ImageMode mode();

	int width();

	int height();

	Color readPixel(int x, int y);

	Color readPixel(double x, double y);

	Image crop(int x, int y, int newWidth, int newHeight);

	Image scale(int newWidth, int newHeight);

	Image scale(int newWidth, int newHeight, Resampling filter);

	Image transpose(Transpose method);

	<T> T visit(Visitor<T> visitor);
	
	Image convert(ImageFactory factory);
	
	Tiles tiles(int width, int height);
}
