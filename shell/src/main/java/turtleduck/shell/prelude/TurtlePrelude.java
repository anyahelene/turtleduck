package turtleduck.shell.prelude;

import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.geometry.Direction;
import turtleduck.display.Canvas;
import turtleduck.turtle.Turtle;
import turtleduck.turtle.Pen;
import turtleduck.text.TextCursor;
import turtleduck.colors.Colors;
import turtleduck.shell.TShell;

public class TurtlePrelude {
	Screen screen = turtleduck.objects.IdentifiedObject.Registry.findObject(Screen.class, "SCREEN_ID");
	Canvas canvas = screen.createCanvas();
	Turtle turtle = canvas.createTurtle();

	void init() {
		turtle.penChange().strokePaint(Colors.BLACK).done();
		turtle.jumpTo(10, 10);
		turtleduck.shell.TShell.testValue = 5;
	}

	void head() {
		turtle.spawn().turn(-170).jump(2.5)//
				.turn(90).draw(5)//
				.turn(30).draw(10)//
				.turn(45).draw(5)//
				.turn(60).draw(5)//
				.turn(45).draw(10)//
				.turn(30).draw(5).done();
	}

	void foot() {
		turtle.spawn().turn(-120).draw(5)//
				.turn(45).draw(7)//
				.turn(45).draw(5)//
				.turn(45).draw(3)//
				.turn(45).draw(5)//
				.turn(45).draw(7)//
				.turn(45).draw(5).done();
	}

	void turtle() {
		turtle.jump(50);
		for (int i = 0; i < 12; i++) {
			turtle.turn(30).draw(10 + (i == 3 || i == 9 ? 5 : 0));
			if (i == 1 || i == 3 || i == 7 | i == 9) {
				foot();
			}
			if (i == 5) {
				head();
			}
		}
	}

}
