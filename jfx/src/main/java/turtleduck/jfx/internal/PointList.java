package turtleduck.jfx.internal;

import java.util.Arrays;

import turtleduck.geometry.Point;

public class PointList {
	double xs[];
	double ys[];
	int nPoints = 0;
	
	public PointList() {
		this(16);
	}
	
	public PointList(int initialCapacity) {
		xs = new double[initialCapacity];
		ys = new double[initialCapacity];
	}
	
	public void add(Point p) {
		if(nPoints >= xs.length) {
			int newCapacity = xs.length * 3 / 2;
			xs = Arrays.copyOf(xs, newCapacity);
			ys = Arrays.copyOf(ys, newCapacity);
		}
		xs[nPoints] = p.x();
		ys[nPoints] = p.y();
		nPoints++;
	}
	
	public int size() {
		return nPoints;
	}
	
	public void clear() {
		nPoints = 0;
	}
	
	public double[] xs() {
		return xs;
	}
	
	public double[] ys() {
		return ys;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		String sep = "";
		for(int i = 0; i < nPoints; i++) {
			sb.append(sep);
			sb.append("(");
			sb.append(xs[i]);
			sb.append(",");
			sb.append(ys[i]);
			sb.append(")");
			sep = ", ";
		}
		sb.append("]");
		return sb.toString();
	}
}
