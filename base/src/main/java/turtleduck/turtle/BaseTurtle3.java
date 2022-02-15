package turtleduck.turtle;

import turtleduck.annotations.Icon;
import turtleduck.annotations.Internal;
import turtleduck.geometry.Point;

@Icon("🐢")
@Internal
public interface BaseTurtle3<T extends BaseTurtle3<T, C>, C> extends BaseTurtle<T, C> {

}
