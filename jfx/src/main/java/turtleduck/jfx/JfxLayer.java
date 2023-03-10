package turtleduck.jfx;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import turtleduck.display.Layer;
import turtleduck.display.impl.BaseLayer;
import turtleduck.geometry.Point;
import turtleduck.paths.Path;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;

public class JfxLayer extends BaseLayer<JfxScreen> implements Layer {
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
	public Layer clear() {
		fxCanvas.getGraphicsContext2D().clearRect(0, 0, fxCanvas.getWidth(), fxCanvas.getHeight());
		return this;
	}


	@Override
	public Layer hide() {
		fxCanvas.setVisible(false);
		return this;
	}

	@Override
	public Layer show() {
		fxCanvas.setVisible(true);
		return this;
	}

	@Override
	public Layer flush() {
		if (Platform.isFxApplicationThread()) {
			for (Path p = queue.poll(); p != null; p = queue.poll()) {
				drawPath(p);
			}
		}
		return this;
	}

	public void draw(Path path) {
		if (Platform.isFxApplicationThread()) {
			drawPath(path);
		} else {
			try {
				queue.put(path);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected void drawPath(Path path) {
		Point from = path.first();
		Pen pen = path.pointPen(0);
		for (int i = 1; i < path.size(); i++) {
			Point to = path.point(i);
			if (pen != lastPen) {
				changePen(pen);
				lastPen = pen;
			}
			context.strokeLine(from.x(), from.y(), to.x(), to.y());
			from = to;
			pen = path.pointPen(i);
		}
	}


	protected void changePen(Pen pen) {
		context.setStroke(JfxColor.toJfxPaint(pen.strokeColor()));
		context.setLineWidth(pen.strokeWidth());
		context.setFill(JfxColor.toJfxPaint(pen.fillColor()));
	}


	protected PathWriter pathWriter(boolean use3d) {
		// TODO Auto-generated method stub
		return null;
	}

}
