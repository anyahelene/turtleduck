package turtleduck.events;

public interface InputControl<T> {
	String id();
	String name();
	String shortName();
	T get();
}
