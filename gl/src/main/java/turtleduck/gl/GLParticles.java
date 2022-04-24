package turtleduck.gl;

import static org.lwjgl.opengl.GL32C.*;

import java.util.Arrays;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL42C;
import org.lwjgl.opengl.GL43C;

import turtleduck.colors.Color;
import turtleduck.display.Camera;
import turtleduck.geometry.Point;
import turtleduck.gl.GLLayer.DrawObject;
import turtleduck.gl.objects.Uniform;
import turtleduck.shapes.Particles;
import turtleduck.util.MathUtil;

public class GLParticles implements Particles {
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
	static final int VERTEX_SIZE = i;
	protected Point point = Point.ZERO;
	protected int nParticles = 1024;
	protected double particleSize = 128;
	protected Camera camera;
	protected float[] data;
	protected int index, lastDrawn;
	protected GLParticle template = new GLParticle();
	protected GLLayer glLayer;
	protected GLScreen glScreen;
	protected int vbo, vao;

	public GLParticles(GLLayer glLayer, GLScreen glScreen) {
		this.glLayer = glLayer;
		this.glScreen = glScreen;
		this.camera = glScreen.camera2;
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
		this.nParticles = n;
		return this;
	}

	@Override
	public int nParticles() {
		return nParticles;
	}

	@Override
	public Particles particleSize(double size) {
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
	public void draw() {
		DrawObject obj = new DrawObject();
		obj.array = null;
		obj.shader = glScreen.shaderPart;
		obj.drawMode = GL_POINT;
		obj.offset = 0;
		obj.nVertices = nParticles;
		obj.type = "particle";
		obj.zOrder = 0;
		int minDepth = Integer.MAX_VALUE;
		int maxDepth = 0;
		obj.transform.identity();
		obj.projection = camera.projectionMatrix;
		obj.blend = true;
		float z = -glLayer.depth++ / 6553f;
		obj.drawMethod = () -> {
			Uniform<Float> uTime = obj.shader.uniform("uTime", Float.class);
			Uniform<Float> uZ = obj.shader.uniform("uZ", Float.class);
			uTime.set((float) glScreen.time); // TODO: deal with short wraparound
			uZ.set(z);
			if (vao == 0) {
				vbo = glGenBuffers();
				vao = glGenVertexArrays();
				glBindVertexArray(vao);
				glBindBuffer(GL_ARRAY_BUFFER, vbo);
				obj.shader.format().setVertexAttributes(0);
			} else {
				glBindVertexArray(vao);
				glBindBuffer(GL_ARRAY_BUFFER, vbo);
			}
			glPointSize((float) particleSize);
			if(index < lastDrawn)
				lastDrawn = 0;
			if (lastDrawn > 0) {
				float[] newData = Arrays.copyOfRange(data, lastDrawn * VERTEX_SIZE,
						index * VERTEX_SIZE);
				glBufferSubData(GL_ARRAY_BUFFER, lastDrawn * obj.shader.format().numBytes(), newData);
			} else {
				glBufferData(GL_ARRAY_BUFFER, data, GL_STREAM_DRAW);
			}
			lastDrawn = index;
			glDrawArrays(GL_POINTS, 0, nParticles);
		//	GL43C.glInvalidateBufferData(vbo);
//			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		};
		glLayer.drawObjects.add(obj);

	}

	public void dispose() {
		if (vao != 0) {
			glDeleteVertexArrays(vao);
			vao = 0;
		}
		if (vbo != 0) {
			glDeleteBuffers(vbo);
		}
		data = null;
	}

	protected void ensureSpace() {
		if (data == null) {
			data = new float[nParticles * VERTEX_SIZE];
		} else if (data.length < nParticles * VERTEX_SIZE) {
			data = Arrays.copyOf(data, nParticles * VERTEX_SIZE);
		}
		if (index >= nParticles)
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
//			start.writeTo(vertex, R1);
//			end.writeTo(vertex, R2);
			return this;
		}

		@Override
		public ParticleTemplate start(Point p) {
			vertex[START_X] = coord(p.x());
			vertex[START_Y] = coord(p.y());
			return this;
		}

		@Override
		public ParticleTemplate end(Point p) {
			vertex[END_X] = coord(p.x());
			vertex[END_Y] = coord(p.y());
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
