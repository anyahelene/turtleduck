package turtleduck.turtle;

public interface Pen extends Stroke, Fill {
	/**
	 * Describes the smoothness of a path as it passes through a point. When
	 * building a path, this can be used to create smooth paths without explicitly
	 * adding control points.
	 * <p>
	 * Points on straight-line
	 * (non-<a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">Bézier</a>)
	 * paths are always {@link #CORNER}.
	 * <ul>
	 * <li>A {@link #CORNER} has a sharp break at any angle; the direction of the
	 * incoming and outgoing line segments are unrelated. This is a
	 * <em>C<sup>0</sup></em> continuous curve; the curve itself is continuous, but
	 * the first derivative is not.
	 * <li>A {@link #SMOOTH} point will have the incoming and outgoing line segments
	 * form a straight line at that point; the control points will be on a straight
	 * line on opposite sides of the point itself. This is a <em>C<sup>1</sup></em>
	 * continuous curve; the curve itself and the first derivative is continuous.
	 * <li>A {@link #SYMMETRIC} is smooth, but with the same distance to the control
	 * points on each side.This is a <em>C<sup>2</sup></em> continuous curve; the
	 * curve itself, the first and the second derivative is continuous.
	 * <ul>
	 * 
	 */
	enum SmoothType {
		/**
		 * <li>A {@link #CORNER} has a sharp break at any angle; the direction of the
		 * incoming and outgoing line segments are unrelated. This is a <a href=
		 * "https://en.wikipedia.org/wiki/Smoothness#Parametric_continuity"><em>C<sup>0</sup></em>
		 * continuous curve</a>; the curve itself is continuous, but the first
		 * derivative is not.
		 */
		CORNER,
		/**
		 * <li>A {@link #SMOOTH} point will have the incoming and outgoing line segments
		 * form a straight line at that point; the control points will be on a straight
		 * line on opposite sides of the point itself. This is a <a href=
		 * "https://en.wikipedia.org/wiki/Smoothness#Parametric_continuity"><em>C<sup>1</sup></em>
		 * continuous curve</a>; the curve itself and the first derivative is
		 * continuous.
		 */
		SMOOTH,
		/**
		 * A {@link #SYMMETRIC} point is also {@link #SMOOTH}, but with the same
		 * distance to the control points on each side.This is a <a href=
		 * "https://en.wikipedia.org/wiki/Smoothness#Parametric_continuity"><em>C<sup>1</sup></em>
		 * continuous curve</a>; the curve itself, the first and the second derivative
		 * is continuous.
		 * 
		 */
		SYMMETRIC
	}

	PenBuilder<Pen> change();

	@Override
	int hashCode();
	
	@Override
	boolean equals(Object other);
	}
