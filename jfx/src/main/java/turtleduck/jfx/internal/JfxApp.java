package turtleduck.jfx.internal;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import turtleduck.jfx.JfxScreen;
import turtleduck.TurtleDuckApp;
import turtleduck.display.Screen;
import turtleduck.jfx.JfxLauncher;

public class JfxApp extends Application {
	public static JfxLauncher launcher;
	public static final long NOMINAL_SMALL_STEP_MILLIS = 1000 / 60;
	public static final long NOMINAL_BIG_STEP_MILLIS = 10;
	private static long startMillis = 0, appTime = 0, appFrames = 0;

	public static void launch(String[] args) {
		startMillis = System.currentTimeMillis();
		System.err.printf("T+%05dms: Launching\n", System.currentTimeMillis() - startMillis);
		Application.launch(args);
	}

	public static void printStats() {
		System.err.println("App: ");
		System.err.printf("  Total frame time: %8.4f\n", appTime / 1000.0);
		System.err.printf("  Average frame time: %8.4f\n", (appTime / 1000.0) / appFrames);
	}

	private TurtleDuckApp app;
	private Timeline bigStepTimeline;
	private Timeline smallStepTimeline;

	private long stepNanos = 0, smallStepMillis = 0, bigStepMillis = 0;

	private AnimationTimer animTimer;
	private Screen screen;

	protected void bigStep() {
		long t = System.currentTimeMillis();
		if (bigStepMillis == 0) {
			System.err.printf("T+%05dms: First Big Step\n", System.currentTimeMillis() - startMillis);
			bigStepMillis = t - NOMINAL_BIG_STEP_MILLIS;
		}
		double dt = (t - bigStepMillis) / 1000.0;
		try {
			app.bigStep(dt);
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
		bigStepMillis = t;
	}

	@Override
	public void init() {
		System.err.printf("T+%05dms: Init\n", System.currentTimeMillis() - startMillis);
	}

	public void setupTimers() {
		animTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				step(now);
			}
		};
		/*
		 * bigStepTimeline = new Timeline(); //
		 * bigStepTimeline.setDelay(Duration.seconds(1));
		 * bigStepTimeline.setCycleCount(Timeline.INDEFINITE); KeyFrame kf = new
		 * KeyFrame(Duration.millis(NOMINAL_BIG_STEP_MILLIS), (ActionEvent event) -> {
		 * bigStep(); }); bigStepTimeline.getKeyFrames().add(kf);
		 * 
		 * smallStepTimeline = new Timeline(); smallStepTimeline.setCycleCount(1); kf =
		 * new KeyFrame(Duration.millis(NOMINAL_SMALL_STEP_MILLIS), (ActionEvent event)
		 * -> { smallStep(); }); smallStepTimeline.getKeyFrames().add(kf);
		 */
		System.err.printf("T+%05dms: Timers setup\n", System.currentTimeMillis() - startMillis);
	}

	protected void smallStep() {
		long t = System.currentTimeMillis();
		if (smallStepMillis == 0) {
			System.err.printf("T+%05dms: First Small Step\n", System.currentTimeMillis() - startMillis);
			smallStepMillis = t - NOMINAL_SMALL_STEP_MILLIS;
		}
		double dt = (t - smallStepMillis) / 1000.0;
		try {
			app.smallStep(dt);
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}

		smallStepMillis = t;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.err.printf("T+%05dms: Starting primary stage\n", System.currentTimeMillis() - startMillis);
		primaryStage.setOnShowing((ev) -> {
			System.err.printf("T+%05dms: Start showing window: %s\n", System.currentTimeMillis() - startMillis,
					ev.getSource());
		});
		primaryStage.setOnShown((ev) -> {
			System.err.printf("T+%05dms: Window shown  : %s\n", System.currentTimeMillis() - startMillis,
					ev.getSource());
			animTimer.start();
		});

		setupTimers();

		app = launcher.getApp();
		screen = JfxScreen.startPaintScene(primaryStage, launcher.config());
		try {
			app.start(screen);
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw ex;
		}
		System.err.printf("T+%05dms: User app started: %s\n", System.currentTimeMillis() - startMillis, app);
		primaryStage.show();
	}

	protected void step(long nanos) {
		if (stepNanos == 0) {
			System.err.printf("T+%05dms: First Step: @ %d ns\n", System.currentTimeMillis() - startMillis, nanos);
			stepNanos = nanos - (1000_000_000 / 60);
		}
		double dt = (nanos - stepNanos) / 1000_000_000.0;
//		System.err.printf("T+%05dms: First Step: @ %20.18f ns\n", System.currentTimeMillis() - startMillis, dt);
		long t = System.currentTimeMillis();
		try {
			try {

				app.bigStep(dt);
				screen.flush();
			} catch (Throwable ex) {
				ex.printStackTrace();
				throw ex;
			}
		} finally {
			long timeTaken = System.currentTimeMillis() - t;
			appTime += timeTaken;
			if (timeTaken > 100)
				System.out.println("Step: " + timeTaken);
			appFrames++;
			stepNanos = nanos;
		}
	}
}