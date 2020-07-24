package turtleduck.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.imageio.ImageIO;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.image.impl.AwtPixelData;
import turtleduck.image.impl.UniformTiles;

public abstract class AbstractImage implements Image {

	protected int width;
	protected int height;
	protected ImageMode mode;
	protected boolean loadAttempted;
	/**
	 * An array for use by subclasses for storing (x,y) data, in particular for
	 * getting around restrictions on use of subclass fields prior to calling
	 * super() in constructor.
	 */
	protected int[] dimTmp;

	/**
	 * Initialize using width and height
	 * 
	 * @param width  the width
	 * @param height the height
	 * @param mode   the image mode
	 */
	protected AbstractImage(int width, int height, ImageMode mode) {
		this.width = width;
		this.height = height;
		this.mode = mode;
		this.dimTmp = null;
	}

	/**
	 * Initialize using dimensions array
	 * 
	 * @param dimensions an int[2] array of {width, height}; the array is stored in
	 *                   the {@link #dimTmp} field but not used further by this
	 *                   class
	 * @param mode       the image mode
	 */
	protected AbstractImage(int[] dimensions, ImageMode mode) {
		this.dimTmp = dimensions;
		this.width = Math.abs(dimensions[0]);
		this.height = Math.abs(dimensions[1]);
		this.mode = mode;
	}

	@Override
	public ImageMode mode() {
		if (mode == null && !loadAttempted)
			load();
		return mode;
	}

	@Override
	public int width() {
		if (width < 0 && !loadAttempted)
			load();
		return width;
	}

	@Override
	public int height() {
		if (height < 0 && !loadAttempted)
			load();
		return height;
	}

	@Override
	public Color readPixel(double x, double y) {
		return readPixel((int) Math.round(x), (int) Math.round(y));
	}

	@Override
	public Image crop(int x, int y, int newWidth, int newHeight) {
		return new CroppedImage(this, x, y, newWidth, newHeight);
	}

	@Override
	public Image scale(int newWidth, int newHeight) {
		return new ScaledImage(this, newWidth, newHeight, Resampling.NEAREST);
	}

	@Override
	public Image scale(int newWidth, int newHeight, Resampling filter) {
		return new ScaledImage(this, newWidth, newHeight, filter);
	}

	@Override
	public Image transpose(Transpose method) {
		return new TransposedImage(this, method);
	}

	@Override
	public Tiles tiles(int width, int height) {
		return new UniformTiles(this, width, height);
	}

	public String toString() {
		return "Image(" + width + ", " + height + ")";
	}

	protected abstract void load();

	public static class CroppedImage extends AbstractImage {
		private Image source;
		private int translateX;
		private int translateY;

		public CroppedImage(Image source, int x, int y, int newWidth, int newHeight) {
			super(newWidth, newHeight, source.mode());
			this.source = source;
			translateX = x;
			translateY = y;
			this.loadAttempted = true;
		}

		@Override
		public Color readPixel(int x, int y) {
			return source.readPixel(x + translateX, y + translateY);
		}

		public String toString() {
			return String.format("crop(%d,%d,%d,%d,%s)", translateX, translateY, width(), height(), source);
		}

		public <T> T visit(Visitor<T> visitor) {
			return visitor.visitCropped(translateX, translateY, height, width, source);
		}

		@Override
		public Image convert(ImageFactory factory) {
			return factory.croppedImage(source.convert(factory), translateX, translateY, width, height);
		}

		protected void load() {
		}

	}

	public static class ScaledImage extends AbstractImage implements Image {

		private final Image source;
		private double scaleX = 0;
		private double scaleY = 0;
		private final Resampling filter;

		public ScaledImage(Image source, int newWidth, int newHeight, Resampling filter) {
			super(newWidth, newHeight, source.mode());
			this.source = source;
			this.filter = filter;
		}

		@Override
		public Color readPixel(int x, int y) {
			if (!loadAttempted)
				load();
			return source.readPixel((int) Math.round(x * scaleX), (int) Math.round(y * scaleY));
		}

		public String toString() {
			return String.format("scaleTo(%d,%d,%s)", width(), height(), source);
		}

		public <T> T visit(Visitor<T> visitor) {
			return visitor.visitScaled(width, height, source);
		}

		@Override
		public Image convert(ImageFactory factory) {
			return factory.scaledImage(source.convert(factory), width, height, filter);
		}

		protected void load() {
			loadAttempted = true;
			if (source.width() >= 0) {
				scaleX = source.width() / ((double) width);
				scaleY = source.height() / ((double) height);
			}
		}

	}

	public static class TransposedImage extends AbstractImage implements Image {
		private final Transpose transposeMethod;
		private final int translateX;
		private final int translateY;
		private final Image source;

		public TransposedImage(Image source, Image.Transpose method) {
			super(method.transform(source.width(), source.height(), new int[2]), source.mode());
			this.transposeMethod = method;
			this.source = source;
			this.loadAttempted = true;
			translateX = dimTmp[0] < 0 ? source.width() - 1 : 0;
			translateY = dimTmp[1] < 0 ? source.height() - 1 : 0;
		}

		@Override
		public Color readPixel(int x, int y) {
//			System.out.printf("(%d,%d) => ", x, y);
			transposeMethod.transform(x, y, dimTmp);
			return source.readPixel(dimTmp[0] + translateX, dimTmp[1] + translateY);
		}

		public String toString() {
			return String.format("transpose(%s, %s)", transposeMethod, source);
		}

		public <T> T visit(Visitor<T> visitor) {
			return visitor.visitTransposed(transposeMethod, source);
		}

		@Override
		public Image convert(ImageFactory factory) {
			return factory.transposedImage(source.convert(factory), transposeMethod);
		}

		@Override
		protected void load() {
		}
	}

	public static class UrlImageSource extends AbstractImage {

		private final URL url;
		private Image pixelData;

		public UrlImageSource(URL url, Image pixelData) {
			super(pixelData.width(), pixelData.height(), pixelData.mode());
			this.url = url;
			this.pixelData = pixelData;
			this.loadAttempted = true;
		}

		public UrlImageSource(URL url, int width, int height, ImageMode mode) {
			super(width, height, mode);
			this.url = url;
		}

		@Override
		public Color readPixel(int x, int y) {
			if (!loadAttempted)
				load();

			if (pixelData != null) {
				return pixelData.readPixel(x, y);
			} else {
				return Colors.TRANSPARENT;
			}
		}

		@Override
		public <T> T visit(Visitor<T> visitor) {
			return visitor.visitResourceUrl(url, width, height);
		}

		@Override
		public Image convert(ImageFactory factory) {
			return factory.imageFromUrl(url, width, height, mode);
		}

		protected void load() {
			loadAttempted = true;
			try {
				BufferedImage img = ImageIO.read(url);
				pixelData = new AwtPixelData(img);
				if (width < 0 || height < 0) {
					width = pixelData.width();
					height = pixelData.height();
				}
				if (mode == null) {
					mode = pixelData.mode();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static class ResourceImageSource extends AbstractImage {

		private final String name;
		private Image pixelData;

		public ResourceImageSource(String name, Image pixelData) {
			super(pixelData.width(), pixelData.height(), pixelData.mode());
			this.name = name;
			this.pixelData = pixelData;
		}

		public ResourceImageSource(String name, int width, int height, ImageMode mode) {
			super(width, height, mode);
			this.name = name;
		}

		@Override
		public Color readPixel(int x, int y) {
			return Colors.TRANSPARENT;
		}

		@Override
		public <T> T visit(Visitor<T> visitor) {
			return visitor.visitResourceName(name, width, height);
		}

		@Override
		public Image convert(ImageFactory factory) {
			return factory.imageFromResource(name, width, height, mode);
		}

		@Override
		protected void load() {
			loadAttempted = true;
			try {
				BufferedImage img = ImageIO.read(getClass().getResourceAsStream(name));
				pixelData = new AwtPixelData(img);
				if (width < 0 || height < 0) {
					width = pixelData.width();
					height = pixelData.height();
				}
				if (mode == null) {
					mode = pixelData.mode();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
