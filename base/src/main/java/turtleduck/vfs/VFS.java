package turtleduck.vfs;

import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.stream.Stream;

import turtleduck.vfs.drivers.AbstractFileSystem;

public class VFS extends AbstractFileSystem.AbstractFolder implements VFolder {
	private static final VFS ROOT = new VFS("/");

	public static VFolder root() {
		return ROOT;
	}

	public VFS(String name) {
		super(name);
	}

	public VEntry mount(Path path, VEntry mountable) throws IOException {
		if (!path.isAbsolute())
			throw new IllegalArgumentException();
		int nameCount = path.getNameCount();
		VFolder folder = this;
		if (nameCount > 1) {
			VEntry entry = lookup(path.subpath(0, nameCount - 1), 0);
			if (!entry.isFolder())
				throw new NotDirectoryException(path.toString());
			folder = (VFolder) entry;
		}
		return folder.mount(path.getFileName().toString(), mountable);
	}

	@Override
	public Stream<String> list() {
		return entries.keySet().stream();
	}

	@Override
	protected VEntry lookupImpl(Path path, int startIndex) throws IOException {
		return null;
	}

	public String toTree(int depth) {
		return name + super.toTree(depth);
	}

	public VFolder mkdir(String dirName) {
		checkFilename(dirName);
		VFS vfs = new VFS(dirName);
		entries.put(dirName, vfs);
		return vfs;
	}

	private void checkFilename(String dirName) {
		// TODO Auto-generated method stub

	}
}
