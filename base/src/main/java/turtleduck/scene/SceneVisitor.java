package turtleduck.scene;

public interface SceneVisitor<T, C extends RenderContext<C>> {
	T visitElement(SceneNode elt, C context);

	default T visitContainer(SceneContainer<?> container, C context) {
		return container.reduce(identity(), (last, next) -> accumulator(last, next.accept(this, context.child())));
	}

	default T identity() {
		return null;
	}

	default T accumulator(T last, T next) {
		return next;
	}

	T visitGroup3(SceneGroup3<?> group, C context);

	T visitTransform2(SceneTransform2<?> container, C context);

	T visitObject2(SceneObject2<?> obj, C context);

	T visitObject3(SceneObject3<?> obj, C context);

	T visitNode(SceneNode obj, C context);

	T visitWorld(SceneWorld world, C context);

	T visitCamera(Camera camera, C context);

}
