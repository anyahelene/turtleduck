package turtleduck.sprites;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.paths.impl.PathPointImpl;
import turtleduck.turtle.impl.BaseNavigatorImpl;

public abstract class AbstractSprite extends BaseNavigatorImpl<Sprite> implements Sprite {
	public AbstractSprite(Point p, Direction b) {
		super(p, b);
	}
	public AbstractSprite(AbstractSprite old) {
		super(old);
	}
	@Override
	protected void addPoint(PathPointImpl point) {
	
	}

}
