package turtleduck.gl;

import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL33C.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import turtleduck.buffer.DataField;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.display.Canvas;
import turtleduck.display.impl.BaseCanvas;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.ArrayBuffer;
import turtleduck.gl.objects.ShaderObject;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.Texture;
import turtleduck.gl.objects.Uniform;
import turtleduck.gl.objects.VertexArray;
import turtleduck.gl.objects.VertexArrayBuilder;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.image.Image;
import turtleduck.image.Tiles;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Path;
import turtleduck.turtle.PathPoint;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.PathWriterImpl;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;

public class GLLayer extends BaseCanvas<GLScreen> implements Canvas {
	public static double angle = 0;
	private static Map<Color, Vector4f> colors = new HashMap<>();
	private final List<Texture> textures = new ArrayList<>();
	private int nVertices;
	private int imageVbo = 0;

	private ArrayBuffer streamBuffer;
	private VertexArray streamArray;
	private VertexArray staticArray;
	private GLPathWriter pathWriter = new GLPathWriter();
//	private VertexArrayBuilder vab;
//	private VertexArrayBuilder vab2;
	private DrawObject lineObject;
//	private List<Point> currentPath = new ArrayList<>();
//	private List<Stroke> currentPathStrokes = new ArrayList<>();
	private List<DrawObject> drawObjects = new ArrayList<>();
	private Map<String, ShaderProgram> programs = new HashMap<>();
	int depth = 0;
	private VertexArrayFormat format;
	private DataField<Vector3f> aPosVec3;
	private DataField<Color> aColorVec4;
	private int quadVertices = -1;

	public GLLayer(String layerId, GLScreen screen, double width, double height) {
		super(layerId, screen, width, height);

		this.format = screen.shader2d.format();
		aPosVec3 = format.setField("aPos", Vector3f.class);
		aColorVec4 = format.setField("aColor", Color.class);
		System.out.println("Layer " + layerId);
		System.out.println(format);
		System.out.println(aPosVec3);
		System.out.println(aColorVec4);
		streamBuffer = new ArrayBuffer(GL_STREAM_DRAW, 2048);
		streamArray = new VertexArray(format, streamBuffer);
		streamArray.setFormat();
		staticArray = new VertexArray(format, GL_STATIC_DRAW, 2048);
		staticArray.setFormat();
		quadVertices = staticArray.nVertices();
		staticArray.begin().put(aPosVec3, 0, 0, 0).put(aColorVec4, 0, 0, 0, 1).end();
		staticArray.begin().put(aPosVec3, 0, 1, 0).put(aColorVec4, 0, 1, 0, 1).end();
		staticArray.begin().put(aPosVec3, 1, 0, 0).put(aColorVec4, 1, 0, 0, 1).end();

		staticArray.begin().put(aPosVec3, 1, 0, 0).put(aColorVec4, 1, 0, 0, 1).end();
		staticArray.begin().put(aPosVec3, 0, 1, 0).put(aColorVec4, 0, 1, 0, 1).end();
		staticArray.begin().put(aPosVec3, 1, 1, 0).put(aColorVec4, 1, 1, 0, 1).end();
//		vab = screen.shader2d.format().build(GL_DYNAMIC_DRAW);

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
			Point p = at.add(0, (rows - 1 - y) * tileHeight);
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
			Point p = at.add(0, (rows - 1 - y) * tileHeight);
			for (int x = 0; x < cols; x++) {
				drawImage(p, tiles.get(data[x + y * cols]).scale(tileWidth, tileHeight));
				p = p.add(tileWidth, 0);
			}
		}
	}

	int count = 0;
	public void plot(Point at, double width, double height, Color color, String function) {
		ShaderProgram program = programs.get(function);
		if (program == null) {
			try {
				String code = "#version 330 core\n" + //
						"\n" + //
						"in vec4 fColor;\n" + //
						"in vec4 fPos;\n" + //
						"in vec4 fNormal;\n" + //
						"in vec2 fTexCoord;\n" + //
						"flat in int fTexNum;\n" + //
						"uniform float MAXITER = 1024;\n" + //
						"uniform vec2 zoom = vec2(2,2);\n" + //
						"uniform vec2 offset = vec2(-1,-1);\n" + //
						"\n" + //
						"out vec4 FragColor;\n" + //
						"\n" + //
						"const float threshold = 0.005;\n" + // 0.005;" + //
						"float equal(float a, float b) {\n" + //
						"  float t0 = min(a, b), t1 = max(a, b);\n" + //
						"  return t0 > t1-threshold && t0 < t1 ? 1.0 : 0.0;" + //
//						"  return step(t1-threshold, t1, t0);\n" + // float diff = a-b;\n" +//
//						"  return diff > -0.3 && diff <= 0;\n" +//
						"}\n" + //
						"float less(float a, float b) { return smoothstep(a-threshold, a, b); }\n" + //
						"float greater(float a, float b) { return smoothstep(b-threshold, b, a); }\n" + //
						"float between(float a, float b, float c) { return c > min(a,b) && c < max(a,b) ? 1.0 : 0.0; }\n"
						+ //
						"void main() {\n" + //
//						"   float MAXITER = 128;\n" + //
						"   vec2 xy0 = zoom*fColor.xy + offset;\n" + // x0 = 0.1*fColor.x+.3, y0 = 0.1*fColor.y-.5;\n" + //
						"   float x = xy0.x, y = xy0.y;\n" + //
						"   float i = 0;\n" + //
						"   while(x*x+y*y <= 4 && ++i < MAXITER) {\n" + //
						"      float tmp = x*x-y*y+xy0.x;\n" + //
						"       y = 2*x*y + xy0.y;\n" + //
						"       x = tmp;\n" + //
						"   }\n" + //
						"   float c = max(i-32, 0) / (128-32);\n" + //
						"   if(i <= 32) FragColor = mix(vec4(0,0,0,1), vec4(0,0,.1,1), i/32.0);\n" + //
						"   else if(i >= 128) FragColor = mix(vec4(1,.9,.4,1), vec4(1,1,1,0), (i-128)/(MAXITER-128));\n" + //
						"   else FragColor = mix(vec4(.0,.0,.1,1), vec4(1,.9,.4,1), c);\n" + //
						"   if(i >= MAXITER) discard;\n" + //
//						"	float x = (2*fColor.x-1)*(1.0+threshold), y = (2*fColor.y-1)*(1.0+threshold);\n" + //
//						"	if(" + function + ") {\n" +  //
//						"		FragColor = vec4(x*x,y*y,1,1) * (" + function + ");\n" + //
						"       if(FragColor.a < 0.05) discard;\n" + //
//						"	} else {\n" +  //
//						"		discard;\n" +  //
//						"	}\n" + 
						"}\n" + //
						"";
				ShaderObject vs = ShaderObject.create("/turtleduck/gl/shaders/twodee-vs.glsl", GL_VERTEX_SHADER);
				ShaderObject fs = ShaderObject.createFromString(code, GL_FRAGMENT_SHADER);
program = ShaderProgram.createProgram("plot_" + function, vs, fs);
//				program.uniform("uProjection", Matrix4f.class).set(screen.projectionMatrix);
//				program.uniform("uView", Matrix4f.class).set(screen.viewMatrix);
				program.uniform("uProjView", Matrix4f.class)
						.set(new Matrix4f(screen.projectionMatrix).mul(screen.viewMatrix));
				programs.put(function, program);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		program.uniform("MAXITER", Float.class).set((float)128+count);
		float zoom = (float) ((screen.fov - 10) / 120);
		program.uniform("zoom", Vector2f.class).set(new Vector2f(2.5f*zoom));
		program.uniform("offset", Vector2f.class).set(new Vector2f(screen.cameraPosition.x-1.8f, screen.cameraPosition.y-1f));

		DrawObject obj = new DrawObject();
		obj.shader = program;
		obj.array = staticArray;
		obj.drawMode = GL_TRIANGLES;
		obj.transform.translation((float) at.x(), (float) at.y(), 0).scale((float) width, (float) height, 1);
		obj.zOrder = depth++;
		obj.offset = quadVertices;
		obj.nVertices = 6;
		obj.blend = true;
		obj.type = "plot";
		drawObjects.add(obj);

		count = (count + 1) % 1024;
	}

	public void drawImage(Point at, Image img, float rotation) {
		DrawObject obj = new DrawObject();
		obj.array = streamArray;
		obj.shader = screen.shader2d;
		obj.drawMode = GL_TRIANGLES;
		obj.transform.set(screen.modelMatrix);
		obj.offset = streamArray.nVertices();
		obj.zOrder = depth++;
		obj.type = "image";
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

		streamArray.begin().put(aPosVec3, p0, texNum)
				.put(aColorVec4, cropX[texCoords[0][0]], cropY[texCoords[0][1]], 0, 0).end();
		streamArray.begin().put(aPosVec3, p1, texNum)
				.put(aColorVec4, cropX[texCoords[1][0]], cropY[texCoords[1][1]], 0, 0).end();
		streamArray.begin().put(aPosVec3, p2, texNum)
				.put(aColorVec4, cropX[texCoords[2][0]], cropY[texCoords[2][1]], 0, 0).end();

		streamArray.begin().put(aPosVec3, p2, texNum)
				.put(aColorVec4, cropX[texCoords[2][0]], cropY[texCoords[2][1]], 0, 0).end();
		streamArray.begin().put(aPosVec3, p3, texNum)
				.put(aColorVec4, cropX[texCoords[3][0]], cropY[texCoords[3][1]], 0, 0).end();
		streamArray.begin().put(aPosVec3, p1, texNum)
				.put(aColorVec4, cropX[texCoords[1][0]], cropY[texCoords[1][1]], 0, 0).end();
		obj.nVertices = streamArray.nVertices() - obj.offset;

		return;
	}

	protected void drawLine(Stroke stroke, Point from, Point to) {
//		if (!currentPath.isEmpty() && currentPath.get(currentPath.size() - 1) == from) {
//			currentPath.add(to);
//			currentPathStrokes.add(stroke);
//		} else {
//			if (!currentPath.isEmpty())
//				flushPath();
//			currentPath.add(from);
//			currentPath.add(to);
//			currentPathStrokes.add(stroke);
//		}
	}

	protected int imageVbo() {
		if (imageVbo == 0)
			imageVbo = glGenBuffers();
		return imageVbo;
	}

	protected void drawPaths() {
		PathWriter.PathStroke stroke;
		DrawObject obj = new DrawObject();
		obj.array = streamArray;
		obj.shader = screen.shader2d;
		obj.drawMode = GL_TRIANGLE_STRIP;
		obj.offset = streamArray.nVertices();
		obj.type = "line";
		obj.zOrder = depth++;
		drawObjects.add(obj);

		Vector2f fromVec = new Vector2f();
		Vector2f toVec = new Vector2f();
		Vector2f off1 = new Vector2f();
		Vector2f off2 = new Vector2f();
		Vector2f tmp = new Vector2f();
		boolean first = true;
		while ((stroke = pathWriter.nextStroke()) != null) {
			List<PathPoint> points = stroke.points();

			if (points.size() > 1) {
				PathPoint from = points.get(0);
				PathPoint to = points.get(1);
				Pen pen = from.pen();
				Color color = pen.strokePaint();
				float w = (float) pen.strokeWidth() / 2;
				Direction fromDir; // = tangents.get(line.get(0));
				fromVec.set((float) from.x(), (float) from.y());
				toVec.set((float) to.x(), (float) to.y());
				off1.set(toVec).sub(fromVec).normalize().perpendicular();
//				off1.set((float) fromDir.dirX(), (float) fromDir.dirY()).normalize().perpendicular();
				if (!first)
					streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(-w, off1), 0).put(aColorVec4, color).end();
				first = false;
				streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(-w, off1), 0).put(aColorVec4, color).end();
				streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(w, off1), 0).put(aColorVec4, color).end();
				for (int i = 1; i < points.size(); i++) {
					to = points.get(i);
//					Direction toDir = tangents.get(line.get(i));
					toVec.set((float) to.x(), (float) to.y());
//					off2.set((float) toDir.dirX(), (float) toDir.dirY()).normalize().perpendicular();
					off2.set(toVec).sub(fromVec).normalize().perpendicular();
					obj.blend = color.opacity() < 1;
					streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(-w, off2), 0).put(aColorVec4, color).end();
					streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(w, off2), 0).put(aColorVec4, color).end();
					if (i == points.size() - 1)
						streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(w, off2), 0).put(aColorVec4, color).end();

					if (false) {
						toVec.set(fromVec).add(50 * (float) fromDir.dirX(), 50 * (float) fromDir.dirY());
						off1.set(toVec).sub(fromVec).normalize().perpendicular();
						off2.set(toVec).sub(fromVec).normalize().perpendicular();
						w = 1;
						color = Colors.RED;
						streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(w, off1), 0).put(aColorVec4, color)
								.end();
						streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(-w, off1), 0).put(aColorVec4, color).end();
						streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(-w, off1), 0).put(aColorVec4, color)
								.end();

						streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(-w, off2), 0).put(aColorVec4, color).end();
						streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(w, off2), 0).put(aColorVec4, color)
								.end();
						streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(w, off2), 0).put(aColorVec4, color).end();
					}
					// System.out.printf("[" + from.point() + ":" + fromDir + " – " + to.point() +
					// ":" + toDir + "] ");
					pen = to.pen();
					color = pen.strokePaint();
					w = (float) pen.strokeWidth() / 2;
					fromVec.set(toVec);
				}
			}
//			System.out.println();
//			pathWriter.clear();
			obj.nVertices = streamArray.nVertices() - obj.offset;
		}
	}

	public void render(boolean frontToBack) {
		if (frontToBack)
			drawPaths();
		ShaderProgram shader = null;
		if (frontToBack) {
			int texNum = GL_TEXTURE0;
			for (Texture tex : textures) {
				tex.bind(texNum++);
			}
		}
		Matrix4f modelTransform = new Matrix4f();
//		System.out.print("render: " + (frontToBack ? "front-to-back" : "back-to-front") + " [");
		for (int i = 0; i < drawObjects.size(); i++) {
			DrawObject obj = frontToBack ? drawObjects.get(drawObjects.size() - 1 - i) : drawObjects.get(i);
			if (obj.nVertices > 0 && !obj.blend == frontToBack) {
				obj.array.done();
				int vao = obj.array.bind();
//				System.out.print(obj.type + ":" + ((float) obj.zOrder / depth) + " ");
				if (obj.shader != shader) {
					shader = obj.shader;
					shader.bind();
				}
				Uniform<Matrix4f> uModel = shader.uniform("uModel", Matrix4f.class);
				if (uModel != null) {
					modelTransform.set(obj.transform).translate(0, 0, (float) obj.zOrder / depth);
					uModel.set(modelTransform);
				}
				glDrawArrays(obj.drawMode, obj.offset, obj.nVertices);
			}
		}
//		System.out.println("]");
		if (!frontToBack) {
			streamArray.clear();
			streamBuffer.clear();
			drawObjects.clear();
			int texNum = GL_TEXTURE0;
			for (Texture tex : textures) {
				tex.unbind(texNum++);
			}
			textures.clear();
			depth = 0;
		}
	}

	class DrawObject {
		Matrix4f transform = new Matrix4f();
		VertexArray array;
		ShaderProgram shader;
		String type;
		int zOrder;
		int drawMode;
		int offset;
		int nVertices;
		boolean blend;

	}

	class GLPathWriter extends PathWriterImpl {

	}

	@Override
	protected PathWriter pathWriter() {
		return pathWriter;
	}
}
