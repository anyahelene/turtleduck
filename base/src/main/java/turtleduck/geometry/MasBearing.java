package turtleduck.geometry;

import java.util.logging.Logger;

public class MasBearing implements DirectionVector, Bearing {
	static final Bearing DUE_NORTH = absolute(0), DUE_EAST = absolute(90), DUE_SOUTH = absolute(180),
			DUE_WEST = absolute(270);

	static final double SIGN_LEFT = -1, SIGN_RIGHT = 1;
	public static final double HALF_PI = Math.PI / 2;
	public static final double TWO_PI = 2 * Math.PI;
	public static final double THREE_PI = 2 * Math.PI;
	public static final int ARCSEC = 3600, MARCSEC = ARCSEC * 100, MI = 180 * MARCSEC;
	public static final int TWO_MI = 360 * MARCSEC;
	static final int ARCSEC_NORTH = 360 * MARCSEC, ARCSEC_EAST = 90 * MARCSEC, //
			ARCSEC_SOUTH = 180 * MARCSEC, ARCSEC_WEST = 270 * MARCSEC;
	public static final double HALF_SQRT_2 = Math.sqrt(2) / 2, HALF_SQRT_3 = Math.sqrt(3) / 2;
	private final int mas;
	private final boolean absolute;

	protected MasBearing(int angle, boolean absolute) {
		if(angle < 0)
			angle += TWO_MI;
		mas = Math.floorMod(angle, TWO_MI);
		this.absolute = absolute;
	}

	public static int degreesToBrad(double angle) {
		if(angle < 0)
			angle += 360;
		int m = Math.floorMod(Math.round(angle * MARCSEC), TWO_MI);
		return angle < 0 ? -m : m;
	}

	public static int radiansToBrad(double angle) {
		int m = Math.floorMod(Math.round(Math.PI * angle * MARCSEC / 180.0), TWO_MI);
		return m;
	}

	public static double bradToDegrees(int angle) {
//		if (angle == Short.MIN_VALUE)
//			return 180.0;
//		else
		double a = angle % MARCSEC;
		return ((double) angle) / MARCSEC;
	}

	public static double bradToRadians(int angle) {
//		if (angle == Short.MIN_VALUE)
//			return Math.PI;
//		else
		return ((double) angle) * Math.PI / MI;
	}

	public static MasBearing absolute(double a) {
		return new MasBearing(degreesToBrad(a), true);
	}

	public static MasBearing relative(double a) {
		return new MasBearing(degreesToBrad(a), false);
	}

	public static MasBearing absolute(double x, double y) {
		return new MasBearing(atan2(x, y), true);
	}

	public static Bearing relative(double x, double y) {
		return new MasBearing(atan2(x, y), false);
	}

	@Override
	public double azimuth() {
		return bradToDegrees(mas);
	}

	/*
	 * default double degrees() { return Math.toDegrees(azimuth()); }
	 * 
	 * default double degreesTo(Bearing other) { return
	 * Math.toDegrees(angleTo(other)); }
	 */

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public double toRadians() {
		return bradToRadians(mas);
	}

	@Override
	public Bearing add(Bearing other) {
		int b = ((MasBearing) other).mas;
		if (!absolute)
			return new MasBearing(mas + b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new MasBearing(mas + b, true);
		} else {
			Logger.getLogger("TurtleDuck").warning("Adding two absolute bearings: " + this + " + " + other);
			return new MasBearing(mas + b, false);
		}
	}

	@Override
	public Bearing sub(Bearing other) {
		int b = ((MasBearing) other).mas;
		if (!absolute)
			return new MasBearing(mas - b, other.isAbsolute());
		else if (!other.isAbsolute()) {
			return new MasBearing(mas - b, true);
		} else {
			return new MasBearing(mas - b, false);
		}
	}

	@Override
	public Bearing interpolate(Bearing other, double t) {
		if (t <= 0.0)
			return this;
		else if (t >= 1.0)
			return other;
		int a = mas, b = ((MasBearing) other).mas;
		if (a + Short.MAX_VALUE < b)
			a += 65536;
		else if (b + Short.MAX_VALUE < a)
			b += 65536;
		return new MasBearing((short) Math.round(a + (b - a) * t), absolute);
	}

	@Override
	public String toNavString() {
		double d = bradToDegrees(mas);
		if (absolute) {
			assert mas >= 0 && mas < ARCSEC_NORTH : String.format("0 <= %d < %d", mas, ARCSEC_NORTH);
			if (mas == 0) {
				return "N";
			} else if (mas == ARCSEC_EAST) {
				return "E";
			} else if (mas == ARCSEC_SOUTH) {
				return "S";
			} else if (mas == ARCSEC_WEST) {
				return "W";
			} else if (mas < ARCSEC_EAST) {
				return "N" + (d) + "°E";
			} else if (mas < ARCSEC_SOUTH) {
				return "S" + (180 - d) + "°E";
			} else if (mas < ARCSEC_WEST) {
				return "S" + (d - 180) + "°W";
			} else if (mas < ARCSEC_NORTH) {
				return "N" + (360 - d) + "°W";
			}
			return String.format("%06.2f°", d);
		} else {
//			assert mas > -180 * MARCSEC && mas <= 180 * MARCSEC : String.format("%d <= %d < %d", -180 * MARCSEC, mas,
//					180 * MARCSEC);
			if (mas == 0)
				return "0°";
			else if (mas == 180 * MARCSEC)
				return "180°";
			else if (mas < 180 * MARCSEC)
				return "G" + d + "°";
			else
				return "R" + (360-d) + "°";
		}
	}

	@Override
	public String toArrow() {
			if (mas == 0)
				return "↑";
			else if (mas < ARCSEC_EAST)
				return "↗";
			else if (mas == ARCSEC_EAST)
				return "→";
			else if (mas < ARCSEC_SOUTH)
				return "↘";
			else if (mas == ARCSEC_SOUTH)
				return "↓";
			else if (mas < ARCSEC_WEST)
				return "↙";
			else if (mas == ARCSEC_WEST)
				return "←";
			else
				return "↖";
//		} else {
//			if (Math.abs(mas) == 0)
//				return "↑";
//			else if (Math.abs(mas) == Math.PI)
//				return "⟳";
//			else if (mas <= -Math.PI / 2)
//				return "⤹";
//			else if (mas < 0)
//				return "↶";
//			else if (mas <= Math.PI / 2)
//				return "↷";
//			else // if(brad <= 3*Math.PI/4)
//				return "⤸";
//		}
	}

	@Override
	public String toString() {
		return toArrow() + (absolute || mas < 0 ? "" : "+") + bradToDegrees(mas) + "°";
	}


	static double sin(int a) {
		double sign = -1;
		if (a < 0)
			a += TWO_MI;
		else if (a >= TWO_MI)
			a -= TWO_MI;
		if (a > MI) // reflect horiz
			a = TWO_MI - a;
		if (a > MI / 2) { // reflect vertical
			a = MI - a;
			sign = 1;
		}

		switch (a) {
		case 0:
			return sign;
		case MI / 2:
			return 0;
		case MI / 3:
			return sign * 0.5;
		case MI / 4:
			return sign * HALF_SQRT_2;
		case MI / 6:
			return sign * HALF_SQRT_3;
		default:
			return sign * Math.cos(Math.PI * a / MI);
		}
	}

	static double cos(int a) {
		double sign = 1;
		if (a < 0)
			a += TWO_MI;
		else if (a >= TWO_MI)
			a -= TWO_MI;
		if (a > MI) { // reflect horiz
			a = TWO_MI - a;
			sign = -1;
		}
		if (a > MI / 2) { // reflect vertical
			a = MI - a;
		}

		switch (a) {
		case 0:
			return 0;
		case MI / 2:
			return sign;
		case MI / 3:
			return sign * HALF_SQRT_3;
		case MI / 4:
			return sign * HALF_SQRT_2;
		case MI / 6:
			return sign * 0.5;
		default:
			return sign * Math.sin(Math.PI * a / MI);
		}
	}

	static int atan2(double x, double y) {
		double absX = Math.abs(x), absY = Math.abs(y);
		if (absX < 1e-10) {
			if (absY < 1e-10)
				throw new ArithmeticException(String.format("atan2(%g,%g", x, y));
			else
				return y > 0 ? MI : 0;
		} else if (absY < 1e-10) {
			if (x > 0)
				return MI / 2;
			else
				return -MI / 2;
		} else {
			int m = degreesToBrad(Math.toDegrees(Math.atan(y / x)) + 90);
			if (x < 0)
				return m - MI;
			else
				return m;
		}
	}

	/*
	 * static double dirToRad(double x, double y) { return Math.atan2(x, -y); }
	 * 
	 * static double radToDirX(double a) { return Math.sin(a); }
	 * 
	 * static double radToDirY(double a) { return -Math.cos(a); }
	 */
	@Override
	public double dirX() {
		return cos(mas);
	}

	@Override
	public double dirY() {
		return sin(mas);
	}

	@Override
	public double dirZ() {
		return 0;
	}
}
