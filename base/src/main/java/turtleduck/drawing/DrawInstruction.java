package turtleduck.drawing;

import java.util.Iterator;
import java.util.stream.Stream;

import turtleduck.geometry.Point;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Stroke;

public interface DrawInstruction {
	enum Instruction { DOT, LINE, POLYGON, TRIANGLES, ELLIPSE, RECTANGLE, QUADRATIC, CUBIC };
	Instruction instruction();
	Stroke stroke();
	Fill fill();
	int n();
	double x(int i);
	double y(int i);
	Iterable<Point> points();
	Stream<Point> pointStream();
}
