package turtleduck.gl;

import static org.lwjgl.glfw.GLFW.glfwTerminate;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.display.Screen;

public class GLLauncher implements Launcher {
	protected int config = Screen.CONFIG_FLAG_DEBUG;
	protected TurtleDuckApp app;
	private GLApp glApp;

	@Override
	public Launcher config(int config) {
		this.config = config;
		return this;
	}

	@Override
	public Launcher app(TurtleDuckApp app) {
		this.app = app;
		return this;
	}

	@Override
	public void launch(String[] args) {
		GLApp.launcher = this;
		glApp = new GLApp(args, app);
		try {
			glApp.start();
			glApp.mainLoop();
		} catch(Throwable t) {
			t.printStackTrace();
		}finally {
			glApp.dispose();
			glfwTerminate();
		}
	}

	@Override
	public <T> void launch(T displaySystem, Class<T> displaySystemType) {
		throw new IllegalArgumentException("" + displaySystem + " of type " + displaySystemType);
	}

	@Override
	public int config() {
		return config;
	}

}
