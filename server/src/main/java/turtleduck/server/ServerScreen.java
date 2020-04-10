package turtleduck.server;

import java.util.function.Predicate;

import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.colors.Paint;
import turtleduck.comms.Channel;
import turtleduck.comms.Message;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseScreen;
import turtleduck.display.impl.BaseScreen.Dimensions;
import turtleduck.events.KeyEvent;
import turtleduck.text.TextWindow;

public class ServerScreen extends BaseScreen {

	public static ServerScreen create(TurtleDuckSession session, int config) {
		Dimensions dim = computeDimensions(ServerDisplayInfo.INSTANCE, config);

		return new ServerScreen(session, dim);
	}

	private TurtleDuckSession session;
	private int channel;

	public ServerScreen(TurtleDuckSession session, Dimensions dim) {
		this.dim = dim;
		int height = (int) Math.floor(dim.winHeight);
		this.session = session;
		setupAspects(dim);
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
		ServerLayer layer = addLayer(new ServerLayer(layerId, this, dim.fbWidth, dim.fbHeight, session));
		return layer;
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
	public void setBackground(Paint bgColor) {
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
		for (Layer l : layers.values()) {
			l.flush();
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

}