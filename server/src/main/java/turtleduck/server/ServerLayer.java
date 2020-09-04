package turtleduck.server;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.comms.Message;
import turtleduck.display.Canvas;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.image.Image;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class ServerLayer extends BaseCanvas<ServerScreen> {
	private int channel;
	private String name;
	private TurtleDuckSession session;

	public ServerLayer(String layerId, ServerScreen screen, double width, double height, TurtleDuckSession session) {
		super(layerId, screen, width, height);
		this.session = session;
	}

	@Override
	public Canvas clear() {
		return this;
	}

	@Override
	public Canvas show() {
		return this;
	}

	@Override
	public Canvas hide() {
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
		Pen lastPen = null;
		Pen pen = path.pointPen(0);
		List<String> paths = new ArrayList<>();
		StringBuilder b = new StringBuilder();
		for (int i = 1; i < path.size(); i++) {
			Point to = path.point(i);
			if (pen != lastPen) {
				if (b.length() != 0)
					b.append(" && ");
				b.append(String.format("%s $ M %.1f %.1f ", pen.strokePaint().toString(), from.x(), from.y()));
//				buf.appendString("P " + pen.strokePaint().toString());
				lastPen = pen;
			}

			b.append(String.format("L %.1f %.1f ", to.x(), to.y()));

			from = to;
			pen = path.pointPen(i);
		}

		session.send(Message.createStringData(0, b.toString()));
		return this;
	}

	@Override
	public Canvas draw(Drawing drawing) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void drawImage(Point at, Image img) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected PathWriter pathWriter() {
		// TODO Auto-generated method stub
		return null;
	}
}
