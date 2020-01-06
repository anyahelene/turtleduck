package turtleduck;

import java.util.ServiceLoader;

public interface Launcher {

	public static Launcher application(TurtleDuckApp app) {
		return ServiceLoader.load(Launcher.class).findFirst().orElseThrow().app(app);
	}

	Launcher app(TurtleDuckApp app);

	Launcher config(int config);

	void launch(String[] args);

	<T> void launch(T displaySystem, Class<T> displaySystemType);
}
