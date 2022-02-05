package turtleduck.tea;

import java.util.List;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

import turtleduck.display.Layer;
import turtleduck.display.impl.BaseLayer;
import turtleduck.geometry.Point;
import turtleduck.paths.Path;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PathWriterImpl;
import turtleduck.tea.net.SockJS;

public class NativeTLayer extends BaseLayer<NativeTScreen> {

	protected HTMLCanvasElement element;
	protected CanvasRenderingContext2D context;
	private Pen lastPen = null;

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

	protected void drawLine(Pen pen, Point from, Point to) {

	}

	public Layer draw(Path path) {
		Point from = path.first();
		Pen pen = path.pointPen(0);
		context.moveTo(from.x(), from.y());

		for (int i = 1; i < path.size(); i++) {
			Point to = path.point(i);
			if (pen != lastPen) {
				context.stroke();
				changePen(pen);
				lastPen = pen;
			}

			context.lineTo(to.x(), to.y());

			from = to;
			pen = path.pointPen(i);
		}
		context.stroke();
		return this;
	}

	protected void changePen(Pen pen) {
		context.setStrokeStyle(pen.strokeColor().toString());
		context.setLineWidth(pen.strokeWidth());
		context.setFillStyle(pen.fillColor().toString());
	}

	public void receive(String obj) {
		context.fillText(obj, 50, 50);
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
						changePen(pen);
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
