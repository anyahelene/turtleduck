package turtleduck.drawing.impl;

import turtleduck.colors.Paint;
import turtleduck.drawing.AbstractImage;
import turtleduck.drawing.Image;
import turtleduck.drawing.ImageMode;
import turtleduck.drawing.Resampling;
import turtleduck.drawing.Image.Visitor;

public class CroppedImage extends AbstractImage {
	private Image source;
	private int translateX;
	private int translateY;


	public CroppedImage(Image source, int x, int y, int newWidth, int newHeight) {
		super(newWidth, newHeight, source.mode());
		this.source = source;
		translateX = x;
		translateY = y;
	}

	@Override
	public Paint readPixel(int x, int y) {
		return source.readPixel(x+translateX, y+translateY);
	}

	
	public String toString() {
		return String.format("crop(%d,%d,%d,%d,%s)", translateX, translateY, width(), height(), source);
	}
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitCropped(translateX, translateY, height, width, source);
	}
}
