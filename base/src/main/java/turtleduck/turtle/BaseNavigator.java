package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.annotations.Internal;
import turtleduck.geometry.Direction;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

@Icon("🧭")
@Internal
public interface BaseNavigator<T extends BaseNavigator<T>> extends PositionVector, DirectionVector {

    /**
     * @return The current position.
     */
    Point point();

    /**
     * @param dx X offset (with X axis perpendicular to current forward direction)
     * @param dy Y offset (with Y axis pointing forward
     * @return A point <em>dy</em> forward and <em>dx</em> to the side of the curent
     *         position.
     */
    Point offset(double dx, double dy);
 
    /**
     * Equivalent to <code>point().add(dx,dy)</code>.
     * 
     * @param dx X offset (with X axis pointing to the right side of the screen)
     * @param dy Y offset (with Y axis pointing to the top of the screen)
     * @return A point <em>dy</em> above and <em>dx</em> to right of the current position.
     */
    Point offsetAxisAligned(double dx, double dy);

    /**
     * @return The current bearing of the turtle (the direction in which it is
     *         facing)
     */
    Direction direction();

    /**
     * Turn to face the given direction.
     * 
     * The given bearing can be relative or absolute.
     * 
     * @param bearing the bearing to turn to
     * @return {@code this}, for sending more draw commands
     */
    T direction(Direction bearing);


}
