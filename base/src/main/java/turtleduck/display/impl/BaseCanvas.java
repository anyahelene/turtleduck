package turtleduck.display.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Direction3;
import turtleduck.geometry.Point;
import turtleduck.geometry.impl.Point3;
import turtleduck.turtle.Chelonian;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.Turtle;
import turtleduck.turtle.Turtle3;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleImpl;
import turtleduck.turtle.impl.TurtleImpl3;

public abstract class BaseCanvas<S extends Screen> extends BaseLayer<S> implements Canvas {
	private int nTurtles = 0;
	private List<Chelonian<?, ?>> turtles = new ArrayList<>();

	public BaseCanvas(String layerId, S screen, double width, double height) {
		super(layerId, screen, width, height);
	}

	@Override
	public String id() {
		return id;
	}

	public Pen createPen() {
		return new BasePen();
	}

	protected abstract PathWriter pathWriter(boolean use3d);

	public Turtle createTurtle() {
		String tId = id + "." + nTurtles++;
		Turtle t = new TurtleImpl.SpecificTurtle(Point.ZERO, Direction.DUE_NORTH, createPen());
		t.writePathsTo(pathWriter(false));
		turtles.add(t);
		return t;
	}

	public Turtle3 createTurtle3() {
		String tId = id + "." + nTurtles++;
		Turtle3 t = new TurtleImpl3.SpecificTurtle3(Point3.ZERO, Direction3.DUE_NORTH, createPen());
		t.writePathsTo(pathWriter(true));
		turtles.add(t);
		return t;
	}

	@Override
	public Canvas flush() {
		for (Chelonian<?, ?> t : turtles) {
			t.jump(0);
		}
		return this;
	}

	protected abstract void drawLine(Stroke stroke, Point from, Point to);

}
