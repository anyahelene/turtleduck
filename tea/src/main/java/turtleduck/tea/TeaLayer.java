package turtleduck.tea;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.display.Layer;
import turtleduck.display.impl.BaseLayer;
import turtleduck.geometry.Point;
import turtleduck.messaging.CanvasService;
import turtleduck.messaging.Message;
import turtleduck.messaging.MessageWriter;
import turtleduck.messaging.generated.CanvasServiceProxy;
import turtleduck.shapes.Text;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.PathStroke;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.PathWriterImpl;
import turtleduck.turtle.Pen;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

public class TeaLayer extends BaseLayer<TeaScreen> {
	private PathWriterImpl pathWriter = new PathWriterImpl();
	protected CanvasService canvas;

	public TeaLayer(String layerId, TeaScreen screen, double width, double height, CanvasService service) {
		super(layerId, screen, width, height);
		this.canvas = service;
	}

	@Override
	public Layer clear() {
		pathWriter.clear();
		canvas.clear();
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

	public void render(boolean frontToBack) {
		if (pathWriter.hasNextStroke()) {
			Array paths = Array.create();
			PathStroke stroke;
			StringBuilder b = new StringBuilder();
			Dict path = Dict.create();
			path.put("GROUP", screen.currentGroup);
//			paths.add(path);
			int len = 0;
			while ((stroke = pathWriter.nextStroke()) != null) {
				List<PathPoint> points = stroke.points();
				if (points.isEmpty())
					continue;
				PathPoint from = points.get(0);
				Pen pen = from.pen();
				Pen lastPen = null;
				if (stroke.text() != null) {
					if (stroke.group() != null)
						path.put(CanvasService.OBJ_ID, stroke.group());
					path.put("TEXT", stroke.text());
					path.put(CanvasService.X, from.x());
					path.put(CanvasService.Y, from.y());
					path.put("ALIGN", from.annotation(Text.TEXT_ALIGN));
					path.put(CanvasService.FONT_SIZE, from.annotation(Text.TEXT_FONT_SIZE));
					Color c = pen.strokeColor();
					if (c != null && c != Colors.TRANSPARENT) {
						path.put("STROKE", c.toString());
						path.put(CanvasService.WIDTH, pen.strokeWidth());
					} else {
						path.put("STROKE", null);
						path.put(CanvasService.WIDTH, null);
					}
					c = pen.fillColor();
					if (c != null && c != Colors.TRANSPARENT)
						path.put("FILL", c.toString());
					else
						path.put("FILL", null);

					paths.add(path);

					path = Dict.create();
					if (c != null && c != Colors.TRANSPARENT) {
						path.put("STROKE", c.toString());
						path.put(CanvasService.WIDTH, pen.strokeWidth());
					} else {
						path.put("STROKE", null);
						path.put(CanvasService.WIDTH, null);
					}
					c = pen.fillColor();
					if (c != null && c != Colors.TRANSPARENT)
						path.put("FILL", c.toString());
					else
						path.put("FILL", null);
					path.put("GROUP", screen.currentGroup);
					b = new StringBuilder();
					continue;
				}
				if (stroke.group() != null)
					path.put("GROUP", stroke.group());
				for (int i = 1; i < points.size(); i++) {
					PathPoint to = points.get(i);

					if (pen != lastPen) {
						if (b.length() != 0) {
							len += b.length();
							path.put("PATH", b.toString());
							paths.add(path);

							path = Dict.create();
							if (stroke.group() != null)
								path.put("GROUP", stroke.group());
							else
								path.put("GROUP", screen.currentGroup);
							b = new StringBuilder();
						}
						b.append(String.format("M %s %s", from.x(), from.y()));
						lastPen = pen;
						Color c = pen.strokeColor();
						if (c != null && c != Colors.TRANSPARENT) {
							path.put("STROKE", c.toString());
							path.put(CanvasService.WIDTH, pen.strokeWidth());
						} else {
							path.put("STROKE", null);
							path.put(CanvasService.WIDTH, null);
						}
						c = pen.fillColor();
						if (c != null && c != Colors.TRANSPARENT)
							path.put("FILL", c.toString());
						else
							path.put("FILL", null);
					}
					b.append(String.format("L %s %s", to.x(), to.y()));

					from = to;
					pen = to.pen();
				}
				if (b.length() != 0) {
					len += b.length();
					path.put("PATH", b.toString());
					paths.add(path);

					path = Dict.create();
					Color c = pen.strokeColor();
					if (c != null && c != Colors.TRANSPARENT) {
						path.put("STROKE", c.toString());
						path.put(CanvasService.WIDTH, pen.strokeWidth());
					} else {
						path.put("STROKE", null);
						path.put(CanvasService.WIDTH, null);
					}
					c = pen.fillColor();
					if (c != null && c != Colors.TRANSPARENT)
						path.put("FILL", c.toString());
					else
						path.put("FILL", null);
					path.put("GROUP", screen.currentGroup);
					b = new StringBuilder();
				}
				if (len > 4096) {
					canvas.drawPath(paths);
					paths = Array.create();
				}
			}
			if (paths.size() > 0)
				canvas.drawPath(paths);
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
