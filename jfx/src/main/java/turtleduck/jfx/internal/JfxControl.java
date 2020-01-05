package turtleduck.jfx.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.jfx.JfxCanvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;

public class JfxControl implements TurtleControl {
	private static final int CACHE_SIZE = 4096;
	private static long nSegments = 0, nAlloc = 0, nReused = 0, nInUse = 0, nDraw = 0, nSkipped = 0, nPartial = 0,
			nComplete = 0;
	private static long nChildren, nChildSegments = 0;
	private static long nStrokeEdges = 0, nStrokes = 0, nStrokeBreaks = 0;
	private static long nFills = 0, nFillEdges = 0, nFillBreaks = 0;
	private static long totQueueSize = 0, nQueueSize = 0;
	private final JfxCanvas canvas;
	private StrokeSegment strokeSeg;
	private FillSegment fillSeg;
	private boolean validation = true;
	private final JfxControl parent;
	private final List<Segment> segs;
	private static final List<FillSegment> fillSegs = new ArrayList<>();
	private static final List<StrokeSegment> strokeSegs = new ArrayList<>();
	private Fill fill;
	private Stroke stroke;

	public JfxControl(JfxCanvas canvas, JfxControl parent) {
		this.canvas = canvas;
		this.parent = parent;
		if (parent != null) {
			validation = parent.validation;
			segs = parent.segs;
			nChildren++;
		} else {
			segs = new ArrayList<>();
		}
	}

	protected static abstract class Segment {
		JfxControl owner;
		PointList points = new PointList();
		boolean closed = false, complete = false;
		int done = 0;

		public Segment() {
			JfxControl.nAlloc++;
		}

		public void init(JfxControl o) {
			JfxControl.nSegments++;
			if (owner == null && o.parent != null)
				JfxControl.nChildSegments++;
			owner = o;
		}

		public void clear() {
			owner = null;
			done = 0;
			closed = false;
			complete = false;
			points.clear();
		}

		public void draw(JfxCanvas canvas) {
			if (points.nPoints > done) {
				drawIt(canvas);
				nDraw++;
				if (complete) {
					nComplete++;

//					points.dispose();
				} else {
					nPartial++;
				}
			} else {
				nSkipped++;
			}
		}

		protected abstract void drawIt(JfxCanvas canvas);

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			toString(builder);
			return builder.toString();
		}

		public void toString(StringBuilder builder) {
			builder.append(points);
		}
	}

	protected static class StrokeSegment extends Segment {
		Stroke stroke;

		public void init(JfxControl o, Stroke s) {
			super.init(o);
			stroke = s;
		}

		public void toString(StringBuilder builder) {
			builder.append(stroke);
			super.toString(builder);
		}

		public void drawIt(JfxCanvas canvas) {
			if (closed && complete) {
				canvas.strokePolygon(stroke, null, points);
			} else {
				canvas.strokePolyline(stroke, null, points);
			}
			done = points.nPoints;
		}
	}

	protected static class FillSegment extends Segment {
		Fill fill;

		public void init(JfxControl o, Fill f) {
			super.init(o);
			fill = f;
		}

		public void toString(StringBuilder builder) {
			builder.append(fill);
			super.toString(builder);
		}

		public void drawIt(JfxCanvas canvas) {
			if (complete) {
				canvas.fillPolygon(fill, null, points);
				done = points.nPoints;
			}
		}
	}

	@Override
	public boolean validationMode() {
		return validation;
	}

	@Override
	public TurtleControl validationMode(boolean enable) {
		validation = enable;
		return this;
	}

	@Override
	public TurtleControl begin(Stroke s, Fill f) {
		if (strokeSeg != null || fillSeg != null) {
			if (validation)
				throw new IllegalStateException("begin without end");
			end();
		}
		fill = f;
		stroke = s;
		if (f != null) {
			nFills++;
		}
		if (s != null) {
			nStrokes++;
		}

		return this;
	}

	@Override
	public TurtleControl pen(Stroke s, Fill f) {
		if (f != null) {
			if (fill == null)
				throw new IllegalStateException("Setting fill pen, but not currently filling");
			if (fillSeg != null && fillSeg.points.nPoints > 0 && !f.equals(fillSeg.fill)) {
				fillSeg.complete = true;
				nFillBreaks++;
				fillSeg.draw(canvas);
				freeFillSegment();
			}
			fill = f;
		}
		if (s != null) {
			if (stroke == null)
				throw new IllegalStateException("Setting stroke pen, but not currently stroking");
			if (strokeSeg != null && strokeSeg.points.nPoints > 0 && !s.equals(strokeSeg.stroke)) {
				strokeSeg.complete = true;
				nStrokeBreaks++;
				strokeSeg.draw(canvas);
				freeStrokeSegment();
			}
			stroke = s;
		}

		return this;
	}

	@Override
	public TurtleControl turn(Bearing from, double degrees, Bearing to) {
		return this;
	}

	protected StrokeSegment strokeSegment(Point from) {
		if (strokeSeg == null) {
			if (!strokeSegs.isEmpty()) {
				strokeSeg = strokeSegs.remove(strokeSegs.size() - 1);
				nReused++;
			} else {
				strokeSeg = new StrokeSegment();
			}
			strokeSeg.init(this, stroke);
			strokeSeg.points.add(from);
			segs.add(strokeSeg);
			nInUse++;
		} else if (strokeSeg.done == strokeSeg.points.nPoints) { // all points drawn
			segs.add(strokeSeg);
			nInUse++;
		}
		return strokeSeg;
	}

	protected FillSegment fillSegment(Point from) {
		if (fillSeg == null) {
			if (!fillSegs.isEmpty()) {
				fillSeg = fillSegs.remove(fillSegs.size() - 1);
				nReused++;
			} else {
				fillSeg = new FillSegment();
			}
			fillSeg.init(this, fill);
			fillSeg.points.add(from);
			segs.add(fillSeg);
			nInUse++;
		} else if (fillSeg.done == fillSeg.points.nPoints) { // all points drawn
			segs.add(fillSeg);
			nInUse++;
		}
		return fillSeg;
	}

	protected void freeStrokeSegment() {
		if (strokeSeg != null) {
			segs.remove(strokeSeg);
			nInUse--;
			if (strokeSegs.size() < CACHE_SIZE) {
				strokeSeg.clear();
				strokeSegs.add(strokeSeg);
			} else {
				strokeSeg.points.dispose();
			}
			strokeSeg = null;
		}
	}

	protected void freeFillSegment() {
		if (fillSeg != null) {
			segs.remove(fillSeg);
			nInUse--;
			if (strokeSegs.size() < CACHE_SIZE) {
				fillSeg.clear();
				fillSegs.add(fillSeg);
			} else {
				fillSeg.points.dispose();
			}
			fillSeg = null;
		}
	}

	@Override
	public TurtleControl go(Bearing bearing, Point from, double distance, Point to) {
		if (stroke != null) {
			if (validation && strokeSeg != null && strokeSeg.complete)
				throw new IllegalStateException("adding more points to complete segment");
			strokeSegment(from).points.add(to);
			nStrokeEdges++;

		}
		if (fill != null) {
			if (validation && fillSeg != null && fillSeg.complete)
				throw new IllegalStateException("adding more points to complete segment");
			fillSegment(from).points.add(to);
			nFillEdges++;
		}
		return this;
	}

	@Override
	public TurtleControl control(Bearing heading, Point from, double distance, Point to) {
		return this;
	}

	@Override
	public TurtleControl cancel() {
		freeFillSegment();
		freeStrokeSegment();
		fill = null;
		stroke = null;
		segs.removeIf((seg) -> seg.owner == this);
		return this;
	}

	@Override
	public TurtleControl end() {
		if (fillSeg != null) {
			fillSeg.complete = true;
			fillSeg.draw(canvas);
			freeFillSegment();
		}
		if (strokeSeg != null) {
			strokeSeg.complete = true;
			strokeSeg.draw(canvas);
			freeStrokeSegment();
		}
		fill = null;
		stroke = null;
		return this;
	}

	long steps = 0;

	public TurtleControl flush() {
		totQueueSize += segs.size();
		nQueueSize++;
//		if (steps++ % 100 < 30)
//			System.err.println("  Avg queue:   " + ((double) totQueueSize / nQueueSize));
		ListIterator<Segment> li = segs.listIterator();
		while (li.hasNext()) {
			Segment seg = li.next();
			seg.draw(canvas);
//			if (seg.complete)
			li.remove();
			nInUse--;
		}
		return this;
	}

	@Override
	public TurtleControl child() {
		JfxControl j = new JfxControl(canvas, this);

		return j;
	}

	public static void printStats() {
		System.err.println("Canvas segments: ");
		System.err.println("  Avg queue:   " + ((double) totQueueSize / nQueueSize));
		System.err.println("  Draw ops:    " + nDraw);
		System.err.println("    Complete:  " + nComplete);
		System.err.println("    Partial:   " + nPartial);
		System.err.println("    Skipped:   " + nSkipped);
		System.err.println("  Segments:    " + nSegments);
		System.err.println("    Alloc:     " + nAlloc);
		System.err.println("    Reused:    " + nReused);
		System.err.println("    Top-Level: " + (nSegments - nChildSegments));
		System.err.println("    Free:      " + (strokeSegs.size() + fillSegs.size()));
		System.err.println("    In use:    " + nInUse);
		System.err.println("  Children:    " + nChildren);
		System.err.println("    Segments:  " + nChildSegments);
		System.err.println("  Strokes:     " + nStrokes);
		System.err.println("    Breaks:    " + nStrokeBreaks);
		System.err.println("    Edges:     " + nStrokeEdges);
		System.err.println("  Fills:       " + nFills);
		System.err.println("    Breaks:    " + nFillBreaks);
		System.err.println("    Edges:     " + nFillEdges);
	}
}
