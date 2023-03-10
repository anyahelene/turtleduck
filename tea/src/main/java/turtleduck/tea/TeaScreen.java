package turtleduck.tea;

import java.util.function.Predicate;

import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.canvas.CanvasImpl;
import turtleduck.display.Camera;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Viewport;
import turtleduck.display.Viewport.ViewportBuilder;
import turtleduck.display.impl.BaseScreen;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.messaging.CanvasService;
import turtleduck.text.TextWindow;

public class TeaScreen extends BaseScreen {
	String currentGroup = null;

	public static TeaScreen create(CanvasService session, int config) {
		ViewportBuilder vpb = Viewport.create(TeaDisplayInfo.INSTANCE);
		Viewport vp = vpb.screenArea(0, 0, 0, 0).width(1280).height(720).fit().done();
		return new TeaScreen(session, vp);
	}

	private CanvasService session;
	private int channel;
	private Camera camera3;
	private Camera camera2;

	public TeaScreen(CanvasService session, Viewport vp) {
		super(vp);
		this.session = session;
		camera3 = viewport.create3dCamera();
		camera2 = viewport.create2dCamera();
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
		TeaLayer teaLayer = new TeaLayer(newLayerId(), this, viewport.width(), viewport.height(), session);
		Canvas canvas = new CanvasImpl<>(newLayerId(), this, viewport.width(), viewport.height(),
				use3d -> teaLayer.pathWriter(use3d), () -> teaLayer.clear(), session);
		return canvas;
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
