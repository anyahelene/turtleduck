package turtleduck.display.impl;

import java.util.ArrayList;
import java.util.List;

import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.turtle.Pen;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleDuckImpl;

public abstract class BaseCanvas<S extends Screen> extends BaseLayer<S> implements Canvas {
	private int nTurtles = 0;
	private List<SimpleTurtle> turtles = new ArrayList<>();
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

	public SimpleTurtle createSimpleTurtle() {
		String tId = id + "." + nTurtles++;
		TurtleDuckImpl t = new TurtleDuckImpl(tId, this);
		turtles.add(t);
		return t;
	}

	public TurtleDuck createTurtleDuck() {
		String tId = id + "." + nTurtles++;
		TurtleDuckImpl t = new TurtleDuckImpl(tId, this);
		turtles.add(t);
		return t;
	}
	
	@Override
	public Canvas flush() {
		for(SimpleTurtle t : turtles) {
			t.move(0);
		}
		return this;
	}


}
