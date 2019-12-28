package turtleduck.geometry;

import turtleduck.geometry.impl.Point2;
import turtleduck.geometry.impl.Point3;

/**
 * @author anya
 *
 */
public class Direction {
	/**
	 * Construct direction from an angle
	 *
	 * @param degrees
	 *            Angle in degrees, where 0 is (1,0)
	 */
	public static Direction fromDegrees(double degrees) {
		return new Direction(degrees);
	}

	/**
	 * Construct direction from an angle
	 *
	 * @param radians
	 *            Angle in radians, where 0 is (1,0)
	 */
	public static Direction fromRadians(double radians) {
		return new Direction(Math.toRadians(radians));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Direction)) {
			return false;
		}
		Direction other = (Direction) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
			return false;
		}
		return true;
	}

	/**
	 * Construct direction from a vector
	 *
	 * @param x
	 *            X direction
	 * @param y
	 *            Y direction
	 */
	public static Direction fromVector(double x, double y) {
		return new Direction(x, y, 0);
	}

	/**
	 * Construct direction from a vector
	 *
	 * @param x
	 *            X direction
	 * @param y
	 *            Y direction
	 * @param z
	 *            Z direction
	 */
	public static Direction fromVector(double x, double y, double z) {
		return new Direction(x, y, z);
	}

	private double x;

	private double y;

	private double z;

	/**
	 * Create a new direction.
	 *
	 * The direction vector will be normalised to a vector of length 1.
	 *
	 * @param radians
	 *            Angle of direction in radians
	 */
	public Direction(double radians) {
		this.x = Math.cos(radians);
		this.y = Math.sin(radians);
		normalize();
	}

	/**
	 * Create a new direction.
	 *
	 * The direction vector will be normalised to a vector of length 1.
	 *
	 * @param xDir
	 *            X-component of direction vector
	 * @param yDir
	 *            Y-component of direction vector
	 */
	public Direction(double xDir, double yDir, double zDir) {
		this.x = xDir;
		this.y = yDir;
		this.z = zDir;
		normalize();
	}

	/**
	 * Multiply direction by distance
	 *
	 * @param distance
	 * @return Position delta
	 */
	public Point getMovement(double distance) {
		if (z == 0) {
			return new Point2(x * distance, y * distance);
		} else {
			return new Point3(x * distance, y * distance, z * distance);
		}
	}

	/**
	 * @return X-component of direction vector
	 *
	 *         Same as the Math.cos(toRadians())
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return Y-component of direction vector
	 *
	 *         Same as the Math.sin(toRadians())
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return Z-component of direction vector (normally zero)
	 *
	 *         Same as 0
	 */
	public double getZ() {
		return z;
	}

	private void normalize() {
		double l = Math.sqrt(x * x + y * y + z * z);
		if (l >= 0.00001) {
			x = x / l;
			y = y / l;
			z = z / l;
		} else if (x > 0) {
			x = 1;
			y = 0;
			z = 0;
		} else if (x < 0) {
			x = -1;
			y = 0;
			z = 0;
		} else if (y > 0) {
			x = 0;
			y = 1;
			z = 0;
		} else if (y < 0) {
			x = 0;
			y = -1;
			z = 0;
		} else if (z > 0) {
			x = 0;
			y = 0;
			z = 1;
		} else if (z < 0) {
			x = 0;
			y = 0;
			z = -1;
		} else {
			x = 1;
			y = 0;
		}

	}

	/**
	 * Translate to angle (in degrees)
	 *
	 * @return Angle in degrees, -180 .. 180
	 */
	public double toDegrees() {
		return Math.toDegrees(Math.atan2(y, x));
	}

	/**
	 * Translate to angle (in radians)
	 *
	 * @return Angle in radians, -2π .. 2π
	 */
	public double toRadians() {
		return Math.atan2(y, x);
	}

	@Override
	public String toString() {
		return String.format("%.2f°", toDegrees());
	}

	/**
	 * Turn (relative)
	 *
	 * @param deltaDir
	 */
	public Direction turn(Direction deltaDir) {
		return new Direction(x + deltaDir.x, y + deltaDir.y, z + deltaDir.z);
	}

	/**
	 * Turn / rotate around Z-axis
	 *
	 * @param angle
	 *            (in degrees)
	 */
	public Direction turn(double angle) {
		return rotZ(Math.toRadians(angle));
	}

	public Direction rotX(double radians) {
		if (radians == 0.0) {
			return this;
		}
		double cos = Math.cos(radians), sin = Math.sin(radians);
		return new Direction(x, y * cos - z * sin, y * sin + z * cos);
	}

	public Direction rotY(double radians) {
		if (radians == 0.0) {
			return this;
		}
		double cos = Math.cos(radians), sin = Math.sin(radians);
		return new Direction(x * cos - z * sin, y, -x * sin + z * cos);
	}

	public Direction rotZ(double radians) {
		if (radians == 0.0) {
			return this;
		}
		double cos = Math.cos(radians), sin = Math.sin(radians);
		return new Direction(x * cos - y * sin, x * sin + y * cos, z);
	}

	/**
	 * Turn around 180 degrees
	 */
	public Direction turnBack() {
		return turn(180.0);
	}

	/**
	 * Turn left 90 degrees
	 */
	public Direction turnLeft() {
		return turn(90.0);
	}

	/**
	 * Turn right 90 degrees
	 */
	public Direction turnRight() {
		return turn(-90.0);
	}

	/**
	 * Absolute turn
	 *
	 * @param degrees
	 *            Angle in degrees, where 0 is (1,0)
	 */
	public Direction turnTo(double degrees) {
		double radians = Math.toRadians(degrees);
		return new Direction(Math.cos(radians), Math.sin(radians), 0);
	}

	/**
	 * Turn slightly towards a directions
	 *
	 * @param dir
	 *            A direction
	 * @param percent
	 *            How much to turn (100.0 is the same as turnTo())
	 */
	public Direction turnTowards(Direction dir, double percent) {
		return new Direction(x * (1.00 - percent / 100.0) + dir.x * (percent / 100.0),
				y * (1.00 - percent / 100.0) + dir.y * (percent / 100.0),
				z * (1.00 - percent / 100.0) + dir.z * (percent / 100.0));
		// double thisAngle = toAngle();
		// double otherAngle = dir.toAngle();
		// turnTo(thisAngle*(1.00 - percent/100.0) +
		// otherAngle*(percent/100.0));
	}

}
