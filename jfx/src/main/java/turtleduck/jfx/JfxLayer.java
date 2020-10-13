package turtleduck.jfx;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import turtleduck.display.Canvas;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.display.impl.BaseLayer;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.image.Image;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class JfxLayer extends BaseCanvas<JfxScreen> implements Canvas {
	private static final int QUEUE_SIZE = 100;
	private final javafx.scene.canvas.Canvas fxCanvas;
	private final BlockingQueue<Path> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
	protected final GraphicsContext context;
	private Pen lastPen = null;

	public JfxLayer(String id, double width, double height, JfxScreen jfxScreen, javafx.scene.canvas.Canvas canvas) {
		super(id, jfxScreen, width, height);
		this.fxCanvas = canvas;
		this.context = canvas.getGraphicsContext2D();
	}

	@Override
	public Canvas clear() {
		fxCanvas.getGraphicsContext2D().clearRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
		return this;
	}

	@Override
	public Canvas clear(Fill fill) {
		fxCanvas.getGraphicsContext2D().clearRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
		return this;
	}

	@Override
	public Canvas hide() {
		fxCanvas.setVisible(false);
		return this;
	}

	@Override
	public Canvas show() {
		fxCanvas.setVisible(true);
		return this;
	}

	@Override
	public Canvas flush() {
		if (Platform.isFxApplicationThread()) {
			for (Path p = queue.poll(); p != null; p = queue.poll()) {
				drawPath(p);
			}
		}
		return this;
	}

	@Override
	public Canvas draw(Path path) {
		if (Platform.isFxApplicationThread()) {
			drawPath(path);
		} else {
			try {
				queue.put(path);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return this;
	}

	protected void drawPath(Path path) {
		Point from = path.first();
		Pen pen = path.pointPen(0);
		for (int i = 1; i < path.size(); i++) {
			Point to = path.point(i);
			if (pen != lastPen) {
				changeStroke(pen);
				lastPen = pen;
			}
			context.strokeLine(from.x(), from.y(), to.x(), to.y());
			from = to;
			pen = path.pointPen(i);
		}
	}

	@Override
	public Canvas draw(Drawing drawing) {
		// TODO Auto-generated method stub
		return this;
	}

	protected void changeFill(Fill fill) {
		context.setFill(JfxColor.toJfxPaint(fill.fillPaint()));
	}

	protected void changeStroke(Stroke stroke) {
		context.setStroke(JfxColor.toJfxPaint(stroke.strokePaint()));
		context.setLineWidth(stroke.strokeWidth());
	}

	@Override
	public void drawImage(Point at, Image img) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void drawLine(Stroke stroke, Point from, Point to) {
		// TODO Auto-generated method stub

	}

	@Override
	protected PathWriter pathWriter(boolean use3d) {
		// TODO Auto-generated method stub
		return null;
	}

}
