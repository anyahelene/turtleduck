package turtleduck.gl;

import static turtleduck.gl.GLScreen.gl;
import static turtleduck.gl.compat.GLA.*;
import static turtleduck.gl.compat.GLConstants.GL_FRAGMENT_SHADER;
import static turtleduck.gl.compat.GLConstants.GL_VERTEX_SHADER;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import org.joml.Matrix3x2d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL32C;
import org.slf4j.Logger;

import turtleduck.buffer.DataField;
import turtleduck.canvas.Canvas;
import turtleduck.colors.Color;
import turtleduck.display.Camera;
import turtleduck.geometry.Point;
import turtleduck.gl.GLLayer.DrawObject;
import turtleduck.gl.compat.GLConstants;
import turtleduck.gl.objects.ShaderObject;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.Uniform;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.messaging.Router;
import turtleduck.scene.GfxNodeImpl;
import turtleduck.shapes.Particles;
import turtleduck.util.Logging;
import turtleduck.util.MathUtil;

public class GLParticles  implements Particles {
    protected static final Logger logger = Logging.getLogger(GLParticles.class);
    final double COORD_SCALE = 16;
    static int i = 0;
    static final int START_X = i++, START_Y = i++;
    static final int CTRL1_X = i++, CTRL1_Y = i++;
    static final int R1 = i++, G1 = i++, B1 = i++, A1 = i++;
    static final int START_TIME = i++, END_TIME = i++;
    static final int CTRL2_X = i++, CTRL2_Y = i++;
    static final int END_X = i++, END_Y = i++;
    static final int R2 = i++, G2 = i++, B2 = i++, A2 = i++;
    static final int START_SIZE = i++, END_SIZE = i++;
    static final int START_T = i++, END_T = i++;
    static final int START_Z = i++, END_Z = i++;
    static final int VERTEX_SIZE = i;
    protected Point point = Point.ZERO;
    protected double particleSize;
    protected Camera camera;
    protected float[] data;
    protected int index, lastDrawn;
    protected GLParticle template = new GLParticle();
    protected GLLayer glLayer;
    protected GLScreen glScreen;
    protected int vbo, vao;
    private DrawObject obj;
    private float z;
    private Matrix4f matrix;
    private static ShaderProgram shaderPart;
    private static Uniform<Float> uTime;
    private static Uniform<Float> uZ;
    private static Uniform<Float> uPointScale;

    private static int maxPointSize = 1;
    private static boolean pointSizeWarn = false;

    public GLParticles(GLLayer glLayer, GLScreen glScreen, Matrix4f matrix) {
        this.glLayer = glLayer;
        this.glScreen = glScreen;
        this.camera = glScreen.camera2;
        this.matrix = matrix;
        if (shaderPart == null) {
            try {
                initialize();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        particleSize(128);
        obj = new DrawObject();
        obj.array = null;
        obj.indices = null;
        obj.shader = shaderPart;
        obj.drawMode = 0;
        obj.offset = 0;
        obj.nVertices = 1024;
        obj.type = "particle";
        obj.transform.set(matrix);
        obj.projection = camera.projectionMatrix;
        obj.blend = true;
    }

    public static void initialize() throws IOException {
        if (shaderPart == null) {
//            VertexArrayFormat formatPart = new VertexArrayFormat();
//            formatPart.addField("startPos", Vector4f.class);
//            formatPart.addField("startColor", Vector4f.class);
//            formatPart.addField("lifetime", Vector2f.class);
//            formatPart.addField("endPos", Vector4f.class);
//            formatPart.addField("endColor", Vector4f.class);
//            formatPart.addField("size", Vector2f.class);
//            formatPart.addField("len", Vector2f.class);
//            formatPart.addField("depth", Vector2f.class);

            ShaderObject vs3 = ShaderObject.create("particles.vert.glsl", GL_VERTEX_SHADER);
            ShaderObject fs3 = ShaderObject.create("twodee.frag.glsl", GL_FRAGMENT_SHADER);
            shaderPart = ShaderProgram.createProgram("shaderPart", null, vs3, fs3);
            uTime = shaderPart.uniform("uTime", Float.class);
            uZ = shaderPart.uniform("uZ", Float.class);
            uPointScale = shaderPart.uniform("uPointScale", Float.class);
            int[] range = new int[2];
            gl.glGetIntegerv(gl.isOpenGL() ? GL11C.GL_POINT_SIZE_RANGE : GLConstants.GL_ALIASED_POINT_SIZE_RANGE,
                    range);
            maxPointSize = range[1];
            logger.info("Particle shader point sizes: {}–{}", range[0], range[1]);
            logger.info("Particle shader input format: {}", shaderPart.format());
        }
    }

    public static void deinitialize() {
        if (shaderPart != null) {
            ShaderProgram p = shaderPart;
            shaderPart = null;
            p.dispose();
        }
    }

    @Override
    public Particles at(Point p) {
        this.point = p;
        return this;
    }

    @Override
    public Particles at(double x, double y) {
        this.point = Point.point(x, y);
        return this;
    }

    @Override
    public Point position() {
        return point;
    }

    @Override
    public Particles nParticles(int n) {
        this.obj.nVertices = n;
        return this;
    }

    @Override
    public int nParticles() {
        return obj.nVertices;
    }

    @Override
    public Particles particleSize(double size) {
        if(size > maxPointSize && !pointSizeWarn)
            logger.warn("particle size {} is larger than max supported size ({})", size, maxPointSize);
        this.particleSize = size;
        return this;
    }

    @Override
    public double particleSize() {
        return particleSize;
    }

    @Override
    public Particles update(Consumer<ParticleTemplate> updater) {
        updater.accept(template);
        return this;
    }

    @Override
    public Particles add(Consumer<Particle> adder) {
        adder.accept(template);
        template.save();
        return this;
    }

    @Override
    public Particles camera(Camera cam) {
        this.camera = cam;
        return this;
    }

    @Override
    public ParticleTemplate template() {
        return new GLParticle();
    }

    @Override
    public void draw() {

        obj.zOrder = glLayer.depth++;
        z = -obj.zOrder / 6553f;
        obj.drawMethod = () -> {

            uTime.set((float) glScreen.time); // TODO: deal with short wraparound
            uZ.set(z);
            if (vao == 0) {
                vbo = gl.glGenBuffers();
                vao = gl.glGenVertexArrays();
                gl.glBindVertexArray(vao);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo);
                obj.shader.format().setVertexAttributes(0);
            } else {
                gl.glBindVertexArray(vao);
                gl.glBindBuffer(GL_ARRAY_BUFFER, vbo);
            }
            if (gl.isOpenGL())
                gl.glEnable(GL32C.GL_PROGRAM_POINT_SIZE);
            // GL30C.glPointSize((float) particleSize);
            // else
            uPointScale.set((float) particleSize);
            if (index < lastDrawn)
                lastDrawn = 0;
            if (lastDrawn > 0) {
                float[] newData = Arrays.copyOfRange(data, lastDrawn * VERTEX_SIZE,
                        index * VERTEX_SIZE);
                gl.glBufferSubData(GL_ARRAY_BUFFER, lastDrawn * obj.shader.format().numBytes(), newData);
            } else {
                gl.glBufferData(GL_ARRAY_BUFFER, data, GL_STREAM_DRAW);
            }
            lastDrawn = index;
            gl.glDrawArrays(GL_POINTS, 0, obj.nVertices);
            // GL43C.gl.glInvalidateBufferData(vbo);
            // gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
            gl.glBindVertexArray(0);
        };
        glLayer.drawObjects.add(obj);

    }

    public void dispose() {
        if (vao != 0) {
            gl.glDeleteVertexArrays(vao);
            vao = 0;
        }
        if (vbo != 0) {
            gl.glDeleteBuffers(vbo);
        }
        data = null;
    }

    protected void ensureSpace() {
        if (data == null) {
            data = new float[obj.nVertices * VERTEX_SIZE];
        } else if (data.length < obj.nVertices * VERTEX_SIZE) {
            data = Arrays.copyOf(data, obj.nVertices * VERTEX_SIZE);
        }
        if (index >= obj.nVertices)
            index = 0;
    }

    static float unorm(double f) {
        // unorm(f);
        return (float) f;
    }

    static float norm(double f) {
        // unorm(f);
        return (float) f;
    }

    static float coord(double a) {
        // MathUtil.clamp(Math.round(p.x() * COORD_SCALE), Short.MIN_VALUE,
        // Short.MAX_VALUE);
        return (float) a;
    }

    class GLParticle implements Particles.ParticleTemplate {
        float[] vertex = new float[VERTEX_SIZE];

        @Override
        public ParticleTemplate size(double start, double end) {
            vertex[START_SIZE] = unorm(start);
            vertex[END_SIZE] = unorm(end);
            return this;
        }

        @Override
        public ParticleTemplate t(double start, double end) {
            vertex[START_T] = unorm(start);
            vertex[END_T] = unorm(end);
            return this;
        }

        @Override
        public ParticleTemplate color(Color c) {
            color(c, c);
            return this;
        }

        @Override
        public ParticleTemplate color(Color start, Color end) {
            vertex[R1] = start.red();
            vertex[G1] = start.green();
            vertex[B1] = start.blue();
            vertex[A1] = start.alpha();
            vertex[R2] = start.red();
            vertex[G2] = start.green();
            vertex[B2] = start.blue();
            vertex[A2] = start.alpha();
            // start.writeTo(vertex, R1);
            // end.writeTo(vertex, R2);
            return this;
        }

        @Override
        public ParticleTemplate start(Point p) {
            vertex[START_X] = coord(p.x());
            vertex[START_Y] = coord(p.y());
            vertex[START_Z] = coord(p.z());
            return this;
        }

        @Override
        public ParticleTemplate end(Point p) {
            vertex[END_X] = coord(p.x());
            vertex[END_Y] = coord(p.y());
            vertex[END_Z] = coord(p.z());
            return this;
        }

        @Override
        public ParticleTemplate ctrl1(Point p) {
            vertex[CTRL1_X] = coord(p.x());
            vertex[CTRL1_Y] = coord(p.y());
            return this;
        }

        @Override
        public ParticleTemplate ctrl2(Point p) {
            vertex[CTRL2_X] = coord(p.x());
            vertex[CTRL2_Y] = coord(p.y());

            return this;
        }

        @Override
        public void save() {
            ensureSpace();
            System.arraycopy(vertex, 0, data, index * VERTEX_SIZE, VERTEX_SIZE);
            index++;
        }

        @Override
        public ParticleTemplate time(double start, double end) {
            start += glScreen.time;
            end += glScreen.time;
            vertex[START_TIME] = (float) start; // (short) (start * 128);
            vertex[END_TIME] = (float) end; // (short) (end * 128);
            return this;
        }

        @Override
        public ParticleTemplate time(double interval) {
            time(0, interval);
            return this;
        }

    }

}
