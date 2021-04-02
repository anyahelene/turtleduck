package turtleduck.canvas;

import java.util.function.Function;

import org.joml.Matrix3x2d;

import turtleduck.colors.Color;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseLayer;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Orientation;
import turtleduck.geometry.Point;
import turtleduck.geometry.impl.Point3;
import turtleduck.messaging.CanvasService;
import turtleduck.shapes.Ellipse.EllipseBuilder;
import turtleduck.shapes.Image.ImageBuilder;
import turtleduck.shapes.Path.PathBuilder;
import turtleduck.shapes.Poly.LineBuilder;
import turtleduck.shapes.Rectangle.RectangleBuilder;
import turtleduck.shapes.Shape;
import turtleduck.shapes.ShapeImpl;
import turtleduck.shapes.Text.TextBuilder;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.PenBuilder;
import turtleduck.turtle.Turtle;
import turtleduck.turtle.Turtle3;
import turtleduck.turtle.Pen.SmoothType;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleImpl;
import turtleduck.turtle.impl.TurtleImpl3;
import turtleduck.util.Dict;

public class CanvasImpl<S extends Screen> extends BaseLayer<S> implements Canvas {
	protected Canvas parent = null;
	protected boolean matrixShared = false;
	protected Matrix3x2d matrix;
	protected Pen pen;
	protected PenBuilder<Pen> penBuilder = null;
	protected PathWriter pathWriter;
	protected Function<Boolean, PathWriter> pathWriterFactory;
	protected int nChildren = 0;
	protected CanvasService canvasService;

	public CanvasImpl(String layerId, S screen, double width, double height, Function<Boolean, PathWriter> pwFactory,
			CanvasService service) {
		super(layerId, screen, width, height);
		this.pen = new BasePen();
		this.matrix = new Matrix3x2d();
		this.pathWriterFactory = pwFactory;
		this.pathWriter = pwFactory.apply(false);
		this.canvasService = service;
	}

	public CanvasImpl(CanvasImpl<S> parent) {
		super(parent.id + "." + parent.nChildren++, parent.screen, parent.width, parent.height);
		this.parent = parent;
		this.pen = parent.pen();
		this.matrix = new Matrix3x2d(parent.matrix);
		this.pathWriterFactory = parent.pathWriterFactory;
		this.pathWriter = pathWriterFactory.apply(false);
	}

	public Canvas strokeWidth(double pixels) {
		return pen(pen.change().strokeWidth(pixels).done());
	}

	public Canvas strokePaint(Color ink) {
		if (ink == null)
			throw new NullPointerException();
		return pen(pen.change().strokePaint(ink).done());
	}

	public Canvas strokeOpacity(double opacity) {
		return pen(pen.change().strokeOpacity(opacity).done());
	}

	public Canvas fillPaint(Color ink) {
		if (ink == null)
			throw new NullPointerException();
		return pen(pen.change().fillPaint(ink).done());
	}

	public Canvas fillOpacity(double opacity) {
		return pen(pen.change().fillOpacity(opacity).done());
	}

	public Canvas smooth(SmoothType smooth) {
		if (smooth == null)
			throw new NullPointerException();
		return pen(pen.change().smooth(smooth).done());
	}

	public Canvas smooth(SmoothType smooth, double amount) {
		if (smooth == null)
			throw new NullPointerException();
		return pen(pen.change().smooth(smooth, amount).done());
	}

	public Canvas reflect(Direction axis) {
		throw new UnsupportedOperationException();
	}

	public Canvas shear(double shearX, double shearY) {
		throw new UnsupportedOperationException();
	}

	public Canvas rotate(double degrees) {
		matrix().rotate(degrees);
		return this;
	}

	public Canvas scale(double scale) {
		matrix().scale(scale);
		return this;
	}

	public Canvas scale(double scaleX, double scaleY) {
		matrix().scale(scaleX, scaleY);
		return this;
	}

	public Canvas translate(double deltaX, double deltaY) {
		matrix().translate(deltaX, deltaY);
		return this;
	}

	public Canvas translate(Point delta) {
		matrix().translate(delta.x(), delta.y());
		return this;
	}

	public Canvas getMatrix(Matrix3x2d destMatrix) {
		if (destMatrix == null)
			throw new NullPointerException();
		matrix.get(destMatrix);
		return this;
	}

	public Canvas multiply(Matrix3x2d rightMatrix) {
		if (rightMatrix == null)
			return this;
		matrix().mul(rightMatrix);
		return this;
	}

	public Canvas multiply(TransformContext2<?> rightTransform) {
		if (rightTransform == null)
			return this;
		Matrix3x2d src = new Matrix3x2d();
		rightTransform.getMatrix(src);
		matrix().mul(src);
		return this;
	}

	public Canvas setMatrix(TransformContext2<?> sourceTransform) {
		if (sourceTransform == null)
			matrix().identity();
		else
			sourceTransform.getMatrix(matrix());
		return this;
	}

	public Canvas setMatrix(Matrix3x2d sourceMatrix) {
		if (sourceMatrix == null)
			matrix().identity();
		else
			matrix().set(sourceMatrix);
		return this;
	}

	public Canvas clearMatrix() {
		matrix().identity();
		return this;
	}

	protected Matrix3x2d matrix() {
		if (matrixShared) {
			matrix = new Matrix3x2d(matrix);
			matrixShared = false;
		}
		return matrix;
	}

	@Override
	public PenBuilder<? extends Canvas> penChange() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Pen pen() {
		if (penBuilder != null) {
			pen = penBuilder.done();
			penBuilder = null;
		}
		return pen;
	}

	@Override
	public Canvas pen(Pen newPen) {
		pen = newPen;
		penBuilder = null;
		return this;
	}

	@Override
	public Canvas done() {
		return parent;
	}

	@Override
	public PathBuilder path() {
		matrixShared = true;
		return new ShapeImpl.PathImpl(this, matrix, pathWriter, Point.ZERO, pen);
	}

	@Override
	public RectangleBuilder rectangle() {
		matrixShared = true;
		return new ShapeImpl.RectangleImpl(this, matrix, pathWriter, Point.ZERO, pen);
	}

	@Override
	public EllipseBuilder ellipse() {
		matrixShared = true;
		return new ShapeImpl.EllipseImpl(this, matrix, pathWriter, Point.ZERO, pen);
	}

	@Override
	public TextBuilder text() {
		matrixShared = true;
		return new ShapeImpl.TextImpl(this, matrix, pathWriter, Point.ZERO, pen);
	}

	@Override
	public ImageBuilder image() {
		matrixShared = true;
		return new ShapeImpl.ImageImpl(this, matrix, pathWriter, Point.ZERO, pen);
	}

	@Override
	public LineBuilder polygon() {
		matrixShared = true;
		return new ShapeImpl.PolyImpl(this, matrix, pathWriter, Point.ZERO, pen, true);
	}

	@Override
	public LineBuilder polyline() {
		matrixShared = true;
		return new ShapeImpl.PolyImpl(this, matrix, pathWriter, Point.ZERO, pen, false);
	}

	@Override
	public Canvas drawRectangle(Point p0, Point p1) {
		Point diff = p1.sub(p0);
		rectangle().at(p0.add(diff.x() / 2, diff.y() / 2)).width(diff.x()).height(diff.y()).strokeAndFill();
		return this;
	}

	@Override
	public Canvas drawRectangle(Point center, double width, double height) {
		rectangle().at(center).width(width).height(height).strokeAndFill();
		return this;
	}

	@Override
	public Canvas drawCircle(Point center, double radius) {
		ellipse().at(center).radius(radius).stroke();
		return this;
	}

	@Override
	public Canvas drawEllipse(Point center, double width, double height) {
		ellipse().at(center).width(width).height(height).stroke();
		return this;
	}

	@Override
	public Canvas drawPoint(Point center) {
		ellipse().at(center).radius(1).stroke();
		return this;
	}

	@Override
	public Canvas drawPolyline(Point first, Point... points) {
		LineBuilder polyline = polyline();
		if (first != null) {
			polyline.at(first);
			for (Point p : points)
				polyline.to(p);
		} else if (points.length > 1) {
			polyline.at(points[0]);
			for (int i = 1; i < points.length; i++)
				polyline.to(points[i]);
		} else {
			throw new IllegalArgumentException("Too few points: " + points.length);
		}
		return this;
	}

	@Override
	public Canvas drawPolygon(Point first, Point... points) {
		LineBuilder polygon = polygon();
		if (first != null) {
			polygon.at(first);
			for (Point p : points)
				polygon.to(p);
		} else if (points.length > 1) {
			polygon.at(points[0]);
			for (int i = 1; i < points.length; i++)
				polygon.to(points[i]);
		} else {
			throw new IllegalArgumentException("Too few points: " + points.length);
		}
		return this;
	}

	@Override
	public Canvas drawShape(Point position, Shape shape) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Canvas spawn() {
		return new CanvasImpl<>(this);
	}

	@Override
	public Turtle turtle() {
		Turtle t = new TurtleImpl.SpecificTurtle(Point.ZERO, Direction.DUE_NORTH, new BasePen(), this);
		t.writePathsTo(pathWriterFactory.apply(false));
		return t;
	}

	@Override
	public Turtle3 turtle3() {
		Turtle3 t = new TurtleImpl3.SpecificTurtle3(Point3.ZERO, Orientation.DUE_NORTH, new BasePen(), this);
		t.writePathsTo(pathWriterFactory.apply(true));
		return t;
	}

	@Override
	public Canvas drawLine(Point from, Point to) {
		polyline().at(from).to(to).stroke();
		return this;
	}

	@Override
	public Layer clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Layer show() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Layer hide() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Layer flush() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Transformation<Canvas> transform(String id) {
		return new TransformationImpl<>(t -> {
			if (canvasService != null) {
				canvasService.styleObject(id, t.toCSS());
			}
			return this;
		});
	}

	@Override
	public CanvasService service() {
		return canvasService;
	}
	
	@Override
	public void onKeyPress(String javaScript) {
		if(canvasService != null) {
			canvasService.onKeyPress(javaScript);
		}
	}
	@Override
	public void evalScript(String javaScript) {
		if(canvasService != null) {
			canvasService.evalScript(javaScript);
		}
	}
	
	@Override
	public void setText(String id, String newText) {
		if(canvasService != null)
			canvasService.setText(id, newText);
	}
}
