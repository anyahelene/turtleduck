package turtleduck.turtle.impl;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.paths.Pen;
import turtleduck.turtle.BaseTurtle3;
import turtleduck.turtle.Turtle3;

public class TurtleImpl3<THIS extends BaseTurtle3<THIS, RESULT>, RESULT> extends TurtleImpl<THIS, RESULT>
		implements BaseTurtle3<THIS, RESULT> {
	public static class SpecificTurtle3 extends TurtleImpl3<Turtle3, Turtle3> implements Turtle3 {

		public SpecificTurtle3(Point p, Direction b, Pen pen, Canvas canvas) {
			super(p, b, pen, canvas);
		}

		public SpecificTurtle3(SpecificTurtle3 parent) {
			super(parent);
		}

		protected SpecificTurtle3 copy() {
			return new SpecificTurtle3(this);
		}
	}

	public TurtleImpl3(Point p, Direction b, Pen pen, Canvas canvas) {
		super(p, b, pen, canvas);
	}

	public TurtleImpl3(TurtleImpl<THIS, RESULT> parent) {
		super(parent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS pitch(double angle) {
		direction = direction.pitch(angle);
		return (THIS) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public THIS roll(double angle) {
		direction = direction.roll(angle);
		return (THIS) this;
	}

}
