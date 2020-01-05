package turtleduck.jfx.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import turtleduck.geometry.Point;

public class PointList {
//	private static Map<Integer, List<double[]>> arrays = new HashMap<>();
	private static int ARRAY_SIZE = 4, CACHE_SIZE = 512;
	private static double[][] smallArrays = new double[CACHE_SIZE][ARRAY_SIZE];
	private static int smallArraysNext = CACHE_SIZE;
	private static Map<Integer, Integer> sizes = new HashMap<>();
	private static long maxSize = 0, minSize = Integer.MAX_VALUE;
	private static long totSize = 0, totN = 0, nReuse = 0, nAlloc = CACHE_SIZE, maxAvail = 0;
	double xs[];
	double ys[];
	int nPoints = 0;

	public PointList() {
		if(smallArraysNext > 1) {
			xs = smallArrays[--smallArraysNext];
			ys = smallArrays[--smallArraysNext];
			nReuse++;
		} else {
			xs = new double[ARRAY_SIZE];
			ys = new double[ARRAY_SIZE];
			nAlloc++;
		}
	}
/*
	public PointList(int initialCapacity) {
		arrays(initialCapacity);
	}
*/
	public void add(Point p) {
		if (nPoints >= xs.length) {
			int newCapacity = (xs.length * 3 / 2) & 0xfffffffe;
			if(xs.length == ARRAY_SIZE && smallArraysNext < CACHE_SIZE-1) { // TODO: not thread safe
				smallArrays[smallArraysNext++] = xs;
				smallArrays[smallArraysNext++] = ys;
			}
			xs = Arrays.copyOf(xs, newCapacity);
			ys = Arrays.copyOf(ys, newCapacity);
//			arrays(newCapacity);
		}
		xs[nPoints] = p.x();
		ys[nPoints] = p.y();
		nPoints++;
	}

	public int size() {
		return nPoints;
	}

	public void clear() {
		maxSize = Math.max(maxSize, nPoints);
		minSize = Math.min(minSize, nPoints);
		totSize += nPoints;
		totN++;
		sizes.put(nPoints, sizes.getOrDefault(nPoints, 0) + 1);
		if(xs.length == ARRAY_SIZE && smallArraysNext < CACHE_SIZE-1) { // TODO: not thread safe
			smallArrays[smallArraysNext++] = xs;
			smallArrays[smallArraysNext++] = ys;
		}
		maxAvail += smallArraysNext;
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
		for (int i = 0; i < nPoints; i++) {
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

	public void dispose() {
		clear();
		xs = null;
		ys = null;
	}
	


	public static void printStats() {
		System.err.println("PointList: ");
		System.err.println("  Array pairs allocated: " + nAlloc);
		System.err.println("  Array pairs reused:    " + nReuse);
		System.err.println("  Avg available:    " + ((double)maxAvail)/totN);
		System.err.println("  Max list size:    " + maxSize);
		System.err.println("  Max list size:    " + maxSize);
		System.err.println("  Min list size:    " + minSize);
		System.err.println("  Avg list size:    " + ((double)totSize/totN));
		System.err.println("  Sizes: " + sizes.toString());
	}
}
