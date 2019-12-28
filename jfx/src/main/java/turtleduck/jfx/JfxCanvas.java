package turtleduck.jfx;

import java.util.Arrays;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import turtleduck.geometry.Point;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.Path;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.base.StatefulCanvas;

public class JfxCanvas extends StatefulCanvas {
	private Canvas canvas;
	private GraphicsContext context;
	private double xs[], ys[];
	private int xyLen;

	public JfxCanvas(Canvas canvas) {
		this.canvas = canvas;
		this.context = canvas.getGraphicsContext2D();
		context.setLineCap(StrokeLineCap.BUTT);
	}

	@Override
	protected void changeStroke(Stroke stroke) {
		context.setStroke(JfxColor.toJfxPaint(stroke.strokePaint()));
		context.setLineWidth(stroke.strokeWidth());
	}

	@Override
	protected void changeFill(Fill fill) {
		context.setFill(JfxColor.toJfxPaint(fill.fillPaint()));
	}

	@Override
	protected void changeGeometry(Geometry geom) {
	}

	@Override
	protected void strokeDot(Point point) {
		double w = context.getLineWidth();
		double x = point.getX() - w / 2;
		double y = point.getY() - w / 2;
		context.fillOval(x, y, w, w);
	}

	@Override
	protected void strokeLine(Point from, Point to) {
		context.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
	}

	@Override
	protected void strokePolyline() {
		context.strokePolyline(xs, ys, xyLen);
	}

	@Override
	protected void strokePolygon() {
		context.strokePolygon(xs, ys, xyLen);
	}

	@Override
	protected void fillPolyline() {
		context.fillPolygon(xs, ys, xyLen);
	}

	@Override
	protected void fillPolygon() {
		context.fillPolygon(xs, ys, xyLen);
	}

	@Override
	protected void strokeTriangles() {
		context.strokePolyline(xs, ys, xyLen);
		for (int i = 2; i < xyLen; i++) {
			context.strokeLine(xs[i], ys[i], xs[i - 2], ys[i - 2]);
		}
	}

	@Override
	protected void fillTriangles() {
		double triXs[] = { 0, 0, 0 }, triYs[] = { 0, 0, 0 };
		for (int i = 2; i < xyLen; i++) {
			triXs[0] = xs[i - 2];
			triYs[0] = ys[i - 2];
			triXs[1] = xs[i - 1];
			triYs[1] = ys[i - 1];
			triXs[2] = xs[i];
			triYs[2] = ys[i];
			context.fillPolygon(triXs, triYs, 3);
		}
	}

	@Override
	protected void strokeShape(IShape shape) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void strokePath(Path path) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fillShape(IShape shape) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fillPath(Path path) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadArray(Point... points) {
		if (points == null) {
			xs = null;
			ys = null;
		} else {
			if (xs == null || xs.length < points.length) {
				xs = new double[points.length];
				ys = new double[points.length];
			}
			xyLen = points.length;
			for (int i = 0; i < points.length; i++) {
				xs[i] = points[i].getX();
				ys[i] = points[i].getY();
			}
		}
	}


	@Override
	protected void fillAll() {
		context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	@Override
	protected void clearAll() {
		context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

}
