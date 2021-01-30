package turtleduck.vfs;

public interface NamedEntry extends VEntry {
	String name();
	VEntry entry();
}
