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

import earcut4j.Earcut;
import turtleduck.buffer.DataField;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.display.Layer;
import turtleduck.display.impl.BaseLayer;
import turtleduck.drawing.Drawing;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.ArrayBuffer;
import turtleduck.gl.objects.ShaderObject;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.Texture;
import turtleduck.gl.objects.Uniform;
import turtleduck.gl.objects.VertexArray;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.image.Image;
import turtleduck.image.Tiles;
import turtleduck.paths.Path;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PathWriterImpl;

public class GLLayer extends BaseLayer<GLScreen> implements Layer {
	public static double angle = 0;
	private final List<Texture> textures = new ArrayList<>();
	private int imageVbo = 0;

	private ArrayBuffer streamBuffer;
	private VertexArray streamArray;
	private VertexArray staticArray;
	private VertexArray streamArray3;
	private GLPathWriter pathWriter = new GLPathWriter();
	private GLPathWriter pathWriter3 = new GLPathWriter3();

//	private VertexArrayBuilder vab;
//	private VertexArrayBuilder vab2;
//	private List<Point> currentPath = new ArrayList<>();
//	private List<Stroke> currentPathStrokes = new ArrayList<>();
	private List<DrawObject> drawObjects = new ArrayList<>();
	private Map<String, ShaderProgram> programs = new HashMap<>();
	int depth = 0;
	private VertexArrayFormat format;
	private DataField<Vector3f> aPosVec3;
	private DataField<Color> aColorVec4;
	private DataField<Vector3f> a3Normal3;
	private DataField<Vector2f> a3TexCoord2;
	private int quadVertices = -1;
	private VertexArrayFormat format3;
	private DataField<Vector3f> a3PosVec3;
	private DataField<Color> a3ColorVec4;

	public GLLayer(String layerId, GLScreen screen, double width, double height) {
		super(layerId, screen, width, height);
		this.format = screen.shader2d.format();
		this.format3 = screen.shader3d.format();
		System.out.println("Layer " + layerId);
		System.out.println(format);
		System.out.println(format3);
		System.out.println(aPosVec3);
		System.out.println(aColorVec4);

		aPosVec3 = format.setField("aPos", Vector3f.class);
		aColorVec4 = format.setField("aColor", Color.class);
		a3PosVec3 = format3.setField("aPos", Vector3f.class);
		a3ColorVec4 = format3.setField("aColor", Color.class);
		a3Normal3 = format3.setField("aNormal", Vector3f.class);
		a3TexCoord2 = format3.setField("aTexCoord", Vector2f.class);
		streamBuffer = new ArrayBuffer(GL_STREAM_DRAW, 2048);
		streamArray = new VertexArray(format, streamBuffer);
		streamArray.setFormat();
		streamArray3 = new VertexArray(format3, GL_STREAM_DRAW, 2048);
		streamArray3.setFormat();
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
	public Layer show() {
		// TODO Auto-generated method stub
		return this;

	}

	@Override
	public Layer hide() {
		// TODO Auto-generated method stub
		return this;

	}

	@Override
	public Layer flush() {
		// TODO Auto-generated method stub
		return this;

	}

	public Layer draw(Path path) {
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

	public Layer draw(Drawing drawing) {
		// TODO Auto-generated method stub
		return this;

	}

	public Layer clear() {
		// TODO Auto-generated method stub
		return this;
	}


	public void drawImage(Point at, Image img) {
		drawImage(at, img, 0);
	}

	public void drawHeightMap(Point at, double xsize, double ysize, double zsize, int cols, int rows, double[] data) {
		DrawObject obj = new DrawObject();
		obj.array = streamArray3;
		obj.shader = screen.shader3d;
		obj.drawMode = GL_TRIANGLES;
		obj.offset = streamArray3.nVertices();
		obj.zOrder = depth++;
		obj.type = "image";
		drawObjects.add(obj);
		obj.transform.identity();
		obj.transform.rotationX(-(float) Math.PI / 4);
//		obj.transform.rotateX((float)Math.PI/3); //.rotateY(-(float)Math.PI/2);
		obj.transform.translate((float) at.x(), (float) at.y(), (float) at.z());
//		obj.transform.translate((float)-xsize/2, (float) -ysize, 0);
		obj.transform.scale((float) xsize, (float) ysize, (float) zsize);
		obj.indices = new int[6 * (cols - 1) * (rows - 1) + 6 + 24];
		Vector3f p0 = new Vector3f(-.5f, -.5f + 1, 0);
		Vector3f p1 = new Vector3f(-.5f, -.5f, 0);
		Vector3f p2 = new Vector3f(-.5f + 1, .5f, 0);
		Vector3f p3 = new Vector3f(-.5f + 1, -.5f, 0);
		Vector3f u = new Vector3f(), v = new Vector3f();
		Color color = Color.color(.2, .2, .9, 1);
		p0.sub(p2, u);
		p1.sub(p2, v);
		u.cross(v).normalize();
		streamArray3.begin().put(a3PosVec3, p0).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p3).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		p0.set(-.5f, .5f, .5f);
		p1.set(-.5f, .5f, 0f);
		p2.set(-.5f + 1, .5f, .5f);
		p3.set(-.5f + 1, .5f, 0f);
		p0.sub(p2, u);
		p1.sub(p2, v);
		u.cross(v).normalize();
		streamArray3.begin().put(a3PosVec3, p0).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p3).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		p0.set(-.5f, -.5f, .5f);
		p1.set(-.5f, -.5f, 0f);
		p2.set(-.5f, .5f, .5f);
		p3.set(-.5f, .5f, 0f);
		p0.sub(p2, u);
		p1.sub(p2, v);
		u.cross(v).normalize();
		streamArray3.begin().put(a3PosVec3, p0).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p3).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		p0.set(.5f, .5f, .5f);
		p1.set(.5f, .5f, 0f);
		p2.set(.5f, -.5f, .5f);
		p3.set(.5f, -.5f, 0f);
		p0.sub(p2, u);
		p1.sub(p2, v);
		u.cross(v).normalize();
		streamArray3.begin().put(a3PosVec3, p0).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p2).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p1).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		streamArray3.begin().put(a3PosVec3, p3).put(a3ColorVec4, color).put(a3Normal3, u).put(a3TexCoord2, 0, 0).end();
		float frows = rows, fcols = cols;
		Vector3f normal = new Vector3f();
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				double opacity = .8; // Math.min(1, Math.max(0, (5 - Math.min(1, data[x + y * cols])) / 10));
				Color c = Color.color(0, .2 * opacity, .8 * opacity, opacity);
				float N = (float) data[x + Math.max(y - 1, 0) * cols];
				float S = (float) data[x + Math.min(y + 1, rows - 1) * cols];
				float W = (float) data[Math.max(x - 1, 0) + y * cols];
				float E = (float) data[Math.min(x + 1, cols - 1) + y * cols];
				normal.set(-2 * (E - W) * fcols, -2 * (N - S) * frows, 4).normalize();
				streamArray3.begin().put(a3PosVec3, (float) x / (fcols - 1) - .5f, .5f - (float) (y) / (frows - 1), // 1f-(float)
																													// (y)
																													// /
																													// (frows
																													// -
																													// 1))
						(float) data[x + y * cols]) //
						.put(a3ColorVec4, c)//
						.put(a3Normal3, normal)//
						.put(a3TexCoord2, 0, 0).end();

			}
		}
		for (int i = 0; i < 24; i++) {
			obj.indices[i] = obj.offset + i;
		}
		for (int y = 0; y < rows - 1; y++) {
			for (int x = 0; x < cols - 1; x++) {
				int i = 24 + 6 * (x + y * (cols - 1));
				int i0 = obj.offset + 24 + x + y * cols;
				int i1 = obj.offset + 24 + x + (y + 1) * cols;
				int i2 = obj.offset + 24 + (x + 1) + (y + 1) * cols;
				int i3 = obj.offset + 24 + (x + 1) + y * cols;
				double l0 = data[x + y * cols] - data[(x + 1) + (y + 1) * cols];
				double l1 = data[(x + 1) + y * cols] - data[x + (y + 1) * cols];
				if (Math.abs(l0) < Math.abs(l1)) {
					obj.indices[i + 0] = i0;
					obj.indices[i + 1] = i1;
					obj.indices[i + 2] = i2;
					obj.indices[i + 3] = i3;
					obj.indices[i + 4] = i0;
					obj.indices[i + 5] = i2;
				} else {
					obj.indices[i + 0] = i0;
					obj.indices[i + 1] = i1;
					obj.indices[i + 2] = i3;
					obj.indices[i + 3] = i3;
					obj.indices[i + 4] = i1;
					obj.indices[i + 5] = i2;
				}
			}
		}
		obj.blend = true;
		obj.nVertices = streamArray3.nVertices() - obj.offset;
		obj.projection = new Matrix4f(screen.perspectiveProjectionMatrix).mul(screen.perspectiveViewMatrix);
		obj.cull = GL_BACK;
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
						"   vec2 xy0 = zoom*fColor.xy + offset;\n" + // x0 = 0.1*fColor.x+.3, y0 = 0.1*fColor.y-.5;\n" +
																		// //
						"   float x = xy0.x, y = xy0.y;\n" + //
						"   float i = 0;\n" + //
						"   while(x*x+y*y <= 4 && ++i < MAXITER) {\n" + //
						"      float tmp = x*x-y*y+xy0.x;\n" + //
						"       y = 2*x*y + xy0.y;\n" + //
						"       x = tmp;\n" + //
						"   }\n" + //
						"   float c = max(i-32, 0) / (128-32);\n" + //
						"   if(i <= 32) FragColor = mix(vec4(0,0,0,1), vec4(0,0,.1,1), i/32.0);\n" + //
						"   else if(i >= 128) FragColor = mix(vec4(1,.9,.4,1), vec4(1,1,1,0), (i-128)/(MAXITER-128));\n"
						+ //
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
//				System.out.println(code);
//				System.exit(0);
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
		program.uniform("MAXITER", Float.class).set((float) 128 + count);
		float zoom = (float) ((screen.fov - 10) / 120);
		program.uniform("zoom", Vector2f.class).set(new Vector2f(2.5f * zoom));
		program.uniform("offset", Vector2f.class)
				.set(new Vector2f(screen.cameraPosition.x - 1.8f, screen.cameraPosition.y - 1f));

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

		int[][] texCoords = { { 0, 1 }, { 1, 1 }, { 0, 0 }, { 1, 0 } };
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

	protected void drawLine(Pen pen, Point from, Point to) {
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

	int step = 0;
	private boolean showNormals;

	protected void drawPaths(GLPathWriter paths) {
		List<Integer> indices = new ArrayList<>();
		PathStroke stroke;
		DrawObject obj = new DrawObject();
		obj.array = paths instanceof GLPathWriter3 ? streamArray3 : streamArray;
		obj.shader = paths instanceof GLPathWriter3 ? screen.shader3d : screen.shader2d;
		obj.drawMode = GL_TRIANGLES;
		obj.offset = obj.array.nVertices();
		obj.type = "line";
		obj.zOrder = depth++;
		obj.transform.identity(); // .rotationX(-(float) Math.PI / 4);// + (step++) / 100.0f);
		obj.projection = paths.projection();
		drawObjects.add(obj);
		DrawObject debugObj = new DrawObject();
		debugObj.array = streamArray;
		debugObj.shader = screen.shader2d;
		debugObj.drawMode = GL_LINES;
		debugObj.offset = streamArray.nVertices();
		debugObj.type = "line";
		debugObj.zOrder = depth - 1;
//		debugObj.transform.translation(0, 0, -.01f).rotateX((float) Math.PI / 2);// + (step++) / 100.0f);
		debugObj.projection = paths.projection();
		// drawObjects.add(debugObj);
		Vector3f fromVec = new Vector3f(), fromDir = new Vector3f();
		Vector3f toVec = new Vector3f(), tmp2 = new Vector3f();
		Vector3f offset = new Vector3f();
		Vector3f normal = new Vector3f();
		Vector2f texCoord = new Vector2f();
//		Vector3f tangent = new Vector3f();
		Vector3f tmp = new Vector3f();
		boolean first = true;
		int index = obj.offset;
		if (paths instanceof GLPathWriter3) {
			PathRenderer3 ren = new PathRenderer3(obj.shader);
			ren.drawPaths(paths, obj);
			obj.cull = GL_BACK;

		} else {
			obj.cull = GL_FRONT;
			while ((stroke = paths.nextStroke()) != null) {
				List<PathPoint> points = stroke.points();

				if (points.size() > 1) {
					PathPoint from = points.get(0);
					PathPoint to = points.get(1);
					Pen pen = from.pen();
					boolean strokeEnabled = pen.stroking() && pen.strokeWidth() > 0;
					boolean fillEnabled = pen.filling();
				
					Color color = pen.strokeColor();

					float w = (float) pen.strokeWidth() / 2;
//				Direction fromDir = from.bearing(); // = tangents.get(line.get(0));
					from.point().toVector(fromVec);
					to.point().toVector(toVec);
					from.bearing().directionVector(fromDir);
					from.bearing().normalVector(normal);
					toVec.sub(fromVec, fromDir).normalize();
					normal.cross(fromDir, offset);

					if (strokeEnabled) {
						for (int i = 1; i < points.size(); i++) {
							from = points.get(i - 1);
							to = points.get(i);
							from.point().toVector(fromVec);
							to.point().toVector(toVec);
//					toVec.sub(fromVec, toDir).normalize();
							from.bearing().directionVector(fromDir).normalize();

//					fromDir.add(toDir).mul(.5f);
							from.bearing().normalVector(normal).normalize();
							normal.cross(fromDir, offset); // .normalize();
							fromDir.cross(normal, offset);

							if (Double.isNaN(fromVec.lengthSquared()))
								System.out.println("fromVec: " + fromVec);
							if (Double.isNaN(toVec.lengthSquared()))
								System.out.println("toVec: " + toVec);
							if (Double.isNaN(offset.lengthSquared()))
								System.out.println("offset: " + offset);
//					showNormals = true;
							if (showNormals) {
								System.out.println("from: " + fromVec);
								System.out.println("to: " + toVec);
								System.out.println("fromDir: " + fromDir);
								System.out.println("normal: " + normal);
								System.out.println("cross: " + offset);
								streamArray.begin().put(aPosVec3, fromVec).put(aColorVec4, Colors.WHITE)//
										// .put(a3Normal3, normal)
										.end();
								streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(3 * w, offset))
										.put(aColorVec4, Colors.RED)
//								.put(a3Normal3, normal)//
										.end();

							}
//					off2.add(off1).mul(.5f);
							obj.blend |= color.opacity() < 1;

							streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(-w, offset)).put(aColorVec4, color)
									.end();
							streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(w, offset)).put(aColorVec4, color)
									.end();
							streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(-w, offset)).put(aColorVec4, color)
									.end();
							streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(w, offset)).put(aColorVec4, color)
									.end();
							indices.add(index + 0);
							indices.add(index + 1);
							indices.add(index + 2);
							indices.add(index + 2);
							indices.add(index + 1);
							indices.add(index + 3);
							index += 4;

//					if (i == points.size() - 1)
//						streamArray.begin().put(aPosVec3, tmp.set(toVec).fma(w, offset)).put(aColorVec4, color).end();

							pen = to.pen();
							color = pen.strokeColor();
							w = (float) pen.strokeWidth() / 2;
//					fromVec.set(toVec);
//					fromDir.set(toDir);
//					from = to;
						}
					}
					if (fillEnabled) {
						int j = 0;
						double[] cutVertices = new double[points.size() * 3];
						for (PathPoint p : points) {
							cutVertices[j++] = p.x();
							cutVertices[j++] = p.y();
							cutVertices[j++] = p.z();
							p.point().toVector(fromVec);
							streamArray.begin().put(aPosVec3, fromVec).put(aColorVec4, p.pen().fillColor()).end();
						}
						List<Integer> earcut = Earcut.earcut(cutVertices, null, 3);
						for (int i : earcut) {
							indices.add(index + i);
						}
						index += points.size();
					}
				}
//			System.out.println();
//			pathWriter.clear();
				obj.nVertices = obj.array.nVertices() - obj.offset;
				obj.indices = indices.stream().mapToInt(i -> i).toArray();
				debugObj.nVertices = streamArray.nVertices() - debugObj.offset;

			}
		}
	}

	public void render(boolean frontToBack, boolean clear) {
		if (frontToBack && pathWriter.hasNextStroke())
			drawPaths(pathWriter);
		if (frontToBack && pathWriter3.hasNextStroke())
			drawPaths(pathWriter3);
		ShaderProgram shader = null;
		if (frontToBack) {
			int texNum = GL_TEXTURE0;
			for (Texture tex : textures) {
				tex.bind(texNum++);
			}
		}
		Matrix4f projection = null;
		Uniform<Matrix4f> uProjView = null;
		Uniform<Matrix4f> uModel = null;
		Uniform<Vector4f> uLightPos = null;
		Uniform<Vector4f> uViewPos = null;
		int cull = 0;

//		System.out.print("render: " + (frontToBack ? "front-to-back" : "back-to-front") + " [");
		for (int i = 0; i < drawObjects.size(); i++) {
			DrawObject obj = frontToBack ? drawObjects.get(drawObjects.size() - 1 - i) : drawObjects.get(i);
			if (obj.nVertices > 0 && !obj.blend == frontToBack) {
				obj.array.done();
				int vao = obj.array.bind();
//				System.out.print(obj.type + ":" + ((float) obj.zOrder / depth) + " ");
				if (obj.cull != cull) {
					cull = obj.cull;
					if (cull != 0) {
						glCullFace(cull);
						glEnable(GL_CULL_FACE);
					} else {
						glDisable(GL_CULL_FACE);
					}
				}
				if (obj.shader != shader) {
					shader = obj.shader;
					shader.bind();
					uProjView = shader.uniform("uProjView", Matrix4f.class);
					uModel = shader.uniform("uModel", Matrix4f.class);
					uLightPos = shader.uniform("uLightPos", Vector4f.class);
					uViewPos = shader.uniform("uViewPos", Vector4f.class);
					projection = obj.projection;
					if (uProjView != null) {
						uProjView.set(projection == null ? screen.projectionMatrix : projection);
					}
				}
				if (uModel != null) {
//					modelTransform.set(obj.transform).translate(0, 0, (float) obj.zOrder / depth);
					uModel.set(obj.transform);
				}
				if (uLightPos != null) {
//				Vector4f light = projection.transform(screen.lightPosition, new Vector4f());
					Vector4f light = new Vector4f(100, 100, 16, 1);// .rotateX(step++/10f);
//				light = projection.transform(light);
//				light.div(light.w);
//					System.out.println("light: " + light + ", " + projection.transform(new Vector4f(0,0,1,1)));
					uLightPos.set(light);
				}
				if (uViewPos != null) {
					Vector4f camera = screen.cameraPosition.mul(16, new Vector4f());
//					System.out.println("camera: " + view);
					uViewPos.set(camera);
				}
				if (obj.projection != projection) {
					projection = obj.projection;
					if (uProjView != null) {
						uProjView.set(projection == null ? screen.projectionMatrix : projection);
					}
				}
				if (obj.indices != null) {
					int eab = glGenBuffers();
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eab);
					glBufferData(GL_ELEMENT_ARRAY_BUFFER, obj.indices, GL_DYNAMIC_DRAW);
					glDrawElements(obj.drawMode, obj.indices.length, GL_UNSIGNED_INT, 0);
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
					glDeleteBuffers(eab);
				} else {
					glDrawArrays(obj.drawMode, obj.offset, obj.nVertices);
				}
			}
		}
//		System.out.println("]");
		if (clear) {
			streamArray.clear();
			streamBuffer.clear();
			streamArray3.clear();
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
		public int[] indices;
		Matrix4f transform = new Matrix4f();
		Matrix4f projection = null;
		VertexArray array;
		ShaderProgram shader;
		String type;
		int zOrder;
		int drawMode;
		int offset;
		int nVertices;
		int cull;
		boolean blend;

	}

	class GLPathWriter extends PathWriterImpl {
		Matrix4f projection() {
			return screen.projectionMatrix;
		}
	}

	class GLPathWriter3 extends GLPathWriter {
		Matrix4f projection() {
			return new Matrix4f(screen.perspectiveProjectionMatrix).mul(screen.perspectiveViewMatrix);
		}
	}

	protected PathWriter pathWriter(boolean use3d) {
		if (use3d)
			return pathWriter3;
		else
			return pathWriter;
	}
}
