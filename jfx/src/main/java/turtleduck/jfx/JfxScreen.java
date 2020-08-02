package turtleduck.jfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import turtleduck.colors.Color;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseScreen;
import turtleduck.display.MouseCursor;
import turtleduck.events.KeyEvent;
import turtleduck.events.KeyCodes;
import turtleduck.turtle.Pen;
import turtleduck.text.TextMode;
import turtleduck.text.TextWindow;

public class JfxScreen extends BaseScreen {
	private static final javafx.scene.paint.Color JFX_BLACK = javafx.scene.paint.Color.BLACK;
	private Clipboard clipboard = Clipboard.getSystemClipboard();
	protected int shortcutKeyMask = KeyEvent.SHORTCUT_MASK;

	public static Screen startPaintScene(Stage stage, int configuration) {
		Dimensions dim = computeDimensions(JfxDisplayInfo.INSTANCE, configuration);
		Group root = new Group();
		Scene scene = new Scene(root, dim.winWidth, dim.winHeight, JFX_BLACK);
		stage.setScene(scene);
		// stage.setTitle(AppInfo.APP_NAME);
		if ((dim.configFlags & CONFIG_FLAG_HIDE_MOUSE) != 0) {
			scene.setCursor(Cursor.NONE);
		}
		boolean debug = (dim.configFlags & CONFIG_FLAG_DEBUG) != 0;
		dim.winWidth = scene.getWidth();
		dim.winHeight = scene.getHeight();
		JfxScreen pScene = new JfxScreen(dim);
//				scene.getWidth(), scene.getHeight(), //
//				dim.fbWidth, dim.fbHeight, //
//				dim.canvasWidth, dim.canvasHeight);
		pScene.subScene.widthProperty().bind(scene.widthProperty());
		pScene.subScene.heightProperty().bind(scene.heightProperty());
		pScene.debug = debug;
		pScene.hideFullScreenMouseCursor = (dim.configFlags & CONFIG_FLAG_NO_AUTOHIDE_MOUSE) == 0;
		root.getChildren().add(pScene.subScene);

		boolean[] suppressKeyTyped = { false };

		switch (dim.configScreen) {
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
			JfxKeyEvent wrapped = new JfxKeyEvent(event, pScene.shortcutKeyMask);
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
					&& pScene.keyTypedHandler.test(new JfxKeyEvent(event, pScene.shortcutKeyMask))) {
				event.consume();
			}
			if (pScene.logKeyEvents)
				System.err.println(event);
		});
		scene.setOnKeyReleased((javafx.scene.input.KeyEvent event) -> {
			suppressKeyTyped[0] = false;
			if (!event.isConsumed() && pScene.keyReleasedHandler != null
					&& pScene.keyReleasedHandler.test(new JfxKeyEvent(event, pScene.shortcutKeyMask))) {
				event.consume();
			}
			if (pScene.logKeyEvents)
				System.err.println(event);
		});
		return pScene;
	}

	private boolean logKeyEvents = false;
	private final SubScene subScene;
	private final List<Canvas> canvases = new ArrayList<>();
	private final Map<Layer, Canvas> layerCanvases = new IdentityHashMap<>();
	protected final Canvas background;
	private final Group root;
	private Color bgColor = Color.color(0, 0, 0);
//	private double currentScale = 1.0;
//	private double currentFit = 1.0;
//	private double resolutionScale = 1.0;
//	private int maxScale = 1;
	private Predicate<KeyEvent> keyOverride = null;

	private Predicate<KeyEvent> keyPressedHandler = null;

	private Predicate<KeyEvent> keyTypedHandler = null;

	private Predicate<KeyEvent> keyReleasedHandler = null;

	private boolean debug = true;

	private boolean hideFullScreenMouseCursor = true;

	private Cursor oldCursor;
	private JfxLayer backgroundPainter;
	private Predicate<String> pasteHandler;

	public JfxScreen(Dimensions dim) {
//	double width, double height, double pixWidth, double pixHeight, double canvasWidth,		double canvasHeight) {
		root = new Group();
		subScene = new SubScene(root, Math.floor(dim.winWidth), Math.floor(dim.winHeight));
		this.dim = dim;
		setupAspects(dim);
		background = new Canvas(dim.fbWidth, dim.fbHeight);
		background.getGraphicsContext2D().scale(dim.resolutionScale(), dim.resolutionScale());
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

	protected Canvas newCanvas() {
		var c = new Canvas(dim.fbWidth, dim.fbHeight);
		var s = dim.resolutionScale();
		c.getGraphicsContext2D().scale(s, s);
		canvases.add(c);
		root.getChildren().add(c);
		return c;
	}

	@Override
	public turtleduck.display.Canvas createCanvas() {
		Canvas canvas = newCanvas();
		var layer = addLayer(new JfxLayer(newLayerId(), width(), getHeight(), this, canvas));
		layerCanvases.put(layer, canvas);
		return layer;
	}

	@Override
	public TextWindow createTextWindow() {
		Canvas canvas = newCanvas();
		JfxTextWindow win = addLayer(
				new JfxTextWindow(newLayerId(), TextMode.MODE_80X30, this, width(), getHeight(), canvas));
		win.drawCharCells();
		win.redraw();
		layerCanvases.put(win, canvas);
		return win;
	}

	@Override
	public Layer getBackgroundPainter() {
		if (backgroundPainter == null) {
			backgroundPainter = new JfxLayer(newLayerId(), width(), getHeight(), this, background);
			layerMap.put(backgroundPainter.id(), backgroundPainter);
		}
		return backgroundPainter;
	}

	@Override
	public double getHeight() {
		return Math.floor(frameBufferHeight() / dim.resolutionScale());
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
	public double frameBufferHeight() {
		return Math.floor(dim.fbWidth / aspects.get(aspect));
	}

	@Override
	public double frameBufferWidth() {
		return dim.fbWidth;
	}

	@Override
	public double width() {
		return Math.floor(dim.fbWidth / dim.resolutionScale());
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
		int code = event.getCode();
		if (event.isShortcutDown() && event.shortcutModifiers() == 0) {
			if (code == 'Q') {
				Platform.exit();
			} else if (code == '+') {
				zoomIn();
				return true;
			} else if (code == '-') {
				zoomOut();
				return true;
			} else if (code == 'V' && pasteHandler != null) {
				if (clipboard.hasString())
					pasteHandler.test(clipboard.getString());
				return true;
			}
		} else if (!event.isModified()) {
			if (code == KeyCodes.Function.F11) {
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

	protected void recomputeLayout(boolean resizeWindow) {
		if(subScene.getWidth() <= 1 || subScene.getHeight() <= 1)
			return;
		dim.winWidth = subScene.getWidth();
		dim.winHeight = subScene.getHeight();
		recomputeDimensions(JfxDisplayInfo.INSTANCE);

		if (resizeWindow) {
			Scene scene = subScene.getScene();
			Window window = scene.getWindow();
			double hBorder = window.getWidth() - scene.getWidth();
			double vBorder = window.getHeight() - scene.getHeight();
			double myWidth = dim.fbWidth * dim.scale;
			double myHeight = dim.fbHeight * dim.scale;
			if (debug)
				System.err.printf(
						"Resizing before: screen: %1.0fx%1.0f, screen: %1.0fx%1.0f, scene: %1.0fx%1.0f, window: %1.0fx%1.0f,%n border: %1.0fx%1.0f, new window size: %1.0fx%1.0f, canvas size: %1.0fx%1.0f%n", //
						javafx.stage.Screen.getPrimary().getVisualBounds().getWidth(),
						javafx.stage.Screen.getPrimary().getVisualBounds().getHeight(), subScene.getWidth(),
						subScene.getHeight(), scene.getWidth(), scene.getHeight(), window.getWidth(),
						window.getHeight(), hBorder, vBorder, myWidth, myHeight, frameBufferWidth(), frameBufferHeight());
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
						window.getHeight(), hBorder, vBorder, myWidth, myHeight, frameBufferWidth(), frameBufferHeight());
		}

		if (debug)
			System.err.printf("Rescaling: virtual %1.2fx%1.2f subscene %1.2fx%1.2f, scale %1.2f, resscale %1.2f, aspect %.4f (%d), framebuffer %1.0fx%1.0f%n",
					width(), getHeight(), subScene.getWidth(), subScene.getHeight(), dim.resolutionScale(), dim.scale, aspects.get(aspect), aspect, frameBufferWidth(),
					frameBufferHeight());
		for (Node n : root.getChildren()) {
			if (debug)
				System.err.printf(" *  layout< %1.2fx%1.2f, translate %1.2fx%1.2f, scale %1.2f%n", n.getLayoutX(), n.getLayoutY(),
						n.getTranslateX(), n.getTranslateY(), n.getScaleX());
			var h = frameBufferHeight();
			var dh = dim.fbHeight - h;
			var dhScaled = dh * dim.scale;
			dhScaled /= 2;
			n.relocate(Math.floor(subScene.getWidth() / 2),
					Math.floor(subScene.getHeight() / 2 + dhScaled));
			n.setTranslateX(-Math.floor(dim.fbWidth / 2));
			n.setTranslateY(-Math.floor(dim.fbHeight / 2));
			if (debug)
				System.err.printf(" *  layout> %1.2fx%1.2f, translate %1.2fx%1.2f, scale %1.2f%n", n.getLayoutX(), n.getLayoutY(),
						n.getTranslateX(), n.getTranslateY(), dim.scale);
			n.setScaleX(dim.scale);
			n.setScaleY(dim.scale);
		}
	}

	@Override
	public void setBackground(Color bgColor) {
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

	@Override
	public void setPasteHandler(Predicate<String> pasteHandler) {
		this.pasteHandler = pasteHandler;
	}

	@Override
	public void clipboardPut(String copied) {
		clipboard.setContent(Map.of(DataFormat.PLAIN_TEXT, copied));
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
	public void flush() {
		layerMap.values().forEach(Layer::flush);
	}

	@Override
	public void useAlternateShortcut(boolean useAlternate) {
		if (useAlternate)
			shortcutKeyMask = KeyEvent.SHORTCUT_MASK_ALT;
		else
			shortcutKeyMask = KeyEvent.SHORTCUT_MASK;
	}
}
