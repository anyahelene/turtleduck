package turtleduck.tea;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

import turtleduck.comms.Message;
import turtleduck.display.Canvas;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.image.Image;
import turtleduck.tea.net.SockJS;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class NativeTLayer extends BaseCanvas<NativeTScreen> {

	protected HTMLCanvasElement element;
	protected CanvasRenderingContext2D context;
	private Pen lastPen = null;
	private int channel;
	private SockJS socket;

	public NativeTLayer(String layerId, NativeTScreen screen, double width, double height, HTMLCanvasElement element) {
		super(layerId, screen, width, height);
		this.element = element;
		context = (CanvasRenderingContext2D) element.getContext("2d");
	}

	@Override
	public Canvas clear() {
		element.clear();
		return this;
	}

	@Override
	public Canvas show() {
		element.setHidden(false);
		return this;
	}

	@Override
	public Canvas hide() {
		element.setHidden(true);
		return this;
	}

	@Override
	public Canvas flush() {
		// TODO Auto-generated method stub
		return this;

	}

	protected void drawLine(Stroke pen, Point from, Point to) {

	}

	@Override
	public Canvas clear(Fill fill) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Canvas draw(Path path) {
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

	@Override
	public Canvas draw(Drawing drawing) {
		// TODO Auto-generated method stub
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

	@Override
	public void drawImage(Point at, Image img) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected PathWriter pathWriter(boolean use3d) {
		// TODO Auto-generated method stub
		return null;
	}
}
