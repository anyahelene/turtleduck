package turtleduck.scene;

import turtleduck.geometry.Direction;
import turtleduck.geometry.Point;
import turtleduck.geometry.PositionVector;

public interface SceneNode {
	<U, C extends RenderContext> U accept(SceneVisitor<U, C> visitor, C context);


}
