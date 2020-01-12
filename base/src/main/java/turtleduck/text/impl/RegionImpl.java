package turtleduck.text.impl;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import turtleduck.text.Position;
import turtleduck.text.Region;

public abstract class RegionImpl extends PositionImpl implements Region {
	protected final int endColumn;
	protected final int endLine;

	public RegionImpl(int startColumn, int startLine, int endColumn, int endLine) {
		super(startColumn, startLine);
		if (startLine > endLine)
			throw new IllegalArgumentException(String.format("startLine=%d > endLine=%d", startLine, endLine));
		else if (startLine == endLine && startColumn > endColumn)
			throw new IllegalArgumentException(String.format("startColumn=%d > endColumn=%d", startColumn, endColumn));
		this.endColumn = endColumn;
		this.endLine = endLine;
	}

	@Override
	public int endColumn() {
		return endColumn;
	}

	@Override
	public int endLine() {
		return endLine;
	}

	@Override
	public String toString() {
		return String.format("%d,%d,%d,%d", column, line, endColumn, endLine);
	}

	public static class RectRegion extends RegionImpl {

		public RectRegion(int startColumn, int startLine, int endColumn, int endLine) {
			super(startColumn, startLine, endColumn, endLine);
		}

		@Override
		public boolean isRectangular() {
			return true;
		}

		@Override
		public boolean contains(Position pos) {
			return column <= pos.column() && endColumn >= pos.column() //
					&& line <= pos.line() && endLine >= pos.line();
		}

		@Override
		public boolean contains(int x, int y) {
			return column <= x && endColumn >= x //
					&& line <= y && endLine >= y;
		}

		@Override
		public Stream<Position> posStream(Region page) {
			int x0 = column, x1 = endColumn, y0 = line, y1 = endLine;
			if (x0 > page.endColumn() && endLine > line) {
				x0 = 1;
				y0++;
			}
			x0 = Math.max(page.column(), x0);
			x1 = Math.min(page.endColumn(), x1);
			y0 = Math.max(page.line(), y0);
			y1 = Math.min(page.endLine(), y1);
			if (y0 > page.endLine() || y1 < page.line())
				return Stream.of();
			else
				return StreamSupport.stream(new RectSpliterator(x0, y0, x1, y1), false);
		}

		@Override
		public String toString() {
			return "rect(" + super.toString() + ")";
		}

		class RectSpliterator implements Spliterator<Position> {
			private int x0, y0, x1, y1;
			private final MutablePosition pos;

			RectSpliterator(int x0, int y0, int x1, int y1) {
				pos = new MutablePosition(x0, y0);
				this.x0 = x0;
				this.y0 = y0;
				this.x1 = x1;
				this.y1 = y1;
			}

			@Override
			public boolean tryAdvance(Consumer<? super Position> action) {
				if (pos.x > x1) {
					pos.y++;
					pos.x = x0;
				}
				if (pos.y <= y1 && pos.x <= x1) {
					action.accept(new PositionImpl(pos.x++, pos.y));
					return true;
				}
				return false;
			}

			@Override
			public Spliterator<Position> trySplit() {
				if (pos.y + 1 < y1 && pos.x == x0) {
					int y2 = (pos.y + y1) / 2;
					RectSpliterator split = new RectSpliterator(x0, pos.y, x1, y2 - 1);
					pos.y = y2;
					return split;
				}
				return null;
			}
			public String toString() {
				return String.format("RectSpliterator(%d,%d,%d,%d)", pos.x, pos.y, x1, y1);
			}

			@Override
			public long estimateSize() {
				return (y1+1 - y0) * (x1+1 - x0);
			}

			@Override
			public int characteristics() {
				return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
			}

		}
	}

	public static class FlowRegion extends RegionImpl {

		public FlowRegion(int startColumn, int startLine, int endColumn, int endLine) {
			super(startColumn, startLine, endColumn, endLine);
		}

		@Override
		public boolean isRectangular() {
			return false;
		}

		@Override
		public boolean contains(Position pos) {
			return contains(pos.column(), pos.line());
		}

		@Override
		public boolean contains(int x, int y) {
			if (line == endLine)
				return x >= column && x <= endColumn;
			else
				return (y > line && y < endLine) || (y == line && x >= column) || (y == endLine && x <= endColumn);
		}

		@Override
		public Stream<Position> posStream(Region page) {
			int x0 = column, x1 = endColumn, y0 = line, y1 = endLine;
			if (x0 > page.endColumn() && endLine > line) {
				x0 = 1;
				y0++;
			}
			x0 = Math.max(page.column(), x0);
			x1 = Math.min(page.endColumn(), x1);
			y0 = Math.max(page.line(), y0);
			y1 = Math.min(page.endLine(), y1);
			if (y0 > page.endLine() || y1 < page.line())
				return Stream.of();
			else
				return StreamSupport.stream(new FlowSpliterator(x0, y0, x1, y1, page), false);
		}

		@Override
		public String toString() {
			return "flow(" + super.toString() + ")";
		}

		class FlowSpliterator implements Spliterator<Position> {
			private final MutablePosition pos;
			private int x1, y1;
			private Region page;

			FlowSpliterator(int x0, int y0, int x1, int y1, Region page) {
				pos = new MutablePosition(x0, y0);
				this.x1 = x1;
				this.y1 = y1;
				this.page = page;
			}

			@Override
			public boolean tryAdvance(Consumer<? super Position> action) {
				if (pos.y < y1) {
					if (pos.x > page.endColumn()) {
						pos.x = page.column();
						pos.y++;
					}
					action.accept(new PositionImpl(pos.x++, pos.y));
					return true;
				} else if (pos.y == y1 && pos.x <= x1) {
					action.accept(new PositionImpl(pos.x++, pos.y));
					return true;
				}
				return false;
			}

			@Override
			public Spliterator<Position> trySplit() {
				if (pos.y + 1 < y1) {
					int y2 = (pos.y + y1) / 2;
					FlowSpliterator split = new FlowSpliterator(pos.x, pos.y, page.endColumn(), y2 - 1, page);
					pos.x = page.column();
					pos.y = y2;
					return split;
				}
				return null;
			}

			public String toString() {
				return String.format("FlowSpliterator(%d,%d,%d,%d,%s)", pos.x, pos.y, x1, y1, page);
			}
			@Override
			public long estimateSize() {
				if (pos.y == y1)
					return x1 - pos.x + 1;
				else
					return page.endColumn() - pos.x + 1 //
							+ x1 - page.column() + 1//
							+ (pos.y+1<y1 ? (page.endColumn()-page.column()+1) * (y1 - pos.y - 1) : 0);
			}

			@Override
			public int characteristics() {
				return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
			}

		}
	}

	protected static class MutablePosition implements Position {
		protected int x, y;

		public MutablePosition(int column, int line) {
			super();
			this.x = column;
			this.y = line;
		}

		@Override
		public int column() {
			return x;
		}

		@Override
		public int line() {
			return y;
		}
		
		@Override
		public String toString() {
			return String.format("(%d,%d)", x, y);
		}

	}
}
