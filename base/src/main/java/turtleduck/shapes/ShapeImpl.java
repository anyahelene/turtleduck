package turtleduck.shapes;

import org.joml.Matrix3x2dc;

import turtleduck.canvas.Canvas;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.geometry.Point;
import turtleduck.shapes.Poly.LineBuilder;
import turtleduck.shapes.Text.TextBuilder;
import turtleduck.text.Attributes;
import turtleduck.text.Attributes.AttributeBuilder;
import turtleduck.text.AttributesImpl;
import turtleduck.turtle.PathStroke;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Pen.SmoothType;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.PathPointImpl;

public abstract class ShapeImpl<T> implements Shape {
	protected Point position;
	protected Matrix3x2dc matrix;
	protected Pen pen;
	protected PenBuilder<Pen> penBuilder = null;
	protected Canvas canvas;
	protected PathWriter pathWriter;

	protected ShapeImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
		if (pos == null)
			throw new NullPointerException();
		if (pen == null)
			pen = new BasePen();
		this.canvas = canvas;
		this.position = pos;
		this.pen = pen;
		this.matrix = matrix;
		this.pathWriter = pw;
	}

	@Override
	public Point position() {
		return position;
	}

	@SuppressWarnings("unchecked")
	public T at(Point p) {
		if (p == null)
			throw new NullPointerException();
		position = p;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T at(double x, double y) {
		position = Point.point(x, y);
		return (T) this;
	}

	public String stroke() {
		return writePath(pathWriter, true, false);
	}

	public String fill() {
		return writePath(pathWriter, false, true);
	}

	public String strokeAndFill() {
		return writePath(pathWriter, true, true);
	}

	protected abstract String writePath(PathWriter writer, boolean stroke, boolean fill);

	public static abstract class ShapeWxH<T> extends ShapeImpl<T> implements WxHBuilder<T> {
		protected double width = 1, height = 1;

		protected ShapeWxH(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
			super(canvas, matrix, pw, pos, pen);
		}

		@SuppressWarnings("unchecked")
		public T width(double width) {
			this.width = width;
			return (T) this;
		}

		@SuppressWarnings("unchecked")
		public T height(double height) {
			this.height = height;
			return (T) this;
		}
	}

	public static class EllipseImpl extends ShapeWxH<Ellipse.EllipseBuilder> implements Ellipse.EllipseBuilder {

		public EllipseImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
			super(canvas, matrix, pw, pos, pen);
		}

		@Override
		public Ellipse.EllipseBuilder radius(double widthAndHeight) {
			this.width = widthAndHeight;
			this.height = widthAndHeight;
			return this;
		}

		@Override
		protected String writePath(PathWriter writer, boolean stroke, boolean fill) {
			PathStroke ps = writer.addStroke();
			double w = width / 2, h = height / 2;
			PathPointImpl start = new PathPointImpl(position.add(w, 0), pen);
			ps.addPoint(start);
			int step = 10;
			for (int i = step; i <= 360; i += step) {
				double a = (2.0 * Math.PI * i) / 360.0;
				ps.addPoint(position.add(Math.cos(a) * w, Math.sin(a) * h));
			}
			String id = String.format("ellipse@%x", System.identityHashCode(this));
			ps.group(id);
			ps.endPath();
			return id;
		}

	}

	public static class RectangleImpl extends ShapeWxH<Rectangle.RectangleBuilder>
			implements Rectangle.RectangleBuilder {

		public RectangleImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
			super(canvas, matrix, pw, pos, pen);
		}

		@Override
		protected String writePath(PathWriter writer, boolean stroke, boolean fill) {
			Point p0 = position.add(-width / 2, -height / 2);
			PathStroke ps = writer.addStroke();
			if (!stroke)
				pen = pen.change().strokePaint(Colors.TRANSPARENT).done();
			if (!fill)
				pen = pen.change().fillPaint(Colors.TRANSPARENT).done();
			PathPointImpl start = new PathPointImpl(p0, pen);
			String id = String.format("rect@%x", System.identityHashCode(this));
			ps.group(id);
			ps.addPoint(start);
			ps.addPoint(p0.add(0, height));
			ps.addPoint(p0.add(width, height));
			ps.addPoint(p0.add(width, 0));
			ps.addPoint(p0);
			ps.endPath();

			return id;
		}

	}

	public static class ImageImpl extends ShapeWxH<Image.ImageBuilder> implements Image.ImageBuilder {

		public ImageImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
			super(canvas, matrix, pw, pos, pen);
		}

		@Override
		protected String writePath(PathWriter writer, boolean stroke, boolean fill) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public static class TextImpl extends ShapeImpl<Text.TextBuilder> implements Text.TextBuilder {
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
		protected String writePath(PathWriter writer, boolean stroke, boolean fill) {
			PathStroke ps = writer.addStroke();
			System.out.println("write: " + text);
			System.out.println("orig pen: " + pen);
			if (fill) {
				if (pen.fillColor() == Colors.TRANSPARENT)
					pen = pen.change().fillPaint(pen.strokeColor()).done();
				System.out.println("fill pen: " + pen);
		} else {
				pen = pen.change().fillPaint(Colors.TRANSPARENT).done();
			}
			if (!stroke)
				pen = pen.change().strokePaint(Colors.TRANSPARENT).done();
			System.out.println("final pen: " + pen);
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

	public static class PolyImpl extends ShapeImpl<Poly.LineBuilder> implements Poly.LineBuilder {
		protected boolean closed;
		protected PathStroke stroke;

		public PolyImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen, boolean closed) {
			super(canvas, matrix, pw, pos, pen);
			this.closed = closed;
		}

		@Override
		public LineBuilder to(Point next) {
			if (stroke == null) {
				PathPointImpl pp = new PathPointImpl(position, pen);
				stroke = pathWriter.addStroke();
				stroke.addPoint(pp);
			}
			stroke.addPoint(next);
			return this;
		}

		@Override
		public LineBuilder to(double x, double y) {
			return to(Point.point(x, y));
		}

		@Override
		public LineBuilder close() {
			closed = true;
			return this;
		}

		@Override
		protected String writePath(PathWriter writer, boolean stroke, boolean fill) {
			if (closed)
				this.stroke.addPoint(position);
			String id = String.format("poly@%x", System.identityHashCode(this));
			this.stroke.group(id);
			this.stroke.endPath();
			return id;
		}

	}

	public static class PathImpl extends ShapeImpl<Path.PathBuilder> implements Path.PathBuilder {
		protected boolean closed;
		protected PathStroke stroke;
		protected Point current;
		private String id;

		public PathImpl(Canvas canvas, Matrix3x2dc matrix, PathWriter pw, Point pos, Pen pen) {
			super(canvas, matrix, pw, pos, pen);
			this.closed = false;
			this.id = String.format("path@%x", System.identityHashCode(this));
		}

		@Override
		public Path.PathBuilder moveTo(Point next) {
			if (position == null)
				position = next;
			finishStroke();
			current = next;
			return this;
		}

		protected void finishStroke() {
			if (stroke != null) {
				if (closed && current != null) {
					stroke.addPoint(current);
					closed = false;
				}
				stroke.endPath();
				stroke = null;
			}
		}

		@Override
		public Path.PathBuilder moveTo(double x, double y) {
			return moveTo(Point.point(x, y));
		}

		@Override
		public Path.PathBuilder drawTo(Point next) {
			if (current == null)
				current = position;
			if (stroke == null) {
				PathPointImpl pp = new PathPointImpl(current, pen);
				stroke = pathWriter.addStroke();
				stroke.group(id);
				stroke.addPoint(pp);
			}
			stroke.addPoint(next);
			return this;
		}

		@Override
		public Path.PathBuilder drawTo(double x, double y) {
			return drawTo(Point.point(x, y));
		}

		@Override
		public Path.PathBuilder close() {
			closed = true;
			return this;
		}

		@Override
		protected String writePath(PathWriter writer, boolean stroke, boolean fill) {
			finishStroke();
			return id;
		}

	}

	public PenBuilder<? extends T> penChange() {
		throw new UnsupportedOperationException();
	}

	public Pen pen() {
		if (penBuilder != null) {
			pen = penBuilder.done();
			penBuilder = null;
		}
		return pen;
	}

	@SuppressWarnings("unchecked")
	public T pen(Pen newPen) {
		pen = newPen;
		penBuilder = null;
		return (T) this;
	}

	public T strokeWidth(double pixels) {
		return pen(pen.change().strokeWidth(pixels).done());
	}

	public T strokePaint(Color ink) {
		if (ink == null)
			throw new NullPointerException();
		return pen(pen.change().strokePaint(ink).done());
	}

	public T strokeOpacity(double opacity) {
		return pen(pen.change().strokeOpacity(opacity).done());
	}

	public T fillPaint(Color ink) {
		if (ink == null)
			throw new NullPointerException();
		return pen(pen.change().fillPaint(ink).done());
	}

	public T fillOpacity(double opacity) {
		return pen(pen.change().fillOpacity(opacity).done());
	}

	public T smooth(SmoothType smooth) {
		if (smooth == null)
			throw new NullPointerException();
		return pen(pen.change().smooth(smooth).done());
	}

	public T smooth(SmoothType smooth, double amount) {
		if (smooth == null)
			throw new NullPointerException();
		return pen(pen.change().smooth(smooth, amount).done());
	}

}
