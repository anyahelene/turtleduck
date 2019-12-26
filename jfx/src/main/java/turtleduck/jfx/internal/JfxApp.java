package turtleduck.jfx.internal;

import javafx.application.Application;
import javafx.stage.Stage;
import turtleduck.jfx.JfxScreen;
import turtleduck.jfx.JfxLauncher;

public class JfxApp extends Application {
	public static JfxLauncher launcher;
	
	public static void launch(String[] args) {
		Application.launch(args);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		launcher.getApp().start(JfxScreen.startPaintScene(primaryStage, launcher.getConfig()));
		primaryStage.show();
	}
	
}