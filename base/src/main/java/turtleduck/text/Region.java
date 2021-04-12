package turtleduck.text;

import java.util.stream.Stream;

import turtleduck.text.impl.RegionImpl;

public interface Region extends Position {
	static Region rectangular(int x0, int y0, int x1, int y1) {
		if (y0 > y1) {
			int tmp = y1;
			y1 = y0;
			y0 = tmp;
		}
		if (x0 > x1) {
			int tmp = x1;
			x1 = x0;
			x0 = tmp;
		}
		return new RegionImpl.RectRegion(x0, y0, x1, y1);
	}
	static Region flow(int x0, int y0, int x1, int y1) {
		if (y0 > y1) {
			int tmp = y1;
			y1 = y0;
			y0 = tmp;
			tmp = x1;
			x1 = x0;
			x0 = tmp;
		} else if (y0 == y1 && x0 > x1) {
			int tmp = x1;
			x1 = x0;
			x0 = tmp;
		}
		return new RegionImpl.FlowRegion(x0, y0, x1, y1);
	}

	/**
	 * Get starting column number
	 * 
	 * Line/column numbering starts at (1,1) (top left), and is <em>inclusive</em>.
	 * 
	 * @return Column number (x-coordinate) of the start of the region
	 */
	int column();

	/**
	 * Get starting line number
	 * 
	 * Line/column numbering starts at (1,1) (top left), and is <em>inclusive</em>.
	 * 
	 * @return Line number (y-coordinate) of the start of the region
	 */
	int line();

	/**
	 * Get ending column number
	 * 
	 * Line/column numbering starts at (1,1) (top left), and is <em>inclusive</em>.
	 * 
	 * @return Column number (x-coordinate) of the end of the region
	 */
	int endColumn();

	/**
	 * Get ending line number
	 * 
	 * Line/column numbering starts at (1,1) (top left), and is <em>inclusive</em>.
	 * 
	 * @return Line number (y-coordinate) of the end of the region
	 */
	int endLine();

	/**
	 * Check if this region is rectangular or flowed.
	 * 
	 * A <em>rectangular</em> region includes all positions <code>(c, l)</code> with
	 * line <code>line() &leq; l &leq; endLine()</code> and column
	 * <code>column() &leq; c &leq; endColumn()</code>, and a <em>flowed</em> region
	 * includes all positions <code>(c, l)</code> with
	 *
	 * <ul>
	 * <li><code>line() &lt; l &lt; endLine()</code>, and
	 * <li><code>(line() == l) == (c &geq; column())</code>, and
	 * <li><code>(endLine() == l) == (c &leq; endColumn())</code>
	 * </ul>
	 * 
	 * @return True if the region is rectangular
	 */
	boolean isRectangular();

	/**
	 * Check if region contains the given position
	 * 
	 * @param pos A position
	 * @return True if <code>pos</code> is inside this region
	 */
	boolean contains(Position pos);

	/**
	 * Check if region contains the given position
	 * 
	 * @param x Column
	 * @param y Line
	 * @return True if <code>(x,y)</code> is inside this region
	 */
	boolean contains(int x, int y);

	/**
	 * Obtain a stream of positions within this region within the given page region.
	 * 
	 * <p>
	 * For each position <code>p</code>,
	 * <code>contains(p) && page.contains(p)</code> will be true.
	 * <p>
	 * The positions will be in order, line by line, starting at (column(), line()).
	 * 
	 * @param page A region defining the bounds of the page
	 * @return A stream of positions.
	 */
	Stream<Position> posStream(Region page);
}
