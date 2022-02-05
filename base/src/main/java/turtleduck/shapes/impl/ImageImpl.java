package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.shapes.Image;
import turtleduck.shapes.Image.ImageBuilder;

public class ImageImpl extends BaseShapeWxH<Image.ImageBuilder> implements Image.ImageBuilder {

	public ImageImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		super(canvas, matrix, pw, pos, pen);
	}

	@Override
	protected String writePath(PathWriter writer, Pen pen) {
		// TODO Auto-generated method stub
		return null;
	}

}