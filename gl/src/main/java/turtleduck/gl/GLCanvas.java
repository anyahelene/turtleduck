package turtleduck.gl;

import static org.lwjgl.opengl.GL30.*;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.VertexArrayBuilder;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.IShape;
import turtleduck.turtle.PathBuilder;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;
import turtleduck.turtle.base.BaseCanvas;

public class GLCanvas extends BaseCanvas {

	private GLScreen screen;
	private VertexArrayBuilder vab;
	private int vao;
	private int vbo;
	private int nVertices;

	public GLCanvas(String id, GLScreen screen) {
		super(id);
		this.screen = screen;

	}

	@Override
	public Canvas dot(Stroke pen, Point point) {
		return line(pen, point, point);
	}

	@Override
	public Canvas line(Stroke pen, Point from, Point to) {
//		
//		vertexArray();
//		vab.vec2((float) from.x(), (float) from.y());
//		float w = (float) pen.strokeWidth();
//		Paint color = pen.strokePaint();
//		vab.vec4((float) color.red(), (float) color.green(), (float) color.blue(), (float) color.opacity());
//		vab.add(w);
//		vab.next();
//		vab.vec2((float) to.x(), (float) to.y());
//		vab.vec4((float) color.red(), (float) color.green(), (float) color.blue(), (float) color.opacity());
////		vab.add(w);
//		vab.next();
		return this;
	}

	@Override
	public Canvas polyline(Stroke pen, Fill fill, Point... points) {
		Point from = points[0];
		for (int i = 1; i < points.length; i++) {
			Point to = points[i];
			line(pen, from, to);
			from = to;
		}

		return this;
	}

	@Override
	public Canvas polygon(Stroke pen, Fill fill, Point... points) {
		Point from = points[0];
		for (int i = 1; i < points.length; i++) {
			Point to = points[i];
			line(pen, from, to);
			from = to;
		}
		line(pen, from, points[0]);

		return this;
	}

	@Override
	public Canvas triangles(Stroke pen, Fill fill, Point... points) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Canvas shape(Stroke pen, Fill fill, IShape shape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Canvas path(Stroke pen, Fill fill, PathBuilder path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Canvas clear() {
		vab.clear();
		return this;
	}

	@Override
	public Canvas clear(Fill fill) {
		if(fill == Colors.TRANSPARENT)
			clear();
		return this;
	}

	@Override
	public TurtleControl createControl() {
		return new GLTurtleControl(this);
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	protected VertexArrayBuilder vertexArray() {
		if (vab == null) {
			if (vao == 0) {
				vao = glGenVertexArrays();
			}
			if (vbo == 0) {
				vbo = glGenBuffers();
			}
			vab = new VertexArrayBuilder(vao, vbo, GL_STATIC_DRAW);
			vab.layout("aPos", 0, 2);
			vab.layout("aColor", 1, 4);
//			vab.layout("aLineWidth", 2, 1);
		}
		return vab;
	}

	public void render() {
		if (vab != null && vab.nVertices() > 0) {
			vab.bindArrayBuffer();
			nVertices = vab.nVertices();
			vab.clear();
		}
		if (vao != 0 && nVertices > 0) {
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, 0, nVertices);
		}

	}

}
