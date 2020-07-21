package turtleduck.drawing;

import turtleduck.colors.Color;
import turtleduck.drawing.impl.CroppedImage;
import turtleduck.drawing.impl.ScaledImage;
import turtleduck.drawing.impl.TransposedImage;

public abstract class AbstractImage implements Image {
	protected final int width;
	protected final int height;
	protected final ImageMode mode;
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
		return mode;
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
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

	public String toString() {
		return "Image(" + width + ", " + height + ")";
	}
}
