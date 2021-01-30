package turtleduck.vfs;

public interface VEntry {
	default boolean isFolder() {
		return false;
	}

	default boolean isFile() {
		return false;
	}

	default VFolder asFolder() {
		throw new UnsupportedOperationException("Not a folder");
	}

	default VFile asFile() {
		throw new UnsupportedOperationException("Not a file");
	}

	<T> T accept(VFSVisitor<T> visitor);

	String toTree(int depth);
}
