package turtleduck.drawing.impl;

import turtleduck.colors.Paint;
import turtleduck.drawing.AbstractImage;
import turtleduck.drawing.Image;
import turtleduck.drawing.ImageMode;
import turtleduck.drawing.Resampling;
import turtleduck.drawing.Image.Visitor;

public class TransposedImage extends AbstractImage implements Image {
	private final Transpose transposeMethod;
	private final int translateX;
	private final int translateY;
	private final Image source;

	public TransposedImage(Image source, Image.Transpose method) {
		super(method.transform(source.width(), source.height(), new int[2]), source.mode());
		this.transposeMethod = method;
		this.source = source;
	
		translateX = dimTmp[0] < 0 ? source.width()-1 : 0;
		translateY = dimTmp[1] < 0 ? source.height()-1 : 0;
	}

	@Override
	public Paint readPixel(int x, int y) {
//		System.out.printf("(%d,%d) => ", x, y);
		transposeMethod.transform(x, y, dimTmp);
		return source.readPixel(dimTmp[0] + translateX, dimTmp[1] + translateY);
	}

	public String toString() {
		return String.format("transpose(%s, %s)", transposeMethod, source);
	}
	public <T> T visit(Visitor<T> visitor) {
		return visitor.visitTransposed(transposeMethod, source);
	}
}
