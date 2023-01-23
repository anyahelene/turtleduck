package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static turtleduck.gl.Vectors.vec4;
import static turtleduck.gl.compat.GLConstants.*;

import java.io.IOException;
import java.nio.ByteBuffer;
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

import org.lwjgl.opengl.GLXSGIVideoSync;
import org.lwjgl.opengles.GLES;
import org.lwjgl.opengles.GLES20;
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
import turtleduck.gl.compat.GLA;
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
    public static GLA gl;
    public final GLA gla;
    private static final int STD_WIDTH = 1280, STD_HEIGHT = 720;

    private static final boolean DOUBLE_BUFFER = true;
    private static final boolean WAIT_FOR_SYNC = true;
    long window;
    private boolean sgiVideoSync;
    private Callback debugProc;
    public ShaderProgram shader2d, shader3d, shaderPart;
    private final int[] oldWindowGeometry = new int[4];

    public final Vector4f lightPosition = vec4(64, 36, 256, 1f);// .rotateX(pi(1f/8));

    protected GLMouse mouse;
    private boolean fullscreen;

    private boolean wireframe = false;

    boolean paused = false;
    private GLStick joysticks;

    private RuntimeException exception;

    private GLLayer layer;

    public Camera camera2, camera3;

    private int frameBuf;

    double time;

    @Override
    public void clearBackground() {
        // TODO Auto-generated method stub

    }

    @Override
    public Screen clear() {
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
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

        if (System.getenv("TD_USE_GLES") != null)
            GLA.setup().apiGLES().initialize();
        else
            GLA.setup().apiGL().initialize();

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

//      glfwWindowHint(GLFW_SAMPLES, 1);

//      glfwWindowHint(GLFW_RED_BITS, 10);
//      glfwWindowHint(GLFW_GREEN_BITS, 10);
//      glfwWindowHint(GLFW_BLUE_BITS, 10);
        System.out.println("running on " + GLA.GLA_STATE.platform());

        window = glfwCreateWindow(width, height, getClass().getName(), NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new AssertionError("Failed to create the GLFW window");
        }
        glfwMakeContextCurrent(window);
        GLDisplayInfo.INSTANCE.updateVideoMode(window);

        glfwSetFramebufferSizeCallback(window, this::callbackFramebufferSize);
        glfwSetWindowSizeCallback(window, this::callbackWindowSize);
        glfwSetKeyCallback(window, this::processInput);
        glfwSetWindowCloseCallback(window, this::callbackWindowClosed);

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

        gla = GLA.get();
        if (gl == null)
            gl = gla;
        describeFramebuffer(GL_DRAW_FRAMEBUFFER, gl.GL_FRONT_FB());
        gl.glDrawBuffer(gl.GL_BACK_FB());
//		glEnable(GL_FRAMEBUFFER_SRGB);

        describeFramebuffer(GL_DRAW_FRAMEBUFFER, gl.GL_BACK_FB());

        mouse = new GLMouse(this, viewport);
        mouse.registerCallbacks(window);
        joysticks = new GLStick(this);
        joysticks.init();
        joysticks.registerCallbacks();

        try (MemoryStack frame = MemoryStack.stackPush()) {
            IntBuffer framebufferSize = frame.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            width = framebufferSize.get(0);
            height = framebufferSize.get(1);
        }

        debugProc = gl.enableDebug();
        System.out.println("Enable debug: " + gl + debugProc);
//		glEnable(GL_MULTISAMPLE);

	//	createBuffers();

        try {
            VertexArrayFormat format = new VertexArrayFormat();
            format.addField("aPos", Vector4f.class);
            format.addField("aColor", Color.class);
            format.addField("aTexCoord", Vector2f.class);
            VertexArrayFormat format3 = new VertexArrayFormat();
            format3.addField("aPos", Vector4f.class);
            format3.addField("aColor", Color.class);
            format3.addField("aNormal", Vector3f.class);
            format3.addField("aTexCoord", Vector2f.class);

            ShaderObject vs = ShaderObject.create("simple.vert.glsl", GL_VERTEX_SHADER);
            ShaderObject fs = ShaderObject.create("color.frag.glsl", GL_FRAGMENT_SHADER);
            shader3d = ShaderProgram.createProgram("shader3d", format3, vs, fs);
            System.out.println(shader3d.format());
            shader3d.format(format3);
            System.out.println(shader3d.format());

            ShaderObject vs2 = ShaderObject.create("twodee.vert.glsl", GL_VERTEX_SHADER);
            ShaderObject fs2 = ShaderObject.create("twodee.frag.glsl", GL_FRAGMENT_SHADER);
            shader2d = ShaderProgram.createProgram("shader2d", format, vs2, fs2);
            System.out.println(shader2d.format());
            shader2d.format(format);
            System.out.println(shader2d.format());

//			shaderPart.format(formatPart);
//			System.out.println(shaderPart.format());
            // shader3d.uniform("uLightPos", Vector4f.class).set(lightPosition);
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
        camera2.updateBoth();
        camera3.updateBoth();
        glfwSetCursorPos(window, width / 2, height / 2);
        gl.glClearColor(0.1f, 0.15f, 0.15f, 0.0f);
        gl.glClearColor(0f, 0f, 0f, 0.0f);
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glfwPollEvents();
        mouse.callbackMousePosition(0, width / 2, height / 2);
        System.out.println("new layer");
        layer = new GLLayer(newLayerId(), this, camera2, camera3, viewport.width(), viewport.height());
        System.out.println(layer);
        addLayer(layer);
    }

    private void describeFramebuffer(int buffer, int attachment) {

        int drawBuffer = gl.glGetInteger(GL_DRAW_BUFFER);
        switch (drawBuffer) {
            case GL_FRONT:
                System.out.println("Drawing to FRONT ");
                break;
            case GL_BACK:
                System.out.println("Drawing to BACK");
                break;
            case GL_DEPTH_ATTACHMENT:
                System.out.println("Drawing to DEPTH_ATTACHMENT");
                break;
            case GL_STENCIL_ATTACHMENT:
                System.out.println("Drawing to STENCIL_ATTACHMENT");
                break;
            case GL_DEPTH_STENCIL_ATTACHMENT:
                System.out.println("Drawing to DEPTH_STENCIL_ATTACHMENT");
                break;
            default:
                if (drawBuffer >= GL_COLOR_ATTACHMENT0 && drawBuffer <= GL_COLOR_ATTACHMENT31) {
                    System.out.printf("Drawing to COLOR_ATTACHMENT%d\n", drawBuffer - GL_COLOR_ATTACHMENT0);
                    break;
                }
                System.out.printf("Drawing to UNKNOWN: %x\n", gl.glGetInteger(drawBuffer));
        }
        int i = gl.glGetFramebufferAttachmentParameteri(buffer, attachment, GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING);
        String colorEncoding;
        switch (i) {
            case GL_LINEAR:
                colorEncoding = "linear";
                break;
            case GL_SRGB:
                colorEncoding = "srgb";
                break;
            default:
                colorEncoding = "UNKNOWN";
        }
        int redSize = gl.glGetFramebufferAttachmentParameteri(buffer, attachment, GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE);
        i = gl.glGetFramebufferAttachmentParameteri(buffer, attachment, GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE);
        String componentType = "UNKNOWN";
        switch (i) {
            case GL_FLOAT:
                componentType = "float";
                break;
            case GL_INT:
                componentType = "int";
                break;
            case GL_UNSIGNED_INT:
                componentType = "uint";
                break;
            case GL_SIGNED_NORMALIZED:
                componentType = "snorm";
                break;
            case GL_UNSIGNED_NORMALIZED:
                componentType = "unorm";
                break;
            case GL_NONE:
                componentType = "none";
                break;
        }

        System.out.printf("Framebuffer: %d bits, %s type, %s encoding\n", redSize, componentType, colorEncoding);
    }

    public void callbackFramebufferSize(long window, int width, int height) {
        if (width > 0 && height > 0 && (viewport.screenWidth() != width || viewport.screenHeight() != height)) {
            recomputeDimensions(0, 0, width, height);
            gl.glViewport(viewport.screenX(), viewport.screenY(), viewport.screenWidth(), viewport.screenHeight());
            if (frameBuf != 0)
                createBuffers();
            camera2.updateBoth();
            camera3.updateBoth();
            System.err.println("Framebuffer size: " + width + ", " + height);
        }
    }

    private void createBuffers() {
        System.err.println("Creating buffers");
        int width = viewport.screenWidth();
        int height = viewport.screenHeight();
        if (frameBuf != 0)
            gl.glDeleteFramebuffers(frameBuf);
        frameBuf = gl.glGenFramebuffers();
        gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBuf);
        int renderBuf = gl.glGenRenderbuffers();
        gl.glBindRenderbuffer(GL_RENDERBUFFER, renderBuf);
        gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        System.err.println("Renderbuffer size: " + width + ", " + height);
        gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderBuf);
        renderBuf = gl.glGenRenderbuffers();
        gl.glBindRenderbuffer(GL_RENDERBUFFER, renderBuf);
        gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, width, height);
        gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuf);
        gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);

        if (true) {
            int brightBuf = gl.glGenTextures();
            gl.glBindTexture(GL_TEXTURE_2D, brightBuf);
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, brightBuf, 0);
            gl.glBindTexture(GL_TEXTURE_2D, 0);

            gl.glDrawBuffers(new int[] { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1 });
        }
        if (gl.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer incomplete!");
            gl.glDeleteFramebuffers(frameBuf);
            frameBuf = 0;
        } else {
            describeFramebuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0);
            describeFramebuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1);
        }
        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
        if (action != GLFW_PRESS && action != GLFW_REPEAT) {
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

    private void processKeys() {
        float amount = (float) (.01 * (width() + height()) / 2);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera2.position.add(-8f, 0, 0, 0);
            camera2.updateBoth();
            camera3.position.add(-amount, 0, 0, 0);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera2.position.add(0, 8f, 0, 0);
            camera2.updateBoth();
            camera3.position.add(0, amount, 0, 0);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera2.position.add(8f, 0, 0, 0);
            camera2.updateBoth();
            camera3.position.add(amount, 0, 0, 0);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera2.position.add(0, -8f, 0, 0);
            camera2.updateBoth();
            camera3.position.add(0, -amount, 0, 0);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS) {
            camera3.position.add(0, 0, -amount, 0);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS) {
            camera3.position.add(0, 0, amount, 0);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
            //camera2.orientation.rotateLocalZ(FloatMath.PI / 64);
            camera2.updateBoth();
            camera3.orientation.rotateLocalY(-FloatMath.PI / 64);
            camera3.updateBoth();
        }
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
            System.out.println(amount);
            //camera2.orientation.rotateLocalZ(-FloatMath.PI / 64);
            camera2.updateBoth();
            camera3.orientation.rotateLocalY(FloatMath.PI / 64);
            camera3.updateBoth();
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
//		updateBoth();

        glfwPollEvents();
        joysticks.processInput();
        processKeys();
        gl.wireframe(wireframe);

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

//		gl.glEnable(GL_DEPTH_TEST);
        // update();
//		gl.glEnable(GL_FRAMEBUFFER_SRGB);
        gl.glEnable(GL_CULL_FACE);
        gl.glFrontFace(GL_CCW);
        gl.glCullFace(GL_BACK);
        gl.glDisable(GL_CULL_FACE);
//		gl.glDisable(GL_FRAMEBUFFER_SRGB); 
//		shader2d.bind();
//		modelMatrix.identity();
//		uModel.set(modelMatrix);

        gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, frameBuf);
//		gl.glViewport(0, 0, viewport.screenWidth(), viewport.screenHeight());
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glDisable(GL_BLEND);
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        forEachLayer(true, (l) -> ((GLLayer) l).render(true, false));

        gl.glDepthMask(false);
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        forEachLayer(false, (l) -> ((GLLayer) l).render(false, !paused));

        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(0);

        if (DOUBLE_BUFFER) {
            if (frameBuf != 0) {
                gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
                gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBuf);
                gl.glReadBuffer(GL_COLOR_ATTACHMENT1);
//			gl.glDrawBuffer(GL_BACK);
//			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
                int x = viewport.screenX(), y = viewport.screenY();
                int w = viewport.screenWidth();
                int h = viewport.screenHeight();
//			System.err.printf("blitting (%d,%d)+%dx%d to (%d,%d)+%dx%d\n", 0, 0, w, h, x, y, w, h);
//			gl.glViewport(x, y, w, h);
                gl.glBlitFramebuffer(0, 0, w, h, x, y, w, h, GL_COLOR_BUFFER_BIT/* |GL_DEPTH_BUFFER_BIT */, GL_LINEAR);
                gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
            }
            glfwSwapBuffers(window);

//			gl.glFinish();
        } else {
            glfwSwapBuffers(window);
            gl.glFlush();
//			gl.glFinish();
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

    public void startFrame(double deltaTime) {
        time += deltaTime;
    }
}
