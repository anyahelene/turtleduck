package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.paths.Pen;
import turtleduck.turtle.impl.TurtleImpl;

@Icon("🐢")
public interface Turtle extends BaseTurtle<Turtle, Turtle> {
    static Turtle create() {
        return new TurtleImpl.SpecificTurtle(null, null, null, null);
    }
    static Turtle create(Point point, Direction dir, Pen pen) {
        return new TurtleImpl.SpecificTurtle(point, dir, pen, null);
    }

}