package turtleduck.scene;

import java.util.function.Consumer;

public interface SceneGroup3<T extends SceneGroup3<T>> 
	extends SceneContainer<T>, SceneObject3<T> {

}
