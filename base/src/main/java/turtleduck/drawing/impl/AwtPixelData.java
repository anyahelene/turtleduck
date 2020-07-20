package turtleduck.drawing.impl;

import java.awt.image.BufferedImage;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.drawing.AbstractImage;
import turtleduck.drawing.Image;
import turtleduck.drawing.ImageMode;

public class AwtPixelData extends AbstractImage implements Image {
	private final BufferedImage data;
	private final Paint background, border;
	private final WrapMode wrapMode;

	public AwtPixelData(BufferedImage data) {
		this(null, null, data);
	}

	public AwtPixelData(Paint background, Paint border, BufferedImage data) {
		super(data.getWidth(), data.getHeight(), ImageMode.RGBA);
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
			int pixel = data.getRGB(x, y);
			return Paint.fromARGB(pixel);
		} else {
			return background;
		}
	}

	public String toString() {
		return String.format("pixeldata(%d, %d, %s, %s)", width, height, mode, data);
	}

	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitData(data.getRGB(0, 0, width, height, null, 0, width));
	}
}
