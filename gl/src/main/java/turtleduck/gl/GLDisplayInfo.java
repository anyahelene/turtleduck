package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.system.MemoryUtil.NULL;

import turtleduck.display.DisplayInfo;
import turtleduck.display.Screen;

public class GLDisplayInfo implements DisplayInfo {
	public static final GLDisplayInfo INSTANCE = new GLDisplayInfo();

	public static DisplayInfo provider() {
		return INSTANCE;
	}

	private int width;
	private int height;
	private int refreshRate;
	private float xScale;
	private float yScale;
	private int mmWidth;
	private int mmHeight;
	private int workX;
	private int workY;
	private int workW;
	private int workH;

	private GLDisplayInfo() {
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		glfwSetMonitorCallback(this::monitorChanged);
		updateVideoMode(0L);
	}

	public void monitorChanged(long monitor, int event) {
		System.err.println("Monitor changed: " + monitor + ", " + event);
		updateVideoMode(NULL);
	}

	@Override
	public double getDisplayDpi() {
		return 25.4 * width / mmWidth;
	}

	public void updateVideoMode(long window) {
		long monitor = NULL;
		if (window != NULL)
			monitor = glfwGetWindowMonitor(window);
		if (monitor == NULL)
			monitor = glfwGetPrimaryMonitor();
		if (monitor != NULL) {
			var vidmode = glfwGetVideoMode(monitor);
			width = vidmode.width();
			height = vidmode.height();
			refreshRate = vidmode.refreshRate();
//			glfwGetMonitorContentScale(monitor, xscale, yscale);
			int itmp0[] = { 0 }, itmp1[] = { 0 }, itmp2[] = { 0 }, itmp3[] = { 0 };
			glfwGetMonitorPhysicalSize(monitor, itmp0, itmp1);
			mmWidth = itmp0[0];
			mmHeight = itmp1[0];
			float ftmp0[] = { 0 }, tmfp1[] = { 0 };
			glfwGetMonitorContentScale(monitor, ftmp0, tmfp1);
			xScale = ftmp0[0];
			yScale = tmfp1[0];
			glfwGetMonitorWorkarea(monitor, itmp0, itmp1, itmp2, itmp3);
			workX = itmp0[0];
			workY = itmp1[0];
			workW = itmp2[0];
			workH = itmp3[0];
		}
	}

	@Override
	public double getRawDisplayWidth() {
		return width;
	}

	@Override
	public double getRawDisplayHeight() {
		return height;
	}

	@Override
	public double getDisplayWidth() {
		return workW;
	}

	@Override
	public double getDisplayHeight() {
		return workH;
	}

	@Override
	public Screen startPaintScene(Object stage) {
		return startPaintScene(stage, Screen.CONFIG_SCREEN_FULLSCREEN_NO_HINT);
	}

	@Override
	public Screen startPaintScene(Object stage, int configuration) {
		return GLScreen.create(configuration);
	}

}
