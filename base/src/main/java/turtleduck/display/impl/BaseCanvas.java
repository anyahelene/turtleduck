package turtleduck.display.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
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

	public Turtle createTurtle() {
		String tId = id + "." + nTurtles++;
		Turtle t = new TurtleImpl.SpecificTurtle(Point.ZERO, Bearing.DUE_NORTH, createPen());
		t.onDraw((from, to) -> {
			drawLine(from.pen(), from.point(), to.point());
		});
		turtles.add(t);
		return t;
	}
	
	@Override
	public Canvas flush() {
		for(Turtle t : turtles) {
			t.jump(0);
		}
		return this;
	}

	protected abstract void drawLine(Stroke stroke, Point from, Point to);

}
