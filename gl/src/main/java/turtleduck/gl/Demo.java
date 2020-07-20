package turtleduck.gl;

import java.net.URL;
import java.io.IOException;

import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.drawing.Image;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Turtle;

public class Demo implements TurtleDuckApp {

	private Layer painter;
	private Canvas canvas;
	private Pen pen;
	private Turtle turtle;
	private GLPixelData image;
	private int count = 0;

	@Override
	public void bigStep(double deltaTime) {

//		GLLayer.angle += deltaTime;
		count = (count + 1) % 256;
		int s = count / 16;
		for(int offY = 0; offY < 256; offY += 32)
		for(int offX = 0; offX < 256; offX += 32) {
			canvas.drawImage(Point.point(offX+s, offY+s), image.crop(offX, offY, 32, 32).crop(s, s, 32-s*2, 32-s*2));
			}
		canvas.drawImage(Point.point(300,0), image);
		canvas.drawImage(Point.point(300, 0), image.transpose(Image.Transpose.FLIP_LEFT_RIGHT).scale(400, 200));

		canvas.drawImage(Point.point(600, 0), image.transpose(Image.Transpose.FLIP_TOP_BOTTOM));
		canvas.drawImage(Point.point(900, 0),
				image.transpose(Image.Transpose.FLIP_TOP_BOTTOM).transpose(Image.Transpose.FLIP_LEFT_RIGHT));
		canvas.drawImage(Point.point(0, 300), image);
		canvas.drawImage(Point.point(300, 300), image.transpose(Image.Transpose.ROTATE_90));
		canvas.drawImage(Point.point(600, 300), image.transpose(Image.Transpose.ROTATE_180));
		canvas.drawImage(Point.point(900, 300), image.transpose(Image.Transpose.ROTATE_270));
		canvas.drawImage(Point.point(0, 600), image);
		Image img = image.transpose(Image.Transpose.ROTATE_90);
		canvas.drawImage(Point.point(300, 600), img);
		img = img.transpose(Image.Transpose.ROTATE_90);
		canvas.drawImage(Point.point(600, 600), img);
		img = img.transpose(Image.Transpose.ROTATE_90);
		canvas.drawImage(Point.point(900, 600), img);

		if (true)
			return;
		turtle.beginPath();

		turtle.turn(1);
		for (int i = 0; i < 10; i++) {
			footprint(turtle.child(), 5);
			turtle.jump(100).turn(36);
		}
//		turtle.jump(0);
//		turtle.moveTo(600,200);
		colorWheel(turtle, -100);
		colorWheel(turtle, 100);

//		turtle.moveTo(500,400);
		turtle.done();

//		System.out.println(deltaTime);
	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Screen screen) {
		canvas = screen.createCanvas();
		pen = canvas.createPen();
		turtle = canvas.createTurtle();
		System.out.println(turtle);
		turtle.jumpTo(500, 500);
		System.out.println(turtle);
		turtle.turn(1);
		for (int i = 0; i < 360; i++) {
			turtle.draw(5).turn(1);
		}
		try {
			image = new GLPixelData("/yeti-juno.jpg");
			System.out.println(image);
//			img = img.crop(50, 50, 200, 200);
			System.out.println(image);


			/*
			 * System.out.println(img); for(int y = 0; y < img.height(); y++) {
			 * turtle.jumpTo(700, 100+y); turtle.bearing(Bearing.DUE_EAST); for(int x = 0; x
			 * < img.width(); x++) { Paint p = img.readPixel(x, y); turtle.penColor(p);
			 * turtle.draw(1); } }
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new GLLauncher().app(new Demo()).launch(args);
	}

	public static void colorWheel(Turtle turtle, double radius) {
		Paint red = Paint.color(1, 0, 0);
		Paint green = Paint.color(0, 1, 0);
		Paint blue = Paint.color(0, 0, 1);
		Paint ink = red;
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
			Turtle sub = turtle.child().turn(90);
			for (int j = 20; j > 0; j--) {
				sub.penChange().strokeWidth(j / 3.5).strokePaint(ink).done();
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
					double a = turtle.bearing().azimuth();
					turtle.turnTo(i).jump(10).turnTo(a);
				}
				turtle.draw(-step / 2);

			}
			turtle.turn(1);
		}
		turtle.done();
	}

	private static void footprint(Turtle turtle, double size) {
		size /= 10;
		turtle.penChange().strokePaint(Colors.WHITE).fillPaint(Colors.WHITE).fillOpacity(0.5).done();
		turtle.turn(60).draw(size * 15).turn(-45).draw(size * 100).turn(-150).draw(size * 30);
		turtle.turn(90).draw(size * 30).turn(-90).draw(size * 30);
		turtle.turn(90).draw(size * 30).turn(-150).draw(size * 100).turn(-45).draw(size * 15);
		turtle.done();
	}

}
