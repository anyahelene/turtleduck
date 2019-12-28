package turtleduck.geometry;

public interface Projection {

	Point project(Point pos);
	/*
	 * public double projectX(IPosition pos) { return Math.max(0, Math.min(width, x0
	 * + pCamX + pos.getX() * focalLength / (pos.getZ() + 1))); }
	 *
	 * public double projectY(IPosition pos) { return Math.max(0, Math.min(height,
	 * y0 + (pCamY - pos.getY() - pos.getZ()) * focalLength / (pos.getZ() + 1))); }
	 */

	void setRotation(double rotX, double rotY);

	void setScale(double sx, double sy, double sz);

	void setTranslate(double dx, double dy, double dz);

}