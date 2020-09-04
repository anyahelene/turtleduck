package turtleduck.gl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.ByteBuffer;

import turtleduck.TurtleDuckApp;
import turtleduck.colors.Colors;
import turtleduck.colors.Color;
import turtleduck.display.Canvas;
import turtleduck.display.Layer;
import turtleduck.display.Screen;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.gl.objects.Util;
import turtleduck.grid.Grid;
import turtleduck.grid.MyGrid;
import turtleduck.image.Image;
import turtleduck.image.ImageFactory;
import turtleduck.image.Tiles;
import turtleduck.sprites.AbstractSprite;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Turtle;

public class Demo implements TurtleDuckApp {
	private final class DemoSprite extends AbstractSprite {
		protected final Point offset;
		protected final Image img;
		protected double speed;
		protected Direction rotation;

		private DemoSprite(Point p, Direction b, double speed, double rotation, Image img) {
			super(p, b);
			this.offset = Point.point(-img.width() / 2, -img.height() / 2);
			this.img = img;
			this.speed = speed;
			this.rotation = Direction.relative(rotation);
//			System.out.println("rotation: " + rotation + " → " + this.rotation);
		}

		@Override
		public void draw(Canvas canvas) {
			((GLLayer) canvas).drawImage(offset, img, (float) bearing().radians());
		}

		public void step() {
			if (x() < -1280 || x() > 1280 * 2 || y() < -720 || y() > 720 * 2)
				bearing(Direction.relative(90));

			bearing(rotation);
			if (this == sprites.get(0)) {
//				System.out.println("bearing: " + bearing() + ", rotation: " + rotation + ", azimuth: " + bearing().azimuth());
			}
			forward(speed);

		}
	}

	private final int GRID_COLS = 48, GRID_ROWS = 32;
	private Grid<GridTile> grid = new MyGrid<>(GRID_COLS, GRID_ROWS, (l) -> new GridTile(l.getY() < 12 ? 190 : 192));
	private List<DemoSprite> sprites = new ArrayList<>();
	private Layer painter;
	private Canvas canvas;
	private Pen pen;
	private Turtle turtle;
	private GLPixelData image;
	private int count = 0, count2 = 0;
	private Image image2;
	private Tiles tiles;
	private GLScreen screen;
	private int[] tileMap;
	private double rotation = 0;

	@Override
	public void bigStep(double deltaTime) {
		turtle.jumpTo(500, 500);
		turtle.child().jumpTo(500, 500).turnTo(0).penWidth(20).penColor(Color.color(.5, .5, .5, 1)).draw(200).turn(45)
				.draw(200).turn(90).draw(200).turn(-90).draw(200);
		turtle.child().jumpTo(600, 500).turnTo(0).penWidth(2).penColor(Color.color(.5, .5, .5, 1)).draw(200).turn(45)
				.draw(200).turn(90).draw(200).turn(-90).draw(200);
//		if(true)
//			return;

		fern(turtle.child().at(200, 900).turnTo(45), 10, 5);
		fern2(turtle.child().at(900, 900).turnTo(0), 5, 10);
	tree(turtle.child().at(850,800).penColor(Color.color(.5, .3, .0)).penWidth(2).turnTo(0), 10, 10);
		wheel1(turtle.child().at(500, 500).turnTo(rotation).penColor(Color.color(0.5, 0, 0, 0.5))); 
		rotation += 16 * deltaTime;
		canvas.drawImage(Point.point(100, 0), image2);
		canvas.drawImage(Point.point(200, 0), image2);
		((GLLayer) canvas).plot(Point.point(100, 100), 1000, 1000, Colors.BLACK,
				"between(cos((x-0.005)*3.14159265),cos((x+0.005)*3.14159265), y) + equal(cos(x*3.14159265), y)"); // "equal(x*x
																													// +
																													// y*y,
																													// 1)");

//		GLLayer.angle += deltaTime;
		count = (count + 1) & 0xff;
		count2 = (count2 + 1) & 0xffff;

		/*
		 * int s = count / 16; for (int offY = 0; offY < 256; offY += 32) for (int offX
		 * = 0; offX < 256; offX += 32) { canvas.drawImage(Point.point(offX + s, offY +
		 * s), image.crop(offX, offY, 32, 32).crop(s, s, 32 - s * 2, 32 - s * 2)); }
		 */
		for (DemoSprite sprite : sprites) {
			sprite.step();
			screen.modelMatrix.translation((float) sprite.x(), (float) sprite.y(), 0f)
					.rotateZ((float) (sprite.bearing().radians()));
			screen.uModel.set(screen.modelMatrix);
			sprite.draw(canvas);
			screen.uModel.set(screen.modelMatrix.identity());
		}

		if (false) {

			for (int y = 0; y < image2.height(); y++) {
				turtle.jumpTo(300, 0 + y);
				turtle.bearing(Direction.DUE_EAST);
				for (int x = 0; x < image2.width(); x++) {
					Color p = image2.readPixel(x, y);
					turtle.penColor(p);
					turtle.draw(1);
				}
			}
		}

//		canvas.drawImage(Point.point(600, 0), image.transpose(Image.Transpose.FLIP_TOP_BOTTOM));
//		canvas.drawImage(Point.point(900, 0),
//				image.transpose(Image.Transpose.FLIP_TOP_BOTTOM).transpose(Image.Transpose.FLIP_LEFT_RIGHT));
//		canvas.drawImage(Point.point(0, 300), image);
//		canvas.drawImage(Point.point(300, 300), image.transpose(Image.Transpose.ROTATE_90));
//		canvas.drawImage(Point.point(600, 300), image.transpose(Image.Transpose.ROTATE_180));
//		canvas.drawImage(Point.point(900, 300), image.transpose(Image.Transpose.ROTATE_270));
//		canvas.drawImage(Point.point(0, 600), image);
//		Image img = image.transpose(Image.Transpose.ROTATE_90);
//		canvas.drawImage(Point.point(300, 600), img);
//		img = img.transpose(Image.Transpose.ROTATE_90);
//		canvas.drawImage(Point.point(600, 600), img);
//		img = img.transpose(Image.Transpose.ROTATE_90);
//		canvas.drawImage(Point.point(900, 600), img);

		if (true) {
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
		}
		grid.locationParallelStream().forEach((l) -> {
			tileMap[l.getX() + l.getY() * GRID_COLS] = grid.get(l).background;
		});
//		((GLLayer) canvas).drawTileMap(Point.point(0, 0), 48, 32, 32, 32, tileMap, tiles);

//		System.out.println(deltaTime);

	}

	@Override
	public void smallStep(double deltaTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Screen screen) {
		System.out.println(ImageFactory.get("turtleduck.gl"));
		this.screen = (GLScreen) screen;
		canvas = screen.createCanvas();
		pen = canvas.createPen();
		turtle = canvas.createTurtle();
		System.out.println(turtle);

		System.out.println(Color.fromARGB(0xffffffff));
		System.out.println(Color.fromARGB(0xff7f7f7f));


		try {
			image2 = new GLPixelData("/yeti-juno.jpg");
			System.out.println(image2);
			image = new GLPixelData("file:///tmp/tiled.png");
			tiles = image.tiles(8, 8);
			for (int row = 0, y = 0; row < tiles.rows(); row++, y += 32) {
				Direction b = Direction.absolute(-20 + 180);
				for (int col = 0, x = 0; col < tiles.columns(); col++, x += 32) {
					Image img = tiles.get(col, row);
					sprites.add(new DemoSprite(Point.point(500 + x, 200 + y), b, 1, (col - 3.5) / (1 + row), img));
					b = b.add(Direction.relative(5));
				}
			}
			tileMap = new int[48 * 32];
			for (int j = 0; j < 32; j++) {
				for (int i = 0; i < 48; i++) {
					tileMap[i + j * 48] = 180 + j;
				}
			}

//			img = img.crop(50, 50, 200, 200);
			System.out.println(image);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GLLauncher().app(new Demo()).launch(args);
	}

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
			Turtle sub = turtle.child().turn(90);
			sub.penChange().strokeWidth(5).strokePaint(ink).done();
			sub.draw(1);
		ink = 	Colors.WHITE; //ink.brighter().brighter().brighter().brighter().brighter();
			sub.penChange().strokeWidth(1).strokePaint(ink).done();
			sub.draw(radius);
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
					double a = turtle.bearing().degrees();
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

	private void circle(Turtle turtle, double radius, int segments) {
		double s = 360.0 / segments;
		turtle.jump(radius).turn(90 + s / 2);
		for (int i = 0; i < segments; i++) {
			turtle.draw(2 * radius * Math.sin(Math.PI / segments));
			turtle.turn(s);
		}
	}
	
	private void fern(Turtle turtle, double h, int n) {
		fernX(turtle, h, n);
	}
	
	private void fernX(Turtle turtle, double h, int n) {
		double factor = 1;
		if(n <= 0)
			return;
		turtle.draw(h*n);
		turtle.turn(-25);
		Turtle ch = turtle.child();
		fernX(ch.child(), h*factor, n-1);
		fernX(ch.turn(-25), h*factor, n-1);
		turtle.turn(25);
		turtle.draw(h*n);
		ch = turtle.child();
		ch.turn(25).draw(h*n);
		fernX(ch, h*factor, n-1);
		turtle.turn(-25);
		fernX(turtle, h*factor, n-1);
	}
	
	private void fern2(Turtle turtle, double h, int n) {
		if(n <= 0)
			return;
		else if(n == 1)
			turtle.draw(h*n*2);
		else {
		turtle.penWidth(n);
		turtle.draw(h*n);
		fern2(turtle.child().turn(n*5), h, Math.max(1,n-2));
		turtle.turn(1).draw(h*n);
		fern2(turtle.child().turn(-n*5), h, Math.max(1, n-2));
		turtle.turn(1).draw(h*n);
		fern2(turtle, h, n-1);
		}
	}
	private void tree(Turtle turtle, double h, int n) {
		double growth = Math.min(1, count2/8192.0);
		double stage = n - (1-growth) * 10;
		if(stage <= 0)
			return;
		
		turtle.penColor(Color.color(.4, 1, 0).mix(Color.color(.4, .25, 0), stage/3));
		if(n <= 1)
			turtle.penColor(Colors.RED).draw(Math.min(stage, 1)*5);
		else {
			double a = Math.sin(2*Math.PI*count/256.0) / n;
			turtle.turn(a);
			turtle.draw(2*h*Math.min(stage/2, 1)/3);
			turtle.penWidth(2*n*Math.min(stage/2, 1));
			turtle.draw(2*h*Math.min(stage/2, 1)*2/3);
//			turtle.penWidth(2*turtle.pen().strokeWidth()*.75);
			
			tree(turtle.child().turn(30), h*.75, n-1);
			tree(turtle.child().turn(-20), h*.75, n-1);
		}
	}

	private void wheel1(Turtle turtle) {
		double hubRadius = 24;
		double wheelRadius = 250;
		double rimRadius = 55;
		int segments = 24;
		double step = 360 / segments;
		turtle.penWidth(2);

		circle(turtle.child().penWidth(10), hubRadius, segments);
		circle(turtle.child().penWidth(10), wheelRadius, segments);

		for (int i = 0; i < 6; i++) {
			Turtle t = turtle.child().penWidth(10);
			t.jump(hubRadius).draw(wheelRadius - hubRadius);
			turtle.turn(60);
		}

		turtle.penWidth(5);
		for (int i = 0; i < 360; i += 15) {
			turtle.child().jump(wheelRadius).draw(Direction.absolute(-45), 2 * rimRadius).turn(97.5).draw(50).done()
					.turn(15);
		}
//		circle(turtle.child().turnTo(-30).jump(2*rimRadius).penWidth(20), wheelRadius, segments);

	}

}
