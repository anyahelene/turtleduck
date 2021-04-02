/namespace meta

import turtleduck.display.Screen;
import turtleduck.geometry.Point
import turtleduck.geometry.Direction;
import turtleduck.canvas.Canvas;
import turtleduck.turtle.Turtle;
import turtleduck.turtle.Pen;
import turtleduck.text.TextCursor;
import turtleduck.colors.Color;
import turtleduck.colors.Colors;
import turtleduck.shell.TShell;
import turtleduck.util.Meta;
import static turtleduck.geometry.Point.point;
import static turtleduck.colors.Colors.*;
import static turtleduck.geometry.Direction.*;
import turtleduck.turtle.SpriteBuilder;
import turtleduck.sprites.Sprite;


Class<?>[] $CLASSES = {Screen.class, Canvas.class, Turtle.class, Color.class, Colors.class};
Meta $META = Meta.create();

String help = "Type /help for help :)";

/namespace startup

Screen screen = turtleduck.objects.IdentifiedObject.Registry.findObject(Screen.class, $SCREEN_ID)
Canvas canvas = screen.createCanvas().strokePaint(Colors.BLACK);

Turtle turtle = canvas.turtle().penColor(Colors.BLACK).jumpTo(0, 0);
turtleduck.shell.TShell.testValue = 5;

double sin(double deg) {
    return Math.sin(Math.toRadians(deg));
}
double cos(double deg) {
    return Math.cos(Math.toRadians(deg));
}

void clear() {
	screen.clear();
}

void reset() {
	turtle.penColor(Colors.BLACK).penWidth(1).fillColor(Colors.TRANSPARENT).jumpTo(0, 0).turnTo(0);
	screen.clear();
}

void head(SpriteBuilder sb) {
	sb.spawn().turn(-170).jump(2.5).turn(90).draw(5)
	.turn(30).draw(10).turn(45).draw(5)
	.turn(60).draw(5).turn(45).draw(10)
	.turn(30).draw(5).done();
}
void foot(SpriteBuilder sb) {
    sb.spawn().turn(-120).draw(5).turn(45).draw(7)
	.turn(45).draw(5).turn(45).draw(3)
	.turn(45).draw(5).turn(45).draw(7)
	.turn(45).draw(5).done();
}
Sprite turtle() {
    SpriteBuilder sb = turtle.sprite();
	sb.jump(50);
	for(int i = 0; i < 12; i++) {
		sb.turn(30).draw(10 + (i==3||i==9 ?5 : 0));
		if(i==1||i==3||i==7|i==9) {
			foot(sb);
		}
		if(i==5) {
			head(sb);
		}
	}
	return sb.done();
}
