package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Paint;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Pen;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleDuck;

public class Demo implements TurtleDuckApp {

	public static void main(String[] args) {
		Launcher.application(new Demo()).launch(args);
	}

	@Override
	public void start(Screen screen) {
		screen.setBackground(Paint.color(1, 1, 1));
		screen.clearBackground();
		Layer layer = screen.createPainter();
		Canvas canvas = layer.canvas();
		TurtleDuck turtle = canvas.createTurtleDuck();
		turtle.moveTo(500, 150);
		colorWheel(turtle, 300);
		turtle.moveTo(200, 500);
		feather(turtle.subTurtle().turn(-90), 200);
		Pen pen = canvas.createPen();
		pen = pen.change().fillPaint(Paint.color(1, 0, 0)).done();
		canvas.line(pen, pen, Point.point(50, 50), Point.point(150, 150));
		canvas.polyline(pen, null, pen, Point.point(100, 50), Point.point(100, 150), Point.point(150, 100));
		canvas.polygon(pen, null, pen, Point.point(200, 50), Point.point(200, 150), Point.point(250, 100));
		canvas.polygon(null, pen, pen, Point.point(300, 50), Point.point(300, 150), Point.point(350, 100));
	}

	public static void feather(TurtleDuck turtle, double radius) {
		double w = 5;
		Paint grey = Paint.color(.5, .5, .5);
		Paint green = Paint.color(.5, 1, .5);
		Paint stem = grey.brighter().opacity(.7);
		Paint leaf = grey.opacity(.9);
		turtle.changePen().strokeWidth(w).strokePaint(stem).done();
		turtle.turn(-10);
		turtle.draw(35);
		double r = 30;
		double l = 20;
		double al = 60, ar = 60;
		double dr = 0.03, dl = 0.01;
		double dal = 5.0/200.0;
		for (int i = 0; i < 200; i++) {
			turtle.subTurtle().changePen().strokePaint(leaf).strokeWidth(.5).done() //
				.turn(-al).draw(l/3).turn(al/3).draw(l/3) //
				.changePen().strokePaint(leaf.mix(green, .5)).done().turn(al/3).draw(l/3);
			turtle.subTurtle().changePen().strokePaint(leaf).strokeWidth(.5).done() //
				.turn(ar).draw(r/3).turn(ar/6).draw(r/3).turn(-ar/3).draw(r/3);
			w = w - 4.9 / 200.0;
			if(i == 100) {
				dr = 0.06;
			}
			if(i == 170) {
				dr = r / 35.0;
				dl = 0.5;
			}
			dal += .0025;
				al -= dal;
			r -= dr;
			l -= dl;
//			if (i > 150)
//				a = a - i / 100.0;
			turtle.changePen().strokePaint(stem).strokeWidth(w).done();
			stem = stem.darker().opacity((19 * stem.opacity() + 1) / 20.0);
			turtle.turn(.1).draw(1);

		}
	}

	public static void colorWheel(TurtleDuck turtle, double radius) {
		Paint red = Paint.color(1, 0, 0);
		Paint green = Paint.color(0, 1, 0);
		Paint blue = Paint.color(0, 0, 1);
		Paint ink = red;
		double step = (2 * Math.PI * radius) / 360.0;
		turtle.move(radius);
		for (int i = 0; i < 360; i++) {
			if (i < 120)
				ink = red.mix(green, i / 119.0);
			else if (i < 240)
				ink = green.mix(blue, (i - 120) / 119.0);
			else
				ink = blue.mix(red, (i - 240) / 119.0);
			turtle.changePen().strokePaint(ink).done();
			turtle.draw(step);
			TurtleDuck sub = turtle.subTurtle().turn(90);
			for (int j = 20; j > 0; j--) {
				sub.changePen().strokeWidth(j / 3.5).strokePaint(ink).done();
				sub.draw(radius / 20.0);
				ink = ink.brighter();
			}
			turtle.turn(1);
		}
	}
}
