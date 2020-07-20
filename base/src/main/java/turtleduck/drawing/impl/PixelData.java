package turtleduck.drawing.impl;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.drawing.AbstractImage;
import turtleduck.drawing.Image;
import turtleduck.drawing.ImageMode;

public class PixelData extends AbstractImage implements Image {
	private final int[] data;
	private final Paint background, border;
	private final WrapMode wrapMode;

	public PixelData(int width, int height, Paint background, Paint border, ImageMode mode) {
		this(width, height, background, border, mode, null);
	}

	public PixelData(int width, int height, Paint background, Paint border, ImageMode mode, int[] data) {
		super(width, height, mode);
		this.wrapMode = Image.WrapMode.MIRROR;
		this.data = data;
		this.background = background != null ? background : Colors.TRANSPARENT;
		this.border = border != null ? border : Colors.TRANSPARENT;
	}

	@Override
	public Paint readPixel(int x, int y) {
		x = wrapMode.clamp(x, width);
		y = wrapMode.clamp(y, height);
		if (x < 0 || y < 0)
			return border;
		if (data != null) {
			int pixel = data[y * width + x];
			return Paint.fromARGB(pixel);
		} else {
			return background;
		}
	}

	public String toString() {
		return String.format("image(%d, %d, %s, int[%d])", width, height, mode, data.length);
	}

	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitData(data);
	}
}
