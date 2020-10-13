package turtleduck.scene;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface SceneContainer<T extends SceneContainer<T>> 
	extends SceneNode, Iterable<SceneNode> {
	T add(SceneNode elt);
	T remove(SceneNode elt);
	int size();
	void forEach(Consumer<? super SceneNode> fun);
	<U> U reduce(U identity, BiFunction<U, ? super SceneNode, U> accumulator);
}
