package turtleduck.scene;

public interface SceneVisitor<T, C extends RenderContext<C>> {

	default T visitContainer(SceneContainer<?> container, C context) {
		return container.reduce(identity(), (last, next) -> accumulator(last, next.accept(this, context.child())));
	}

	default T identity() {
		return null;
	}

	default T accumulator(T last, T next) {
		return next;
	}

	default T visitGroup3(SceneGroup3<?> group, C context) {
		return group.reduce(identity(), (last, next) -> accumulator(last, next.accept(this, context.child())));
	}

	default T visitTransform2(SceneTransform2<?> container, C context) {
		return container.reduce(identity(), (last, next) -> accumulator(last, next.accept(this, context.child())));
	}

	T visitObject2(SceneObject2<?> obj, C context);

	T visitObject3(SceneObject3<?> obj, C context);

	T visitNode(SceneNode obj, C context);

	T visitWorld(SceneWorld world, C context);

	T visitCamera(Camera camera, C context);

}
