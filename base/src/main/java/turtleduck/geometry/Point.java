package turtleduck.geometry;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

import turtleduck.geometry.impl.Point2;
import turtleduck.geometry.impl.Point3;

public interface Point extends PositionVector {
    static final Point ZERO = point(0, 0);

    static Point point(double x, double y) {
        return new Point2(x, y);
    }

    static Point point(double x, double y, double z) {
        if (z == 0.0)
            return new Point2(x, y);
        else
            return new Point3(x, y, z);
    }

    /**
     * Calculate direction towards other point
     *
     * @param otherPoint
     * @return
     */
    default Direction directionTo(PositionVector otherPoint) {
        return directionTo(otherPoint, Direction.DUE_NORTH);
    }

    /**
     * Calculate direction towards other point.
     * 
     * @param otherPoint The other point
     * @param dflt       Default direction, if otherPoint is too close to this to
     *                   determine a direction
     * @return Direction from this to otherPoint
     */
    Direction directionTo(PositionVector otherPoint, Direction dflt);

    /**
     * Calculate direction towards other point
     *
     * @param otherPoint
     * @return
     */
    double angleTo(PositionVector otherPoint);

    /**
     * Calculate distance to other point
     *
     * @param otherPoint
     * @return
     */
    double distanceTo(PositionVector otherPoint);

    /**
     * @return The X coordinate
     */
    double x();

    /**
     * @return The Y coordinate
     */
    double y();

    /**
     * @return The Z coordinate (normally zero)
     */
    double z();

    /**
     * Relative move
     *
     * @param deltaX
     * @param deltaY
     * @return A new point at x+deltaX, y+deltaY
     */
    Point add(double deltaX, double deltaY);

    /**
     * Relative move
     *
     * @param dir      Direction
     * @param distance Distance to move
     */
    Point add(DirectionVector dir, double distance);

    /**
     * Relative move
     *
     * @param other
     */
    Point add(PositionVector other);

    /**
     * Change point
     *
     * @param newX the new X coordinate
     * @param newY the new Y coordinate
     * @return A new point at newX, newY
     */
    Point xy(double newX, double newY);

    Point xyz(double newX, double newY, double newZ);

    @Override
    String toString();

    /**
     * Multiply this point by a scale factor.
     *
     * @param factor A scale factor
     * @return A new IPoint, (getX()*factor, getY()*factor)
     */
    Point scale(double factor);

    /**
     * Find difference between points.
     * <p>
     * The returned value will be such that
     * <code>this.move(deltaTo(point)).equals(point)</code>.
     *
     * @param other Another point
     * @return A new Point, (point.getX()-getX(), point.getY()-getY())
     */
    Point sub(PositionVector other);

    /**
     * @return Bearing from (0,0) to this point
     */
    Direction asBearing();

    /**
     * @return The distance from (0,0) to this point
     */
    double asLength();

    double distanceToSq(PositionVector otherPoint);

    boolean isLeftOf(PositionVector otherPoint);

    boolean isAbove(PositionVector otherPoint);

    Point interpolate(Point otherPoint, double fraction);

    @Override
    int hashCode();

    @Override
    boolean equals(Object other);

    /**
     * Fuzzy equals
     * 
     * @param other another point
     * @return True if the difference between this and other is < 10e-6
     */
    boolean like(PositionVector other);

    static Point point(PositionVector point) {
        if (point instanceof Point)
            return (Point) point;
        else if (point.z() == 0)
            return point(point.x(), point.y());
        else
            return point(point.x(), point.y(), point.z());
    }

    Vector3f toVector(Vector3f dest);

    Vector3d toVector(Vector3d dest);

    Vector4f toVector(Vector4f dest);

    Vector4d toVector(Vector4d dest);

    static Point point(Vector3d position) {
        return point(position.x, position.y, position.z);
    }

    static Point point(Vector3f position) {
        return point(position.x, position.y, position.z);
    }

    static Point point(Vector4d position) {
        return point(position.x, position.y, position.z);
    }

    static Point point(Vector4f position) {
        return point(position.x, position.y, position.z);
    }

}