package turtleduck.shapes;

import turtleduck.canvas.Canvas;
import turtleduck.canvas.PenContext;
import turtleduck.geometry.Point;
import turtleduck.turtle.Pen;

public interface Shape {
	Point position();

	public interface Builder<T> extends PenContext<T> {
		/**
		 * End the current shape, and draw it by stroking
		 * 
		 * @return An identifier for the drawn path object
		 */
		String stroke();

		/**
		 * End the current shape, and draw it by filling
		 * 
		 * @return An identifier for the drawn path object
		 */
		String fill();

		/**
		 * End the current shape, and draw it by filling and stroking
		 * 
		 * @return An identifier for the drawn path object
		 */
		String strokeAndFill();

		/**
		 * End the current shape, and draw it according to current pen settings
		 * 
		 * @return An identifier for the drawn path object
		 * @see Pen.filling()
		 * @see Pen.stroking()
		 */
		String done();

		/**
		 * Start the shape at the given point
		 * 
		 * @param p A point
		 * @return this
		 */
		T at(Point p);

		/**
		 * Start the shape at the given point
		 * 
		 * @param x Point's x coordinate
		 * @param y Point's y coordinate
		 * @return this
		 */
		T at(double x, double y);
	}

	public interface WxHShape {
		/**
		 * @return Width of the shape
		 */
		double width();

		/**
		 * @return Height of the shape
		 */
		double height();
	}

	public interface WxHBuilder<T> extends Builder<T> {
		/**
		 * Set the width of the shape
		 * 
		 * @param width New width
		 * @return this
		 */
		T width(double width);

		/**
		 * Set the height of the shape
		 * 
		 * @param width New height
		 * @return this
		 */
		T height(double height);
	}
}
