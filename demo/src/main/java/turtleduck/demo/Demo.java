package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.turtle.Pen;
import turtleduck.turtle.TurtleDuck;

public class Demo implements TurtleDuckApp {

	private static Canvas duckCanvas;
	private static Layer duckLayer;
	private static TurtleDuck debugTurtle;

	public static void colorWheel(TurtleDuck turtle, double radius) {
		Paint red = Paint.color(1, 0, 0);
		Paint green = Paint.color(0, 1, 0);
		Paint blue = Paint.color(0, 0, 1);
		Paint ink = red;
		double step = (2 * Math.PI * radius) / 360.0;
		turtle.move(radius);

//		for (int k = 0; k < 360; k++)
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
			if (radius < 0) {
				if (i % 3 == 0) {
					TurtleDuck child = turtle.child().moveTo(turtle.position()).turnTo(i).draw(20);
					// child./*draw(step/2).*/turn(180).move(20).turn(-180);//.draw(20);
					if (i % 6 == 0)
						footprint(child.turn(90).move(2.5).turn(-80), 1);
					else// if(i % 20 == 10)
						footprint(child.turn(-90).move(2.5).turn(80), 1);
					child.done();
				}
				turtle.turn(10);
				if (i % 2 == 0) {
					double a = turtle.angle();
					turtle.turnTo(i).move(10).turnTo(a);
				}
				turtle.draw(-step / 2);

			}
			turtle.turn(1);
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

	public static void footprint(TurtleDuck turtle, double size) {
		size /= 10;
		turtle.changePen().strokePaint(Colors.BLACK).fillPaint(Colors.BLACK).fillOpacity(0.5).done();
		turtle.turn(60).draw(size * 15).turn(-45).draw(size * 100).turn(-150).draw(size * 30);
		turtle.turn(90).draw(size * 30).turn(-90).draw(size * 30);
		turtle.turn(90).draw(size * 30).turn(-150).draw(size * 100).turn(-45).draw(size * 15).fill();
		turtle.done();
	}

	public static void main(String[] args) {
		Launcher.application(new Demo()).launch(args);
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

	private Canvas canvas, debugCanvas;

	private TurtleDuck turtle;

	private Pen debugPen;

	long step = 0;

	@Override
	public void bigStep(double deltaTime) {
//		if(true)
//			return;

		long startMillis = System.currentTimeMillis();
//		System.out.println(deltaTime);

		if (true) {
			debugTurtle.moveTo(200 * Math.cos(step / 36.0) + 500, 200 * Math.sin(step / 36.0) + 500).turnTo(step * 10);
			colorWheel(debugTurtle, -50);
			debugTurtle.moveTo(300 * Math.sin(step / 36.0) + 600, 300 * Math.cos(step / 36.0) + 300).turnTo(step * 5);
			colorWheel(debugTurtle, 50);
			debugTurtle.moveTo(300 * Math.sin(step / 50.0) + 300, 300 * Math.cos(step / 25.0) + 300).turnTo(step * 3);
//			feather(debugTurtle.child().turn(-90), 200);
		}
//		System.err.println("Draw:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
		long t = System.currentTimeMillis();
		canvas.flush();
		debugCanvas.flush();
		duckCanvas.flush();
//		System.err.println("Flush:   " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
//		System.err.println("Total:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
		step++;
	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Screen screen) {
		screen.setBackground(Paint.color(1, 1, 1));
		screen.clearBackground();
		canvas = screen.createCanvas();
		debugCanvas = screen.debugCanvas();
		debugPen = debugCanvas.createPen().change().strokePaint(Colors.GREEN).strokeWidth(1).done();
		duckCanvas = screen.createCanvas();

		debugTurtle = debugCanvas.createTurtleDuck().pen(debugPen);
		long startMillis = System.currentTimeMillis();
		turtle = canvas.createTurtleDuck();
		double t = System.currentTimeMillis();

		turtle.moveTo(300, 350).turnTo(0);//.draw(50).turn(90).draw(50).turn(90).draw(50).move(0);
		Paint red = Paint.color(1, 0, 0);
		Paint green = Paint.color(0, 1, 0);
		Paint blue = Paint.color(0, 0, 1);
		TurtleDuck turt = turtle;
		for(int i = 0; i < 100; i++) {
			turtle.child().changePen().strokePaint(red.mix(green, i / 100.0)).done().turn(10).draw(200).done();
//			turtle.changePen().strokePaint(red.mix(green, i / 100.0)).done().turn(10).draw(200).turnAround().move(200).turnAround().turn(-10);
			turtle.turn(3).draw(10);
		}
	
//		if(true)return;	
		
		turtle.moveTo(400, 500);
		colorWheel(turtle, 300);
		turtle.moveTo(500, 150);
		colorWheel(turtle, -100);
		turtle.turnTo(90);
		footprint(turtle.child().turn(0).move(50).move(0), 10);
		int a = 10;
		for (int i = 0; i < 360; i += a) {
			if (i % 20 == 0)
				footprint(turtle.child().turn(90).move(2.5).turn(-80), 1);
			else
				footprint(turtle.child().turn(-90).move(2.5).turn(80), 1);
			turtle.move(15);
			turtle.turn(a / 3.0);
		}
//		turtle.moveTo(30, 30).draw(50);
//		turtle.moveTo(200, 500).draw(200);
		/*
		 * turtle.moveTo(300, 100); Parrot parrot = new Parrot(); parrot.draw(turtle);
		 */
		turtle.moveTo(500, 650).turnTo(0);
//		turtle.startRecording();
		footprint(turtle.child().turn(90).move(2.5).turn(-80), 1);
//		turtle.moveTo(200, 500);
//		turtle.turn(0).draw(100).move(0);
		colorWheel(turtle.child(), -100);
		System.err.println("Wheel:   " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		t = System.currentTimeMillis();
		turtle.moveTo(200, 500);
		feather(turtle.child().turn(-90), 200);
		System.err.println("Feather: " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		t = System.currentTimeMillis();
//		for (int i = 0; i < 36; i++)
//			turtle.turn(10).draw(5);
		turtle.moveTo(400, 500);
		turtle.move(100);
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
//		recorder = turtle.endRecording();
//		System.out.println(recorder.toString().replaceAll("\\),", "),\n    "));
		turtle.moveTo(0, 0);
		turtle.moveTo(500, 350).turnTo(0);
//		anim = ((CommandRecorder) recorder).playbackAnimation(turtle);
		System.err.println("Draw:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
		t = System.currentTimeMillis();
		canvas.flush();
		debugCanvas.flush();
		duckCanvas.flush();
		System.err.println("Flush:   " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		System.err.println("Total:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
	}
}
