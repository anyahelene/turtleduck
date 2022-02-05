package turtleduck.gfxapp;

import org.slf4j.Logger;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Text;

import turtleduck.canvas.Canvas;
import turtleduck.colors.Colors;
import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.paths.Pen;
import turtleduck.tea.CanvasServer;
import turtleduck.tea.JSUtil;
import turtleduck.tea.TeaDisplayInfo;
import turtleduck.util.Logging;
import java.util.Random;
import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.geometry.Direction;
import turtleduck.canvas.Canvas;
import turtleduck.turtle.Turtle;
import turtleduck.text.TextCursor;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.util.Meta;
import static turtleduck.geometry.Point.point;
import static turtleduck.colors.Colors.*;
import static turtleduck.geometry.Direction.*;
import turtleduck.turtle.SpriteBuilder;
import turtleduck.sprites.Sprite;

public class App implements JSObject {
	public static final Logger logger = Logging.getLogger(App.class);
	private static App app;
	private static JSObject WINDOW_MAP;
	protected CanvasServer canvasServer;
	private Screen screen;
	private Canvas canvas;
	private Turtle turtle;

	public App() {

	}

	public static void main(String[] args) {
		Logging.setLogDest(JSUtil::logger);
		Logging.useCustomLoggerFactory();
		WINDOW_MAP = Window.current().cast();
		app = new App();
		app.start();
	}

	private void start() {
		HTMLDocument document = Window.current().getDocument();
		HTMLElement page = document.getElementById("page");
		System.out.print("start");
		logger.info("start");
		canvasServer = new CanvasServer(null);
		logger.info("server: {}", canvasServer);
		screen = TeaDisplayInfo.INSTANCE.startPaintScene(canvasServer);
		canvas = screen.createCanvas();
		canvas.drawCircle(Point.ZERO, 500);
		 turtle = canvas.turtle().stroke(Colors.WHITE).jumpTo(0, 0);
		try {
			run();
			screen.flush();
		} catch (Throwable t) {
			logger.error("Java exception thrown", t);
			throw t;
		}

	}
	void reset() {
		canvas.stroke(Colors.WHITE, 1).fill(Colors.TRANSPARENT).background(Colors.BLACK);
		turtle.stroke(Colors.WHITE, 1).fill(Colors.TRANSPARENT).jumpTo(0, 0).turnTo(0);
		screen.clear();
	}

	int angle = 45;

	void petal(Turtle turtle) {
		// turtle.penColor(Color.hsv(i,1,1));
		int a = (int) (angle + 15 * (Math.random() - .5));
		turtle.turn(-60);
		Turtle t2 = turtle.spawn();
		turtle.fill(PURPLE.perturb()).stroke(PURPLE.darker());
		for (int i = a; i < 360 - a; i++) {
			turtle.draw(1).turn(1);
			if (i == 180)
				turtle.turn(a);
		}
		t2.turn(a / 2);
		a = 90;
		t2.fill(BLACK.brighter().brighter().perturb().opacity(.3)).stroke(TRANSPARENT);
		for (int i = a; i < 360 - a; i++) {
			t2.draw(.5).turn(1);
			if (i == 180)
				t2.turn(a);
		}

	}

	void flower(Turtle turtle) {
		for (int i = 0; i < 6; i++)
			petal(turtle.turn(60).spawn().stroke(GREEN, 3).draw(15));
		turtle.turn(30);
		for (int i = 0; i < 6; i++)
			turtle.turn(60).spawn().stroke(MAGENTA).draw(12);
	}

	private void run() {
		reset();

		turtle.strokeWidth(2);

		flower(turtle.jumpTo(-100, 50));
		flower(turtle.jumpTo(200, 11));

	}
}
