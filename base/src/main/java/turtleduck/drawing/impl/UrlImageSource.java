package turtleduck.drawing.impl;

import java.net.URL;

import turtleduck.colors.Colors;
import turtleduck.colors.Color;
import turtleduck.drawing.AbstractImage;
import turtleduck.drawing.ImageMode;

public class UrlImageSource extends AbstractImage {

	private final URL url;

	public UrlImageSource(URL url, int width, int height, ImageMode mode) {
		super(width, height, mode);
		this.url = url;
	}

	@Override
	public Color readPixel(int x, int y) {
		return Colors.TRANSPARENT;
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitResourceUrl(url, width, height);
	}

}
