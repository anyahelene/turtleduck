package turtleduck.turtle;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.geometry.unused.Direction;

public class TurtleMark {
	Point point;
	Bearing dir;
	String name;
	
	public TurtleMark(Point p, Bearing bearing, String n) {
		point = p;
		dir = bearing;
		name = n;
	}
	
	public Point getPoint() {
		return point;
	}
	
	public Bearing getDirection() {
		return dir;
	}
	
	public TurtleMark turn(double degrees) {
		return new TurtleMark(point, dir.add(Bearing.relative(degrees)), name);
	}
	
	public TurtleMark move(double distance) {
		return new TurtleMark(point.add(dir, distance), dir, name);
	}
	
	public TurtleMark rename(String name) {
		return new TurtleMark(point, dir, name);
	}
	
	public String getName() {
		return name;
	}
}
