package turtleduck.jfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import turtleduck.colors.Paint;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.MouseCursor;
import turtleduck.events.KeyEvent;
import turtleduck.turtle.Pen;
import turtleduck.text.Printer;

public class JfxScreen implements Screen {
	private static final javafx.scene.paint.Color JFX_BLACK = javafx.scene.paint.Color.BLACK;
	private static final double STD_CANVAS_WIDTH = 1280;
	private static final List<Double> STD_ASPECTS = Arrays.asList(16.0 / 9.0, 16.0 / 10.0, 4.0 / 3.0);

	public static Screen startPaintScene(Stage stage, int configuration) {
		int configAspect = (configuration & _CONFIG_ASPECT_MASK);
		int configScreen = (configuration & _CONFIG_SCREEN_MASK);
		int configPixels = (configuration & _CONFIG_PIXELS_MASK);
		int configCoords = (configuration & _CONFIG_COORDS_MASK);
		int configFlags = (configuration & _CONFIG_FLAG_MASK);
		boolean debug = (configFlags & CONFIG_FLAG_DEBUG) != 0;
		if (configPixels == CONFIG_PIXELS_DEFAULT) {
			if (configCoords == CONFIG_COORDS_DEVICE || configScreen == CONFIG_SCREEN_FULLSCREEN)
				configPixels = CONFIG_PIXELS_DEVICE;
			else
				configPixels = CONFIG_PIXELS_STEP_SCALED;
		}
		double rawWidth = JfxDisplayInfo.INSTANCE.getRawDisplayWidth();
		double rawHeight = JfxDisplayInfo.INSTANCE.getRawDisplayHeight();
		double width = JfxDisplayInfo.INSTANCE.getDisplayWidth() - 40;
		double height = JfxDisplayInfo.INSTANCE.getDisplayHeight() - 100;
		double canvasAspect = configAspect == CONFIG_ASPECT_DEVICE ? rawWidth / rawHeight
				: STD_ASPECTS.get(configAspect);
		double xScale = (height * canvasAspect) / JfxScreen.STD_CANVAS_WIDTH;
		double yScale = (width / canvasAspect) / (JfxScreen.STD_CANVAS_WIDTH / canvasAspect);
		double scale = Math.min(xScale, yScale);
		if (configPixels == CONFIG_PIXELS_STEP_SCALED) {
			if (scale > 1.0)
				scale = Math.max(1, Math.floor(scale));
			else if (scale < 1.0)
				scale = 1 / Math.max(1, Math.floor(1 / scale));
		}
		double winWidth = Math.floor(JfxScreen.STD_CANVAS_WIDTH * scale);
		double winHeight = Math.floor((JfxScreen.STD_CANVAS_WIDTH / canvasAspect) * scale);
		double canvasWidth = JfxScreen.STD_CANVAS_WIDTH;
		double canvasHeight = Math.floor(3 * JfxScreen.STD_CANVAS_WIDTH / 4);
		double pixWidth = canvasWidth;
		double pixHeight = canvasHeight;
		if (configPixels == CONFIG_PIXELS_SCALED || configPixels == CONFIG_PIXELS_STEP_SCALED) {
			pixWidth *= scale;
			pixHeight *= scale;
		} else if (configPixels == CONFIG_PIXELS_DEVICE) {
			pixWidth = rawWidth;
			pixHeight = rawHeight;
		}
		if (configCoords == CONFIG_COORDS_DEVICE) {
			canvasWidth = pixWidth;
			canvasHeight = pixHeight;
		}
		if (debug) {
			System.out.printf("Screen setup:%n");
			System.out.printf("  Display: %.0fx%.0f (raw %.0fx%.0f)%n", width, height, rawWidth, rawHeight);
			System.out.printf("  Window:  %.0fx%.0f%n", winWidth, winHeight);
			System.out.printf("  Canvas:  physical %.0fx%.0f, logical %.0fx%.0f%n", pixWidth, pixHeight, canvasWidth,
					canvasHeight);
			System.out.printf("  Aspect:  %.5f   Scale: %.5f%n", canvasAspect, scale);
		}
		Group root = new Group();
		Scene scene = new Scene(root, winWidth, winHeight, JFX_BLACK);
		stage.setScene(scene);
		// stage.setTitle(AppInfo.APP_NAME);
		if ((configFlags & CONFIG_FLAG_HIDE_MOUSE) != 0) {
			scene.setCursor(Cursor.NONE);
		}

		JfxScreen pScene = new JfxScreen(scene.getWidth(), scene.getHeight(), //
				pixWidth, pixHeight, //
				canvasWidth, canvasHeight);
		pScene.subScene.widthProperty().bind(scene.widthProperty());
		pScene.subScene.heightProperty().bind(scene.heightProperty());
		pScene.debug = debug;
		pScene.hideFullScreenMouseCursor = (configFlags & CONFIG_FLAG_NO_AUTOHIDE_MOUSE) == 0;
		root.getChildren().add(pScene.subScene);

		boolean[] suppressKeyTyped = { false };

		switch (configScreen) {
		case CONFIG_SCREEN_WINDOWED:
			break;
		case CONFIG_SCREEN_BORDERLESS:
			stage.initStyle(StageStyle.UNDECORATED);
			break;
		case CONFIG_SCREEN_TRANSPARENT:
			stage.initStyle(StageStyle.TRANSPARENT);
			break;
		case CONFIG_SCREEN_FULLSCREEN_NO_HINT:
			stage.setFullScreenExitHint("");
			// fall-through
		case CONFIG_SCREEN_FULLSCREEN:
			stage.setFullScreen(true);
			break;
		}
		scene.setOnKeyPressed((javafx.scene.input.KeyEvent event) -> {
			JfxKeyEvent wrapped = new JfxKeyEvent(event);
			if (!event.isConsumed() && pScene.keyOverride != null && pScene.keyOverride.test(wrapped)) {
				event.consume();
			}
			if (!event.isConsumed() && pScene.minimalKeyHandler(wrapped)) {
				event.consume();
			}
			if (!event.isConsumed() && pScene.keyPressedHandler != null && pScene.keyPressedHandler.test(wrapped)) {
				event.consume();
			}
			if (pScene.logKeyEvents)
				System.err.println(event);
			suppressKeyTyped[0] = event.isConsumed();
		});
		scene.setOnKeyTyped((javafx.scene.input.KeyEvent event) -> {
			if (suppressKeyTyped[0]) {
				suppressKeyTyped[0] = false;
				event.consume();
			}
			if (!event.isConsumed() && pScene.keyTypedHandler != null
					&& pScene.keyTypedHandler.test(new JfxKeyEvent(event))) {
				event.consume();
			}
			if (pScene.logKeyEvents)
				System.err.println(event);
		});
		scene.setOnKeyReleased((javafx.scene.input.KeyEvent event) -> {
			suppressKeyTyped[0] = false;
			if (!event.isConsumed() && pScene.keyReleasedHandler != null
					&& pScene.keyReleasedHandler.test(new JfxKeyEvent(event))) {
				event.consume();
			}
			if (pScene.logKeyEvents)
				System.err.println(event);
		});
		return pScene;
	}

	private final double rawCanvasWidth;
	private final double rawCanvasHeight;
	private boolean logKeyEvents = false;
	private final SubScene subScene;
	private final List<Canvas> canvases = new ArrayList<>();
	private final Map<Layer, Canvas> layerCanvases = new IdentityHashMap<>();
	private final Canvas background;
	private Layer debugLayer;
	private final Group root;
	private Paint bgColor = Paint.color(0, 0, 0);
	private int aspect = 0;
	private double scaling = 0;
	private double currentScale = 1.0;
	private double currentFit = 1.0;
	private double resolutionScale = 1.0;
	private int maxScale = 1;
	private Predicate<KeyEvent> keyOverride = null;

	private Predicate<KeyEvent> keyPressedHandler = null;

	private Predicate<KeyEvent> keyTypedHandler = null;

	private Predicate<KeyEvent> keyReleasedHandler = null;

	private boolean debug = true;

	private List<Double> aspects;

	private boolean hideFullScreenMouseCursor = true;

	private Cursor oldCursor;
	private JfxLayer backgroundPainter;

	public JfxScreen(double width, double height, double pixWidth, double pixHeight, double canvasWidth,
			double canvasHeight) {
		root = new Group();
		subScene = new SubScene(root, Math.floor(width), Math.floor(height));
		resolutionScale = pixWidth / canvasWidth;
		this.rawCanvasWidth = Math.floor(pixWidth);
		this.rawCanvasHeight = Math.floor(pixHeight);
		double aspectRatio = width / height;
		aspect = 0;
		for (double a : STD_ASPECTS)
			if (Math.abs(aspectRatio - a) < 0.01) {
				break;
			} else {
				aspect++;
			}
		aspects = new ArrayList<>(STD_ASPECTS);
		if (aspect >= STD_ASPECTS.size()) {
			aspects.add(aspectRatio);
		}
		background = new Canvas(rawCanvasWidth, rawCanvasHeight);
		background.getGraphicsContext2D().scale(resolutionScale, resolutionScale);
		setBackground(bgColor);
		clearBackground();
		root.getChildren().add(background);
		subScene.layoutBoundsProperty()
				.addListener((ObservableValue<? extends Bounds> observable, Bounds oldBounds, Bounds bounds) -> {
					recomputeLayout(false);
				});
	}

	@Override
	public void clearBackground() {
		background.getGraphicsContext2D().setFill(JfxColor.toJfxColor(bgColor));
		background.getGraphicsContext2D().fillRect(0.0, 0.0, background.getWidth(), background.getHeight());
	}

	@Override
	public Layer createPainter() {
		Canvas canvas = new Canvas(rawCanvasWidth, rawCanvasHeight);
		canvas.getGraphicsContext2D().scale(resolutionScale, resolutionScale);
		canvases.add(canvas);
		root.getChildren().add(canvas);
		return new JfxLayer(getWidth(), getHeight(), this, canvas);
	}

	public Layer debugLayer() {
		if (debugLayer == null) {
			Canvas canvas = new Canvas(rawCanvasWidth, rawCanvasHeight);
			canvas.getGraphicsContext2D().scale(resolutionScale, resolutionScale);
			canvases.add(canvas);
			root.getChildren().add(canvas);
			canvas.toFront();
			debugLayer = new JfxLayer(getWidth(), getHeight(), this, canvas);
		}
		return debugLayer;
	}

	@Override
	public Printer createPrinter() {
		Canvas canvas = new Canvas(rawCanvasWidth, rawCanvasHeight);
		canvas.getGraphicsContext2D().scale(resolutionScale, resolutionScale);
		canvases.add(canvas);
		root.getChildren().add(canvas);
		return new JfxPrinter(this, canvas);
	}

	@Override
	public void cycleAspect() {
		aspect = (aspect + 1) % aspects.size();
		recomputeLayout(false);
	}

	@Override
	public void fitScaling() {
		scaling = 0;
		recomputeLayout(true);
	}

	@Override
	public int getAspect() {
		return aspect;
	}

	@Override
	public Layer getBackgroundPainter() {
		if (backgroundPainter == null)
			backgroundPainter = new JfxLayer(getWidth(), getHeight(), this, background);
		return backgroundPainter;
	}

	@Override
	public double getHeight() {
		return Math.floor(getRawHeight() / resolutionScale);
	}

	/** @return the keyOverride */
	@Override
	public Predicate<KeyEvent> getKeyOverride() {
		return keyOverride;
	}

	/** @return the keyHandler */
	@Override
	public Predicate<KeyEvent> getKeyPressedHandler() {
		return keyPressedHandler;
	}

	/** @return the keyReleasedHandler */
	@Override
	public Predicate<KeyEvent> getKeyReleasedHandler() {
		return keyReleasedHandler;
	}

	/** @return the keyTypedHandler */
	@Override
	public Predicate<KeyEvent> getKeyTypedHandler() {
		return keyTypedHandler;
	}

	@Override
	public double getRawHeight() {
		return Math.floor(rawCanvasWidth / aspects.get(aspect));
	}

	@Override
	public double getRawWidth() {
		return rawCanvasWidth;
	}

	@Override
	public double getWidth() {
		return Math.floor(getRawWidth() / resolutionScale);
	}

	@Override
	public void hideMouseCursor() {
		subScene.getScene().setCursor(Cursor.NONE);
	}

	@Override
	public boolean isFullScreen() {
		Window window = subScene.getScene().getWindow();
		if (window instanceof Stage)
			return ((Stage) window).isFullScreen();
		else
			return false;
	}

	@Override
	public boolean minimalKeyHandler(KeyEvent event) {
		javafx.scene.input.KeyCode code = event.getCode().as(javafx.scene.input.KeyCode.class);
		if (event.isShortcutDown()) {
			if (code == javafx.scene.input.KeyCode.Q) {
				System.exit(0);
			} else if (code == javafx.scene.input.KeyCode.PLUS) {
				zoomIn();
				return true;
			} else if (code == javafx.scene.input.KeyCode.MINUS) {
				zoomOut();
				return true;
			}
		} else if (!(event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown())) {
			if (code == javafx.scene.input.KeyCode.F11) {
				setFullScreen(!isFullScreen());
				return true;
			}
		}

		return false;
	}

	@Override
	public void moveToBack(Layer layer) {
		Canvas canvas = layerCanvases.get(layer);
		if (canvas != null) {
			canvas.toBack();
			background.toBack();
		}
	}

	@Override
	public void moveToFront(Layer layer) {
		Canvas canvas = layerCanvases.get(layer);
		if (canvas != null) {
			canvas.toFront();
		}
	}

	private void recomputeLayout(boolean resizeWindow) {
		double xScale = subScene.getWidth() / getRawWidth();
		double yScale = subScene.getHeight() / getRawHeight();
		double xMaxScale = JfxDisplayInfo.INSTANCE.getDisplayWidth() / getRawWidth();
		double yMaxScale = JfxDisplayInfo.INSTANCE.getDisplayHeight() / getRawHeight();
		currentFit = Math.min(xScale, yScale);
		maxScale = (int) Math.max(1, Math.ceil(Math.min(xMaxScale, yMaxScale)));
		currentScale = scaling == 0 ? currentFit : scaling;

		if (resizeWindow) {
			Scene scene = subScene.getScene();
			Window window = scene.getWindow();
			double hBorder = window.getWidth() - scene.getWidth();
			double vBorder = window.getHeight() - scene.getHeight();
			double myWidth = getRawWidth() * currentScale;
			double myHeight = getRawHeight() * currentScale;
			if (debug)
				System.err.printf(
						"Resizing before: screen: %1.0fx%1.0f, screen: %1.0fx%1.0f, scene: %1.0fx%1.0f, window: %1.0fx%1.0f,%n border: %1.0fx%1.0f, new window size: %1.0fx%1.0f, canvas size: %1.0fx%1.0f%n", //
						javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
						javafx.stage.Screen.getPrimary().getVisualBounds().getHeight(), subScene.getWidth(),
						subScene.getHeight(), scene.getWidth(), scene.getHeight(), window.getWidth(),
						window.getHeight(), hBorder, vBorder, myWidth, myHeight, getRawWidth(), getRawHeight());
			// this.setWidth(myWidth);
			// this.setHeight(myHeight);
			window.setWidth(myWidth + hBorder);
			window.setHeight(myHeight + vBorder);
			if (debug)
				System.err.printf(
						"Resizing after : screen: %1.0fx%1.0f, screen: %1.0fx%1.0f, scene: %1.0fx%1.0f, window: %1.0fx%1.0f,%n border: %1.0fx%1.0f, new window size: %1.0fx%1.0f, canvas size: %1.0fx%1.0f%n",
						javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
						javafx.stage.Screen.getPrimary().getVisualBounds().getHeight(), subScene.getWidth(),
						subScene.getHeight(), scene.getWidth(), scene.getHeight(), window.getWidth(),
						window.getHeight(), hBorder, vBorder, myWidth, myHeight, getRawWidth(), getRawHeight());
		}

		if (debug)
			System.err.printf("Rescaling: subscene %1.2fx%1.2f, scale %1.2f, aspect %.4f (%d), canvas %1.0fx%1.0f%n",
					subScene.getWidth(), subScene.getHeight(), currentScale, aspects.get(aspect), aspect, getRawWidth(),
					getRawHeight());
		for (Node n : root.getChildren()) {
			n.relocate(Math.floor(subScene.getWidth() / 2),
					Math.floor(subScene.getHeight() / 2 + (rawCanvasHeight - getRawHeight()) * currentScale / 2));
			n.setTranslateX(-Math.floor(rawCanvasWidth / 2));
			n.setTranslateY(-Math.floor(rawCanvasHeight / 2));
			if (debug)
				System.err.printf(" *  layout %1.2fx%1.2f, translate %1.2fx%1.2f%n", n.getLayoutX(), n.getLayoutY(),
						n.getTranslateX(), n.getTranslateY());
			n.setScaleX(currentScale);
			n.setScaleY(currentScale);
		}
	}

	@Override
	public void setAspect(int aspect) {
		this.aspect = (aspect) % aspects.size();
		recomputeLayout(false);
	}

	@Override
	public void setBackground(Paint bgColor) {
		this.bgColor = bgColor;
		subScene.setFill(JfxColor.toJfxColor(bgColor.darker()));
	}

	@Override
	public void setFullScreen(boolean fullScreen) {
		Window window = subScene.getScene().getWindow();
		if (window instanceof Stage) {
			((Stage) window).setFullScreenExitHint("");
			((Stage) window).setFullScreen(fullScreen);
			if (hideFullScreenMouseCursor) {
				if (fullScreen) {
					oldCursor = subScene.getScene().getCursor();
					subScene.getScene().setCursor(Cursor.NONE);
				} else if (oldCursor != null) {
					subScene.getScene().setCursor(oldCursor);
					oldCursor = null;
				} else {
					subScene.getScene().setCursor(Cursor.DEFAULT);
				}
			}
		}
	}

	@Override
	public void setHideFullScreenMouseCursor(boolean hideIt) {
		if (hideIt != hideFullScreenMouseCursor && isFullScreen()) {
			if (hideIt) {
				oldCursor = subScene.getScene().getCursor();
				subScene.getScene().setCursor(Cursor.NONE);
			} else if (oldCursor != null) {
				subScene.getScene().setCursor(oldCursor);
				oldCursor = null;
			} else {
				subScene.getScene().setCursor(Cursor.DEFAULT);
			}
		}
		hideFullScreenMouseCursor = hideIt;
	}

	/**
	 * @param keyOverride the keyOverride to set
	 */
	@Override
	public void setKeyOverride(Predicate<KeyEvent> keyOverride) {
		this.keyOverride = keyOverride;
	}

	/**
	 * @param keyHandler the keyHandler to set
	 */
	@Override
	public void setKeyPressedHandler(Predicate<KeyEvent> keyHandler) {
		this.keyPressedHandler = keyHandler;
	}

	/**
	 * @param keyReleasedHandler the keyReleasedHandler to set
	 */
	@Override
	public void setKeyReleasedHandler(Predicate<KeyEvent> keyReleasedHandler) {
		this.keyReleasedHandler = keyReleasedHandler;
	}

	/**
	 * @param keyTypedHandler the keyTypedHandler to set
	 */
	@Override
	public void setKeyTypedHandler(Predicate<KeyEvent> keyTypedHandler) {
		this.keyTypedHandler = keyTypedHandler;
	}

	@Override
	public void setMouseCursor(MouseCursor cursor) {
		subScene.getScene().setCursor(cursor.as(Cursor.class));
	}

	@Override
	public void showMouseCursor() {
		subScene.getScene().setCursor(Cursor.DEFAULT);
	}

	@Override
	public void zoomCycle() {
		scaling++;
		if (scaling > maxScale)
			scaling = ((int) scaling) % maxScale;
		recomputeLayout(true);
	}

	@Override
	public void zoomFit() {
		scaling = 0;
		recomputeLayout(false);
	}

	@Override
	public void zoomIn() {
		scaling = Math.min(10, currentScale + 0.2);
		recomputeLayout(false);
	}

	@Override
	public void zoomOne() {
		scaling = 1;
		recomputeLayout(false);
	}

	@Override
	public void zoomOut() {
		scaling = Math.max(0.1, currentScale - 0.2);
		recomputeLayout(false);
	}

}
