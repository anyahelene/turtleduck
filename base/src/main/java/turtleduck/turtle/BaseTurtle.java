package turtleduck.turtle;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import turtleduck.colors.Color;
import turtleduck.annotations.Icon;
import turtleduck.annotations.Internal;
import turtleduck.canvas.Canvas;
import turtleduck.drawing.Functional;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;
import turtleduck.objects.IdentifiedObject;
import turtleduck.paths.Path;
import turtleduck.paths.PathPoint;
import turtleduck.paths.PathStroke;
import turtleduck.paths.PathWriter;
import turtleduck.paths.Pen;
import turtleduck.paths.impl.PenContext;
import turtleduck.paths.impl.PenSettingsContext;

/**
 * @author anya
 *
 * @param <T>
 * @param <C>
 */
@Icon("🐢")
@Internal
public interface BaseTurtle<T extends BaseTurtle<T, C>, C>
        extends Functional<T>, Navigator<T>, PenSettingsContext<T>, IdentifiedObject {

    /**
     * This method is used to convert the turtle to an other type, determined by the
     * class object given as an argument.
     * <p>
     * This can be used to access extra functionality not provided by this
     * interface, such as direct access to the underlying graphics context.
     *
     * @param clazz
     * @return This object or an appropriate closely related object of the given
     *         type; or <code>null</code> if no appropriate object can be found
     */
    <U> U as(Class<U> clazz);

    /**
     * Move to the given position while drawing a curve
     *
     * <p>
     * The resulting curve is a cubic Bézier curve with the control points located
     * at <code>getPos().move(getBearing, startControl)</code> and
     * <code>to.move(Bearing.fromDegrees(endAngle+180), endControl)</code>.
     * <p>
     * The turtle is left at point <code>to</code>, facing <code>endAngle</code>.
     * <p>
     * The turtle will start out moving in its current direction, aiming for a point
     * <code>startControl</code> pixels away, then smoothly turning towards its
     * goal. It will approach the <code>to</code> point moving in the direction
     * <code>endAngle</code> (an absolute bearing, with 0° pointing right and 90°
     * pointing up).
     *
     * @param to           Position to move to
     * @param startControl Distance to the starting control point.
     * @return {@code this}, for sending more draw commands
     */
    T curveTo(Point to, double startControl, double endAngle, double endControl);

    /**
     * Move to the given position while drawing a line
     *
     * @param to Position to move to
     * @return {@code this}, for sending more draw commands
     */
    T drawTo(Point to);

    /**
     * Move to the given position while drawing a line
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    default T drawTo(double x, double y, double z) {
        return drawTo(Point.point(x, y, z));
    }

    /**
     * Move to the given position while drawing a line
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    default T drawTo(double x, double y) {
        return drawTo(Point.point(x, y, 0));
    }

    /**
     * Move forward the given distance while drawing a line.
     * 
     * <p>
     * Negative distances will draw backwards.
     *
     * @param dist Relative distance to move
     * @return {@code this}, for sending more draw commands
     */
    T draw(double dist);

    /**
     * Move a distance without drawing.
     *
     * <p>
     * Negative distances will move backwards.
     * 
     * @param dist Distance to move
     * @return {@code this}, for sending more draw commands
     */
    T jump(double dist);

    /**
     * Move a distance in the given direction, without drawing.
     *
     * <p>
     * Negative distances will move backwards.
     * 
     * @param heading A direction
     * @param dist    Distance to move
     * @return {@code this}, for sending more draw commands
     */
    T jump(Direction heading, double dist);

    /**
     * Draw a line in the given direction.
     * 
     * @param heading A direction
     * @param dist    Distance to draw
     * @return this
     */
    T draw(Direction heading, double dist);

    /**
     * Move to a new position, without drawing.
     *
     * @param x New x position
     * @param y New y position
     * @return {@code this}, for sending more draw commands
     */
    default T jumpTo(double x, double y) {
        return jumpTo(Point.point(x, y));
    }

    /**
     * Move to a new position, without drawing.
     *
     * @param newPos New position
     * @return {@code this}, for sending more draw commands
     */
    T jumpTo(PositionVector newPos);

    /**
     * Adjust heading by turning the given number of degrees (relative to the
     * current direction).
     *
     * <p>
     * Positive angles turn <em>left</em> while negative angles turn <em>right</em>.
     *
     * <p>
     * This method will rotate the turtle around its own <em>up</em> (yaw, intrinsic
     * Z) axis. E.g., imagine a turtle standing on four legs and turning left or
     * right.
     * 
     * @param angle Adjustment
     * @return {@code this}, for sending more draw commands
     * @see {@link #useDegrees()}, {@link #useRadians()}
     */
    T turn(double angle);

    /**
     * Change heading to the given angle.
     *
     * <p>
     * 0 is due right, 90° is up.
     *
     * @param angle Absolute heading angle
     * @return {@code this}, for sending more draw commands
     */
    default T turnTo(double angle) {
        return direction(Direction.absolute(angle));
    }

    default T turnTo(Point point) {
        return turnTo(point().directionTo(point, direction()));
    }
    /**
     * Change pen
     * 
     * @param newPen The new pen
     * @return <code>this</code>
     */
    T pen(Pen newPen);

    /**
     * Enable stroking and disable filling.
     * 
     * The current colors are unchanged.
     * 
     * @return {@code this}
     */
    default T strokeOnly() {
        return penChange().stroke(true).fill(false).done();
    }

    /**
     * Enable filling and disable stroking.
     * 
     * The current colors are unchanged.
     * 
     * @return {@code this}
     */
    default T fillOnly() {
        return penChange().stroke(false).fill(true).done();
    }

    /**
     * Enable stroking and filling.
     * 
     * The current colors are unchanged.
     * 
     * @return {@code this}
     */
    default T strokeAndFill() {
        return penChange().stroke(true).fill(true).done();
    }

    /**
     * Finish with whatever we're doing, and continue with whatever we were doing
     * 
     * @return Either the result of the current operation, or the previous object we
     *         were working on, for continued draw commands
     */
    C done();

    /**
     * Spawn a sub-turtle for making a sub-drawing.
     * 
     * <p>
     * The sub-turtle will have its own position, heading and pen, all of which
     * start off equal to that of the parent turtle (<code>this</code>). Further
     * changes to
     * <code>this</code> turtle will not affect the sub-turtle, and vice versa.
     * <p>
     * Calling {@link #done()} on the sub-turtle will finish its current drawing
     * command and yield this turtle. For example, each of these three code snippets
     * will
     * draw a T-like shape:
     * 
     * <pre>
     * turtle.draw(50).spawn().turn(90).draw(100).done().draw(50);
     * </pre>
     * 
     * <pre>
     * turtle.draw(50);
     * turtle.spawn().turn(90).draw(100);
     * turtle.draw(50);
     * </pre>
     * 
     * <pre>
     * turtle.draw(50).spawn(t -> t.turn(90).draw(100)).draw(50);
     * </pre>
     * 
     * @return A fresh turtle, equal to this one, ready for new drawing adventures
     */
    T spawn();

    /**
     * @return True if this turtle was {@link #spawn()}ed from another turtle
     */
    boolean hasParent();

    /**
     * @return The turtle this turtle was {@link #spawn()}ed from, or null
     */
    T parent();

    /**
     * Start drawing a sprite.
     * 
     * The returned SpriteBuilder behaves like a normal turtle, except that calling
     * {@link #done()} will yield a {@link Sprite}. The sprite builder starts out in
     * the ‘forward’ direction of the sprite, with the same pen as
     * <code>this</code>. The finished sprite will start with
     * the same position and orientation as the current turtle.
     * 
     * @return A sprite builder
     */
    SpriteBuilder sprite();

    /**
     * Start drawing a Drawing object.
     * 
     * The returned DrawingBuilder behaves like a normal turtle, except that calling
     * {@link #done()} will yield a {@link Drawing}. The builder starts with
     * the same position, orientation and pen as the current turtle, but is
     * otherwise independent.
     * 
     * @return A sprite builder
     */
    DrawingBuilder drawing();

    /**
     * Redirect draw commands to the given path writer.
     * 
     * @param pathWriter Target for draw commands
     * @return this
     */
    T writePathsTo(PathWriter pathWriter);

    /**
     * End any ongoing path, and start a new one.
     * 
     * There is generally no need to call {@link #beginPath()} – any drawing command
     * will start a new path if there is no currently active path. However,
     * {@link #beginPath()} will end any ongoing path, and pairs nicely with
     * {@link #endPath()}.
     * 
     */
    T beginPath();

    /**
     * End and return the current path.
     * 
     * Any move command without drawing (i.e., <code>jump</code>) will also end the
     * current path, so this method is only necessary if you need the Path object.
     * 
     * @return A {@link Path} object with the drawing commands
     */
    Path endPath();

    /**
     * Close the current path by drawing a line back to its first point, then return
     * it as a Path object.
     * 
     * Any move command without drawing (i.e., <code>jump</code>) will also end the
     * current path, so this method is only necessary if you need the Path object.
     * 
     * @return A {@link Path} object with the drawing commands
     */
    Path closePath();

    /**
     * Adjust heading by the given direction (relative to the
     * current direction).
     *
     * <p>
     * Positive angles turn <em>left</em> while negative angles turn <em>right</em>.
     *
     * <p>
     * This method will rotate the turtle around its own <em>up</em> (yaw, intrinsic
     * Z) axis. E.g., imagine a turtle standing on four legs and turning left or
     * right.
     * 
     * @param dir Adjustment
     * @return {@code this}, for sending more draw commands
     */
    T turn(Direction dir);

    /**
     * Adjust heading to the given absolute direction.
     *
     * <p>
     * This method will rotate the turtle around its own <em>up</em> (yaw, intrinsic
     * Z) axis. E.g., imagine a turtle standing on four legs and turning left or
     * right.
     * <p>
     * Afterwards {@link #direction()} will be equal to <code>dir</code>.
     * 
     * @param dir Adjustment
     * @return {@code this}, for sending more draw commands
     */
    T turnTo(Direction dir);

    /**
     * Add an annotation to the current path node.
     * 
     * @param <U>   The type of the annotation's data value
     * @param anno  Annotation identifier
     * @param value Data value
     * @return this
     */
    <U> T annotate(Annotation<U> anno, U value);

    /**
     * Retrieve an annotation for the current path node.
     * 
     * @param <U>  The type of the annotation's data value
     * @param anno Annotation identifier
     * @return The data value, or <code>null</code>
     */
    <U> U annotation(Annotation<U> anno);

    /**
     * Move to a new position, without drawing.
     *
     * @param x New x position
     * @param y New y position
     * @param z New z position
     * @return {@code this}, for sending more draw commands
     */
    default T jumpTo(double x, double y, double z) {
        return jumpTo(Point.point(x, y, z));
    }

    /**
     * Set the current position
     * 
     * @param x the new X position
     * @param y the new Y position
     * @param z the new Z position
     * @return {@code this}, for sending more draw commands
     */
    default T at(double x, double y, double z) {
        return goTo(Point.point(x, y, z));
    }

    /**
     * Adjust heading by climbing the given number of degrees (relative to the
     * current direction).
     *
     * <p>
     * Positive angles climb <em>upwards</em> while negative angles dive
     * <em>downwards</em>.
     *
     * <p>
     * This method will rotate the turtle around its own <em>right</em> (pitch,
     * intrinsic X) axis. E.g., imagine a turtle rising up on its hind legs.
     * 
     * @param angle Adjustment
     * @return {@code this}, for sending more draw commands
     */
    T pitch(double angle);

    /**
     * Adjust heading by rolling the given number of degrees (relative to the
     * current direction).
     *
     * <p>
     * TODO: positive is left or right?
     *
     * <p>
     * This method will rotate the turtle around its own <em>forward</em> (roll,
     * intrinsic Y) axis. E.g., imagine a turtle rolling sideways.
     * 
     * @param angle Adjustment
     * @return {@code this}, for sending more draw commands
     */
    T roll(double angle);

    /**
     * Set options for the current path stroke.
     * 
     * @param newOptions
     * @return this
     * @see {@link PathStroke#PATH_TRIANGLE_STRIP} etc.
     */
    T pathOptions(int newOptions);

}