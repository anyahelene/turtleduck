package turtleduck.gl;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector4f;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.display.impl.BaseLayer;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.VertexArrayBuilder;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class GLLayer extends BaseCanvas<GLScreen> implements Canvas {
	private static Map<Paint, Vector4f> colors = new HashMap<>(); 

	private int vao;
	private int vbo;
	private int nVertices;

	private VertexArrayBuilder vab;

	public GLLayer(String layerId, GLScreen screen, double width, double height) {
		super(layerId, screen, width, height);
	}


	@Override
	public Canvas show() {
		// TODO Auto-generated method stub
		return this;

	}

	@Override
	public Canvas hide() {
		// TODO Auto-generated method stub
		return this;

	}

	@Override
	public Canvas flush() {
		// TODO Auto-generated method stub
		return this;

	}

	@Override
	public Canvas draw(Path path) {
		Point from = path.first();
		Pen pen = path.pointPen(0);
		for (int i = 1; i < path.size(); i++) {
			Point to = path.point(i);
			drawLine(pen, from, to);
			from = to;
			pen = path.pointPen(i);
		}		
		return this;

	}

	@Override
	public Canvas draw(Drawing drawing) {
		// TODO Auto-generated method stub
		return this;

	}

	@Override
	public Canvas clear() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public Canvas clear(Fill fill) {
		// TODO Auto-generated method stub
		return this;
	}

	protected void drawLine(Stroke stroke, Point from, Point to) {
//		System.out.println("Draw from " + from + " to " + to + ", stroke " + stroke);
//		vertexArray();
//		vab.vec2((float) from.x(), (float) from.y());
//		float w = (float) pen.strokeWidth();
//		Paint color = pen.strokePaint();
//		vab.vec4((float) color.red(), (float) color.green(), (float) color.blue(), (float) color.opacity());
////		vab.add(w);
////		vab.next();
//		vab.vec2((float) to.x(), (float) to.y());
//		vab.vec4((float) color.red(), (float) color.green(), (float) color.blue(), (float) color.opacity());
////		vab.add(w);
////		vab.next();
		if (stroke != null) {
			Vector4f strokeColor = paintToVec(stroke);
			Vector2f fromVec = new Vector2f((float) from.x(), (float) from.y());
			Vector2f toVec = new Vector2f((float) to.x(), (float) to.y());
			Vector2f off = new Vector2f(toVec).sub(fromVec).normalize().perpendicular();
//			Vector2f off = new Vector2f((float)bearing.dirX(), (float)bearing.dirY()).normalize().perpendicular();
			Vector2f tmp = new Vector2f();
			VertexArrayBuilder vertexArray = vertexArray();
			float w = (float) stroke.strokeWidth()/2;
			vertexArray.vec2(tmp.set(fromVec).fma(w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(toVec).fma(-w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(fromVec).fma(-w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(toVec).fma(-w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(fromVec).fma(w, off));
			vertexArray.vec4(strokeColor);
			vertexArray.vec2(tmp.set(toVec).fma(w, off));
			vertexArray.vec4(strokeColor);
		}

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
	protected static Vector4f paintToVec(Stroke stroke) {
		if(stroke != null) {
			Paint color = stroke.strokePaint();
			Vector4f strokeColor = colors.get(color);
			if(strokeColor == null) {
				float r = (float) Colors.Gamma.gammaExpand(color.red());
				float g = (float) Colors.Gamma.gammaExpand(color.green());
				float b = (float) Colors.Gamma.gammaExpand(color.blue());
				float a = (float) color.opacity();
				strokeColor = new Vector4f(r, g, b, 1f);
				colors.put(color, strokeColor);
			}
			return strokeColor;
		} else {
			return null;
		}

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
