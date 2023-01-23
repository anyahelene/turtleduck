package turtleduck.geometry;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import turtleduck.geometry.impl.AngleImpl;
import turtleduck.geometry.impl.DirVecImpl;

public interface Direction extends DirectionVector {
	static final int DEGREES_NORTH = 90, DEGREES_WEST = 180, DEGREES_SOUTH = -90, DEGREES_EAST = 0;
	static final Direction DUE_NORTH = absolute(DEGREES_NORTH), DUE_WEST = absolute(DEGREES_WEST),
			DUE_SOUTH = absolute(DEGREES_SOUTH), DUE_EAST = absolute(DEGREES_EAST);
	static final Direction FORWARD = relative(0), RIGHT = relative(-90), BACK = relative(180), LEFT = relative(90);

	/**
	 * Create a new absolute direction
	 * 
	 * @param a absolute angle in degrees
	 * @return the direction
	 */
	public static Direction absolute(double a) {
		return AngleImpl.absolute(a);
	}

	/**
	 * Create a new relative direction.
	 * 
	 * Relative directions are intended to be added to absolute directions.
	 * 
	 * @param a relative angle in degrees
	 * @return the direction
	 */
	public static Direction relative(double a) {
		return AngleImpl.relative(a);
	}

	/**
	 * Create a new absolute direction from a vector
	 * 
	 * The angle is calculated using {@link Math#atan2(double, double)}.
	 * 
	 * @param x x component of vector
	 * @param y y component of vector
	 * @return the direction from (0,0) towards (x,y)
	 */
	public static Direction absolute(double x, double y) {
		return AngleImpl.absolute(x, y);
	}

	/**
	 * Create a new relative direction from a vector
	 * 
	 * The angle is calculated using {@link Math#atan2(double, double)}.
	 * 
	 * @param x x component of vector
	 * @param y y component of vector
	 * @return the direction from (0,0) towards (x,y)
	 */
	public static Direction relative(double x, double y) {
		return AngleImpl.relative(x, y);
	}

	/**
	 * Return direction angle, measured in degrees
	 * 
	 * For {@link #isAbsolute()}, this is the angle between {@link #DUE_NORTH} and
	 * the current direction.
	 * 
	 * @return Angle of this direction
	 * @see #radians()
	 */
	double degrees();

	/**
	 * 
	 * @return True if this direction is *absolute*
	 */
	boolean isAbsolute();

	/**
	 * Return direction angle, measured in radians
	 * 
	 * For {@link #isAbsolute()}, this is the angle between {@link #DUE_NORTH} and
	 * the current direction.
	 * 
	 * @return Angle of this direction
	 * @see #degrees()
	 */
	double radians();

	/**
	 * Add two directions
	 * 
	 * The angle of the resulting direction will be this.degrees()+other.degrees()
	 * (normalized).
	 * 
	 * At least one of the directions must be *relative*.
	 * 
	 * @param other another direction
	 * @return Sum of this and other
	 */
	Direction add(Direction other);

	/**
	 * Subtract two directions
	 * 
	 * The angle of the resulting direction will be this.degrees()-other.degrees()
	 * (normalized).
	 * 
	 * @param other another direction
	 * @return Difference between this and other
	 */
	Direction sub(Direction other);

	/**
	 * Interpolate linearly between two directions
	 * 
	 * Generally, there are two possible solutions to interpolation – clockwise and
	 * counter-clockwise. This method will pick whichever gives the smallest change
	 * in direction. If *other* is the inverse of *this* (i.e., they are 180°
	 * apart), there are two valid answers and this method may pick either one of
	 * them.
	 * 
	 * 
	 * @param other the other direction
	 * @param t     interpolation factor from 0 (this) to 1 (other)
	 * @return a new direction between this and other
	 */
	Direction interpolate(Direction other, double t);

	/**
	 * Return a navigational string representation of the direction
	 * 
	 * For absolute directions, this will be something like "N" or "N45°E".
	 * 
	 * For relative directions, this will be either 0°, 180° or something like G45°
	 * (*green* – starboard, turning right) or R45° (*red* – port, turning left).
	 * 
	 * @return direction as a string
	 */
	String toNavString();

	/**
	 * Represent the direction as an arrow symbol
	 * 
	 * @return A string with the Unicode arrow symbol most closely matching the
	 *         direction
	 */
	String toArrow();

	/**
	 * Return a string with the direction's angle in degrees.
	 * 
	 * Relative directions will have a sign, absolute directions will be 0°–360°
	 * 
	 * @return direction as a string
	 */
	String toString();

	/**
	 * @return X-component of direction vector
	 */
	double dirX();

	/**
	 * @return Y-component of direction vector
	 */
	double dirY();

	/**
	 * @return Z-component of direction vector (always zero for 2D directions)
	 */
	double dirZ();

	@Override
	int hashCode();

	@Override
	boolean equals(Object other);

	/**
	 * @return True if this is a direction in 3D space
	 * @see {@link Orientation} for 3D operations
	 */
	boolean is3d();

	/**
	 * Directions in 3D space can be represented by a horizontal angle (the azimuth)
	 * and a vertical angle (the altitude or elevation).
	 * 
	 * {@link #degrees()} gives the azimuth, this method gives the altitude.
	 * 
	 * @return the 'altitude' of a 3D direction
	 */
	double altDegrees();

	/**
	 * Find a Vector3d perpendicular to this direction
	 * 
	 * The result is written to *dest*
	 * 
	 * @param dest destination vector, not null
	 * @return dest, set to a vector perpendicular to
	 *         {@link #directionVector(Vector3d)}
	 */
	Vector3d perpendicular(Vector3d dest);

	/**
	 * Find a Vector3d perpendicular to this direction
	 * 
	 * The result might be a shared constant.
	 * 
	 * @return a vector perpendicular to this direction
	 */
	default Vector3dc perpendicular() {
		return perpendicular(new Vector3d());
	}

	/**
	 * Convert to Vector3d
	 * 
	 * The result is written to *dest*
	 * 
	 * @param dest destination vector, not null
	 * @return dest, set to this direction
	 */
	Vector3d directionVector(Vector3d dest);

	/**
	 * Convert to Vector3d
	 * 
	 * For 3D directions, the result is most likely a shared constant.
	 * 
	 * @return this direction as a vector
	 */
	default Vector3dc directionVector() {
		return directionVector(new Vector3d());
	}

	/**
	 * Get the normal (the "up" direction) of this direction.
	 * 
	 * The result is written to *dest*
	 * 
	 * For 2D directions this will be (0,0,1), i.e., the Z-axis points up.
	 * 
	 * @param dest destination vector or null
	 * @return dest, set to this direction's normal
	 */
	Vector3d normalVector(Vector3d dest);

	/**
	 * Get the normal (the "up" direction) of this direction.
	 * 
	 * The result might be a shared constant.
	 * 
	 * For 2D directions this will be a shared constant (0,0,1), i.e., the Z-axis
	 * points up.
	 * 
	 * @return this direction's normal vector
	 */
	default Vector3dc normalVector() {
		return normalVector(new Vector3d());
	}

	/**
	 * Rotate *angle* degrees
	 * 
	 * This is normal 2D rotation, around the Z-axis.
	 * 
	 * Equivalent to this.add(Direction.relative(angle))
	 * 
	 * @param angle the angle to rotate
	 * @return the rotated direction
	 */
	Direction yaw(double angle);

	/**
	 * Rotate *angle* degrees around the X-axis
	 * 
	 * If this is a 2D direction, the result will be a 3D {@link Orientation} with
	 * an {@link #altDegrees()} of *angle* and "up" being the Z-axis.
	 * 
	 * @param angle the angle to rotate
	 * @return the rotated direction
	 */
	Orientation pitch(double angle);

	/**
	 * Rotate *angle* degrees around the Y-axis
	 * 
	 * If this is a 2D direction, the result will be a 3D {@link Orientation}
	 * pointing in the same direction and "up" being *angle* degrees off the Z-axis.
	 * 
	 * @param angle the angle to rotate
	 * @return the rotated direction
	 */
	Orientation roll(double angle);

	/**
	 * Fuzzy equals
	 * 
	 * @param other another direction
	 * @return True if the difference between this and other is < 10e-6
	 */
	boolean like(Direction other);

	Direction rotateTo(Direction other);

	Orientation toOrientation();

	Direction rotateTo(double angle);

	Point transform(Point point);
}