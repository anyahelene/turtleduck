package turtleduck.demo;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.colors.Color;
import turtleduck.canvas.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Direction;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Turtle;

public class Demo implements TurtleDuckApp {

	private static Canvas duckCanvas;
	private static Layer duckLayer;
	private static Turtle debugTurtle;

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
				if (i % 3 == 0) {
					Turtle child = turtle.spawn().jumpTo(turtle.point()).turnTo(i).draw(20);
					// child./*draw(step/2).*/turn(180).move(20).turn(-180);//.draw(20);
					if (i % 6 == 0)
						footprint(child.turn(90).jump(2.5).turn(-80), 1);
					else// if(i % 20 == 10)
						footprint(child.turn(-90).jump(2.5).turn(80), 1);
					child.done();
				}
				turtle.turn(10);
				if (i % 2 == 0) {
					turtle.jump(Direction.absolute(i), 10);
				}
				turtle.draw(-step / 2);

			}
			turtle.turn(1);
		}
		turtle.done();
	}

	public static void feather(Turtle turtle, double radius) {
		double w = 5;
		Color grey = Color.color(.5, .5, .5);
		Color green = Color.color(.5, 1, .5);
		Color stem = grey.brighter().opacity(.7);
		Color leaf = grey.opacity(.9);
		turtle.penChange().strokeWidth(w).strokePaint(stem).done();
		turtle.turn(-10);
		turtle.draw(35);
		double r = 30;
		double l = 20;
		double al = 60, ar = 60;
		double dr = 0.03, dl = 0.01;
		double dal = 5.0 / 200.0;
		for (int i = 0; i < 200; i++) {
			turtle.spawn().penChange().strokePaint(leaf).strokeWidth(.5).done() //
					.turn(-al).draw(l / 3).turn(al / 3).draw(l / 3) //
					/* .changePen().strokePaint(leaf.mix(green, .5)).done() */.turn(al / 3).draw(l / 3).done();
			turtle.spawn().penChange().strokePaint(leaf).strokeWidth(.5).done() //
					.turn(ar).draw(r / 3).penChange().strokePaint(leaf.mix(green, .5)).done().turn(ar / 6).draw(r / 3)
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
			turtle.penChange().strokePaint(stem).strokeWidth(w).done();
			stem = stem.darker().opacity((19 * stem.opacity() + 1) / 20.0);
			turtle.turn(.1).draw(1);

		}
		turtle.done();
	}

	public static void footprint(Turtle turtle, double size) {
		size /= 10;
		turtle.penChange().strokePaint(Colors.BLACK).fillPaint(Colors.BLACK).fillOpacity(0.5).done();
		turtle.turn(60).draw(size * 15).turn(-45).draw(size * 100).turn(-150).draw(size * 30);
		turtle.turn(90).draw(size * 30).turn(-90).draw(size * 30);
		turtle.turn(90).draw(size * 30).turn(-150).draw(size * 100).turn(-45).draw(size * 15);
		turtle.done();
	}

	public static void main(String[] args) {
		Launcher.application(new Demo()).launch(args);
	}

	public static void stem(Turtle turtle, double len) {
		double w = 5;
		Color grey = Color.color(.5, .5, .5);
		Color stem = grey.brighter().opacity(.7);
		Color leaf = grey.opacity(.9);
		turtle.penChange().strokeWidth(w).strokePaint(stem).done();
		turtle.turn(-10);
		turtle.draw(35);
		for (int i = 0; i < len; i++) {
			turtle.turn(.1).draw(1);
//			turtle.changePen().done();
		}
		turtle.done();
	}

	private Canvas canvas, debugCanvas;

	private Turtle turtle;

	private Pen debugPen;

	long step = 0;

	@Override
	public void bigStep(double deltaTime) {
//		if(true)
//			return;

		long startMillis = System.currentTimeMillis();
//		System.out.println(deltaTime);

		if (true) {
			debugTurtle.jumpTo(200 * Math.cos(step / 36.0) + 500, 200 * Math.sin(step / 36.0) + 500).turnTo(step * 10);
			colorWheel(debugTurtle, -50);
			debugTurtle.jumpTo(300 * Math.sin(step / 36.0) + 600, 300 * Math.cos(step / 36.0) + 300).turnTo(step * 5);
			colorWheel(debugTurtle, 50);
			debugTurtle.jumpTo(300 * Math.sin(step / 50.0) + 300, 300 * Math.cos(step / 25.0) + 300).turnTo(step * 3);
//			feather(debugTurtle.child().turn(-90), 200);
		}
//		System.err.println("Draw:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
		long t = System.currentTimeMillis();

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
		screen.setBackground(Color.color(1, 1, 1));
		screen.clearBackground();
		canvas = screen.createCanvas();
		debugCanvas = screen.debugCanvas();
		debugPen = debugCanvas.pen().change().strokePaint(Colors.GREEN).strokeWidth(1).done();
		duckCanvas = screen.createCanvas();

		debugTurtle = debugCanvas.turtle().pen(debugPen);
		long startMillis = System.currentTimeMillis();
		turtle = canvas.turtle();
		double t = System.currentTimeMillis();

		turtle.jumpTo(300, 350).turnTo(0);//.draw(50).turn(90).draw(50).turn(90).draw(50).move(0);
		Color red = Color.color(1, 0, 0);
		Color green = Color.color(0, 1, 0);
		Color blue = Color.color(0, 0, 1);
		Turtle turt = turtle;
		for(int i = 0; i < 100; i++) {
			turtle.spawn().penChange().strokePaint(red.mix(green, i / 100.0)).done().turn(10).draw(200).done();
//			turtle.changePen().strokePaint(red.mix(green, i / 100.0)).done().turn(10).draw(200).turnAround().move(200).turnAround().turn(-10);
			turtle.turn(3).draw(10);
		}
	
//		if(true)return;	
		
		turtle.jumpTo(400, 500);
		colorWheel(turtle, 300);
		turtle.jumpTo(500, 150);
		colorWheel(turtle, -100);
		turtle.turnTo(90);
		footprint(turtle.spawn().turn(0).jump(50).jump(0), 10);
		int a = 10;
		for (int i = 0; i < 360; i += a) {
			if (i % 20 == 0)
				footprint(turtle.spawn().turn(90).jump(2.5).turn(-80), 1);
			else
				footprint(turtle.spawn().turn(-90).jump(2.5).turn(80), 1);
			turtle.jump(15);
			turtle.turn(a / 3.0);
		}
//		turtle.moveTo(30, 30).draw(50);
//		turtle.moveTo(200, 500).draw(200);
		/*
		 * turtle.moveTo(300, 100); Parrot parrot = new Parrot(); parrot.draw(turtle);
		 */
		turtle.jumpTo(500, 650).turnTo(0);
//		turtle.startRecording();
		footprint(turtle.spawn().turn(90).jump(2.5).turn(-80), 1);
//		turtle.moveTo(200, 500);
//		turtle.turn(0).draw(100).move(0);
		colorWheel(turtle.spawn(), -100);
		System.err.println("Wheel:   " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		t = System.currentTimeMillis();
		turtle.jumpTo(200, 500);
		feather(turtle.spawn().turn(-90), 200);
		System.err.println("Feather: " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		t = System.currentTimeMillis();
//		for (int i = 0; i < 36; i++)
//			turtle.turn(10).draw(5);
		turtle.jumpTo(400, 500);
		turtle.jump(100);
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
		turtle.jumpTo(0, 0);
		turtle.jumpTo(500, 350).turnTo(0);
//		anim = ((CommandRecorder) recorder).playbackAnimation(turtle);
		System.err.println("Draw:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
		t = System.currentTimeMillis();
	
		System.err.println("Flush:   " + ((System.currentTimeMillis() - t) / 1000.0) + " s");
		System.err.println("Total:   " + ((System.currentTimeMillis() - startMillis) / 1000.0) + " s");
	}
}
