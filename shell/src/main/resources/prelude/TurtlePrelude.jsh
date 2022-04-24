/namespace meta
import java.util.Random;
import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.geometry.Direction;
import turtleduck.canvas.Canvas;
import turtleduck.turtle.Turtle;
import turtleduck.paths.Pen;
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

Class<?>[] $CLASSES = {Screen.class, Canvas.class, Turtle.class, TextCursor.class, Color.class, Colors.class};
Meta $META = Meta.create();
Random $RANDOM = new Random();

String help = "Type /help for help :)";

/namespace startup

Screen screen = turtleduck.objects.IdentifiedObject.Registry.findObject(Screen.class, $SCREEN_ID);
Canvas canvas = screen.createCanvas().stroke(Colors.WHITE, 1).fill(Colors.TRANSPARENT).background(Colors.BLACK);
TextCursor cursor = turtleduck.objects.IdentifiedObject.Registry.findObject(TextCursor.class, $CURSOR_ID);

Turtle turtle = canvas.turtle().color(Colors.WHITE).jumpTo(0, 0);
static {
turtleduck.shell.TShell.testValue = 5;
}

double sin(double deg) {
    return Math.sin(Math.toRadians(deg));
}
double cos(double deg) {
    return Math.cos(Math.toRadians(deg));
}

double sqrt(double n) {
	return Math.sqrt(n);
}

double perturb(double n) {
	return perturb(n, 0.1);
}

double rand() {
	return $RANDOM.nextDouble();
}
/** Return a random integer, with (approximately) uniform probability 
    @return A pseudorandom integer, in the range -2<sup>31</sup> – +2<sup>31</sup>-1 (inclusive)
    @see java.util.random#nextInt()
*/
int randInt() {
	return $RANDOM.nextInt();
}

/namespace meta

double perturb(double n, double factor) {
	return n + n * (2 * $RANDOM.nextDouble() - 0.5) * factor;
}


double rand(double to) {
	return to * $RANDOM.nextDouble();
}
double rand(double from, double to) {
	return from + (to - from) * $RANDOM.nextDouble();
}


/** Return a random integer
    @return A pseudorandom integer, in the range 0 (inclusive) to <code>to</code> (exclusive)
    @see java.util.random#nextInt(int)
*/
int randInt(int to) {
	return $RANDOM.nextInt(to);
}

/** Return a random integer
    @return A pseudorandom integer, in the range <code>from</code> (inclusive) to <code>to</code> (exclusive)
    @see java.util.random#nextInt(int)
*/
int randInt(int from, int to) {
	return from + $RANDOM.nextInt(to-from);
}

/namespace startup

void clear() {
	screen.clear();
}

void reset() {
	canvas = screen.createCanvas().stroke(Colors.WHITE, 1).fill(Colors.TRANSPARENT).background(Colors.BLACK);
	turtle = canvas.turtle().color(Colors.WHITE).jumpTo(0, 0).turnTo(0);
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
