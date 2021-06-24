package turtleduck.turtle;

public interface PathPattern {

	/**
	 * @return A pattern that matches any subpath
	 */
	PathPattern anything();

	/**
	 * @return A pattern that matches at the beginning of a path
	 */
	PathPattern beginning();

	/**
	 * @return A pattern that matches at the end of a path
	 */
	PathPattern end();

	/**
	 * 
	 * <code>p1.then().p2</code> matches <code>p1</code> followed by
	 * <code>p2</code>.
	 * 
	 * @return A combination pattern
	 */
	PathPattern then();

	/**
	 * @param n
	 * @return A pattern that matches n lines
	 */
	PathPattern lines(int n);

	/**
	 * @param n
	 * @return A pattern that matches n points
	 */
	PathPattern points(int n);

	/**
	 * @return Matches a sequence of lines p0, p1, … , p0' (where p0 ≃ p0')
	 */
	PathPattern isClosed();

	/**
	 * @return Matches a sequence of lines where all interior angles are < 180°
	 *         (i.e., all corners turn either left or right)
	 */
	PathPattern isConvex();

	/**
	 * @param a
	 * @return Matches a <code>±a</code> degrees turn
	 */
	PathPattern turn(double a);

	/**
	 * @param a
	 * @return Matches a sequence of lines with angles adding up to to
	 *         <code>sum</code>
	 */
	PathPattern sumAngles(double sum);

	/**
	 * <code>p1.and().p2</code> matches a path where <code>p1</code> and
	 * <code>p2</code> match the same set of points.
	 * 
	 * For example,
	 * <code>isClosed().and().isConvex() matches a closed, convex polygon or loop; <code>isClosed().and().isConvex().and().sumAngles(360)</code>
	 * matches a closed, convex polygon.
	 * 
	 * @return A combination pattern
	 */
	PathPattern and();
}
