package turtleduck.tea;

import java.util.function.Predicate;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.canvas.CanvasImpl;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Screen;
import turtleduck.display.Viewport;
import turtleduck.display.Viewport.ViewportBuilder;
import turtleduck.display.impl.BaseScreen;
import turtleduck.events.InputControl;
import turtleduck.events.KeyEvent;
import turtleduck.text.TextWindow;

public class NativeTScreen extends BaseScreen {
	private HTMLElement mainElement;

	public static NativeTScreen create(int config) {
		ViewportBuilder vpb = Viewport.create(TeaDisplayInfo.INSTANCE);
		Viewport vp = vpb.screenArea(0, 0, 0, 0).width(1280).height(720).fit().done();

		return new NativeTScreen(vp);
	}

	public NativeTScreen(Viewport vp) {
		super(vp);

		setupAspects(vp.aspect());

		mainElement = Browser.document.getElementById("screen0");
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
		HTMLCanvasElement canvas = (HTMLCanvasElement) Browser.document.createElement("canvas");
		String layerId = newLayerId();
		canvas.setAttribute("id", layerId);
//		canvas.setAttribute("width", "1280");
//		canvas.setAttribute("height", "960");
		CSSStyleDeclaration style = canvas.getStyle();
		style.setProperty("position", "relative");
		style.setProperty("top", "0");
		style.setProperty("left", "0");
		CanvasRenderingContext2D context = (CanvasRenderingContext2D) canvas.getContext("2d");
		context.strokeText("Hello, world!", 10, 10);
		mainElement.appendChild(canvas);
		NativeTLayer layer = addLayer(new NativeTLayer(layerId, this, viewport.width(), viewport.height(), canvas));
		return new CanvasImpl<>(layerId, this, viewport.width(), viewport.height(), use3d -> layer.pathWriter(use3d),
				() -> layer.clear(), Client.client.canvas);

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
		// TODO Auto-generated method stub
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
