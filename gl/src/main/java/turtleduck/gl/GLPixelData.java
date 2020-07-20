package turtleduck.gl;

import turtleduck.drawing.Image;
import turtleduck.drawing.ImageMode;
import turtleduck.gl.objects.Texture;

import java.io.IOException;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.drawing.AbstractImage;

public class GLPixelData extends AbstractImage implements Image {

	private Texture texture;

	public GLPixelData(Texture texture) {
		super(texture.getWidth(), texture.getHeight(), ImageMode.RGBA);
		this.texture = texture;
	}
	public GLPixelData(String filename) throws IOException {
		this(Texture.create().clamp().load(filename));
	}

	@Override
	public Paint readPixel(int x, int y) {
		return Colors.TRANSPARENT;
	}

	@Override
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitData(texture);
	}
	
	public void render() {
		
	}

}
