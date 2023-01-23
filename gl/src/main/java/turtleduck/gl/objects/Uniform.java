package turtleduck.gl.objects;

public interface Uniform<T> {
	public T get(T dest);
	public T get();
	public void set(T val);
	public String typeName();

	public int typeId();

	public int size();
	public int location();
	public boolean isDeclared();
}
