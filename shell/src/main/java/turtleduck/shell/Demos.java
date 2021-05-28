package turtleduck.shell;

import turtleduck.colors.Color;
import turtleduck.turtle.Turtle;

public class Demos {
	public static void colorWheel(Turtle turtle, double radius) {
		Color red = Color.color(1, 0, 0);
		Color green = Color.color(0, 1, 0);
		Color blue = Color.color(0, 0, 1);
		Color ink = red;
		double step = (2 * Math.PI * radius) / 360.0;
		turtle.jump(radius);

//		for (int k = 0; k < 360; k++)
		for (int i = 0; i < 360; i++) {
			if (i < 120)
				ink = red.mix(green, i / 119.0);
			else if (i < 240)
				ink = green.mix(blue, (i - 120) / 119.0);
			else
				ink = blue.mix(red, (i - 240) / 119.0);
			turtle.penChange().strokePaint(ink).done();
			turtle.draw(step);
			Turtle sub = turtle.spawn().turn(90);
			for (int j = 20; j > 0; j--) {
				sub.penChange().strokeWidth(j / 3.5).strokePaint(ink).done();
				sub.draw(radius / 20.0);
				ink = ink.brighter();
			}
			sub.done();
			if (radius < 0) {
				turtle.turn(10);
				if (i % 2 == 0) {
					double a = turtle.direction().degrees();
					turtle.turnTo(i).jump(10).turnTo(a);
				}
				turtle.draw(-step / 2);

			}
			turtle.turn(1);
		}
		turtle.done();
	}
}
