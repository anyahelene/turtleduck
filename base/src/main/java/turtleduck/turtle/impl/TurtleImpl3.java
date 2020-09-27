package turtleduck.turtle.impl;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Direction3;
import turtleduck.geometry.Point;
import turtleduck.turtle.Chelonian3;
import turtleduck.turtle.Pen;
import turtleduck.turtle.Turtle3;

public class TurtleImpl3<THIS extends Chelonian3<THIS, RESULT>, RESULT> extends TurtleImpl<THIS, RESULT>
		implements Chelonian3<THIS, RESULT> {
	public static class SpecificTurtle3 extends TurtleImpl3<Turtle3, Turtle3> implements Turtle3 {

		public SpecificTurtle3(Point p, Direction b, Pen pen) {
			super(p, b, pen);
		}

		public SpecificTurtle3(SpecificTurtle3 parent) {
			super(parent);
		}

		protected SpecificTurtle3 copy() {
			return new SpecificTurtle3(this);
		}
	}

	public TurtleImpl3(Point p, Direction b, Pen pen) {
		super(p, b, pen);
	}

	public TurtleImpl3(TurtleImpl<THIS, RESULT> parent) {
		super(parent);
	}

	@Override
	public THIS pitch(double angle) {
		current.bearing = current.bearing.pitch(angle);
		return (THIS)this;
	}
	@Override
	public THIS roll(double angle) {
		current.bearing = current.bearing.roll(angle);
		return (THIS)this;
	}

}
