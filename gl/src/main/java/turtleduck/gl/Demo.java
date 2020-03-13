package turtleduck.gl;

import turtleduck.Launcher;
import turtleduck.TurtleDuckApp;
import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.turtle.Pen;
import turtleduck.turtle.TurtleDuck;

public class Demo implements TurtleDuckApp {

	private Layer painter;
	private Canvas canvas;
	private Pen pen;
	private TurtleDuck turtle;

	@Override
	public void bigStep(double deltaTime) {
//		for (int i = 0; i < 700; i += 10) {
//			pen = pen.change().strokePaint(Paint.color(i/700.0,(700.0-i)/700.0,0.5)).done();
//			canvas.line(pen, Point.point(0, i), Point.point(1000, 100+i));
//		}
		turtle.turn(1);
		for(int i = 0; i < 360; i++) {
			turtle.draw(5).turn(1);
		}
//		turtle.moveTo(600,200);
		colorWheel(turtle, -100);
		colorWheel(turtle, 100);

//		turtle.moveTo(500,400);
		turtle.done();
		
		System.out.println(deltaTime);
	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Screen screen) {
		canvas = screen.createCanvas();
		pen = canvas.createPen();
		 turtle = canvas.createTurtleDuck();
			turtle.moveTo(500,500);

	}

	public static void main(String[] args) {
		new GLLauncher().app(new Demo()).launch(args);
	}
	
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
//					TurtleDuck child = turtle.child(duckCanvas).moveTo(turtle.position()).turnTo(i).draw(20);
//					// child./*draw(step/2).*/turn(180).move(20).turn(-180);//.draw(20);
//					if (i % 6 == 0)
//						footprint(child.turn(90).move(2.5).turn(-80), 1);
//					else// if(i % 20 == 10)
//						footprint(child.turn(-90).move(2.5).turn(80), 1);
//					turtle.done();
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

	private static void footprint(TurtleDuck turn, int i) {
		// TODO Auto-generated method stub
		
	}

}
