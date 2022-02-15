package turtleduck.sprites;

import org.joml.Matrix3x2d;

import turtleduck.canvas.Canvas;
import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.messaging.CanvasService;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.turtle.impl.BaseNavigatorImpl;
import turtleduck.util.Array;
import turtleduck.util.Dict;

public class SpriteImpl extends BaseNavigatorImpl<Sprite> implements Sprite {
	protected Matrix3x2d matrix = new Matrix3x2d();
	protected String transition = "0s";
	private String objId;
	private Canvas canvas;

	public SpriteImpl(Point p, Direction b, String objId, Canvas canvas) {
		super(p, b);
		this.objId = objId;
		this.canvas = canvas;
	}

	@Override
	protected void addPoint(PathPointImpl point) {
		System.out.println("old: " + point.point + ", ");
	}

	@Override
	public Sprite update() {
		matrix.translation(current.point.x(), current.point.y()).rotate(direction.radians());
		Dict dict = Dict.create();
		dict.put("_transform", // String.format("matrix(%f, %f, %f, %f, %f, %f)", //
				Array.of(matrix.m00, matrix.m01, matrix.m10, //
						matrix.m11, matrix.m20, matrix.m21));
		dict.put("transition", transition);
		canvas.service().styleObject(objId, dict);
		return this;
	}

	@Override
	public Sprite transition(String spec) {
		if (spec != null)
			transition = spec;
		else
			transition = "0s";
		return this;
	}

	@Override
	public String id() {
		return objId;
	}

}
