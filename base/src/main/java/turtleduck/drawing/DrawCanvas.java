package turtleduck.drawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import turtleduck.colors.Colors;
import turtleduck.colors.Paint;
import turtleduck.geometry.BoundingBox;
import turtleduck.geometry.Point;
import turtleduck.geometry.impl.BoundingBoxImpl;
import turtleduck.turtle.Canvas;
import turtleduck.turtle.Fill;
import turtleduck.turtle.Geometry;
import turtleduck.turtle.IShape;
import turtleduck.turtle.LineBuilder;
import turtleduck.turtle.Path;
import turtleduck.turtle.Stroke;

public class DrawCanvas implements Canvas {
	protected BoundingBox bb = new BoundingBoxImpl(Double.MAX_VALUE, Double.MAX_VALUE, 0, 0);
	protected List<Instr> instructions = new ArrayList<>();
	protected Paint background = Colors.TRANSPARENT;

	@Override
	public DrawCanvas dot(Stroke pen, Geometry geom, Point point) {
		if (pen != null) {
			instructions.add(new Instr(DrawInstruction.Instruction.DOT, pen, null, geom, point));
		}
		return this;
	}

	@Override
	public DrawCanvas line(Stroke pen, Geometry geom, Point from, Point to) {
		if (pen != null) {
			if (pen != null) {
				instructions.add(new Instr(DrawInstruction.Instruction.POLYGON, pen, null, geom, from, to));
			}
		}
		return this;
	}

	@Override
	public LineBuilder lines(Stroke pen, Geometry geom, Point from) {
		return new DrawLineBuilder(pen, geom, from);
	}

	@Override
	public DrawCanvas polyline(Stroke pen, Fill fill, Geometry geom, Point... points) {
		if (pen != null || fill != null) {
			instructions.add(new Instr(DrawInstruction.Instruction.LINE, pen, fill, geom, points));
		}
		return this;
	}

	@Override
	public DrawCanvas polygon(Stroke pen, Fill fill, Geometry geom, Point... points) {
		if (pen != null || fill != null) {
			instructions.add(new Instr(DrawInstruction.Instruction.POLYGON, pen, fill, geom, points));
		}
		return this;
	}

	@Override
	public DrawCanvas triangles(Stroke pen, Fill fill, Geometry geom, Point... points) {
		if (pen != null || fill != null) {
			instructions.add(new Instr(DrawInstruction.Instruction.TRIANGLES, pen, fill, geom, points));
		}
		return this;
	}

	@Override
	public DrawCanvas shape(Stroke pen, Fill fill, Geometry geom, IShape shape) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public DrawCanvas path(Stroke pen, Fill fill, Geometry geom, Path path) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public DrawCanvas clear() {
		instructions.clear();
		background = Colors.TRANSPARENT;
		return this;
	}

	@Override
	public DrawCanvas clear(Fill fill) {
		instructions.clear();
		background = fill.fillPaint();
		return this;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}
	
	public List<Instr> done() {
		return instructions;
	}
	static class Instr implements DrawInstruction {
		Instruction instr;
		Stroke stroke;
		Fill fill;
		float[] data;
		int n;

		public Instr() {
		}

		public Instr(Instruction ins, Stroke s, Fill f, Geometry geom, Point... ps) {
			instr = ins;
			stroke = s;
			fill = f;
			n = ps.length;
			data = new float[n * 2];
			for (int i = 0; i < ps.length; i++) {
				Point p1 = geom.projection().project(ps[i]);
				data[i * 2] = (float) p1.x();
				data[i * 2 + 1] = (float) p1.y();
			}

		}

		@Override
		public Instruction instruction() {
			return instr;
		}

		@Override
		public Stroke stroke() {
			return stroke;
		}

		@Override
		public Fill fill() {
			return fill;
		}

		@Override
		public int n() {
			return n;
		}

		@Override
		public double x(int i) {
			return data[i * 2];
		}

		@Override
		public double y(int i) {
			return data[i * 2 + 1];
		}

		@Override
		public Iterable<Point> points() {
			return () -> new Iterator<Point>() {
				int i = 0;

				@Override
				public boolean hasNext() {
					return i < n;
				}

				@Override
				public Point next() {
					Point p = Point.point(data[i * 2], data[i * 2 + 1]);
					i += 2;
					return p;
				}

			};
		}

		@Override
		public Stream<Point> pointStream() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class DrawLineBuilder implements LineBuilder {
		private Instr instr = null;
		private boolean polygon = false;
		private Point last;
		private Stroke stroke;
		private Geometry geom;

		public DrawLineBuilder(Stroke stroke, Geometry geom, Point first) {
			this.stroke = stroke;
			this.geom = geom;
			this.last = first;
		}

		@Override
		public LineBuilder to(Point next) {
			if (instr == null) {
				instr = new Instr();
				instr.data = new float[16];
				instr.stroke = stroke;
				Point p = geom.projection().project(last);
				instr.data[instr.n * 2] = (float) p.x();
				instr.data[instr.n * 2 + 1] = (float) p.y();
				instr.n++;
			} else if (instr.data.length <= instr.n * 2) {
				instr.data = Arrays.copyOf(instr.data, instr.n * 3 + 1);
			}
			Point p = geom.projection().project(next);
			instr.data[instr.n * 2] = (float) p.x();
			instr.data[instr.n * 2 + 1] = (float) p.y();
			instr.n++;

			return this;
		}

		@Override
		public LineBuilder to(Stroke stroke, Point next) {
			if (instr.stroke != stroke) {
				done();
			}
			if (instr == null) {
				instr = new Instr();
				instr.data = new float[16];
				instr.stroke = stroke;
				Point p = geom.projection().project(last);
				instr.data[instr.n * 2] = (float) p.x();
				instr.data[instr.n * 2 + 1] = (float) p.y();
				instr.n++;
			} else if (instr.data.length <= instr.n * 2) {
				instr.data = Arrays.copyOf(instr.data, instr.n * 3 + 1);
			}
			Point p = geom.projection().project(next);
			instr.data[instr.n * 2] = (float) p.x();
			instr.data[instr.n * 2 + 1] = (float) p.y();
			instr.n++;

			return this;
		}

		@Override
		public LineBuilder close() {
			polygon = true;
			return this;
		}

		@Override
		public Canvas done() {
			if (polygon)
				instr.instr = DrawInstruction.Instruction.POLYGON;
			else
				instr.instr = DrawInstruction.Instruction.LINE;
			instructions.add(instr);
			instr = null;
			return DrawCanvas.this;
		}

	}
}
