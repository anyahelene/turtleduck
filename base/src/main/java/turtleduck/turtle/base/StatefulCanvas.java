package turtleduck.turtle.base;

import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.Path;
import turtleduck.turtle.Stroke;

/**
 * This canvas sends its drawing commands to one or more other canvases.
 *
 */
public abstract class StatefulCanvas implements Canvas {
	private Stroke currentStroke;
	private Fill currentFill;
	private Geometry currentGeometry;

	public StatefulCanvas() {
	}

	protected void setup(Stroke stroke, Fill fill, Geometry geom) {
		if (stroke != null && stroke != currentStroke)
			changeStroke(stroke);
		if (fill != null && fill != currentFill)
			changeFill(fill);
		if (geom != null && geom != currentGeometry)
			changeGeometry(geom);
	}

	@Override
	public Canvas dot(Stroke stroke, Geometry geom, Point point) {
		setup(stroke, null, geom);
		strokeDot(point);
		return this;
	}

	@Override
	public Canvas line(Stroke stroke, Geometry geom, Point from, Point to) {
		setup(stroke, null, geom);
		strokeLine(from, to);
		return this;
	}

	@Override
	public Canvas polyline(Stroke stroke, Fill fill, Geometry geom, Point... points) {
		setup(stroke, fill, geom);
		loadArray(points);
		if (fill != null)
			fillPolyline();
		if (stroke != null)
			strokePolyline();
		return this;
	}

	@Override
	public Canvas polygon(Stroke stroke, Fill fill, Geometry geom, Point... points) {
		setup(stroke, fill, geom);
		loadArray(points);
		if (fill != null)
			fillPolygon();
		if (stroke != null)
			strokePolygon();
		return this;
	}

	@Override
	public Canvas triangles(Stroke stroke, Fill fill, Geometry geom, Point... points) {
		setup(stroke, fill, geom);
		loadArray(points);
		if (fill != null)
			fillTriangles();
		if (stroke != null)
			strokeTriangles();
		return this;
	}

	@Override
	public Canvas shape(Stroke stroke, Fill fill, Geometry geom, IShape shape) {
		setup(stroke, fill, geom);
		if (fill != null)
			fillShape(shape);
		if (stroke != null)
			strokeShape(shape);
		return this;
	}

	@Override
	public Canvas path(Stroke stroke, Fill fill, Geometry geom, Path path) {
		setup(stroke, fill, geom);
		if (fill != null)
			fillPath(path);
		if (stroke != null)
			strokePath(path);
		return this;
	}

	@Override
	public Canvas clear() {
		clearAll();
		return this;
	}
	@Override
	public Canvas clear(Fill fill) {
		setup(null, fill, null);
		fillAll();
		return this;
	}

	protected abstract void changeStroke(Stroke stroke);

	protected abstract void changeFill(Fill fill);

	protected abstract void changeGeometry(Geometry geom);

	protected abstract void strokeDot(Point point);

	protected abstract void strokeLine(Point from, Point to);

	protected abstract void strokePolyline();

	protected abstract void strokePolygon();

	protected abstract void fillPolyline();

	protected abstract void fillPolygon();

	protected abstract void fillAll();
	protected abstract void clearAll();

	protected abstract void strokeTriangles();

	protected abstract void fillTriangles();

	protected abstract void strokeShape(IShape shape);

	protected abstract void strokePath(Path path);

	protected abstract void fillShape(IShape shape);

	protected abstract void fillPath(Path path);

	protected abstract void loadArray(Point... points);
}
