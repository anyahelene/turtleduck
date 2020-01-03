package turtleduck.geometry;

public interface Calculator<T> {

	T get();
	Calculator<T> distanceTo();
}
