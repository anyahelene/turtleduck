package turtleduck.gl;

import static turtleduck.gl.Vectors.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

import org.lwjgl.BufferUtils;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

import java.io.IOException;
import java.nio.IntBuffer;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
//import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.GLXSGIVideoSync;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import turtleduck.canvas.CanvasImpl;
import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseScreen;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.gl.objects.CubeModel;
import turtleduck.gl.objects.ShaderObject;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.Uniform;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.scene.Camera;
import turtleduck.scene.SceneNode;
import turtleduck.scene.SceneObject2;
import turtleduck.scene.SceneObject3;
import turtleduck.scene.SceneVisitor;
import turtleduck.scene.SceneWorld;
import turtleduck.text.TextWindow;

public class GLScreen extends BaseScreen implements Screen {

	private static final int STD_WIDTH = 1280, STD_HEIGHT = 720;

	private static final boolean DOUBLE_BUFFER = true;
	private static final boolean WAIT_FOR_SYNC = false;
	long window;
	private boolean sgiVideoSync;
	private GLCapabilities caps;
	private Callback debugProc;
	public ShaderProgram shader2d, shader3d;
	private final int[] oldWindowGeometry = new int[4];
	Uniform<Matrix4f> uModel;
	private Uniform<Matrix4f> uView;
	private Uniform<Matrix4f> uProjection;
	private Uniform<Matrix4f> uProjView;
	public final Matrix4f perspectiveProjectionMatrix = new Matrix4f();
	public final Matrix4f perspectiveProjectionMatrixInv = new Matrix4f();
	public final Matrix4f projectionMatrix = new Matrix4f();
	public final Matrix4f modelMatrix = new Matrix4f();
	public final Matrix4f viewMatrix = new Matrix4f();
	public final Matrix4f perspectiveViewMatrix = new Matrix4f();
	public final Matrix4f projectionMatrixInv = new Matrix4f();
	public final Matrix4f viewMatrixInv = new Matrix4f();
//	public final Vector4f lightPosition = vec4(-1.2f,0f,5f, 1f);// .rotateX(pi(1f/8));
	public final Vector4f lightPosition = vec4(0f, -2.5f, -2.5f, 1f);// .rotateX(pi(1f/8));
	public final Vector4f defaultCameraPosition = vec4(0f, 0f, 25f, 1f);
	private final Quaternionf defaultCameraOrientation = new Quaternionf(new AxisAngle4f(0, 0, 0, -1));
	public final Vector4f cameraPosition = vec4(defaultCameraPosition);
	final Quaternionf cameraOrientation = new Quaternionf(defaultCameraOrientation);
	private ShaderProgram renderShader;
	private ShaderProgram blurShader;
	private CubeModel cubeModel;
//	private int fbWidth;
//	private int fbHeight;
//	private int width;
//	int height;
	protected GLMouse mouse;
	private boolean fullscreen;
	public double fov = 50;
	private boolean wireframe = false;
	private int frameBuf;
	boolean paused = false;
	private GLStick joysticks;

	private RuntimeException exception;

	private GLLayer layer;

	private double zoom;

	private double visibleWidth;

	private double visibleHeight;

	@Override
	public void clearBackground() {
		// TODO Auto-generated method stub

	}

	@Override
	public Canvas createCanvas() {
		Canvas canvas = new CanvasImpl<>(newLayerId(), this, dim.winWidth, dim.winHeight,
				use3d -> layer.pathWriter(use3d), null);
		return canvas;
	}

	@Override
	public TextWindow createTextWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cycleAspect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void fitScaling() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAspect() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Layer getBackgroundPainter() {
		// TODO Auto-generated method stub
		return null;
	}

	public GLLayer getGLLayer() {
		return layer;
	}

	@Override
	public void hideMouseCursor() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFullScreen() {
		return fullscreen;
	}

	@Override
	public boolean minimalKeyHandler(KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void moveToBack(Layer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToFront(Layer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAspect(int aspect) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBackground(Color bgColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHideFullScreenMouseCursor(boolean hideIt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMouseCursor(MouseCursor cursor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showMouseCursor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomCycle() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomFit() {
	}

	@Override
	public void zoomIn() {
		fov(fov / 1.05);
	}

	public void fov(double fov) {
		if (fov < 10.0f) {
			this.fov = 10.0f;
		} else if (fov > 120.0f) {
			this.fov = 120.0f;
		} else {
			this.fov = fov;
		}
		this.zoom = 50.0 / this.fov;
		updateProjection();
	}

	@Override
	public void zoomOne() {
		fov(50);
	}

	@Override
	public void zoomOut() {
		fov(fov * 1.05);
	}

	@Override
	public void flush() {
		render();
	}

	@Override
	public void clipboardPut(String copied) {
		glfwSetClipboardString(window, copied);
	}

	public static GLScreen create(int config) {
		Dimensions dim = computeDimensions(GLDisplayInfo.INSTANCE, config);

		return new GLScreen(dim);
	}

	public GLScreen(Dimensions dim) {
		int width = (int) Math.floor(dim.winWidth);
		int height = (int) Math.floor(dim.winHeight);
		this.dim = dim;
		setupAspects(dim);
//			background = new Canvas(dim.fbWidth, dim.fbHeight);
//			background.getGraphicsContext2D().scale(dim.resolutionScale(), dim.resolutionScale());
//			setBackground(bgColor);
//			clearBackground();
//			root.getChildren().add(background);
//			subScene.layoutBoundsProperty()
//					.addListener((ObservableValue<? extends Bounds> observable, Bounds oldBounds, Bounds bounds) -> {
//						recomputeLayout(false);
//					});

		init(width, height);
		System.out.println(dim);
	}

	protected void init(int width, int height) {
		glfwSetErrorCallback(this::callbackGlfwError);
		if (!glfwInit()) {
			throw new RuntimeException("Failed to initialize OpenGL");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		// glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
//		glfwWindowHint(GLFW_SAMPLES, 1);
		glfwWindowHint(GLFW_DOUBLEBUFFER, DOUBLE_BUFFER ? GLFW_TRUE : GLFW_FALSE);

		window = glfwCreateWindow(width, height, getClass().getName(), NULL, NULL);
		if (window == NULL) {
			glfwTerminate();
			throw new AssertionError("Failed to create the GLFW window");
		}
		glfwMakeContextCurrent(window);
		GLDisplayInfo.INSTANCE.updateVideoMode(window);
		caps = GL.createCapabilities();
		if (!caps.GL_ARB_program_interface_query) {
			glfwTerminate();
			throw new AssertionError("Required OpenGL extension missing: ARB_program_interface_query");
		}
		if (!caps.GL_ARB_shader_objects) {
			glfwTerminate();
			throw new AssertionError("Required OpenGL extension missing: ARB_shader_objects");
		}
		if (!caps.GL_ARB_vertex_shader) {
			glfwTerminate();
			throw new AssertionError("Required OpenGL extension missing: ARB_vertex_shader");
		}
		if (!caps.GL_ARB_separate_shader_objects) {
			glfwTerminate();
			throw new AssertionError("Required OpenGL extension missing: ARB_fragment_shader");
		}
		glfwSetFramebufferSizeCallback(window, this::callbackFramebufferSize);
		glfwSetWindowSizeCallback(window, this::callbackWindowSize);
		glfwSetKeyCallback(window, this::processInput);
		glfwSetWindowCloseCallback(window, this::callbackWindowClosed);
		mouse = new GLMouse(this, dim);
		mouse.registerCallbacks(window);
		joysticks = new GLStick(this);
		joysticks.init();
		joysticks.registerCallbacks();
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidmode = glfwGetVideoMode(monitor);
		glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(0);
		glfwShowWindow(window);
		glfwPollEvents();
		setFullScreen(false);
		if (WAIT_FOR_SYNC) {
			if (glfwExtensionSupported("GLX_SGI_video_sync")) {
				System.err.println("Has GLX_SGI_video_sync");
				sgiVideoSync = true;
			}
			if (glfwExtensionSupported("WGL_EXT_swap_control_tear")
					|| glfwExtensionSupported("GLX_EXT_swap_control_tear")) {
				System.err.println("swap_control_tear supported");
				glfwSwapInterval(-1);
			} else {
				System.err.println("swap_control_tear not supported");
				glfwSwapInterval(1);
			}
		}

		try (MemoryStack frame = MemoryStack.stackPush()) {
			IntBuffer framebufferSize = frame.mallocInt(2);
			nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
			width = framebufferSize.get(0);
			height = framebufferSize.get(1);
		}

		debugProc = GLUtil.setupDebugMessageCallback();

//		glEnable(GL_MULTISAMPLE);

		System.err.println("Creating buffers");

		try {
			ShaderObject rvs = ShaderObject.create("/turtleduck/gl/shaders/render-vs.glsl", GL_VERTEX_SHADER);
			ShaderObject rfs = ShaderObject.create("/turtleduck/gl/shaders/render-fs.glsl", GL_FRAGMENT_SHADER);
			renderShader = ShaderProgram.createProgram("render", rvs, rfs);
			ShaderObject bfs = ShaderObject.create("/turtleduck/gl/shaders/blur-fs.glsl", GL_FRAGMENT_SHADER);
			blurShader = ShaderProgram.createProgram("blur", rvs, bfs);
			ShaderObject vs = ShaderObject.create("/turtleduck/gl/shaders/simple-vs.glsl", GL_VERTEX_SHADER);
			ShaderObject fs = ShaderObject.create("/turtleduck/gl/shaders/color-fs.glsl", GL_FRAGMENT_SHADER);
			shader3d = ShaderProgram.createProgram("shader3d", vs, fs);
			ShaderObject vs2 = ShaderObject.create("/turtleduck/gl/shaders/twodee-vs.glsl", GL_VERTEX_SHADER);
			ShaderObject fs2 = ShaderObject.create("/turtleduck/gl/shaders/twodee-fs.glsl", GL_FRAGMENT_SHADER);
			shader2d = ShaderProgram.createProgram("shader2d", vs2, fs2);
			VertexArrayFormat format = new VertexArrayFormat();
			format.addField("aPos", Vector3f.class);
			format.addField("aColor", Color.class);
			VertexArrayFormat format3 = new VertexArrayFormat();
			format3.addField("aPos", Vector3f.class);
			format3.addField("aColor", Color.class);
			format3.addField("aNormal", Vector3f.class);
			format3.addField("aTexCoord", Vector2f.class);

			shader2d.format(format);
			shader3d.format(format3);
//			shader3d.uniform("uLightPos", Vector4f.class).set(lightPosition);
//			shader3d.uniform("uViewPos", Vector4f.class).set(defaultCameraPosition);
//			uModel = shader2d.uniform("uModel", Matrix4f.class);
//			uProjection = shader2d.uniform("uProjection", Matrix4f.class);
//			uView = shader2d.uniform("uView", Matrix4f.class);
//			uProjView = shader2d.uniform("uProjView", Matrix4f.class);
			shader2d.setUniform("texture0", 0);
			shader2d.setUniform("texture1", 1);

		} catch (IOException e) {
			e.printStackTrace();
//			throw new RuntimeException("Error loading shaders", e);
		}
		cubeModel = new CubeModel();
		cubeModel.scale(.2f, 0.05f, .1f);

		fov(50);
		updateProjection();
		updateView();

		glfwSetCursorPos(window, width / 2, height / 2);
		glClearColor(0.1f, 0.15f, 0.15f, 0.0f);
		glClearColor(0f, 0f, 0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glfwPollEvents();
		mouse.callbackMousePosition(0, width / 2, height / 2);

		layer = new GLLayer(newLayerId(), this, dim.winWidth, dim.winHeight);
		addLayer(layer);
	}

	public void callbackGlfwError(int error, long description) {
		String desc = MemoryUtil.memUTF8(description);
		exception = new RuntimeException("GLFW error " + error + ": " + desc);
	}

	public void checkError() {
		if (exception != null) {
			RuntimeException ex = exception;
			exception = null;
			throw ex;
		}
	}

	public void callbackFramebufferSize(long window, int width, int height) {
		if (width > 0 && height > 0 && (dim.winWidth != width || dim.winHeight != height)) {
			dim.winWidth = width;
			dim.winHeight = height;
			glViewport(0, 0, width, height);
			createBuffers();
			updateProjection();
			System.err.println("Framebuffer size: " + width + ", " + height);
		}
	}

	private void createBuffers() {
		frameBuf = glGenFramebuffers();
//		glBindFramebuffer(GL_FRAMEBUFFER, frameBuf);

	}

	public void callbackWindowClosed(long window) {
		System.err.println("Window closed");
		System.exit(0);
	}

	public void callbackWindowSize(long window, int width, int height) {
		if (width > 0 && height > 0 && (dim.winWidth != width || dim.winHeight != height)) {
			dim.winWidth = width;
			dim.winHeight = height;
			System.err.println("Window size: " + width + ", " + height);
		}
	}

	public void setFullScreen(boolean fullscreen) {
		if (this.fullscreen == fullscreen) {
		} else {
			long monitor = glfwGetPrimaryMonitor(); // glfwGetWindowMonitor(window);
			GLFWVidMode vidmode = glfwGetVideoMode(monitor);
			System.err.printf("vidmode: %dx%d R%d_G%d_B%d @ %d Hz%n", vidmode.width(), vidmode.height(),
					vidmode.redBits(), vidmode.greenBits(), vidmode.blueBits(), vidmode.refreshRate());
			if (fullscreen) {
				System.err.println("Going fullscreen");

				IntBuffer b = BufferUtils.createIntBuffer(4);
				IntBuffer[] bs = BufferUtil.sliceBuffer(b, 4);
				glfwGetWindowPos(window, bs[0], bs[1]);
				glfwGetWindowSize(window, bs[2], bs[3]);
				b.get(oldWindowGeometry, 0, 4);
				glfwSetWindowMonitor(window, monitor, 0, 0, vidmode.width(), vidmode.height(), GLFW_DONT_CARE);
			} else {
				System.err.println("Going windowed, " + Arrays.toString(oldWindowGeometry));
				int w = oldWindowGeometry[2];
				int h = oldWindowGeometry[3];
				if (w == 0 || h == 0) {
					w = STD_WIDTH;
					h = STD_HEIGHT;
				}
				glfwSetWindowMonitor(window, NULL, oldWindowGeometry[0], oldWindowGeometry[1], w, h, GLFW_DONT_CARE);
				// glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() -
				// HEIGHT) / 2);
			}
			this.fullscreen = fullscreen;
		}
	}

	private void processInput(long window, int key, int scancode, int action, int mods) {
		if (action != GLFW_PRESS) {
			return;
		}

		if (key == GLFW_KEY_ESCAPE) {
			glfwSetWindowShouldClose(window, true);
			callbackWindowClosed(window);
		} else if (mods == 0) {
		} else if (mods == GLFW_MOD_ALT)
			if (key == GLFW_KEY_W) {
				wireframe = !wireframe;
			} else if (key == GLFW_KEY_P) {
				paused = !paused;
			} else if (key == GLFW_KEY_F) {
				setFullScreen(!fullscreen);
			}
	}

	public Vector2f deviceToScreen(Vector2f coord) {
		return deviceToScreen(coord.x, coord.y, coord);
	}

	public Vector2f deviceToScreen(double x, double y) {
		return deviceToScreen(x, y, new Vector2f());
	}

	public Vector2f deviceToScreen(double x, double y, Vector2f dest) {
		dest.set((2f * (float) x + 1f) / (float) dim.winWidth - 1f, (2f * (float) y + 1f) / (float) dim.winHeight - 1f);
		return dest;
	}

	public static void deviceToScreen(Vector2f coord, Vector2fc screenSize) {
		coord.set((2f * coord.x + 1f) / screenSize.x() - 1f, (2f * coord.y + 1f) / screenSize.y() - 1f);
	}

	public static void screenToDevice(Vector2f coord, Vector2fc screenSize) {
		coord.set(Math.round((coord.x + 1f) * screenSize.x() - 1f) / 2,
				Math.round((coord.y + 1f) * screenSize.y() - 1f) / 2);
		// coord.add(1,1);
		// coord.mul(screenSize);
		// coord.add(-1,-1);
		// coord.mul(0.5f,0.5f);
	}

	/**
	 * Reverse project a screen position to a position in object space.
	 *
	 * @param mousePos A screen position
	 * @return A new vector representing a position somewhere on a line through the
	 *         camera and the given screen position
	 */
	public Vector3f unproject(Vector2f mousePos) {
		Vector3f v = vec3(mousePos, 0f);
		projectionMatrixInv.transformProject(v);
		viewMatrixInv.transformPosition(v);

		return v;
	}

	/**
	 * Reverse project a screen position to a position in object space, store result
	 * in dest.
	 *
	 * @param mousePos A screen position
	 * @return dest
	 */
	public Vector3f unproject(Vector2i mousePos, Vector3f dest) {
		dest.set(mousePos, 0);
		projectionMatrixInv.transformProject(dest);
		viewMatrixInv.transformPosition(dest);

		return dest;
	}

	/**
	 * Project pos according to current view and projection matrices.
	 *
	 * @param pos
	 * @return A new vector, P*V*pos
	 */
	public Vector3f project(Vector3f pos) {
		Vector3f v = vec3(pos);
		viewMatrix.transformPosition(v);
		projectionMatrix.transformProject(v);

		return v;
	}

	public void updateProjection() {
		visibleWidth = dim.canvasWidth;
		visibleHeight = dim.canvasHeight = (dim.canvasWidth * dim.winHeight / dim.winWidth);
		float w = (float) visibleWidth, h = (float) visibleHeight;
		perspectiveProjectionMatrix.setPerspective((float) Math.toRadians(fov), (float) (dim.winWidth / dim.winHeight),
				1f, 1000.0f);
		perspectiveProjectionMatrix.invertPerspective(perspectiveProjectionMatrixInv);
		projectionMatrix.setOrtho(-w / 2, w / 2, -h / 2, h / 2, -1, 1);
		projectionMatrix.scale(1, -1, 1);
		projectionMatrix.scale((float) zoom);
		projectionMatrix.invertOrtho(projectionMatrixInv);
		System.err.println(projectionMatrix.transformProject(new Vector3f(0, 0, 0)));
		System.err.println(projectionMatrix.transformProject(new Vector3f(640, 0, 0)));
		System.err.println(projectionMatrix.transformProject(new Vector3f(-640, 0, 0)));
		System.err.println(projectionMatrix.transformProject(new Vector3f(0, 360, 0)));
		System.err.println(projectionMatrix.transformProject(new Vector3f(0, -360, 0)));
//		if (uProjection != null) {
//			uProjection.set(projectionMatrix);
//		}
	}

	public void updateView() {
		cameraOrientation.get(perspectiveViewMatrix);
		perspectiveViewMatrix.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z).scale(1f / 16f);
		viewMatrix.identity();
		viewMatrix.invertAffine(viewMatrixInv);
//		if (uView != null) {
//			uView.set(viewMatrix);
//		}
//		if (uProjView != null) {
//			uProjView.set(new Matrix4f(projectionMatrix).mul(viewMatrix));
//		}
	}

	@Override
	protected void recomputeLayout(boolean b) {
		// TODO Auto-generated method stub

	}

	Stats stats = new Stats();

	public void render() {
//		updateProjection();
//		updateView();

//		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, frameBuf);
		stats.startFrame();
		glfwPollEvents();
		joysticks.processInput();
		if (wireframe) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		} else {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		}

		for (SceneWorld world : scenes) {
			List<GLRenderContext> cameras = new ArrayList<>();
			List<SceneNode> objs = new ArrayList<>();
			List<Matrix4f> mats = new ArrayList<>();
			GLRenderContext context = new GLRenderContext();
			world.accept(new SceneVisitor<Void, GLRenderContext>() {

				@Override
				public Void visitObject2(SceneObject2<?> obj, GLRenderContext context) {
					return null;
				}

				@Override
				public Void visitObject3(SceneObject3<?> obj, GLRenderContext context) {
					objs.add(obj);
					mats.add(context.matrix());
					return null;
				}

				@Override
				public Void visitNode(SceneNode obj, GLRenderContext context) {
					return null;
				}

				@Override
				public Void visitWorld(SceneWorld world, GLRenderContext context) {
					return visitGroup3(world, context);
				}

				@Override
				public Void visitCamera(Camera camera, GLRenderContext context) {
					context.proj = new Matrix4f();
					if (camera.isPerspective())
						context.proj.setPerspective((float) camera.fieldOfView(),
								(float) (dim.winWidth / dim.winHeight), (float) camera.nearClip(),
								(float) camera.farClip());
					else
						context.proj.setOrtho(0, (float) dim.canvasWidth, 0, (float) dim.canvasHeight,
								(float) camera.nearClip(), (float) camera.farClip());
//					Orientation orient = camera.orientation();
//					Matrix4f view = orient.toMatrix(new Matrix4f());
//					view.translate(-(float) camera.x(), -(float) camera.y(), -(float) camera.z());
					context.view = context.matrix();
					context.projView = new Matrix4f(context.proj).mul(context.view);
					cameras.add(context);
					return null;
				}
			}, context);
		}

//		glEnable(GL_DEPTH_TEST);
		// update();
		glEnable(GL_FRAMEBUFFER_SRGB);
		glEnable(GL_CULL_FACE);
		glFrontFace(GL_CCW);
		glCullFace(GL_BACK);
		glDisable(GL_CULL_FACE);
//		glDisable(GL_FRAMEBUFFER_SRGB); 
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		shader2d.bind();
		modelMatrix.identity();
//		uModel.set(modelMatrix);

		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		glDepthMask(true);
//		glDisable(GL_CULL_FACE);
		forEachLayer(true, (l) -> ((GLLayer) l).render(true, false));

		glEnable(GL_DEPTH_TEST);
		glDepthMask(false);
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
//		glEnable(GL_CULL_FACE);
//		glDisable(GL_CULL_FACE);
		forEachLayer(false, (l) -> ((GLLayer) l).render(false, !paused));
		glDepthMask(true);
		glDisable(GL_BLEND);

//		shader3d.bind();
//		cubeModel.moveTo(-0,0, 3);
//		cubeModel.rotate(0.01f, 0.01f, 0.01f);
//		cubeModel.scale(2f);
//		cubeModel.render(this);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		if (DOUBLE_BUFFER) {
			glDrawBuffer(GL_BACK);
//			 glBlitFramebuffer(0, 0, width, height, 0, 0, fbWidth, fbHeight, GL_COLOR_BUFFER_BIT/*|GL_DEPTH_BUFFER_BIT*/, GL_LINEAR);
			glfwSwapBuffers(window);
		} else {
//			glFlush();
			glFinish();
			if (sgiVideoSync) {
				int[] count = new int[1];
				GLXSGIVideoSync.glXGetVideoSyncSGI(count);
				GLXSGIVideoSync.glXWaitVideoSyncSGI(2, (count[0] + 1) % 2, count);
			}
		}
		stats.endFrame(true);
	}

	public void dispose() {
		if (debugProc != null) {
			debugProc.free();
		}
		if (window != NULL) {
			glfwSetFramebufferSizeCallback(window, null);
			glfwSetWindowSizeCallback(window, null);
			glfwSetKeyCallback(window, null);
			joysticks.unregisterCallbacks();
			glfwSetMouseButtonCallback(window, null);
			glfwSetCursorPosCallback(window, null);
			glfwSetScrollCallback(window, null);
			glfwDestroyWindow(window);
		}
	}

	@Override
	public <T> InputControl<T> inputControl(Class<T> type, int code, int controller) {
		return joysticks.inputControl(type, code, controller);
	}

	@Override
	public ScreenControls controls() {
		return this;
	}

	@Override
	protected void exit() {
		dispose();
		System.exit(0);
	}

	@Override
	protected String getClipboardString() {
		return GLFW.glfwGetClipboardString(window);
	}
}
