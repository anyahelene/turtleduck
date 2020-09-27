package turtleduck.scene;

public interface SceneVisitor<T, C extends RenderContext> {
	T visitElement(SceneNode elt, C context);

	T visitContainer(SceneContainer<?> container, C context);

	T visitGroup3(SceneGroup3<?> group, C context);

	T visitTransform2(SceneTransform2<?> container, C context);

	T visitObject2(SceneObject2<?> obj, C context);

	T visitObject3(SceneObject3<?> obj, C context);

	T visitNode(SceneNode obj, C context);

	T visitWorld(SceneWorld world, C context);

	T visitCamera(Camera camera, C context);

}
