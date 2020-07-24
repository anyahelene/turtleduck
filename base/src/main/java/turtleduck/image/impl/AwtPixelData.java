package turtleduck.image.impl;

import java.awt.image.BufferedImage;

import turtleduck.colors.Colors;
import turtleduck.image.AbstractImage;
import turtleduck.image.Image;
import turtleduck.image.ImageFactory;
import turtleduck.image.ImageMode;
import turtleduck.colors.Color;

public class AwtPixelData extends AbstractImage implements Image {
	private final BufferedImage data;
	private final Color background, border;
	private final WrapMode wrapMode;

	public AwtPixelData(BufferedImage data) {
		this(null, null, data);
	}

	public AwtPixelData(Color background, Color border, BufferedImage data) {
		super(data.getWidth(), data.getHeight(), ImageMode.RGBA);
		this.wrapMode = Image.WrapMode.MIRROR;
		this.data = data;
		this.background = background != null ? background : Colors.TRANSPARENT;
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
			int pixel = data.getRGB(x, y);
			return Color.fromARGB(pixel);
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
	
	@Override
	public Image convert(ImageFactory factory) {
		return factory.imageFromPixels(width, height, border, mode, data.getRGB(0, 0, width, height, null, 0, width));
	}

	@Override
	protected void load() {
	}
}
