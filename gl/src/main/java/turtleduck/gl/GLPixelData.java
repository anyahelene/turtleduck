package turtleduck.gl;

import turtleduck.gl.objects.Texture;
import turtleduck.image.AbstractImage;
import turtleduck.image.Image;
import turtleduck.image.ImageFactory;
import turtleduck.image.ImageMode;

import java.io.IOException;

import turtleduck.colors.Colors;
import turtleduck.colors.Color;

public class GLPixelData extends AbstractImage implements Image {

	private Texture texture;

	protected GLPixelData(Texture texture) {
		super(texture.getWidth(), texture.getHeight(), ImageMode.RGBA);
		this.texture = texture;
		this.loadAttempted = true;
	}

	public GLPixelData(String filename) throws IOException {
		this(Texture.create().clamp().load(filename));
	}

	@Override
	public Color readPixel(int x, int y) {
		return Colors.TRANSPARENT;
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitData(texture);
	}

	public void render() {

	}

	@Override
	public Image convert(ImageFactory factory) {
		return this;
	}

	@Override
	protected void load() {
	}

}
