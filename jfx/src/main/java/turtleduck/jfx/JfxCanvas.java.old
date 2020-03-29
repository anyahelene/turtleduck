package turtleduck.jfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import turtleduck.geometry.Point;
import turtleduck.jfx.internal.JfxApp;
import turtleduck.jfx.internal.PointList;
import turtleduck.turtle.Fill;
import turtleduck.turtle.IShape;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.base.StatefulCanvas;

public class JfxCanvas  {
	private static int totalOps = 0;
	private static int lineOps = 0;
	private static int fillOps = 0;
	private static int contextOps = 0;
	private static int lineSegments = 0;
	private static long flushTime = 0, flushN = 0;
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				printStats();
			}
		});
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
		System.err.printf("    Average:    %10.5f ms\n", (flushTime * 1.0) / flushN);
		PointList.printStats();
		JfxApp.printStats();
	}

	private Canvas canvas;
	GraphicsContext context;
	private double xs[], ys[];

	private int xyLen;


	public JfxCanvas(String canvasId, Canvas canvas) {
		super(canvasId);
		this.canvas = canvas;
		this.context = canvas.getGraphicsContext2D();
		context.setLineCap(StrokeLineCap.BUTT);
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


	@Override
	protected void clearAll() {
		synchronized (context) {
			context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
		totalOps++;
		fillOps++;
	}

	
	public void endLines(Object obj) {
		if (obj instanceof StrokeLineCap)
			context.setLineCap((StrokeLineCap) obj);
	}

	@Override
	protected void fillAll() {
		synchronized (context) {
			context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
		totalOps++;
		fillOps++;
	}

	@Override
	protected void fillPolygon() {
		synchronized (context) {
			context.fillPolygon(xs, ys, xyLen);
		}
		totalOps++;
		fillOps++;
	}

	public void fillPolygon(Fill fill, PointList points) {

		setup(null, fill);
		synchronized (context) {
			context.fillPolygon(points.xs(), points.ys(), points.size());
		}
		totalOps++;
		fillOps++;
		lineSegments += points.size() + 1;
	}

	@Override
	protected void fillPolyline() {
		synchronized (context) {
			context.fillPolygon(xs, ys, xyLen);
		}
		totalOps++;
		fillOps++;
	}

	@Override
	protected void fillShape(IShape shape) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fillTriangles() {
		synchronized (context) {
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
	}

	@Override
	public void flush() {
		this.context = canvas.getGraphicsContext2D();
		long t = System.currentTimeMillis();

	
		flushTime += System.currentTimeMillis() - t;
		flushN++;
//		this.context = null;
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
	protected void strokeDot(Point point) {
		synchronized (context) {
			double w = context.getLineWidth();
			double x = point.x() - w / 2;
			double y = point.y() - w / 2;
			context.save();
			context.setFill(context.getStroke());
			context.fillOval(x, y, w, w);
//		context.restore();
			totalOps++;
		}
	}

	@Override
	protected void strokeLine(Point from, Point to) {
		synchronized (context) {
			context.strokeLine(from.x(), from.y(), to.x(), to.y());
		}
		totalOps++;
		lineOps++;
		lineSegments++;
	}


	@Override
	protected void strokePolygon() {
		synchronized (context) {
			context.strokePolygon(xs, ys, xyLen);
		}
		totalOps++;
		lineOps++;
		lineSegments += xyLen + 1;
	}

	public void strokePolygon(Stroke stroke, PointList points) {
		setup(stroke, null);
		synchronized (context) {
			context.strokePolygon(points.xs(), points.ys(), points.size());
		}
		totalOps++;
		lineOps++;
		lineSegments += points.size() + 1;
	}

	@Override
	protected void strokePolyline() {
		synchronized (context) {
			context.strokePolyline(xs, ys, xyLen);
		}
		totalOps++;
		lineOps++;
		lineSegments += xyLen;
	}

	public void strokePolyline(Stroke stroke, PointList points) {
		synchronized (context) {
			setup(stroke, null);
			context.strokePolyline(points.xs(), points.ys(), points.size());
		}
		totalOps++;
		lineOps++;
		lineSegments += points.size();
	}

	@Override
	protected void strokeShape(IShape shape) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void strokeTriangles() {
		synchronized (context) {
			context.strokePolyline(xs, ys, xyLen);
			for (int i = 2; i < xyLen; i++) {
				context.strokeLine(xs[i], ys[i], xs[i - 2], ys[i - 2]);
				totalOps++;
				lineOps++;
				lineSegments++;
			}
		}
	}

}
