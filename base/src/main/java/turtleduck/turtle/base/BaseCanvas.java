package turtleduck.turtle.base;

import turtleduck.turtle.Canvas;
import turtleduck.turtle.Pen;
import turtleduck.turtle.SimpleTurtle;
import turtleduck.turtle.TurtleDuck;
import turtleduck.turtle.impl.BasePen;
import turtleduck.turtle.impl.TurtleDuckImpl;

public abstract class BaseCanvas implements Canvas {
	private final String id;
	private int nTurtles;
	
	public BaseCanvas(String id) {
		this.id = id;
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
		return new TurtleDuckImpl(tId, this, createControl());
	}

	public TurtleDuck createTurtleDuck() {
		String tId = id + "." + nTurtles++;
		return new TurtleDuckImpl(tId, this, createControl());
	}

}
