package turtleduck.turtle.base;

import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.PathBuilder;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;

/**
 * This canvas sends its drawing commands to one or more other canvases.
 *
 */
public class DistributionCanvas extends BaseCanvas {

	private final Canvas[] canvases;

	public DistributionCanvas(String id, Canvas... canvases) {
		super(id);
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
	public Canvas path(Stroke pen, Fill fill, Geometry geom, PathBuilder path) {
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

	@Override
	public TurtleControl createControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		for (Canvas c : canvases)
			c.flush();
	}
}
