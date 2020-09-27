package turtleduck.scene;

import java.util.function.Consumer;

public interface SceneContainer<T extends SceneContainer<T>> 
	extends SceneNode, Iterable<SceneNode> {
	T add(SceneNode elt);
	T remove(SceneNode elt);
	int size();
	T foreach(Consumer<SceneNode> fun);
}
