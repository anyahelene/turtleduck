package turtleduck.turtle;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;

public class TurtleMark {
	Point point;
	Direction dir;
	String name;
	
	public TurtleMark(Point p, Direction d, String n) {
		point = p;
		dir = d;
		name = n;
	}
	
	public Point getPoint() {
		return point;
	}
	
	public Direction getDirection() {
		return dir;
	}
	
	public TurtleMark turn(double degrees) {
		return new TurtleMark(point, dir.turn(degrees), name);
	}
	
	public TurtleMark move(double distance) {
		return new TurtleMark(point.move(dir, distance), dir, name);
	}
	
	public TurtleMark rename(String name) {
		return new TurtleMark(point, dir, name);
	}
	
	public String getName() {
		return name;
	}
}
