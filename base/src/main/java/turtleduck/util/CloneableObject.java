package turtleduck;

public class CloneableObject<T extends CloneableObject<T>> implements Cloneable {

	@SuppressWarnings("unchecked")
	public T clone() {
		try {
			return (T) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
