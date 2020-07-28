package turtleduck.gl;

import static org.lwjgl.opengl.GL30.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.AxisAngle4f;
import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.colors.Colors;
import turtleduck.colors.Color;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.display.impl.BaseLayer;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.Texture;
import turtleduck.gl.objects.Uniform;
import turtleduck.gl.objects.VertexArrayBuilder;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.image.Image;
import turtleduck.image.Image.Transpose;
import turtleduck.image.Tiles;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class GLLayer extends BaseCanvas<GLScreen> implements Canvas {
	public static double angle = 0;
	private static Map<Color, Vector4f> colors = new HashMap<>();
	private final List<Texture> textures = new ArrayList<>();
	private int nVertices;
	private int imageVbo = 0;

	private VertexArrayBuilder vab;
	private VertexArrayBuilder vab2;
	private DrawObject lineObject;
	private List<DrawObject> drawObjects = new ArrayList<>();

	public GLLayer(String layerId, GLScreen screen, double width, double height) {
		super(layerId, screen, width, height);

		vab = screen.shader2d.format().build(GL_DYNAMIC_DRAW);

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

	@Override
	public void drawImage(Point at, Image img) {
		drawImage(at, img, 0);
	}

	public void drawTileMap(Point at, int cols, int rows, int[] data, Tiles tiles) {
		int tileWidth = tiles.width(), tileHeight = tiles.height();
		if (data.length < cols * rows) {
			throw new IllegalArgumentException("Data too short (" + data.length + ") for " + cols + "x" + rows);
		}
		for (int y = 0; y < rows; y++) {
			Point p = at.add(0, (rows-1-y) * tileHeight);
			for (int x = 0; x < cols; x++) {
				drawImage(p, tiles.get(data[x + y * cols]));
				p = p.add(tileWidth, 0);
			}
		}
	}

	public void drawTileMap(Point at, int cols, int rows, int tileWidth, int tileHeight, int[] data, Tiles tiles) {
		if (data.length < cols * rows) {
			throw new IllegalArgumentException("Data too short (" + data.length + ") for " + cols + "x" + rows);
		}
		for (int y = 0; y < rows; y++) {
			Point p = at.add(0, (rows-1-y) * tileHeight);
			for (int x = 0; x < cols; x++) {
				drawImage(p, tiles.get(data[x + y * cols]).scale(tileWidth, tileHeight));
				p = p.add(tileWidth, 0);
			}
		}
	}

	public void drawImage(Point at, Image img, float rotation) {
		if (lineObject != null) {
			lineObject.nVertices = vab.nVertices() - lineObject.offset;
			lineObject = null;
		}

		DrawObject obj = new DrawObject();
		obj.shader = screen.shader2d;
		obj.drawMode = GL_TRIANGLES;
		obj.transform.set(screen.modelMatrix);
		obj.offset = vab.nVertices();
		drawObjects.add(obj);
		float x = (float) at.x(), y = (float) at.y();
		Vector2f srcOffset = new Vector2f(0, 0), srcSize = new Vector2f(0, 0), srcCrop = new Vector2f(0, 0);
		Matrix3x2f transform = new Matrix3x2f();
		Vector3f tr = new Vector3f();
//		System.out.println(screen.modelMatrix);
//		screen.modelMatrix.getTranslation(tr);
//		AxisAngle4f aa = new AxisAngle4f();
//		screen.modelMatrix.getRotation(aa);
//		System.out.println(aa);
//		System.out.println(aa.angle-Math.PI);
		transform.translate(tr.x, tr.y);
		transform.rotate(rotation);// -(float)Math.PI);

		transform.translate(x, y);

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
		float cropX[] = { srcOffset.x / srcSize.x, srcCrop.x / srcSize.x };
		float cropY[] = { srcOffset.y / srcSize.y, srcCrop.y / srcSize.y };

		Vector4f tmp = new Vector4f(1, 1, 1, 1);
		vab.vec3(p0, texNum).vec4(tmp.set(cropX[texCoords[0][0]], cropY[texCoords[0][1]], 0, 0));
		vab.vec3(p1, texNum).vec4(tmp.set(cropX[texCoords[1][0]], cropY[texCoords[1][1]], 0, 0));
		vab.vec3(p2, texNum).vec4(tmp.set(cropX[texCoords[2][0]], cropY[texCoords[2][1]], 0, 0));

		vab.vec3(p2, texNum).vec4(tmp.set(cropX[texCoords[2][0]], cropY[texCoords[2][1]], 0, 0));
		vab.vec3(p3, texNum).vec4(tmp.set(cropX[texCoords[3][0]], cropY[texCoords[3][1]], 0, 0));
		vab.vec3(p1, texNum).vec4(tmp.set(cropX[texCoords[1][0]], cropY[texCoords[1][1]], 0, 0));
		obj.nVertices = vab.nVertices() - obj.offset;

		return;
	}

	protected void drawLine(Stroke stroke, Point from, Point to) {
		if (lineObject == null) {
			lineObject = new DrawObject();
			lineObject.shader = screen.shader2d;
			lineObject.drawMode = GL_TRIANGLES;
			lineObject.offset = vab.nVertices();
		}
		if (!drawObjects.contains(lineObject))
			drawObjects.add(lineObject);

		if (stroke != null) {
			Vector2f fromVec = new Vector2f((float) from.x(), (float) from.y());
			Vector2f toVec = new Vector2f((float) to.x(), (float) to.y());
			Vector2f off = new Vector2f(toVec).sub(fromVec).normalize().perpendicular();
//			Vector2f off = new Vector2f((float)bearing.dirX(), (float)bearing.dirY()).normalize().perpendicular();
			Vector2f tmp = new Vector2f();
			float w = (float) stroke.strokeWidth() / 2;
			vab.vec3(tmp.set(fromVec).fma(w, off), 0).color(stroke.strokePaint()).nextVertex();
			vab.vec3(tmp.set(toVec).fma(-w, off), 0).color(stroke.strokePaint()).nextVertex();
			vab.vec3(tmp.set(fromVec).fma(-w, off), 0).color(stroke.strokePaint()).nextVertex();

			vab.vec3(tmp.set(toVec).fma(-w, off), 0).color(stroke.strokePaint()).nextVertex();
			vab.vec3(tmp.set(fromVec).fma(w, off), 0).color(stroke.strokePaint()).nextVertex();
			vab.vec3(tmp.set(toVec).fma(w, off), 0).color(stroke.strokePaint()).nextVertex();
		}

	}


	protected int imageVbo() {
		if (imageVbo == 0)
			imageVbo = glGenBuffers();
		return imageVbo;
	}

	public void render() {
		if (lineObject != null) {
			lineObject.nVertices = vab.nVertices() - lineObject.offset;
			lineObject = null;
		}
		int texNum = GL_TEXTURE0;
		for (Texture tex : textures) {
			tex.bind(texNum++);
		}
		ShaderProgram shader = null;
		vab.bindArrayBuffer();
		for (DrawObject obj : drawObjects) {
			if (obj.nVertices > 0) {
				if (obj.shader != shader) {
					shader = obj.shader;
					shader.bind();
				}
				Uniform<Matrix4f> uModel = shader.uniform("uModel", Matrix4f.class);
				if (uModel != null)
					uModel.set(obj.transform);
				glDrawArrays(obj.drawMode, obj.offset, obj.nVertices);
			}
		}
		vab.clear();
		drawObjects.clear();
		texNum = GL_TEXTURE0;
		for (Texture tex : textures) {
			tex.unbind(texNum++);
		}
		textures.clear();
	}

	class DrawObject {
		Matrix4f transform = new Matrix4f();
		VertexArrayBuilder buffer;
		ShaderProgram shader;
		int zOrder;
		int drawMode;
		int offset;
		int nVertices;

	}
}
