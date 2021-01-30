package turtleduck.vfs;

public interface VFSVisitor<T> {
	T visitFile(VFile file);

	T visitFolder(VFolder folder);
}
