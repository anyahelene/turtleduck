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
public class DistributionCanvas implements Canvas {

	private final Canvas[] canvases;

	public DistributionCanvas(Canvas... canvases) {
		this.canvases = canvases;
	}

	@Override
	public Canvas dot(Stroke pen, Geometry geom, Point point) {
		for (Canvas c : canvases)
			c.dot(pen, geom, point);
		return this;
	}

	@Override
	public Canvas line(Stroke pen, Geometry geom, Point from, Point to) {
		for (Canvas c : canvases)
			c.line(pen, geom, from, to);
		return this;
	}

	@Override
	public Canvas polyline(Stroke pen, Fill fill, Geometry geom, Point... points) {
		for (Canvas c : canvases)
			c.polygon(pen, fill, geom, points);
		return this;
	}

	@Override
	public Canvas polygon(Stroke pen, Fill fill, Geometry geom, Point... points) {
		for (Canvas c : canvases)
			c.polygon(pen, fill, geom, points);
		return this;
	}

	@Override
	public Canvas triangles(Stroke pen, Fill fill, Geometry geom, Point... points) {
		for (Canvas c : canvases)
			c.triangles(pen, fill, geom, points);
		return this;
	}

	@Override
	public Canvas shape(Stroke pen, Fill fill, Geometry geom, IShape shape) {
		for (Canvas c : canvases)
			c.shape(pen, fill, geom, shape);
		return this;
	}

	@Override
	public Canvas path(Stroke pen, Fill fill, Geometry geom, Path path) {
		for (Canvas c : canvases)
			c.path(pen, fill, geom, path);
		return this;
	}

	@Override
	public Canvas clear() {
		for (Canvas c : canvases)
			c.clear();
		return this;
	}

	@Override
	public Canvas clear(Fill fill) {
		for (Canvas c : canvases)
			c.clear(fill);
		return this;
	}
}