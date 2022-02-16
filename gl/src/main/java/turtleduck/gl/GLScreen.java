package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.opengl.GL32C.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static turtleduck.gl.Vectors.vec4;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43C;
//import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.GLXSGIVideoSync;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import turtleduck.canvas.Canvas;
import turtleduck.canvas.CanvasImpl;
import turtleduck.colors.Color;
import turtleduck.display.Camera;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Screen;
import turtleduck.display.Viewport;
import turtleduck.display.Viewport.ViewportBuilder;
import turtleduck.display.impl.BaseScreen;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.gl.objects.FloatMath;
import turtleduck.gl.objects.ShaderObject;
import turtleduck.gl.objects.ShaderProgram;
import turtleduck.gl.objects.VertexArrayFormat;
import turtleduck.scene.SceneNode;
import turtleduck.scene.SceneObject2;
import turtleduck.scene.SceneObject3;
import turtleduck.scene.SceneVisitor;
import turtleduck.scene.SceneWorld;
import turtleduck.text.TextWindow;

public class GLScreen extends BaseScreen implements Screen {

	private static final int STD_WIDTH = 1280, STD_HEIGHT = 720;

	private static final boolean DOUBLE_BUFFER = true;
	private static final boolean WAIT_FOR_SYNC = true;
	long window;
	private boolean sgiVideoSync;
	private GLCapabilities caps;
	private Callback debugProc;
	public ShaderProgram shader2d, shader3d;
	private final int[] oldWindowGeometry = new int[4];

	public final Vector4f lightPosition = vec4(0f, -2.5f, -2.5f, 1f);// .rotateX(pi(1f/8));

	protected GLMouse mouse;
	private boolean fullscreen;

	private boolean wireframe = false;

	boolean paused = false;
	private GLStick joysticks;

	private RuntimeException exception;

	private GLLayer layer;

	public Camera camera2, camera3;

	private int frameBuf;

	public static boolean glHasProgramInterfaceQuery;

	public static int glMajor;

	public static int glMinor;

	@Override
	public void clearBackground() {
		// TODO Auto-generated method stub

	}

	@Override
	public Screen clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		return this;
	}

	@Override
	public Canvas createCanvas() {
		Canvas canvas = new CanvasImpl<>(newLayerId(), this, viewport.width(), viewport.height(),
				use3d -> layer.pathWriter(use3d), () -> layer.clear(), null);
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
	public void zoomFit() {
	}

	@Override
	public void zoomIn() {
		camera2.zoomIn();
		camera3.zoomIn();
	}

	public void fov(double fov) {
		camera2.fov(fov);
		camera3.fov(fov);
	}

	@Override
	public void zoomOne() {
		fov(50);
	}

	@Override
	public void zoomOut() {
		camera2.zoomOut();
		camera3.zoomOut();
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
		ViewportBuilder vpb = Viewport.create(GLDisplayInfo.INSTANCE);
		int w = 1600, h = 900;
		Viewport vp = vpb.screenArea(0, 0, 0, 0).width(1280).height(720).fit().done();

		return new GLScreen(vp);
	}

	public GLScreen(Viewport vp) {
		super(vp);

		camera3 = viewport.create3dCamera();
		camera2 = viewport.create2dCamera();
		setupAspects(vp.aspect());
//			background = new Canvas(dim.fbWidth, dim.fbHeight);
//			background.getGraphicsContext2D().scale(dim.resolutionScale(), dim.resolutionScale());
//			setBackground(bgColor);
//			clearBackground();
//			root.getChildren().add(background);
//			subScene.layoutBoundsProperty()
//					.addListener((ObservableValue<? extends Bounds> observable, Bounds oldBounds, Bounds bounds) -> {
//						recomputeLayout(false);
//					});

		int width = vp.screenWidth(), height = vp.screenHeight();

		glfwSetErrorCallback(this::callbackGlfwError);
		if (!glfwInit()) {
			throw new RuntimeException("Failed to initialize OpenGL");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		// glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glMajor = 3;
		glMinor = 2;
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, glMajor);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, glMinor);
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
		if (caps.GL_ARB_program_interface_query) {
			glHasProgramInterfaceQuery = true;
		}
		if (caps.GL_ARB_separate_shader_objects) {
			// would be needed for glBindProgramPipeline
		}
		glfwSetFramebufferSizeCallback(window, this::callbackFramebufferSize);
		glfwSetWindowSizeCallback(window, this::callbackWindowSize);
		glfwSetKeyCallback(window, this::processInput);
		glfwSetWindowCloseCallback(window, this::callbackWindowClosed);
		mouse = new GLMouse(this, viewport);
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
		glDrawBuffer(GL_FRONT);

		try (MemoryStack frame = MemoryStack.stackPush()) {
			IntBuffer framebufferSize = frame.mallocInt(2);
			nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
			width = framebufferSize.get(0);
			height = framebufferSize.get(1);
		}

		if (glMajor >= 4 && glMinor >= 3) {
			debugProc = GLUtil.setupDebugMessageCallback();
			GL43C.glDebugMessageControl(GL_DONT_CARE, GL43C.GL_DEBUG_TYPE_OTHER, GL43C.GL_DEBUG_SEVERITY_NOTIFICATION, (IntBuffer) null,
					false);
			glEnable(GL43C.GL_DEBUG_OUTPUT);
		}
//		glEnable(GL_MULTISAMPLE);

		System.err.println("Creating buffers");

		try {
			VertexArrayFormat format = new VertexArrayFormat();
			format.addField("aPos", Vector3f.class);
			format.addField("aColor", Color.class);
			format.addField("aTexCoord", Vector2f.class);
			VertexArrayFormat format3 = new VertexArrayFormat();
			format3.addField("aPos", Vector3f.class);
			format3.addField("aColor", Color.class);
			format3.addField("aNormal", Vector3f.class);
			format3.addField("aTexCoord", Vector2f.class);

			ShaderObject vs = ShaderObject.create("/turtleduck/gl/shaders/simple-vs.glsl", GL_VERTEX_SHADER);
			ShaderObject fs = ShaderObject.create("/turtleduck/gl/shaders/color-fs.glsl", GL_FRAGMENT_SHADER);
			shader3d = ShaderProgram.createProgram("shader3d", format3, vs, fs);
			ShaderObject vs2 = ShaderObject.create("/turtleduck/gl/shaders/twodee-vs.glsl", GL_VERTEX_SHADER);
			ShaderObject fs2 = ShaderObject.create("/turtleduck/gl/shaders/twodee-fs.glsl", GL_FRAGMENT_SHADER);
			shader2d = ShaderProgram.createProgram("shader2d", format, vs2, fs2);
System.out.println(shader2d.format());
			shader2d.format(format);
			System.out.println(shader3d.format());
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

		fov(50);
		camera2.updateProjection();
		camera2.updateView();
		camera3.updateProjection();
		camera3.updateView();
		glfwSetCursorPos(window, width / 2, height / 2);
		glClearColor(0.1f, 0.15f, 0.15f, 0.0f);
		glClearColor(0f, 0f, 0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glfwPollEvents();
		mouse.callbackMousePosition(0, width / 2, height / 2);
		System.out.println("new layer");
		layer = new GLLayer(newLayerId(), this, camera2, camera3, viewport.width(), viewport.height());
		System.out.println(layer);
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
		if (width > 0 && height > 0 && (viewport.screenWidth() != width || viewport.screenHeight() != height)) {
			recomputeDimensions(0, 0, width, height);
			glViewport(viewport.screenX(), viewport.screenY(), viewport.screenWidth(), viewport.screenHeight());
			createBuffers();
			camera2.updateProjection();
			camera3.updateProjection();
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
		if (width > 0 && height > 0 && (viewport.screenWidth() != width || viewport.screenHeight() != height)) {
//			recomputeDimensions(200, 200, width-200, height-200);

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
			if (key == GLFW_KEY_A) {
				camera2.position.add(-8f, 0, 0, 0);
				camera2.updateView();
			} else if (key == GLFW_KEY_S) {
				camera2.position.add(0, 8f, 0, 0);
				camera2.updateView();
			} else if (key == GLFW_KEY_D) {
				camera2.position.add(8f, 0, 0, 0);
				camera2.updateView();
			} else if (key == GLFW_KEY_W) {
				camera2.position.add(0, -8f, 0, 0);
				camera2.updateView();
			} else if (key == GLFW_KEY_Q) {
				camera2.orientation.rotateLocalZ(-FloatMath.PI / 8f);
				camera2.updateView();
			} else if (key == GLFW_KEY_E) {
				camera2.orientation.rotateLocalZ(FloatMath.PI / 8f);
				camera2.updateView();

			}
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
		dest.set((2f * (float) x + 1f) / (float) viewport.screenWidth() - 1f,
				(2f * (float) y + 1f) / (float) viewport.screenHeight() - 1f);
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

	@Override
	protected void recomputeLayout(boolean b) {
		// TODO Auto-generated method stub

	}

	public void render() {
//		updateProjection();
//		updateView();

//		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, frameBuf);
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
				public Void visitCamera(turtleduck.scene.Camera camera, GLRenderContext context) {
					context.proj = new Matrix4f();
					if (camera.isPerspective())
						context.proj.setPerspective((float) camera.fieldOfView(), (float) viewport.aspect(),
								(float) camera.nearClip(), (float) camera.farClip());
					else
						context.proj.setOrtho(0, (float) viewport.width(), 0, (float) viewport.height(),
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
//		shader2d.bind();
//		modelMatrix.identity();
//		uModel.set(modelMatrix);

		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);
		glDisable(GL_BLEND);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		forEachLayer(true, (l) -> ((GLLayer) l).render(true, false));

		glDepthMask(false);
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		forEachLayer(false, (l) -> ((GLLayer) l).render(false, !paused));

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		if (DOUBLE_BUFFER) {
			glDrawBuffer(GL_BACK);
//			 glBlitFramebuffer(0, 0, width, height, 0, 0, fbWidth, fbHeight, GL_COLOR_BUFFER_BIT/*|GL_DEPTH_BUFFER_BIT*/, GL_LINEAR);
			glfwSwapBuffers(window);
//			glFinish();
		} else {
			glFlush();
//			glFinish();
			if (sgiVideoSync) {
				int[] count = new int[1];
				GLXSGIVideoSync.glXGetVideoSyncSGI(count);
				GLXSGIVideoSync.glXWaitVideoSyncSGI(2, (count[0] + 1) % 2, count);
			}
		}
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
