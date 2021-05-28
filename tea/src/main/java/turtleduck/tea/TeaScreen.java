package turtleduck.tea;

import java.util.function.Predicate;

import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.canvas.CanvasImpl;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.impl.BaseScreen;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.CanvasService;
import turtleduck.text.TextWindow;

public class TeaScreen extends BaseScreen {
	String currentGroup = null;

	public static TeaScreen create(CanvasService session, int config) {
		Dimensions dim = computeDimensions(TeaDisplayInfo.INSTANCE, config);

		return new TeaScreen(session, dim);
	}

	private CanvasService session;
	private int channel;

	public TeaScreen(CanvasService session, Dimensions dim) {
		this.dim = dim;
		this.session = session;
		setupAspects(dim);
	}

	public void group(String group) {
		synchronized (this) {
			currentGroup = group;
		}
	}

	@Override
	protected void recomputeLayout(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearBackground() {
		// TODO Auto-generated method stub

	}

	@Override
	public Canvas createCanvas() {
		String layerId = newLayerId();
		TeaLayer layer = addLayer(new TeaLayer(layerId, this, dim.fbWidth, dim.fbHeight, session));
		return new CanvasImpl<>(layerId, this, dim.fbWidth, dim.fbHeight, use3d -> layer.pathWriter(use3d), layer.canvas);
	}

	@Override
	public TextWindow createTextWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Layer getBackgroundPainter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Predicate<KeyEvent> getKeyOverride() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate<KeyEvent> getKeyPressedHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate<KeyEvent> getKeyReleasedHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate<KeyEvent> getKeyTypedHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double frameBufferHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double frameBufferWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double width() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void hideMouseCursor() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFullScreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean minimalKeyHandler(KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void moveToBack(Layer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveToFront(Layer layer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBackground(Color bgColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFullScreen(boolean fullScreen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHideFullScreenMouseCursor(boolean hideIt) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKeyOverride(Predicate<KeyEvent> keyOverride) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKeyPressedHandler(Predicate<KeyEvent> keyHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKeyReleasedHandler(Predicate<KeyEvent> keyReleasedHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setKeyTypedHandler(Predicate<KeyEvent> keyTypedHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMouseCursor(MouseCursor cursor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showMouseCursor() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		render();
//		for (Layer l : layerMap.values()) {
//			l.flush();
//		}
	}

	public void render() {
		for (Layer l : layerMap.values()) {
			((TeaLayer) l).render(false);
		}
	}

	@Override
	public void useAlternateShortcut(boolean useAlternate) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPasteHandler(Predicate<String> pasteHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clipboardPut(String copied) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> InputControl<T> inputControl(Class<T> type, int code, int controller) {
		// TODO Auto-generated method stub
		return null;
	}

}
