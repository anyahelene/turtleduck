package turtleduck.gl;

import turtleduck.gl.objects.Texture;
import turtleduck.image.AbstractImage;
import turtleduck.image.Image;
import turtleduck.image.ImageFactory;
import turtleduck.image.ImageMode;
import turtleduck.image.Image.WrapMode;

import java.io.IOException;

import turtleduck.colors.Colors;
import turtleduck.colors.Color;

public class GLPixelData extends AbstractImage implements Image {

	private Texture texture;
	private final WrapMode wrapMode;

	protected GLPixelData(Texture texture) {
		super(texture.getWidth(), texture.getHeight(), ImageMode.RGBA);
		this.texture = texture;
		this.loadAttempted = true;
		this.wrapMode = Image.WrapMode.MIRROR;
	}

	public GLPixelData(String filename) throws IOException {
		this(Texture.create().clamp().nearest().nonlinearColor().load(filename));
	}

	@Override
	public Color readPixel(int x, int y) {
		x = wrapMode.clamp(x, width);
		y = wrapMode.clamp(y, height);
		if (x < 0 || y < 0)
			return Colors.TRANSPARENT;
		if (texture.isReadable()) {
			int ch = texture.getChannels();
			int pos = ch * (y * width + x);
			byte r = texture.read(pos++);
			byte g = ch > 1 ? texture.read(pos++) : r;
			byte b = ch > 2 ? texture.read(pos++) : g;
			byte a = ch > 3 ? texture.read(pos++) : -128;
			return Color.color((r&0xff)/255.0, (g&0xff)/255.0, (b&0xff)/255.0, 1);//.fromRGBA(r, g, b, a);
		} else {
			return Colors.TRANSPARENT;
		}
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
