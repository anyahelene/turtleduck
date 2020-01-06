package turtleduck.jfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import turtleduck.geometry.Point;
import turtleduck.jfx.internal.JfxApp;
import turtleduck.jfx.internal.JfxControl;
import turtleduck.jfx.internal.PointList;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Path;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;
import turtleduck.turtle.base.StatefulCanvas;

public class JfxCanvas extends StatefulCanvas {
	private static int totalOps = 0;
	private static int lineOps = 0;
	private static int fillOps = 0;
	private static int contextOps = 0;
	private static int lineSegments = 0;
	private static long flushTime = 0, flushN = 0;
	private Canvas canvas;
	GraphicsContext context;
	private double xs[], ys[];
	private int xyLen;
	private List<JfxControl> controls = new ArrayList<>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				printStats();
			}
		});
	}

	public JfxCanvas(String canvasId, Canvas canvas) {
		super(canvasId);
		this.canvas = canvas;
		this.context = canvas.getGraphicsContext2D();
		context.setLineCap(StrokeLineCap.BUTT);
	}

	@Override
	protected void changeStroke(Stroke stroke) {
		context.setStroke(JfxColor.toJfxPaint(stroke.strokePaint()));
		context.setLineWidth(stroke.strokeWidth());
		contextOps += 2;
	}

	@Override
	protected void changeFill(Fill fill) {
		context.setFill(JfxColor.toJfxPaint(fill.fillPaint()));
		contextOps += 1;
	}

	@Override
	protected void changeGeometry(Geometry geom) {
	}

	@Override
	protected void strokeDot(Point point) {
		double w = context.getLineWidth();
		double x = point.x() - w / 2;
		double y = point.y() - w / 2;
		context.save();
		context.setFill(context.getStroke());
		context.fillOval(x, y, w, w);
//		context.restore();
		totalOps++;
	}

	@Override
	protected void strokeLine(Point from, Point to) {
		context.strokeLine(from.x(), from.y(), to.x(), to.y());
		totalOps++;
		lineOps++;
		lineSegments++;
	}

	@Override
	protected void strokePolyline() {
		context.strokePolyline(xs, ys, xyLen);
		totalOps++;
		lineOps++;
		lineSegments += xyLen;
	}

	@Override
	protected void strokePolygon() {
		context.strokePolygon(xs, ys, xyLen);
		totalOps++;
		lineOps++;
		lineSegments += xyLen + 1;
	}

	@Override
	protected void fillPolyline() {
		context.fillPolygon(xs, ys, xyLen);
		totalOps++;
		fillOps++;
	}

	@Override
	protected void fillPolygon() {
		context.fillPolygon(xs, ys, xyLen);
		totalOps++;
		fillOps++;
	}

	@Override
	protected void strokeTriangles() {
		context.strokePolyline(xs, ys, xyLen);
		for (int i = 2; i < xyLen; i++) {
			context.strokeLine(xs[i], ys[i], xs[i - 2], ys[i - 2]);
			totalOps++;
			lineOps++;
			lineSegments++;
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
			totalOps++;
			fillOps++;
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
				xs[i] = points[i].x();
				ys[i] = points[i].y();
			}
		}
	}

	@Override
	protected void fillAll() {
		context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		totalOps++;
		fillOps++;
	}

	@Override
	protected void clearAll() {
		context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		totalOps++;
		fillOps++;
	}

	public Object beginLines() {
		StrokeLineCap lineCap = context.getLineCap();
		if (lineCap != StrokeLineCap.BUTT) {
			context.setLineCap(StrokeLineCap.BUTT);
			return lineCap;
		} else {
			return null;
		}
	}

	public void endLines(Object obj) {
		if (obj instanceof StrokeLineCap)
			context.setLineCap((StrokeLineCap) obj);
	}

	public void strokePolyline(Stroke stroke, Geometry geom, PointList points) {
		setup(stroke, null, geom);
		context.strokePolyline(points.xs(), points.ys(), points.size());
		totalOps++;
		lineOps++;
		lineSegments += points.size();
	}

	public void strokePolygon(Stroke stroke, Geometry geom, PointList points) {
		setup(stroke, null, geom);
		context.strokePolygon(points.xs(), points.ys(), points.size());
		totalOps++;
		lineOps++;
		lineSegments += points.size() + 1;
	}

	public void fillPolygon(Fill fill, Geometry geom, PointList points) {
		setup(null, fill, geom);
		context.fillPolygon(points.xs(), points.ys(), points.size());
		totalOps++;
		fillOps++;
		lineSegments += points.size() + 1;
	}

	@Override
	public void flush() {
		this.context = canvas.getGraphicsContext2D();
		long t = System.currentTimeMillis();

		for (JfxControl j : controls) {
			j.flush();
		}
		flushTime += System.currentTimeMillis() - t;
		flushN++;
//		this.context = null;
	}

	public static void printStats() {
		System.err.println("Canvas stats: ");
		System.err.println("  Total ops: " + totalOps);
		System.err.println("  Line ops: " + lineOps);
		System.err.println("  Fill ops: " + fillOps);
		System.err.println("  Context ops: " + contextOps);
		System.err.println("  Line segments: " + lineSegments);
		System.err.printf("  Flushes:      %10d\n", flushN);
		System.err.printf("    Total time: %10.5f s\n", (flushTime / 1000.0));
		System.err.printf("    Average:    %10.5f ms\n", (flushTime * 1.0)/flushN);
		JfxControl.printStats();
		PointList.printStats();
		JfxApp.printStats();
	}

	@Override
	public TurtleControl createControl() {
		JfxControl journal = new JfxControl(this, null);
		controls.add(journal);
		return journal;
	}

}
