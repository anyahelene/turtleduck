package turtleduck.shapes.impl;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Point;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.shapes.Path;
import turtleduck.shapes.Text;
import turtleduck.shapes.Text.TextBuilder;
import turtleduck.text.Attributes;
import turtleduck.text.AttributesImpl;
import turtleduck.text.Attributes.AttributeBuilder;

public class TextImpl extends BaseShapeImpl<Text.TextBuilder> implements Text.TextBuilder {
	protected String text = "";
	protected String align = "center";
	protected Double size = null;
	protected Attributes attrs = new AttributesImpl<TextBuilder>(a -> {
		this.attrs = a;
		return this;
	});
	private Double angle = null;
	private String font = null;

	public TextImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		super(canvas, matrix, pw, pos, pen);
	}

	@Override
	public TextBuilder text(String text) {
		this.text = text;
		return this;
	}

	@Override
	public TextBuilder along(Path path) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public TextBuilder size(double pointSize) {
		size = pointSize;
		return this;
	}

	@Override
	public TextBuilder font(String font) {
		this.font = font;
		return this;
	}

	public TextBuilder rotate(double angle) {
		this.angle = angle;
		return this;
	}

	@Override
	public TextBuilder style(Attributes attrs) {
		this.attrs = attrs;
		return this;
	}

	@Override
	public AttributeBuilder<TextBuilder> style() {
		return new AttributesImpl<TextBuilder>(a -> {
			this.attrs = a;
			return this;
		});
	}

	@Override
	protected String writePath(PathWriter writer, Pen pen) {
		PathStroke ps = writer.addStroke();
		System.out.println("write: " + text);
		PathPointImpl start = new PathPointImpl(position, pen);
		start.annotation(Text.TEXT_ALIGN, align);
		start.annotation(Text.TEXT_FONT_SIZE, size);
		start.annotation(Text.TEXT_ROTATION, angle);
		start.annotation(Text.TEXT_FONT_FAMILY, font);
		ps.addText(start, text);
		String id = String.format("text@%x", System.identityHashCode(this));
		ps.group(id);
		ps.endPath();
		return id;
	}

	@Override
	public TextBuilder align(String alignment) {
		this.align = alignment;
		return this;
	}

}