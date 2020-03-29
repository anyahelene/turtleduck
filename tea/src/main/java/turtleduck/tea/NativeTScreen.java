package turtleduck.tea;

import java.util.function.Predicate;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.MouseCursor;
import turtleduck.display.Screen;
import turtleduck.display.impl.BaseScreen;
import turtleduck.display.impl.BaseScreen.Dimensions;
import turtleduck.events.KeyEvent;
import turtleduck.text.TextWindow;
import xtermjs.Terminal;

public class NativeTScreen extends BaseScreen {
	

	public static NativeTScreen create(int config) {
		Dimensions dim = computeDimensions(NativeTDisplayInfo.INSTANCE, config);

		return new NativeTScreen(dim);
	}

	private Window window;
	private HTMLDocument document;
	private HTMLElement mainElement;
    @JSBody(params = { "window" }, script = "return window.terminal;")
    protected static native Terminal getTerminal(Window window);

    @JSBody(params = { "message" }, script = "console.log(message)")
    protected static native void consoleLog(JSObject message);

    @JSBody(params = { "message" }, script = "console.log(message)")
	public
	static native void consoleLog(String string);

    protected Terminal getTerminal() {
    	return getTerminal(window);
    }
    
    public NativeTScreen(Dimensions dim) {
		this.dim = dim;
		window = Window.current();
		document = window.getDocument();
		mainElement = document.getElementById("screen0");
		int height = (int) Math.floor(dim.winHeight);
		this.dim = dim;
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
		HTMLCanvasElement canvas = (HTMLCanvasElement) document.createElement("canvas");
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
		NativeTLayer layer = addLayer(new NativeTLayer(layerId, this, dim.fbWidth, dim.fbHeight, canvas));
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
		// TODO Auto-generated method stub
		
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
