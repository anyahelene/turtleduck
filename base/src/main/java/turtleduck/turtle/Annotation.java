package turtleduck.turtle;

public interface Annotation<T> {
	public static <T> Annotation<T> create() {
		return new Annotation<T>() {};
	}
}
