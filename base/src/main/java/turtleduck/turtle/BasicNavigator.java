package turtleduck.turtle;

import turtleduck.geometry.Direction;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

public interface BasicNavigator<T extends BasicNavigator<T>> extends PositionVector, DirectionVector {
	enum RelativeTo {
		/**
		 * Positions and bearings are measured relative to one's own position and
		 * bearing.
		 * 
		 * The positive Y axis points forward
		 */
		SELF, 
		/** Positions are measured relative to one's own positon, but bearings are absolute.
		 * 
		 * The positive Y axis points upwards.
		 */
		POSITION, 
		/** Positions and bearings are absolute.
		 * 
		 * The positive Y axis points upwards.
		 */
		WORLD
	};

	/**
	 * @return The current position.
	 */
	Point at();


	/**
	 * @return The current bearing of the turtle (the direction in which it is
	 *         facing)
	 */
	Direction bearing();

	/**
	 * Turn to face the given direction.
	 * 
	 * The given bearing can be relative or absolute.
	 * 
	 * @param bearing the bearing to turn to
	 * @return {@code this}, for sending more draw commands
	 */
	T bearing(Direction bearing);


	/**
	 * Move a distance in the given direction
	 *
	 * <p>
	 * Negative distances will move backwards.
	 * 
	 * @param bearing A direction
	 * @param dist    Distance to move
	 * @return {@code this}, for sending more draw commands
	 */
	T go(PositionVector offset, RelativeTo rel);

	Point findPoint(PositionVector point, RelativeTo rel);
	
	
}
