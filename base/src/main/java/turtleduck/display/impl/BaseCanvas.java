package turtleduck.display.impl;

import turtleduck.display.Canvas;
import turtleduck.display.Screen;
import turtleduck.turtle.Pen;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleDuckImpl;

public abstract class BaseCanvas<S extends Screen> extends BaseLayer<S> implements Canvas {
	private int nTurtles = 0;
	
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
		return new TurtleDuckImpl(tId, this);
	}

	public TurtleDuck createTurtleDuck() {
		String tId = id + "." + nTurtles++;
		return new TurtleDuckImpl(tId, this);
	}

}
