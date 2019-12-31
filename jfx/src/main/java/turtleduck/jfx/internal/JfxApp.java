package turtleduck.jfx.internal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import turtleduck.jfx.JfxScreen;
import turtleduck.TurtleDuckApp;
import turtleduck.jfx.JfxLauncher;

public class JfxApp extends Application {
	public static JfxLauncher launcher;
	public static final double NOMINAL_SMALL_STEP_MILLIS = 1000.0/60.0;
	public static final double NOMINAL_BIG_STEP_MILLIS = 10;
	private TurtleDuckApp app;
	private Timeline bigStepTimeline;
	private Timeline smallStepTimeline;
	public static void launch(String[] args) {
		Application.launch(args);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		app = launcher.getApp();
		app.start(JfxScreen.startPaintScene(primaryStage, launcher.getConfig()));
		setupTimers();
		primaryStage.show();
		bigStepTimeline.playFromStart();
	}
	
	public void setupTimers() {
		bigStepTimeline = new Timeline();
		bigStepTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame kf = new KeyFrame(Duration.millis(NOMINAL_BIG_STEP_MILLIS), (ActionEvent event) -> {
			bigStep();
		});
		bigStepTimeline.getKeyFrames().add(kf);
		
		smallStepTimeline = new Timeline();
		smallStepTimeline.setCycleCount(1);
		kf = new KeyFrame(Duration.millis(NOMINAL_SMALL_STEP_MILLIS), (ActionEvent event) -> {
			smallStep();
		});
		smallStepTimeline.getKeyFrames().add(kf);

	}
	double smallStepMillis = 0, bigStepMillis = 0;
	protected void smallStep() {
		double t = System.currentTimeMillis();
		if(smallStepMillis == 0)
			smallStepMillis = t - NOMINAL_SMALL_STEP_MILLIS;
		double dt = (t - smallStepMillis) / 1000.0;
		app.smallStep(dt);
		smallStepMillis = t;
	}
	protected void bigStep() {
		double t = System.currentTimeMillis();
		if(bigStepMillis == 0)
			bigStepMillis = t - NOMINAL_BIG_STEP_MILLIS;
		double dt = (t - bigStepMillis) / 1000.0;
		app.bigStep(dt);
		bigStepMillis = t;
	}
	
}