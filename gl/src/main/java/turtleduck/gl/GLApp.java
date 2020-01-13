package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

import turtleduck.TurtleDuckApp;

public class GLApp {

	protected static GLLauncher launcher;
	public static final long NOMINAL_SMALL_STEP_MILLIS = 1000 / 60;
	public static final long NOMINAL_BIG_STEP_MILLIS = 10;
	private static long startMillis = 0, appTime = 0, appFrames = 0;
	private static GLScreen screen;
	private TurtleDuckApp app;

	public GLApp(String[] args, TurtleDuckApp app) {
		this.app = app;
	}

	public void start() {
		startMillis = System.currentTimeMillis();
		System.err.printf("T+%05dms: Launching\n", System.currentTimeMillis() - startMillis);
		System.err.printf("T+%05dms: Starting primary stage\n", System.currentTimeMillis() - startMillis);
		setupTimers();

		System.err.println("DPI: " + GLDisplayInfo.INSTANCE.getDisplayDpi());

		screen = GLScreen.create(launcher.config());
		try {
			app.start(screen);
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.err.printf("T+%05dms: User app started: %s\n", System.currentTimeMillis() - startMillis, app);
	}

	public void mainLoop() {
		while (!glfwWindowShouldClose(screen.window)) {
			app.bigStep(0.1);
			screen.render();
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException("Interrupted!", e);
			}
		}

	}

	public void dispose() {
		if (screen != null)
			screen.dispose();
	}

	public static void printStats() {
		System.err.println("App: ");
		System.err.printf("  Total frame time: %8.4f\n", appTime / 1000.0);
		System.err.printf("  Average frame time: %8.4f\n", (appTime / 1000.0) / appFrames);
	}

	private void setupTimers() {
		// TODO Auto-generated method stub

	}

}
