package turtleduck.jfx.internal;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.jfx.JfxCanvas;
import turtleduck.jfx.internal.JfxControl.StrokeSegment;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Stroke;
import turtleduck.turtle.TurtleControl;

public class JfxControl implements TurtleControl {
	protected static class FillSegment extends PolySegment {
		Fill fill;

		public void drawIt(JfxCanvas canvas) {
			if (complete) {
				canvas.fillPolygon(fill, points);
				done = points.nPoints;
			}
		}

		public void init(JfxControl o, Fill f) {
			super.init(o);
			fill = f;
		}

		public void toString(StringBuilder builder) {
			builder.append(fill);
			super.toString(builder);
		}

		public synchronized FillSegment add(Point p) {
			points.add(p);
			return this;
		}
	}

	protected static interface Segment {
		void clear();

		boolean isComplete();

		void end();

		void draw(JfxCanvas canvas);

		boolean isOwnedBy(JfxControl obj);

		Segment add(Point p);
	}

	protected static interface StrokeSegment extends Segment {
		void stroke(Stroke s);

		StrokeSegment add(Point p);

		boolean isDone();

		Stroke stroke();
	}

	protected static class LineSegment implements StrokeSegment {
		Point from, to;
		Stroke stroke;
		boolean done = false, complete = false;

		@Override
		public synchronized boolean isDone() {
			return done;
		}

		@Override
		public synchronized boolean isComplete() {
			return complete;
		}

		public LineSegment(Stroke s, Point f, Point t) {
			nSimpleLines++;
			stroke = s;
			from = f;
			to = t;
		}

		public void stroke(Stroke s) {
			stroke = s;
		}

		@Override
		public void clear() {
			from = to = null;
		}

		@Override
		public void draw(JfxCanvas canvas) {
			if (from != null && to != null) {
				canvas.line(stroke, from, to);
				done = true;
			}

		}

		@Override
		public boolean isOwnedBy(JfxControl obj) {
			return false;
		}

		@Override
		public StrokeSegment add(Point p) {
			if (from == null)
				from = p;
			else if (to == null)
				to = p;
			else {
				StrokeSegment seg = SegmentCache.strokeSegment();
				seg.stroke(stroke);
				seg.add(from).add(to).add(p);
				nUpgradedLines++;
				return seg;
			}
			return this;
		}

		@Override
		public void end() {
			complete = true;
		}

		@Override
		public Stroke stroke() {
			return stroke;
		}

	}

	protected static abstract class PolySegment implements Segment {
		protected JfxControl owner;
		protected PointList points = new PointList();
		protected boolean closed = false, complete = false;
		protected int done = 0;

		@Override
		public synchronized boolean isComplete() {
			return complete;
		}

		public boolean isOwnedBy(JfxControl obj) {
			return owner == obj;
		}

		public PolySegment() {
			JfxControl.nAlloc++;
		}

		@Override
		public void end() {
			complete = true;
		}

		public synchronized boolean isDone() {
			return done == points.nPoints;
		}

		public synchronized Segment add(Point p) {
			points.add(p);
			return this;
		}

		public synchronized void clear() {
			owner = null;
			done = 0;
			closed = false;
			complete = false;
			points.clear();
		}

		public synchronized void draw(JfxCanvas canvas) {
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

		public synchronized void init(JfxControl o) {
			JfxControl.nSegments++;
			if (owner == null && o.parent != null)
				JfxControl.nChildSegments++;
			owner = o;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			toString(builder);
			return builder.toString();
		}

		public void toString(StringBuilder builder) {
			builder.append(points);
		}

		public void dispose() {
			points.dispose();
		}
	}

	protected static class PolyStrokeSegment extends PolySegment implements StrokeSegment {
		Stroke stroke;

		public void drawIt(JfxCanvas canvas) {
			if (closed && complete) {
				canvas.strokePolygon(stroke, points);
			} else {
				canvas.strokePolyline(stroke, points);
			}
			done = points.nPoints;
		}

		@Override
		public Stroke stroke() {
			return stroke;
		}

		public void init(JfxControl o, Stroke s) {
			super.init(o);
			stroke = s;
		}

		public synchronized StrokeSegment add(Point p) {
			points.add(p);
			return this;
		}

		public void toString(StringBuilder builder) {
			builder.append(stroke);
			super.toString(builder);
		}

		@Override
		public void stroke(Stroke s) {
			stroke = s;
		}
	}

	private static final int CACHE_SIZE = 4096;
	private static long nSegments = 0, nAlloc = 0, nReused = 0, nInUse = 0, nDraw = 0, nSkipped = 0, nPartial = 0,
			nComplete = 0;
	private static long nChildren, nChildSegments = 0;
	private static long nSimpleLines = 0, nUpgradedLines = 0, nStrokeEdges = 0, nStrokes = 0, nStrokeBreaks = 0;
	private static long nFills = 0, nFillEdges = 0, nFillBreaks = 0;
	private static long totQueueSize = 0, nQueueSize = 0;

	protected static class SegmentCache {
		private static final List<FillSegment> fillSegs = new ArrayList<>();
		private static final List<StrokeSegment> strokeSegs = new ArrayList<>();

		public static synchronized FillSegment fillSegment() {
			if (!fillSegs.isEmpty()) {
				nReused++;
				return fillSegs.remove(fillSegs.size() - 1);
			} else {
				return new FillSegment();
			}
		}

		public static synchronized StrokeSegment strokeSegment() {
			if (!strokeSegs.isEmpty()) {
				nReused++;
				return strokeSegs.remove(strokeSegs.size() - 1);
			} else {
				return new PolyStrokeSegment();
			}
		}

		public static synchronized void free(FillSegment seg) {
			if (fillSegs.size() < CACHE_SIZE) {
				seg.clear();
				fillSegs.add(seg);
			} else {
				seg.dispose();
			}
		}

		public static synchronized void free(StrokeSegment seg) {
			if (seg instanceof PolyStrokeSegment) {
				PolyStrokeSegment ps = (PolyStrokeSegment) seg;
				if (strokeSegs.size() < CACHE_SIZE) {
					ps.clear();
					strokeSegs.add(ps);
				} else {
					ps.dispose();
				}
			}
		}

		public static synchronized int numFree() {
			return strokeSegs.size() + fillSegs.size();
		}
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
		System.err.println("    Free:      " + SegmentCache.numFree());
		System.err.println("    In use:    " + nInUse);
		System.err.println("  Children:    " + nChildren);
		System.err.println("    Segments:  " + nChildSegments);
		System.err.println("  Strokes:     " + nStrokes);
		System.err.println("    Breaks:    " + nStrokeBreaks);
		System.err.println("    Edges:     " + nStrokeEdges);
		System.err.println("    Simple:    " + nSimpleLines);
		System.err.println("    Upgraded:  " + nUpgradedLines);
		System.err.println("  Fills:       " + nFills);
		System.err.println("    Breaks:    " + nFillBreaks);
		System.err.println("    Edges:     " + nFillEdges);
	}

	private final JfxCanvas canvas;
	private StrokeSegment strokeSeg;
	private FillSegment fillSeg;
	private boolean validation = true;

	private final JfxControl parent;

	private final BlockingQueue<Segment> segs;

	private Fill fill;

	private Stroke stroke;

	long steps = 0;

	public JfxControl(JfxCanvas canvas, JfxControl parent) {
		this.canvas = canvas;
		this.parent = parent;
		if (parent != null) {
			validation = parent.validation;
			segs = parent.segs;
			nChildren++;
		} else {
			segs = new ArrayBlockingQueue<>(16384);
		}
	}

	@Override
	public synchronized TurtleControl begin(Stroke s, Fill f) {
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
	public synchronized TurtleControl cancel() {
		freeFillSegment();
		freeStrokeSegment();
		fill = null;
		stroke = null;
		segs.removeIf(seg -> seg.isOwnedBy(this));
		return this;
	}

	@Override
	public synchronized TurtleControl child() {
		JfxControl j = new JfxControl(canvas, this);

		return j;
	}

	@Override
	public synchronized TurtleControl control(Bearing heading, Point from, double distance, Point to) {
		return this;
	}

	@Override
	public synchronized TurtleControl end() {
		if (fillSeg != null) {
			fillSeg.complete = true;
			flushFillSegment();
		}
		if (strokeSeg != null) {
			strokeSeg.end();
			flushStrokeSegment();
		}
		fill = null;
		stroke = null;
		return this;
	}

	protected FillSegment fillSegment(Point from) {
		if (fillSeg == null) {
			fillSeg = SegmentCache.fillSegment();
			fillSeg.init(this, fill);
			fillSeg = fillSeg.add(from);
			enqueue(fillSeg);
			nInUse++;
		} else if (fillSeg.isDone()) { // all points drawn, re-add to queue
			enqueue(fillSeg);
			nInUse++;
		}
		return fillSeg;
	}

	public TurtleControl flush() { // should not be synchronized
		flushAndAdd(null);
		return this;
	}

	protected void flushAndAdd(Segment s) {
		int size = segs.size();
		if (size > 0) {
			totQueueSize += segs.size();
			nQueueSize++;
			if (steps++ % 100 < 30)
				System.err.println("  Cur/Avg queue:   " + segs.size() + ", " + ((double) totQueueSize / nQueueSize));
			Segment seg;
			int i = 0;
			while (i++ < size && (seg = segs.poll()) != null) {
				if (s != null && segs.offer(s))
					s = null;
//				System.err.println(seg.points.size());
				seg.draw(canvas);
				nInUse--;
			}
		}
		if (s != null)
			segs.add(s);
	}

	protected void freeFillSegment() {
		if (fillSeg != null) {
			segs.remove(fillSeg);
			nInUse--;
			SegmentCache.free(fillSeg);
			fillSeg = null;
		}
	}

	protected void freeStrokeSegment() {
		if (strokeSeg != null) {
			segs.remove(strokeSeg);
			nInUse--;
			SegmentCache.free(strokeSeg);
			strokeSeg = null;
		}
	}

	@Override
	public synchronized TurtleControl go(Bearing bearing, Point from, double distance, Point to) {
		if (stroke != null) {
			if (validation && strokeSeg != null && strokeSeg.isComplete())
				throw new IllegalStateException("adding more points to complete segment");
			strokeSeg = strokeSegment(from).add(to);
			nStrokeEdges++;

		}
		if (fill != null) {
			if (validation && fillSeg != null && fillSeg.isComplete())
				throw new IllegalStateException("adding more points to complete segment");
			fillSeg = fillSegment(from).add(to);
			nFillEdges++;
		}
		return this;
	}

	@Override
	public synchronized TurtleControl pen(Stroke s, Fill f) {
		if (f != null) {
			if (fill == null)
				throw new IllegalStateException("Setting fill pen, but not currently filling");
			if (fillSeg != null && !f.equals(fillSeg.fill)) {
				fillSeg.complete = true;
				nFillBreaks++;
				flushFillSegment();
			}
			fill = f;
		}
		if (s != null) {
			if (stroke == null)
				throw new IllegalStateException("Setting stroke pen, but not currently stroking");
			if (strokeSeg != null && !s.equals(strokeSeg.stroke())) {
				strokeSeg.end();
				nStrokeBreaks++;
				flushStrokeSegment();
			}
			stroke = s;
		}

		return this;
	}

	private void flushStrokeSegment() {
		if (strokeSeg != null) {
			if (Platform.isFxApplicationThread()) {
				strokeSeg.draw(canvas);
				freeStrokeSegment();
			} // otherwise, draw operation will happen later
			strokeSeg = null;
		}
	}

	private void flushFillSegment() {
		if (fillSeg != null) {
			if (Platform.isFxApplicationThread()) {
				fillSeg.draw(canvas);
				freeFillSegment();
			} // otherwise, draw operation will happen later
			fillSeg = null;
		}
	}

	protected StrokeSegment strokeSegment(Point from) {
		if (strokeSeg == null) {
			strokeSeg = new LineSegment(stroke, from, null);
//			strokeSeg = SegmentCache.strokeSegment();
//			strokeSeg.init(this, stroke);
//			strokeSeg = strokeSeg.add(from);
			enqueue(strokeSeg);
			nInUse++;
		} else if (strokeSeg.isDone()) { // all points drawn, re-add to queue
			enqueue(strokeSeg);
			nInUse++;
		}
		return strokeSeg;
	}

	private void enqueue(Segment seg) {
		try {
			if (Platform.isFxApplicationThread()) {
				if (!segs.offer(seg)) {
					flushAndAdd(seg);
				}
			} else {
				segs.offer(seg, 1, TimeUnit.DAYS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public TurtleControl turn(Bearing from, double degrees, Bearing to) {
		return this;
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
}
