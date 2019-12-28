package turtleduck.geometry.impl;

import turtleduck.display.Screen;
import turtleduck.geometry.Point;
import turtleduck.geometry.Projection;

public class PerspectiveProjection implements Projection {
	private final double width;
	private final double height;
	private double x0, y0;
	public static double focalLength = 100;
	/**
	 * The camera angles, in radians.
	 *
	 * The angles define rotation around the X (pitch – up/down), Y (yaw – left/right) and Z (roll) axes.
	 */
	public static double aCamX = 0 /*-Math.PI/200*/, aCamY = 0, aCamZ = 0;
	/**
	 * The camera position
	 */
	public static double pCamX = 0, pCamY = 2, pCamZ = -1000;
	public double aRotY = 0, sRotY = 0, cRotY = 0;
	public double aRotX = 0, sRotX = 0, cRotX = 0;
	public double X, Y;

	public PerspectiveProjection(Screen screen) {
		width = screen.getWidth();
		height = screen.getHeight();
		x0 = width / 2;
		y0 = height / 2;
		setRotation(0,0);
	}

	/* (non-Javadoc)
	 * @see gfx.gfxmode.IProjection#project(gfx.gfxmode.IPoint3)
	 */
	@Override
	public Point project(Point pos) {
		double px = pos.getX(), py = pos.getY(), pz = pos.getZ();

		double x = px * cRotY - pz * sRotY;// - z*cRotY; //x + 0*x*cRotY + z*sRotY;
		double z = pz * cRotY * cRotX + px * sRotY * sRotX;// + x*sRotY; //x*sRotY + z + 0*z*cRotY;
		double y = py * cRotX + pz * sRotX;
		X = x;
		Y = z;
		x -= pCamX;// *cRotY-pCamZ*sRotY;
		y -= pCamY;
		z -= pCamZ;// *cRotY+pCamX*sRotY;
		double cx = Math.cos(aCamX), cy = Math.cos(aCamY), cz = Math.cos(aCamZ);
		double sx = Math.sin(aCamX), sy = Math.sin(aCamY), sz = Math.sin(aCamZ);

		double dx = cy * (sz * y + cz * x) - sy * z;
		double dy = sx * (cy * z + sy * (sz * y + cz * x)) + cx * (cz * y - sz * x);
		double dz = cx * (cy * z + sy * (sz * y + cz * x)) - sx * (cz * y - sz * x);
		if (dz <= 0) {
			dz = 0.01;
		}
		double bx = x0 + 10 * dx * focalLength / dz;
		double by = y0 - 10 * dy * focalLength / dz;
		// System.out.printf("(%5.0f,%5.0f,%5.0f) -> (%5.0f,%5.0f,%5.0f) ->
		// (%4.0f,%4.0f)%n", pos.getX(), pos.getY(),
		// pos.getZ(), dx, dy, dz, bx, by);

		if (dz > 0) {
			return new Point2(bx, by);
		} else {
			return null;
		}
	}
	/*
	 * public double projectX(IPosition pos) { return Math.max(0, Math.min(width, x0
	 * + pCamX + pos.getX() * focalLength / (pos.getZ() + 1))); }
	 *
	 * public double projectY(IPosition pos) { return Math.max(0, Math.min(height,
	 * y0 + (pCamY - pos.getY() - pos.getZ()) * focalLength / (pos.getZ() + 1))); }
	 */

	public static void rotCam(double ax, double ay, double az) {
		aCamX += ax / 10.0;
		aCamY += ay / 10.0;
		aCamZ += az / 10.0;
		System.out.printf("Camera: (%.1f,%.1f,%.1f) (%.5f,%.5f,%.5f)°%n", pCamX, pCamY, pCamZ, (aCamX), (aCamY),
				(aCamZ));
	}

	public static void moveCam(double dx, double dy, double dz) {
		pCamX += dx;
		pCamY += dy;
		pCamZ += dz;
		System.out.printf("Camera: (%.1f,%.1f,%.1f) (%.1f,%.1f,%.1f)°%n", pCamX, pCamY, pCamZ, (aCamX), (aCamY),
				(aCamZ));
	}

	/* (non-Javadoc)
	 * @see gfx.gfxmode.IProjection#setRotation(double, double)
	 */
	@Override
	public void setRotation(double rotX, double rotY) {
		aRotX = rotX;
		sRotX = Math.sin(aRotX);
		cRotX = Math.cos(aRotX);
		aRotY = rotY;
		sRotY = Math.sin(aRotY);
		cRotY = Math.cos(aRotY);
	}

	@Override
	public void setScale(double sx, double sy, double sz) {
	}

	@Override
	public void setTranslate(double dx, double dy, double dz) {
		this.x0 = dx;
		this.y0 = dy;
	}

}
