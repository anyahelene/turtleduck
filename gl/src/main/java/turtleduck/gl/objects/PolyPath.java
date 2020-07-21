package turtleduck.gl.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import turtleduck.colors.Colors;
import turtleduck.colors.Color;

public class PolyPath {
	List<Vector2f> verts = new ArrayList<>();
	List<Vector3f> cols = new ArrayList<>();
	List<Edge> edges = new ArrayList<>();
	List<Integer> contours = new ArrayList<>();
	List<Edge> triEdges = new ArrayList<>();
	List<Edge> triBadEdges = new ArrayList<>();
	List<Vector3f> triOutput  = new ArrayList<>();
	Edge e1 = null, e2 = null, e3 = null;
	Map<Edge, Float> addedEdges = new HashMap<>();
	Map<Edge, Float> removedEdges = new HashMap<>();
	int count = 0;
	public PolyPathWriter writer() {
		return new PolyPathWriter();
	}
	class PolyPathWriter {
		Edge firstEdge = null, lastEdge = null, currentEdge = null;
		int last = -1, current = -1, first = -1;
		Color c = Colors.RED;

		public PolyPathWriter lineTo(float x, float y) {
			last = current;
			current = vertex(x, y);
			currentEdge = new Edge(verts, last, current);
			currentEdge.contour = true;
			edges.add(currentEdge);
			c = c.mix(Colors.CYAN, 0.01);
			if(lastEdge != null) {
				Edge.connect(lastEdge, currentEdge);
			} else {
				firstEdge = currentEdge;
			}
			lastEdge = currentEdge;
			return this;
		}
		public PolyPathWriter moveTo(float x, float y) {
			close();
			last = -1;
			c = c.mix(Colors.CYAN, 0.1);
			first = current = vertex(x, y);
			contours.add(current);
			return this;
		}

		public PolyPathWriter close() {
			if(firstEdge != null) {
				Edge.connect(lastEdge, firstEdge);
			}
			currentEdge = lastEdge = firstEdge = null;
			return this;
		}

		private int vertex(float x, float y) {
			Vector2f v = new Vector2f(x, y);
			int i = verts.indexOf(v);
			if(i < 0) {
				i = verts.size();
				verts.add(v);
				cols.add(new Vector3f((float)c.red(), (float)c.green(), (float)c.blue()));
			}
			return i;
		}
	}

	public List<Vector3f> triangulate() {
		startTriangulation();
		stepTriangulation();
		return getTrianglesAndEdges();
	}

	public void startTriangulation() {
		triOutput.clear();
		triEdges.clear();
		triEdges.addAll(edges);
		triBadEdges.clear();
		addedEdges.clear();
		removedEdges.clear();
		e1 = e2 = e3 = null;
	}

	public void stepTriangulation() {
		float threshold = -(float) Math.toRadians(15);
		if(triEdges.size() == 0) {
			e1 = null;
			return;
		}

		if(e1 == null) {
			e1 = triEdges.get(0);
		}
		e2 = null;
		e3 = null;

		if(e1.isTriangle()) {
			e2 = e1.next;
			e3 = e2.next;
			emitTriangle(new Vector3f(.7f,0,.7f));
			triEdges.remove(e1);
			triEdges.remove(e2);
			triEdges.remove(e3);
			removedEdges.put(e1, 1f);
			removedEdges.put(e2, 1f);
			removedEdges.put(e3, 1f);
			e1 = e2 = e3 = null;
			return;
		}

		double bestAngle = 2 * Math.PI;
		for(Edge e : Arrays.asList(e1.prev,e1, e1.next)) {
			double a = e.angle(e.next);
			if(a < threshold) {
				a =	Math.abs(a + Math.PI/2);

				if(a < bestAngle) {
					Edge tmp = new Edge(verts, e.next.to, e.from);
					if(checkTriangle(e,e.next,tmp)) {
						e1 = e;
						e2 = e.next;
						e3 = tmp;
					} else {
						removedEdges.put(tmp, .5f);
					}
				}
			}
		}


		if(e2 != null) {
			emitTriangle(new Vector3f(.7f,.7f,0));
			e3.reverse();
			Edge.connect(e1.prev, e3);
			Edge.connect(e3, e2.next);
			triEdges.remove(e1);
			triEdges.remove(e2);
			triEdges.add(e3);
			removedEdges.put(e1, 1f);
			removedEdges.put(e2, 1f);
			addedEdges.put(e3, 1f);
			e1 = e3;
			return;
		}

		Vector2f v1 = verts.get(e1.to);
		Vector2f v2 = new Vector2f();
		Vector2f v3 = verts.get(e1.from);
		float closestDist = Float.MAX_VALUE;
		Edge tmp2 = null, tmp3 = null, tmp2r, tmp3r, ec = null;
		e2 = null;
		e3 = null;
		for(Edge e : triEdges) {
			if(e1.to == e.from || e.from == e1.from) {
				continue;
			} else if(e1.prev == e || e1.next == e) {
				continue;
			}
			v2.set(verts.get(e.from));
			tmp2 = new Edge(verts, e1.to, e.from);
			tmp3 = new Edge(verts, e.from, e1.from);
			tmp2r = new Edge(verts, e.from, e1.to);
			tmp3r = new Edge(verts, e1.from, e.from);
			float dist = Math.max(v1.distanceSquared(v2), v3.distanceSquared(v2));
			if(e1.angle(tmp2) < 0 //
					//					&& e.prev.angle(tmp2) < 0 //
					//					&& tmp3r.angle(e) < threshold //
					&& dist < closestDist //
					&& checkTriangle(e1,tmp2,tmp3)) {
				float a1 = (float) (Math.PI + e.prev.angle), a2 = e.angle;
				if(a1 > 2*Math.PI) {
					a1 -= 2*Math.PI;
				}
				System.out.println("Closest dist: " + closestDist);
				System.out.println("Vector angle: " + e.prev.v.angle(e.v));
				System.out.println("Vector angle e2: " + e.prev.v.angle(tmp2r.v));
				System.out.println("Vector angle e3: " + e.prev.v.angle(tmp3.v));
				System.out.println("Angle at e: " + a1 + "–" + a2 + " (" + Math.toDegrees(a1) + "–" + Math.toDegrees(a2) + ")");
				System.out.println("Angle of e2: " + tmp2.angle + " (" + Math.toDegrees(tmp2.angle));
				System.out.println("Angle of e3: " + tmp3r.angle + " (" + Math.toDegrees(tmp3r.angle));
				if(e.prev.v.angle(tmp2r.v) > e.prev.v.angle(e.v)
						|| e.prev.v.angle(tmp2r.v) < -Math.PI
						||	e.prev.v.angle(tmp3.v) > e.prev.v.angle(e.v)
						|| e.prev.v.angle(tmp3.v) < -Math.PI
						// tmp2.angle < a1 || tmp2.angle > a2 //
						// || tmp3.angle < a1 || tmp3.angle > a2 //
						) {
					System.out.println("dropping this one");
					removedEdges.put(e, .5f);
					continue;
				}
				e2 = tmp2;
				e3 = tmp3;
				ec = e;
				closestDist = dist;
			}
			System.out.println("Closest dist: " + closestDist);
		}
		if(e2 != null && e3 != null) {
			emitTriangle(new Vector3f(0,.7f,.7f));
			triEdges.remove(e1);
			removedEdges.put(e1, 1f);
			e2.reverse();
			e3.reverse();
			Edge prev = ec.prev;
			Edge.connect(e3, ec);
			Edge.connect(e1.prev, e3);
			triEdges.add(e3);
			addedEdges.put(e3, 1f);
			Edge.connect(prev, e2);
			Edge.connect(e2, e1.next);
			triEdges.add(e2);
			addedEdges.put(e2, 1f);
			e1 = e2;
			return;
		}
		triBadEdges.add(e1);
		triEdges.remove(e1);
		e1 = null;
	}

	private void drawEdge(Edge e, Vector3f col, List<Vector3f> vs) {
		Vector2f v1 = verts.get(e.from);
		Vector2f v2 = verts.get(e.to);
		Vector2f yoff = new Vector2f(v2);
		yoff.sub(v1);
		Vector2f xoff = new Vector2f(yoff);
		xoff.set(xoff.y,-xoff.x).normalize().mul(.0025f);
		yoff.normalize().mul(0.025f);
		Vector2f v2a = new Vector2f(v2).sub(xoff);
		Vector2f v2b = new Vector2f(v2).add(xoff);
		Vector2f v1a = new Vector2f(v1).sub(xoff);
		Vector2f v1b = new Vector2f(v1).add(xoff);
		xoff.mul(2.5f);
		Vector2f v3a = new Vector2f(v2).sub(yoff).sub(xoff);
		Vector2f v3b = new Vector2f(v2).sub(yoff).add(xoff);
		vs.add(new Vector3f(v1b,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v2a,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v1a,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v1b,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v2a,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v2b,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v3a,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v3b,.5f));
		vs.add(new Vector3f(col));
		vs.add(new Vector3f(v2,.5f));
		vs.add(new Vector3f(col));
	}

	public List<Vector3f> getTriangles() {
		return triOutput;
	}

	public List<Vector3f> getTrianglesAndEdges() {
		List<Vector3f> vs = new ArrayList<>(triOutput);
		float c = (float) Math.abs(Math.sin(count++/2f));
		c = .5f + c/2;
		for(Edge e: triEdges) {
			e.color = null;
		}
		Color col = Colors.YELLOW;
		for(Edge e: triEdges) {
			if(e.color == null) {
				col = col.mix(Colors.BLUE, 0.1);
			}
			while(e.color == null) {
				e.color = new Vector3f((float)col.red(), (float)col.green(), (float)col.blue());
				//				col = col.deriveColor(3, 1, 1, 1);
				e = e.next;
			}
		}
		for(Edge e: triEdges) {
			if(e == e1) {
				drawEdge(e, new Vector3f(e.color).mul(c), vs);
			} else {
				drawEdge(e, e.color, vs);
			}
		}
		Vector3f badCol = new Vector3f(1,1,1);
		for(Edge e: triBadEdges) {
			drawEdge(e, badCol, vs);
		}
		for(Edge e : new ArrayList<>(addedEdges.keySet())) {
			float t = addedEdges.get(e);
			badCol.set(0,c,0);
			drawEdge(e,badCol, vs);
			if(t > 0) {
				addedEdges.put(e, t-0.05f);
			} else {
				addedEdges.remove(e);
			}
		}
		for(Edge e : new ArrayList<>(removedEdges.keySet())) {
			float t = removedEdges.get(e);
			badCol.set(c,0,0);
			drawEdge(e,badCol, vs);
			if(t > 0) {
				removedEdges.put(e, t-0.05f);
			} else {
				removedEdges.remove(e);
			}
		}
		return vs;
	}

	private boolean checkTriangle(Edge e1, Edge e2, Edge e3) {
		Vector2f v1 = e1.origin;
		Vector2f v2 = e2.origin;
		Vector2f v3 = e3.origin;
		/*		for(Edge e : edges) {
			if(Intersectionf.testLineSegmentTriangle(new Vector3f(e.origin,0), new Vector3f(e.dest,0), new Vector3f(v1,0), new Vector3f(v2,0), new Vector3f(v3,0), 0.0001f)) {
				return false;
			}
		}
		 */

		for(Vector2f v : verts) {
			if(v == v1 || v == v2 || v == v3) {
				continue;
			}
			if(Intersectionf.testPointTriangle(v, v1, v2, v3)) {
				return false;
			}
		}

		return true;
	}

	private void emitTriangle(Vector3f color) {
		for(Edge e : Arrays.asList(e1, e2, e3)) {
			//			System.out.println("Triangle: " + e1 + ", " + e2 +", " +e3);
			triOutput.add(new Vector3f(verts.get(e.from),.5f));
			triOutput.add(color);
		}
	}
	static class Edge {
		int from, to;
		Vector2f origin, dest, v;
		Edge prev, next;
		float angle;
		boolean contour;
		Vector3f color;

		public Edge(List<Vector2f> verts, int from, int to) {
			this.from = from;
			this.to = to;
			this.origin = verts.get(from);
			this.dest = verts.get(to);
			this.v = new Vector2f(dest);
			v.sub(origin);
			this.angle = (float) Math.atan2(v.y, v.x);
			if(angle < 0) {
				angle += 2*Math.PI;
			}
		}

		public void reverse() {
			int tmp = from;
			from = to;
			to = tmp;
			Vector2f vTmp = origin;
			origin = dest;
			dest = vTmp;
			v.negate();
			angle = (float) Math.atan2(v.y, v.x);
		}

		public boolean isTriangle() {
			return from == next.next.to;
		}

		public static void connect(Edge from, Edge to) {
			if(!from.dest.equals(to.origin)) {
				throw new IllegalArgumentException("" + from + " cannot be connected to " + to);
			}
			from.next = to;
			to.prev = from;
		}

		@Override
		public String toString() {
			return String.format("(%.6f,%.6f) --(%.3f,%.2f°)-> +(%.6f,%.6f)", origin.x, origin.y, v.length(),Math.toDegrees(angle),v.x,v.y);
		}

		public float angle(Edge other) {
			return v.angle(other.v);
		}
	}

}
