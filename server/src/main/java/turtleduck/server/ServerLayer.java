package turtleduck.server;

import java.util.ArrayList;
import java.util.List;

import turtleduck.comms.Message;
import turtleduck.display.Layer;
import turtleduck.display.impl.BaseLayer;
import turtleduck.geometry.Point;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.PathStroke;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.PathWriterImpl;
import turtleduck.turtle.Pen;

public class ServerLayer extends BaseLayer<ServerScreen> {
	private TurtleDuckSession session;
	private PathWriterImpl pathWriter = new PathWriterImpl();

	public ServerLayer(String layerId, ServerScreen screen, double width, double height, TurtleDuckSession session) {
		super(layerId, screen, width, height);
		this.session = session;
	}

	@Override
	public Layer clear() {
		return this;
	}

	@Override
	public Layer show() {
		return this;
	}

	@Override
	public Layer hide() {
		return this;
	}

	public void draw(Path path) {
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
	}

	public void render(boolean frontToBack) {
		if (pathWriter.hasNextStroke()) {
			PathStroke stroke;
			StringBuilder b = new StringBuilder();
			while ((stroke = pathWriter.nextStroke()) != null) {
				List<PathPoint> points = stroke.points();
				PathPoint from = points.get(0);
				Pen pen = from.pen();
				Pen lastPen = null;

				for (int i = 1; i < points.size(); i++) {
					PathPoint to = points.get(i);

					if (pen != lastPen) {
						if (b.length() != 0)
							b.append(" && ");
						b.append(String.format("%s $ M %.1f %.1f ", pen.strokePaint().toString(), from.x(), from.y()));
						lastPen = pen;
					}
					b.append(String.format("L %.1f %.1f ", to.x(), to.y()));

					from = to;
					pen = to.pen();
				}
				if (b.length() > 4096) {
					session.send(Message.createStringData(0, b.toString()));
					b.delete(0, b.length());
				}
			}
			if (b.length() > 0)
				session.send(Message.createStringData(0, b.toString()));
		}
	}

	protected PathWriter pathWriter(boolean use3d) {
		return pathWriter;
	}

	@Override
	public Layer flush() {
		return this;
	}

}
