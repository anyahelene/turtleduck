package turtleduck.scene;

public interface Camera extends SceneObject3<Camera> {
	boolean isOrthographic();

	boolean isPerspective();

	double fieldOfView();

	Camera orthographic();

	Camera perspective();

	Camera fieldOfView(double fov);
	Camera farClip(double distance);
	Camera nearClip(double distance);
	
	double farClip();
	double nearClip();

}
