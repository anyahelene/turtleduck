package turtleduck.geometry.impl;

import turtleduck.geometry.Point;
import turtleduck.geometry.Projection;

public class OrthographicProjection implements Projection {
	private double dx, dy;
	private double sx, sy;

	public OrthographicProjection(double width, double height) {
		dx = width / 2;
		dy = height / 2;
		sx = 1;
		sy = -1;
	}

	@Override
	public Point project(Point pos) {
		return new Point2(pos.x() * sx + dx, pos.y() * sy + dy);
	}

	@Override
	public void setRotation(double rotX, double rotY) {

	}

	@Override
	public void setScale(double sx, double sy, double sz) {
		this.sx = sx;
		this.sy = sy;
	}

	@Override
	public void setTranslate(double dx, double dy, double dz) {
		this.dx = dx;
		this.dy = dy;
	}

}
