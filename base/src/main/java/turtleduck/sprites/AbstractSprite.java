package turtleduck.sprites;

import turtleduck.geometry.Bearing;
import turtleduck.geometry.Point;
import turtleduck.turtle.impl.NavigatorImpl;
import turtleduck.turtle.impl.PathPointImpl;

public abstract class AbstractSprite extends NavigatorImpl<Sprite> implements Sprite {
	public AbstractSprite(Point p, Bearing b) {
		super(p, b);
	}
	public AbstractSprite(AbstractSprite old) {
		super(old);
	}
	@Override
	protected void addPoint(PathPointImpl point) {
	
	}

}
