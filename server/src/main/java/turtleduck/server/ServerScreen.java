package turtleduck.server;

import java.util.function.Predicate;

import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.canvas.CanvasImpl;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Viewport;
import turtleduck.display.Viewport.ViewportBuilder;
import turtleduck.display.impl.BaseScreen;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.text.TextWindow;

public class ServerScreen extends BaseScreen {
	String currentGroup = null;

	public static ServerScreen create(TurtleDuckSession session, int config) {
		ViewportBuilder vpb = Viewport.create(ServerDisplayInfo.INSTANCE);
		Viewport vp = vpb.screenArea(0, 0, 0, 0).width(1280).height(720).fit().done();


		return new ServerScreen(session, vp);
	}

	private TurtleDuckSession session;
	private int channel;

	public ServerScreen(TurtleDuckSession session, Viewport vp) {
		super(vp);
		this.session = session;
		setupAspects(vp.aspect());
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
		ServerLayer layer = addLayer(new ServerLayer(layerId, this, viewport.width(), viewport.height(), session));
		return new CanvasImpl<>(layerId, this,viewport.width(), viewport.height(), use3d -> layer.pathWriter(use3d), () -> layer.clear(), layer.canvas);
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
	public double height() {
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
			((ServerLayer) l).render(false);
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

	@Override
	public ScreenControls controls() {
		return this;
	}

	@Override
	protected void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getClipboardString() {
		// TODO Auto-generated method stub
		return null;
	}

}
