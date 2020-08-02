package turtleduck.display.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.turtle.PathWriter;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.Turtle;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleImpl;

public abstract class BaseCanvas<S extends Screen> extends BaseLayer<S> implements Canvas {
	private int nTurtles = 0;
	private List<Turtle> turtles = new ArrayList<>();

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

	protected abstract PathWriter pathWriter();

	public Turtle createTurtle() {
		String tId = id + "." + nTurtles++;
		Turtle t = new TurtleImpl.SpecificTurtle(Point.ZERO, Direction.DUE_NORTH, createPen());
		t.writePathsTo(pathWriter());
		turtles.add(t);
		return t;
	}

	@Override
	public Canvas flush() {
		for (Turtle t : turtles) {
			t.jump(0);
		}
		return this;
	}

	protected abstract void drawLine(Stroke stroke, Point from, Point to);

}
