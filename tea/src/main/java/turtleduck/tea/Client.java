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
import turtleduck.turtle.Pen;
import turtleduck.turtle.TurtleDuck;
import turtleduck.colors.Colors;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Bearing;

public class Client  {

	public static void main(String[] args) {
		Screen screen = NativeTDisplayInfo.INSTANCE.startPaintScene(null, 0);
		Canvas canvas = screen.createCanvas();
		TurtleDuck turtle = canvas.createTurtleDuck();
		turtle.changePen().strokePaint(Colors.RED).done();
		turtle.moveTo(0, 0);
		turtle.drawTo(300,100);
		turtle.done();
	}
}
