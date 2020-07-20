package turtleduck.gl;

import static org.lwjgl.opengl.GL30.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.display.impl.BaseLayer;
import turtleduck.drawing.Drawing;
import turtleduck.drawing.Image;
import turtleduck.drawing.Image.Transpose;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.Texture;
import turtleduck.gl.objects.VertexArrayBuilder;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class GLLayer extends BaseCanvas<GLScreen> implements Canvas {
	public static double angle = 0;
	private static Map<Paint, Vector4f> colors = new HashMap<>();
	private final List<Texture> textures = new ArrayList<>();
	private int vao;
	private int vbo;
	private int nVertices;
	private int imageVbo = 0;

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

	private void addPoint(Vector2f pos, Vector4f color, int texture) {
		vertexArray().vec3(pos, texture).vec4(color);
	}

	@Override
	public void drawImage(Point at, Image img) {
		float x = (float) at.x(), y = (float) at.y();
		Vector2f srcOffset = new Vector2f(0, 0), srcSize = new Vector2f(0, 0), srcCrop = new Vector2f(0,0);
		Matrix3x2f transform = new Matrix3x2f().translate(x, y);
		int[][] texCoords = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
		Texture tex = img.visit(new Image.Visitor<Texture>() {
			@Override
			public Texture visitData(Object data) {
				if (data instanceof Texture) {
					srcSize.set(((Texture) data).getWidth(), ((Texture) data).getHeight());
					srcCrop.set(srcSize);
					transform.rotate((float) Math.toRadians(angle));
					return ((Texture) data);
				} else {
					return null;
				}
			}

			@Override
			public Texture visitCropped(int x, int y, int w, int h, Image source) {
				Texture tex = source.visit(this);
				srcOffset.add(x, y);
				srcCrop.set(w, h);
				return tex;
			}

			@Override
			public Texture visitTransposed(Image.Transpose method, Image source) {
				Texture tex = source.visit(this);
				for (int[] p : texCoords) {
					method.transformNormalized(p[0], p[1], p);
				}
				return tex;
			}
		});

		if (tex == null) {
			throw new IllegalArgumentException();
		}

		int texNum = textures.indexOf(tex) + 1;
		if (texNum < 1) {
			textures.add(tex);
			texNum = textures.size();
		}
		float w = img.width(), h = img.height();
		Vector2f p0 = new Vector2f(0, 0);
		Vector2f p1 = new Vector2f(p0).add(w, 0);
		Vector2f p2 = new Vector2f(p0).add(0, h);
		Vector2f p3 = new Vector2f(p0).add(w, h);
		transform.transformPosition(p0);
		transform.transformPosition(p1);
		transform.transformPosition(p2);
		transform.transformPosition(p3);
		srcCrop.add(srcOffset);
		float cropX[] = {srcOffset.x / srcSize.x, srcCrop.x / srcSize.x};
		float cropY[] = {srcOffset.y / srcSize.y, srcCrop.y / srcSize.y};
	
		Vector4f tmp = new Vector4f(1, 1, 1, 1);
		addPoint(p0, tmp.set(cropX[texCoords[0][0]], cropY[texCoords[0][1]], 0, 0), texNum);
		addPoint(p1, tmp.set(cropX[texCoords[1][0]], cropY[texCoords[1][1]], 0, 0), texNum);
		addPoint(p2, tmp.set(cropX[texCoords[2][0]], cropY[texCoords[2][1]], 0, 0), texNum);

		addPoint(p2, tmp.set(cropX[texCoords[2][0]], cropY[texCoords[2][1]], 0, 0), texNum);
		addPoint(p3, tmp.set(cropX[texCoords[3][0]], cropY[texCoords[3][1]], 0, 0), texNum);
		addPoint(p1, tmp.set(cropX[texCoords[1][0]], cropY[texCoords[1][1]], 0, 0), texNum);

		return; /*
				 * int vbo = imageVbo(); glBindBuffer(GL_ARRAY_BUFFER, vbo);
				 * glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);
				 * glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
				 * glEnableVertexAttribArray(0); glDrawArrays(GL_TRIANGLES, 0, vertices.length);
				 */
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
			float w = (float) stroke.strokeWidth() / 2;
			vertexArray.vec3(tmp.set(fromVec).fma(w, off), 0);
			vertexArray.vec4(strokeColor);
			vertexArray.vec3(tmp.set(toVec).fma(-w, off), 0);
			vertexArray.vec4(strokeColor);
			vertexArray.vec3(tmp.set(fromVec).fma(-w, off), 0);
			vertexArray.vec4(strokeColor);
			vertexArray.vec3(tmp.set(toVec).fma(-w, off), 0);
			vertexArray.vec4(strokeColor);
			vertexArray.vec3(tmp.set(fromVec).fma(w, off), 0);
			vertexArray.vec4(strokeColor);
			vertexArray.vec3(tmp.set(toVec).fma(w, off), 0);
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
			vab.layout("aPos", 0, 3);
			vab.layout("aColor", 1, 4);
//			vab.layout("aLineWidth", 2, 1);
		}
		return vab;
	}

	protected int imageVbo() {
		if (imageVbo == 0)
			imageVbo = glGenBuffers();
		return imageVbo;
	}

	protected static Vector4f paintToVec(Stroke stroke) {
		if (stroke != null) {
			Paint color = stroke.strokePaint();
			Vector4f strokeColor = colors.get(color);
			if (strokeColor == null) {
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
			int texNum = GL_TEXTURE0;
			for (Texture tex : textures) {
				tex.bind(texNum++);
			}
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, 0, nVertices);
			textures.clear();
		}

	}

}
