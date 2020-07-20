package turtleduck.drawing.impl;

import turtleduck.colors.Paint;
import turtleduck.drawing.AbstractImage;
import turtleduck.drawing.Image;
import turtleduck.drawing.ImageMode;
import turtleduck.drawing.Resampling;
import turtleduck.drawing.Image.Visitor;

public class ScaledImage extends AbstractImage implements Image {

	private final Image source;
	private final double scaleX;
	private final double scaleY ;
	private final Resampling filter;
	
	public ScaledImage(Image source, int newWidth, int newHeight, Resampling filter) {
		super(newWidth, newHeight, source.mode());
		this.source = source;
		this.filter = filter;
		scaleX = source.width() /  ((double)newWidth);
		scaleY = source.height() / ((double)newHeight);
	}
	
	@Override
	public Paint readPixel(int x, int y) {
		return source.readPixel((int)Math.round(x*scaleX), (int)Math.round(y*scaleY));
	}
	
	public String toString() {
		return String.format("scaleTo(%d,%d,%s)", width(), height(), source);
	}
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitScaled(width, height, source);
	}

}
