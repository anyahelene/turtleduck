package turtleduck.image.impl;

import turtleduck.colors.Colors;
import turtleduck.image.AbstractImage;
import turtleduck.image.Image;
import turtleduck.image.ImageFactory;
import turtleduck.image.ImageMode;
import turtleduck.colors.Color;

public class PixelData extends AbstractImage implements Image {
	private final int[] data;
	private final Color border;
	private final WrapMode wrapMode;

	public PixelData(int width, int height, Color border, ImageMode mode) {
		this(width, height, border, mode, null);
	}

	public PixelData(int width, int height, Color border, ImageMode mode, int[] data) {
		super(width, height, mode);
		this.wrapMode = Image.WrapMode.MIRROR;
		this.data = data;
		this.border = border != null ? border : Colors.TRANSPARENT;
		this.loadAttempted = true;
	}

	@Override
	public Color readPixel(int x, int y) {
		x = wrapMode.clamp(x, width);
		y = wrapMode.clamp(y, height);
		if (x < 0 || y < 0)
			return border;
		if (data != null) {
			int pixel = data[y * width + x];
			return Color.fromARGB(pixel);
		} else {
			return Colors.TRANSPARENT;
		}
	}

	public String toString() {
		return String.format("image(%d, %d, %s, int[%d])", width, height, mode, data.length);
	}

	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitData(data);
	}

	@Override
	public Image convert(ImageFactory factory) {
		return factory.imageFromPixels(width, height, border, mode, data);
	}

	@Override
	protected void load() {
	}
}
