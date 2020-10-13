package turtleduck.scene;

public interface SceneNode {
	<T, C extends RenderContext<C>> T accept(SceneVisitor<T, C> visitor, C context);


}
