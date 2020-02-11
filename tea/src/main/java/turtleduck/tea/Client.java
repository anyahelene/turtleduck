package turtleduck.tea;

import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.css.CSSStyleDeclaration;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.interop.*;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Bearing;

public class Client  {

	public static void main(String[] args) {
		Screen screen = NativeTDisplayInfo.INSTANCE.startPaintScene(null, 0);
		Layer layer = screen.createPainter();
		Canvas canvas = layer.canvas();
		canvas.line(canvas.createPen(), null, Point.point(0, 0), Point.point(300, 100));
	}
}
