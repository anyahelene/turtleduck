package turtleduck.jfx;

import javafx.stage.Stage;
import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.jfx.internal.JfxApp;

public class JfxLauncher implements Launcher  {
	protected  int config = 0;
	protected  TurtleDuckApp app;

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


	public void launch(String[] args) {
		JfxApp.launcher = this;
		JfxApp.launch(args);
	}

	@Override
	public <T> void launch(T displaySystem, Class<T> displaySystemType) {
		if (displaySystemType == Stage.class)
			getApp().start(JfxScreen.startPaintScene((Stage) displaySystem, getConfig()));
		else
			throw new IllegalArgumentException();
	}

	public TurtleDuckApp getApp() {
		return app;
	}

	public int getConfig() {
		return config;
	}

}
