package turtleduck.turtle;

public interface Annotation<T> {
	/**
	 * Create a new annotation identifier.
	 * 
	 * @param <T> Type of annotation's data value
	 * @return An annotation identifier
	 */
	public static <T> Annotation<T> create() {
		return new Annotation<T>() {};
	}
}
