package turtleduck.tea;

import java.util.List;

import org.joml.Matrix4f;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

import turtleduck.comms.Message;
import turtleduck.display.Layer;
import turtleduck.display.impl.BaseLayer;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.image.Image;
import turtleduck.tea.net.SockJS;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.PathStroke;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.PathWriterImpl;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class NativeTLayer extends BaseLayer<NativeTScreen> {

	protected HTMLCanvasElement element;
	protected CanvasRenderingContext2D context;
	private Pen lastPen = null;
	private int channel;
	private SockJS socket;
	protected TeaPathWriter pathWriter = new TeaPathWriter();

	public NativeTLayer(String layerId, NativeTScreen screen, double width, double height, HTMLCanvasElement element) {
		super(layerId, screen, width, height);
		this.element = element;
		context = (CanvasRenderingContext2D) element.getContext("2d");
	}

	@Override
	public Layer clear() {
		element.clear();
		return this;
	}

	@Override
	public Layer show() {
		element.setHidden(false);
		return this;
	}

	@Override
	public Layer hide() {
		element.setHidden(true);
		return this;
	}

	@Override
	public Layer flush() {
		// TODO Auto-generated method stub
		return this;

	}

	protected void drawLine(Stroke pen, Point from, Point to) {

	}

	public Layer draw(Path path) {
		Point from = path.first();
		Pen pen = path.pointPen(0);
		context.moveTo(from.x(), from.y());

		for (int i = 1; i < path.size(); i++) {
			Point to = path.point(i);
			if (pen != lastPen) {
				context.stroke();
				changeStroke(pen);
				lastPen = pen;
			}

			context.lineTo(to.x(), to.y());

			from = to;
			pen = path.pointPen(i);
		}
		context.stroke();
		return this;
	}

	protected void changeFill(Fill fill) {
		context.setFillStyle(fill.fillPaint().toString());
	}

	protected void changeStroke(Stroke stroke) {
		context.setStrokeStyle(stroke.strokePaint().toString());
		context.setLineWidth(stroke.strokeWidth());
	}

	public void receive(Message obj) {
		if (obj.type().equals("Data")) {
			context.fillText(((Message.StringDataMessage) obj).data(), 50, 50);
		}
	}

	public void render(boolean frontToBack) {
		if (pathWriter.hasNextStroke()) {
			PathStroke stroke;
			while ((stroke = pathWriter.nextStroke()) != null) {
				List<PathPoint> points = stroke.points();
				PathPoint from = points.get(0);
				Pen pen = from.pen();
				context.moveTo(from.x(), from.y());

				for (int i = 1; i < points.size(); i++) {
					PathPoint to = points.get(i);
					if (pen != lastPen) {
						context.stroke();
						changeStroke(pen);
						lastPen = pen;
					}

					context.lineTo(to.x(), to.y());

					from = to;
					pen = to.pen();
				}
				context.stroke();
			}
		}
	}

	protected PathWriter pathWriter(boolean use3d) {
		return pathWriter;
	}

	class TeaPathWriter extends PathWriterImpl {
	}

}
