package turtleduck.scene;

public interface SceneGroup3<T extends SceneGroup3<T>> 
	extends SceneContainer<T>, SceneObject3<T> {

	public SceneObject3<?> createObject();
	public SceneGroup3<?> createGroup();
	public Camera createCamera();

}
