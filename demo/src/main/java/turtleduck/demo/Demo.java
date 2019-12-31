package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.CommandRecorder;
import turtleduck.turtle.Pen;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleAnimation;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.base.SvgCanvas;

public class Demo implements TurtleDuckApp {

	private CommandRecorder recorder;
	private TurtleAnimation anim;
	private Canvas canvas, debugCanvas;
	private TurtleDuck turtle;
	private Pen debugPen;
	private TurtleDuck debugTurtle;

	public static void main(String[] args) {
		Launcher.application(new Demo()).launch(args);
	}

	@Override
	public void start(Screen screen) {
		screen.setBackground(Paint.color(1, 1, 1));
		screen.clearBackground();
		Layer layer = screen.createPainter();
		canvas = false ? new SvgCanvas() : layer.canvas();
		debugCanvas = screen.debugLayer().canvas();
		debugPen = debugCanvas.createPen().change().strokePaint(Colors.GREEN).done();
		debugTurtle = debugCanvas.createTurtleDuck().pen(debugPen);
		long startMillis = System.currentTimeMillis();
		turtle = canvas.createTurtleDuck();
		double t = System.currentTimeMillis();

		turtle.moveTo(300, 350).turnTo(0);
		colorWheel(turtle, 100);

		
		/*
		 * turtle.moveTo(300, 100); Parrot parrot = new Parrot(); parrot.draw(turtle);
		 */
		turtle.moveTo(500, 350).turnTo(0);
		turtle.startRecording();
		colorWheel(turtle.child(), 100);
		System.err.println("Wheel:   " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		t = System.currentTimeMillis();
		turtle.moveTo(200, 500); 
		feather(turtle.child().turn(-90), 200); System.err.println("Feather: " +
				((System.currentTimeMillis() - t) / 1000.0) + " s");
		t = System.currentTimeMillis();
//		for (int i = 0; i < 36; i++)
//			turtle.turn(10).draw(5);
		turtle.moveTo(400, 500);
		turtle.draw(50);
		stem(turtle.turn(-90), 200);
		System.err.println("Stem:    " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		/*
		 * canvas.flush(); Pen pen = canvas.createPen(); pen =
		 * pen.change().fillPaint(Paint.color(1, 0, 0)).done(); canvas.line(pen, pen,
		 * Point.point(50, 50), Point.point(150, 150)); canvas.polyline(pen, null, pen,
		 * Point.point(100, 50), Point.point(100, 150), Point.point(150, 100));
		 * canvas.polygon(pen, null, pen, Point.point(200, 50), Point.point(200, 150),
		 * Point.point(250, 100)); canvas.polygon(null, pen, pen, Point.point(300, 50),
		 * Point.point(300, 150), Point.point(350, 100));
		 */
		System.err.println("Total:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
		recorder = turtle.endRecording();
		System.out.println(recorder.toString().replaceAll(",", ",\n    "));
		turtle.moveTo(0, 0);
		turtle.useRadians();
		turtle.moveTo(500, 350).turnTo(0);
		anim = recorder.playbackAnimation(turtle);
	}

	public static void stem(TurtleDuck turtle, double len) {
		double w = 5;
		Paint grey = Paint.color(.5, .5, .5);
		Paint stem = grey.brighter().opacity(.7);
		Paint leaf = grey.opacity(.9);
		turtle.changePen().strokeWidth(w).strokePaint(stem).done();
		turtle.turn(-10);
		turtle.draw(35);
		for (int i = 0; i < len; i++) {
			turtle.turn(.1).draw(1);
//			turtle.changePen().done();
		}
		turtle.done();
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
		double dal = 5.0 / 200.0;
		for (int i = 0; i < 200; i++) {
			turtle.child().changePen().strokePaint(leaf).strokeWidth(.5).done() //
					.turn(-al).draw(l / 3).turn(al / 3).draw(l / 3) //
					/* .changePen().strokePaint(leaf.mix(green, .5)).done() */.turn(al / 3).draw(l / 3).done();
			turtle.child().changePen().strokePaint(leaf).strokeWidth(.5).done() //
					.turn(ar).draw(r / 3).changePen().strokePaint(leaf.mix(green, .5)).done().turn(ar / 6).draw(r / 3)
					.turn(-ar / 3).draw(r / 3).done();
			w = w - 4.9 / 200.0;
			if (i == 100) {
				dr = 0.06;
			}
			if (i == 170) {
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
		turtle.done();
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
			TurtleDuck sub = turtle.child().turn(90);
			for (int j = 20; j > 0; j--) {
				sub.changePen().strokeWidth(j / 3.5).strokePaint(ink).done();
				sub.draw(radius / 20.0);
				ink = ink.brighter();
			}
			sub.done();
			turtle.turn(1);
		}
		turtle.done();
	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void bigStep(double deltaTime) {
//		System.out.println(deltaTime);
		if (anim != null) {
			boolean more = anim.step(deltaTime);
			debugCanvas.clear();
			anim.debug(debugTurtle);
			if(!more) {
				turtle.turn(Math.PI/2);
				turtle.move(100);
				anim = recorder.playbackAnimation(turtle);
			}
		}
	}
}
