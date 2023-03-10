package turtleduck.gl;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix3x2d;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import earcut4j.Earcut;
import turtleduck.buffer.VertexAttribute;
import turtleduck.buffer.VertexLayout;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.display.Camera;
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
import turtleduck.image.Image;
import turtleduck.image.Tiles;
import turtleduck.paths.Path;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PathWriterImpl;
import turtleduck.shapes.Particles;

public class GLLayer extends BaseLayer<GLScreen> implements Layer {
    public static double angle = 0;
    private final List<Texture> textures = new ArrayList<>();
    private int imageVbo = 0;

    private ArrayBuffer streamBuffer;
    private VertexArray streamArray;
    private VertexArray streamArray3;
    private GLPathWriter pathWriter;
    private GLPathWriter pathWriter3;

    List<DrawObject> drawObjects = new ArrayList<>();
    private Map<String, ShaderProgram> programs = new HashMap<>();
    int depth = 0;
    private VertexLayout format;
    private VertexAttribute<Vector4f> aPosVec3;
    private VertexAttribute<Color> aColorVec4;
    private VertexAttribute<Vector3f> a3Normal3;
    private VertexAttribute<Vector2f> a3TexCoord2;
    private VertexLayout format3;
    private VertexAttribute<Vector4f> a3PosVec3;
    private VertexAttribute<Color> a3ColorVec4;
    private Camera camera2;
    private Camera camera3;
    private VertexAttribute<Vector2f> aTexCoord2;

    public GLLayer(String layerId, GLScreen screen, Camera camera2, Camera camera3, double width, double height) {
        super(layerId, screen, width, height);
        this.format = screen.shader2d.format();
        this.format3 = screen.shader3d.format();
        this.camera2 = camera2;
        this.camera3 = camera3;
        pathWriter = new GLPathWriter(camera3);
        pathWriter3 = new GLPathWriter3(camera3);

        //format.specifyInputFormat().setInputFormat(name, type)
        aPosVec3 = format.attribute("aPos"); //.setField("aPos", Vector4f.class);
        aColorVec4 = format.attribute("aColor");//, Color.class);
        aTexCoord2 = format.attribute("aTexCoord");//, Vector2f.class);
        a3PosVec3 = format3.attribute("aPos");//, Vector4f.class);
        a3ColorVec4 = format3.attribute("aColor");//, Color.class);
        a3Normal3 = format3.attribute("aNormal");//, Vector3f.class);
        a3TexCoord2 = format3.attribute("aTexCoord");//, Vector2f.class);
        streamBuffer = new ArrayBuffer(GL_STREAM_DRAW, format.numBytes() * 1024 * 1024);
        streamArray = new VertexArray(format, streamBuffer);
        streamArray.setFormat();
        streamArray3 = new VertexArray(format3, GL_STREAM_DRAW, 2048);
        streamArray3.setFormat();

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

    protected void draw2Rectangle(VertexArray array, float x0, float y0, float x1, float y1) {
        array.begin().put(aPosVec3, x0, y0, 0).put(aColorVec4, 0, 0, 0, 1).put(aTexCoord2, 0, 0).end();
        array.begin().put(aPosVec3, x0, y1, 0).put(aColorVec4, 0, 1, 0, 1).put(aTexCoord2, 0, 1).end();
        array.begin().put(aPosVec3, x1, y0, 0).put(aColorVec4, 1, 0, 0, 1).put(aTexCoord2, 1, 0).end();

        array.begin().put(aPosVec3, x1, y0, 0).put(aColorVec4, 1, 0, 0, 1).put(aTexCoord2, 1, 0).end();
        array.begin().put(aPosVec3, x0, y1, 0).put(aColorVec4, 0, 1, 0, 1).put(aTexCoord2, 0, 1).end();
        array.begin().put(aPosVec3, x1, y1, 0).put(aColorVec4, 1, 1, 0, 1).put(aTexCoord2, 1, 1).end();
    }

    public Layer clear() {
        streamArray.clear();
        streamBuffer.clear();
        streamArray3.clear();
        drawObjects.clear();
        int texNum = GL_TEXTURE0;
        for (Texture tex : textures) {
            tex.unbind(texNum++);
        }
        textures.clear();
//		System.out.println("Clearing: depth was " + depth);
        depth = 0;
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
        obj.transform.translate((float) at.x(), (float) at.y(), (float) at.z());
        obj.transform.rotateX(-(float) Math.PI *2);
        obj.transform.scale((float) xsize, (float) ysize, (float) zsize);
        // obj.transform.scale(100f,100f,100f);
//		obj.transform.rotateX((float)Math.PI/3); //.rotateY(-(float)Math.PI/2);
//		obj.transform.translate((float)-xsize/2, (float) -ysize, 0);
        obj.projection = new Matrix4f(camera3.projectionMatrix).mul(camera3.viewMatrix);
        obj.indices = new int[6 * (cols - 1) * (rows - 1) + 6 + 24];
        Vector3f p0 = new Vector3f(-.5f, -.5f + 1, 0);
        Vector3f p1 = new Vector3f(-.5f, -.5f, 0);
        Vector3f p2 = new Vector3f(-.5f + 1, .5f, 0);
        Vector3f p3 = new Vector3f(-.5f + 1, -.5f, 0);
        Vector3f u = new Vector3f(), v = new Vector3f();
        Color color = Colors.WHITE;
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
                double opacity = .9; // Math.min(1, Math.max(0, (5 - Math.min(1, data[x + y * cols])) / 10));
                Color c = Color.color(.3, .6, 1).mul(opacity);
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
            // obj.indices[i] = obj.offset + i;
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
                program = ShaderProgram.createProgram("plot_" + function, format, vs, fs);
//				program.uniform("uProjection", Matrix4f.class).set(screen.projectionMatrix);
//				program.uniform("uView", Matrix4f.class).set(screen.viewMatrix);
                program.uniform("uProjView", Matrix4f.class)
                        .set(new Matrix4f(camera2.projectionMatrix).mul(camera2.viewMatrix));
                programs.put(function, program);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        program.uniform("MAXITER", Float.class).set((float) 128 + count);
        float zoom = (float) ((camera3.fov - 10) / 120);
        program.uniform("zoom", Vector2f.class).set(new Vector2f(2.5f * zoom));
        program.uniform("offset", Vector2f.class).set(new Vector2f(camera3.position.x - 1.8f, camera3.position.y - 1f));

        DrawObject obj = new DrawObject();
        obj.shader = program;
        obj.array = streamArray;
        obj.offset = streamArray.nVertices();
        obj.drawMode = GL_TRIANGLES;

        float x = (float) at.x(), y = (float) at.y();
        float w = (float) width, h = (float) height;
        draw2Rectangle(obj.array, x, y, x + w, y + h);
        obj.zOrder = depth++;
        obj.nVertices = streamArray.nVertices() - obj.offset;
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
//		obj.transform.set(screen.modelMatrix);
        obj.offset = streamArray.nVertices();
        obj.zOrder = depth;
        obj.type = "image";
        drawObjects.add(obj);
        float x = (float) at.x(), y = (float) at.y();
        Vector2f srcOffset = new Vector2f(0, 0), srcSize = new Vector2f(0, 0), srcCrop = new Vector2f(0, 0);
        Matrix4f transform = new Matrix4f();

        transform.rotateY(rotation);// -(float)Math.PI);

        transform.translate(x, y, 0);

        int[][] texCoords = { { 0, 1 }, { 1, 1 }, { 0, 0 }, { 1, 0 } };
        Texture tex = img.visit(new Image.Visitor<Texture>() {
            @Override
            public Texture visitData(Object data) {
                if (data instanceof Texture) {
                    srcSize.set(((Texture) data).getWidth(), ((Texture) data).getHeight());
                    srcCrop.set(srcSize);
                    transform.rotateY((float) Math.toRadians(angle));
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
            throw new IllegalArgumentException("No texture found for " + img);
        }

        int texNum = textures.indexOf(tex) + 1;
        if (texNum < 1) {
            textures.add(tex);
            texNum = textures.size();
        }

        transform.mulLocal(camera2.projectionMatrix);
        float w = img.width(), h = img.height();
        Vector3f p0 = new Vector3f(0, 0, depth++);
        Vector3f p1 = new Vector3f(p0).add(w, 0, 0);
        Vector3f p2 = new Vector3f(p0).add(0, h, 0);
        Vector3f p3 = new Vector3f(p0).add(w, h, 0);
        transform.transformPosition(p0);
        transform.transformPosition(p1);
        transform.transformPosition(p2);
        transform.transformPosition(p3);
        srcCrop.add(srcOffset);
        float cropX[] = { srcOffset.x / srcSize.x, srcCrop.x / srcSize.x };
        float cropY[] = { srcOffset.y / srcSize.y, srcCrop.y / srcSize.y };

        streamArray.begin().put(aPosVec3, p0, texNum).put(aColorVec4, Colors.BLACK)//
                .put(aTexCoord2, cropX[texCoords[0][0]], cropY[texCoords[0][1]]).end();
        streamArray.begin().put(aPosVec3, p1, texNum).put(aColorVec4, Colors.BLUE)//
                .put(aTexCoord2, cropX[texCoords[1][0]], cropY[texCoords[1][1]]).end();
        streamArray.begin().put(aPosVec3, p2, texNum).put(aColorVec4, Colors.RED)//
                .put(aTexCoord2, cropX[texCoords[2][0]], cropY[texCoords[2][1]]).end();

        streamArray.begin().put(aPosVec3, p2, texNum).put(aColorVec4, Colors.MAGENTA)//
                .put(aTexCoord2, cropX[texCoords[2][0]], cropY[texCoords[2][1]]).end();
        streamArray.begin().put(aPosVec3, p3, texNum).put(aColorVec4, Colors.GREEN)//
                .put(aTexCoord2, cropX[texCoords[3][0]], cropY[texCoords[3][1]]).end();
        streamArray.begin().put(aPosVec3, p1, texNum).put(aColorVec4, Colors.BROWN)//
                .put(aTexCoord2, cropX[texCoords[1][0]], cropY[texCoords[1][1]]).end();
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
            imageVbo = gl.glGenBuffers();
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
        obj.zOrder = 0;
        int minDepth = Integer.MAX_VALUE;
        int maxDepth = 0;// depth++;
        obj.transform.identity();
        obj.projection = null;
        obj.blend = false;
        drawObjects.add(obj);

//		paths.drawBounds(Pen.create().stroke(Colors.WHITE, 1).done());
        Vector4f fromVec = new Vector4f();
        Vector3f fromDir = new Vector3f();
        Vector4f toVec = new Vector4f();
        Vector4f offset = new Vector4f();
        Vector4f negOffset = new Vector4f();
        Vector3f normal = new Vector3f(0, 0, 1);
        Vector4f tmp = new Vector4f();
        Vector3f tmp3 = new Vector3f();
        int index = obj.offset;
        if (paths instanceof GLPathWriter3) {
            PathRenderer3 ren = new PathRenderer3(obj.shader);
            ren.drawPaths(paths, obj);
            obj.cull = 0;// GL_BACK;
            obj.projection = paths.projection();

        } else {
            obj.cull = GL_BACK;
            Camera camera = camera2;
            Matrix4f projectionMatrix = new Matrix4f(camera.projViewMatrix);
//            projectionMatrix.scale(1f/256f);
            while ((stroke = paths.nextStroke()) != null) {
                List<PathPoint> points = stroke.points();
                boolean closed = (stroke.options() & PathStroke.PATH_CLOSED) != 0;
                boolean strip = (stroke.options() & PathStroke.PATH_TRIANGLE_STRIP) != 0;
                if (points.size() > 1) {
                    PathPoint from = points.get(0);
                    PathPoint to = points.get(1);
                    Pen pen = from.pen();
                    boolean strokeEnabled = pen.stroking() && pen.strokeWidth() > 0 && !strip;
                    boolean fillEnabled = pen.filling() || strip;
                    boolean useStrokeDepth = false;
                    int strokeDepth = stroke.depth();
                    minDepth = Math.min(minDepth, strokeDepth);
                    maxDepth = Math.max(maxDepth, strokeDepth);
                    // strokeDepth = 100;
                    Color color = pen.strokeColor();
                    obj.blend |= color.alpha() < 1;

                    float w = (float) pen.strokeWidth() / 2;
                    /*
                     * from.point().toVector(fromVec); to.point().toVector(toVec);
                     * from.bearing().directionVector(fromDir); from.bearing().normalVector(normal);
                     * toVec.sub(fromVec, fromDir).normalize(); normal.cross(fromDir, offset);
                     */
                    if (strokeEnabled) {
                        from = points.get(0);
                        from.point().toVector(fromVec);
                        if (useStrokeDepth)
                            fromVec.z = strokeDepth;
                        projectionMatrix.transform(fromVec);
                        int n = points.size();
                        if (closed)
                            n++;
                        for (int i = 1; i < n; i++) {
                            if (i == points.size()) {
                                to = points.get(0);
                            } else {
                                to = points.get(i);
                            }
                            if (from.equals(to)) {
                                pen = to.pen();
                                color = pen.strokeColor();
                                w = (float) pen.strokeWidth() / 2;
                                from = to;
                                toVec.get(fromVec);
                                continue;
                            }
                            Pen toPen = to.pen();
                            Color toColor = toPen.color();
                            to.point().toVector(toVec);
                            if (useStrokeDepth)
                                toVec.z = strokeDepth;
                            projectionMatrix.transform(toVec);
                            toVec.sub(fromVec, tmp).normalize();
                            fromDir.set(tmp.x, tmp.y, tmp.z);
                            fromDir.cross(normal, tmp3);
                            tmp3.mul(w);
                            projectionMatrix.transformDirection(tmp3);
                            offset.set(tmp3, 0);
                            offset.negate(negOffset);

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
                                streamArray.begin().put(aPosVec3, tmp.set(fromVec).fma(3 * 1, offset))
                                        .put(aColorVec4, Colors.RED)
//								.put(a3Normal3, normal)//
                                        .end();

                            }

                            streamArray.begin().put(aPosVec3, tmp.set(fromVec).add(negOffset))//
                                    .put(aColorVec4, color)//
                                    .put(aTexCoord2, 0, 0).end();
                            streamArray.begin().put(aPosVec3, tmp.set(fromVec).add(offset))//
                                    .put(aColorVec4, color)//
                                    .put(aTexCoord2, 1, 0).end();
                            streamArray.begin().put(aPosVec3, tmp.set(toVec).add(negOffset))//
                                    .put(aColorVec4, toColor)//
                                    .put(aTexCoord2, 0, 1).end();
                            streamArray.begin().put(aPosVec3, tmp.set(toVec).add(offset))//
                                    .put(aColorVec4, toColor)//
                                    .put(aTexCoord2, 1, 1).end();
                            indices.add(index + 0);
                            indices.add(index + 1);
                            indices.add(index + 2);
                            indices.add(index + 2);
                            indices.add(index + 1);
                            indices.add(index + 3);
                            index += 4;

                            pen = toPen;
                            color = toColor;
                            obj.blend |= color.alpha() < 1;
                            w = (float) pen.strokeWidth() / 2;
                            from = to;
                            toVec.get(fromVec);
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
                            if (useStrokeDepth)
                                fromVec.z = strokeDepth;
                            projectionMatrix.transform(fromVec);
                            Color fillColor = p.pen().fillColor();
                            obj.blend |= fillColor.alpha() < 1.0;
                            streamArray.begin().put(aPosVec3, fromVec)//
                                    .put(aColorVec4, fillColor)//
                                    .put(aTexCoord2, (float) p.x(), (float) p.y()).end();
                        }
                        if (strip) {
                            for (int i = 0; i < points.size(); i++) {
                                if (i <= 2) {
                                    indices.add(index + i);
                                } else if (i % 2 == 0) {
                                    indices.add(index + i - 2);
                                    indices.add(index + i - 1);
                                    indices.add(index + i);
                                } else {
                                    indices.add(index + i);
                                    indices.add(index + i - 1);
                                    indices.add(index + i - 2);
                                }
                            }
                            index += points.size();
                        } else {
                            List<Integer> earcut = Earcut.earcut(cutVertices, null, 3);
                            for (int i : earcut) {
                                indices.add(index + i);
                            }
                            index += points.size();
                        }
                    }
                }
            }

            obj.zOrder = minDepth;
            obj.nVertices = obj.array.nVertices() - obj.offset;
            // TODO: this is slow
            obj.indices = indices.stream().mapToInt(i -> i).toArray();
// This is slower:
            // obj.indices = new int[indices.size()];
//				for(int i = 0; i < indices.size(); i++) {
//					obj.indices[i] = indices.get(i);
//				}
//				int i = 0;
//				for(Integer ind : indices) {
//					obj.indices[i++] = ind;
//				}
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
        Uniform<Vector2f> uViewPort = null;
        int cull = 0;
        if (!frontToBack)
            drawObjects.sort((obj1, obj2) -> Integer.compare(obj1.zOrder, obj2.zOrder));
        // System.out.print("render: " + (frontToBack ? "front-to-back" :
        // "back-to-front") + " [");
        for (int i = 0; i < drawObjects.size(); i++) {
            DrawObject obj = frontToBack ? drawObjects.get(drawObjects.size() - 1 - i) : drawObjects.get(i);
            if (obj.nVertices > 0 && !obj.blend == frontToBack) {
                if (obj.array != null) {
                    obj.array.done();
                    obj.array.bind();
                }
                // System.out.print(obj.type + ":" + obj.zOrder + " ");
                if (obj.cull != cull) {
                    cull = obj.cull;
                    if (cull != 0) {
                        gl.glCullFace(cull);
                        gl.glEnable(GL_CULL_FACE);
                    } else {
                        gl.glDisable(GL_CULL_FACE);
                    }
                }
                if (obj.shader != shader) {
                    shader = obj.shader;
                    shader.bind();
                    uProjView = shader.uniform("uProjView", Matrix4f.class);
                    uModel = shader.uniform("uModel", Matrix4f.class);
                    uLightPos = shader.uniform("uLightPos", Vector4f.class);
                    uViewPos = shader.uniform("uViewPos", Vector4f.class);
                    uViewPort = shader.uniform("uViewPort", Vector2f.class);
                    projection = obj.projection;
                    if (uProjView.isDeclared()) {
                        uProjView.set(projection == null ? camera2.projectionMatrix : projection);
                    }
                    if (uViewPort.isDeclared()) {
                        uViewPort.set(new Vector2f(camera2.viewport.width(), camera2.viewport.height()));
                    }
                }
                if (uModel.isDeclared() && obj.transform != null) {
                    // modelTransform.set(obj.transform).translate(0, 0,);
                    // uModel.set(new Matrix4f().translation(0,0, ((float) obj.zOrder) / depth));
                    uModel.set(obj.transform);
                }
                if (uLightPos.isDeclared()) {
//				Vector4f light = projection.transform(screen.lightPosition, new Vector4f());
                    Vector4f light = new Vector4f(10, -10, -16, 1);// .rotateX(step++/10f);
//				light = projection.transform(light);
//				light.div(light.w);
//					System.out.println("light: " + light + ", " + projection.transform(new Vector4f(0,0,1,1)));
                    uLightPos.set(screen.lightPosition);
                }
                if (uViewPos.isDeclared()) {
                    Vector4f camera = camera2.position.mul(16, new Vector4f());
//					System.out.println("camera: " + view);
                    uViewPos.set(camera);
                }
                if (obj.projection != projection) {
                    projection = obj.projection;
                    if (uProjView.isDeclared()) {
                        uProjView.set(projection == null ? camera2.projectionMatrix : projection);
                    }
                }
                if (obj.indices != null) {
                    int eab = gl.glGenBuffers();
                    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eab);
                    gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, obj.indices, GL_STREAM_DRAW);
                    gl.glDrawElements(obj.drawMode, obj.indices.length, GL_UNSIGNED_INT, 0);
                    gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
                    gl.glDeleteBuffers(eab);
                } else if (obj.array != null) {
                    gl.glDrawArrays(obj.drawMode, obj.offset, obj.nVertices);
                } else if (obj.drawMethod != null) {
                    obj.drawMethod.run();
                }
            }
        }
        // System.out.println("]");
//		streamBuffer.dump(this.format, 10);
//		System.out.println("---------------");
//		System.out.println(streamArray);
        ShaderProgram.unbind();
        if (false && clear) {
            clear();
        }
    }

    static class DrawObject {
        public Runnable drawMethod;
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
        protected Camera camera;

        GLPathWriter(Camera cam) {
            this.camera = cam;
        }

        public int depth() {
            return GLLayer.this.depth++;
        }

        Matrix4f projection() {
            return new Matrix4f(camera.projectionMatrix).mul(camera.viewMatrix); // camera.projectionMatrix;
        }

        public Particles addParticles(Matrix4f matrix) {
            return new GLParticles(GLLayer.this, screen, matrix);
        }
    }

    class GLPathWriter3 extends GLPathWriter {

        GLPathWriter3(Camera cam) {
            super(cam);
        }

        Matrix4f projection() {
            return new Matrix4f(camera.projectionMatrix).mul(camera.viewMatrix);
        }

        public boolean is3d() {
            return true;
        }
    }

    protected PathWriter pathWriter(boolean use3d) {
        if (use3d)
            return pathWriter3;
        else
            return pathWriter;
    }

}
