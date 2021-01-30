package turtleduck.vfs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class VFSPath implements Path {
	private final VFSPath parent;
	private final String name;
	private final int nameCount;
	private final boolean absolute;
	private VFSFileSystem fs;

	VFSPath(VFSFileSystem fs, VFSPath parent, String name) {
		this.fs = fs;
		this.parent = parent;
		this.name = name;
		if (parent != null) {
			this.nameCount = parent.nameCount + 1;
			this.absolute = parent.absolute;
		} else if (name != null) {
			this.nameCount = 1;
			this.absolute = false;
		} else {
			this.nameCount = 0;
			this.absolute = true;
		}
	}

	@Override
	public FileSystem getFileSystem() {
		return fs;
	}

	@Override
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public Path getRoot() {
		if (parent == null && name == null)
			return this;
		else if (parent != null)
			return parent.getRoot();
		else
			return null;
	}

	@Override
	public Path getFileName() {
		if (parent != null)
			return new VFSPath(fs, null, name);
		else
			return this;
	}

	@Override
	public Path getParent() {
		return parent;
	}

	@Override
	public int getNameCount() {
		return nameCount;
	}

	@Override
	public Path getName(int index) {
		if (index == nameCount - 1)
			return this;
		else if (index < nameCount - 1)
			return parent.getName(index);
		throw new IllegalArgumentException();
	}

	@Override
	public Path subpath(int beginIndex, int endIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean startsWith(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean endsWith(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public VFSPath normalize() {
		if (name == null || parent == null)
			return this;
		else if (name.equals(".")) {
			return parent.normalize();
		} else if (name.equals("..")) {
			VFSPath p = parent.parent;
			if (p != null)
				return p.normalize();
			else
				return new VFSPath(fs, null, ".");
		} else {
			VFSPath p = parent.normalize();
			if (p != parent)
				return new VFSPath(fs, p, name);
			else
				return this;
		}
	}

	@Override
	public Path resolve(Path other) {
		if (other.isAbsolute())
			return other;
		else if (other.getFileName() == null) {
			return this;
		} else {
			VFSPath newParent = null;
			if (other.getParent() == null) {
				newParent = this;
			} else {
				newParent = (VFSPath) resolve(other.getParent());
			}
			String filename = other.getFileName().toString();
//			if (filename.equals("."))
//				return newParent;
//			else if (filename.equals(".."))
//				return newParent.parent;
//			else
				return new VFSPath(fs, newParent, filename);
		}
	}

	@Override
	public Path relativize(Path other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI toUri() {
		try {
			if (parent == null && name == null) {
				return new URI("vfs", "", "/", null);
			} else if (absolute) {
				return new URI("vfs", "", toString(), null);
			} else {
				return new URI(null, null, toString(), null);
			}
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Path toAbsolutePath() {
		if (absolute)
			return this;
		else if (parent == null)
			return new VFSPath(fs, fs.root(), name);
		else
			return new VFSPath(fs, (VFSPath) parent.toAbsolutePath(), name);
	}

	@Override
	public Path toRealPath(LinkOption... options) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Path other) {
		if (other == this)
			return 0;
		else
			return toString().compareTo(other.toString());
	}

	protected boolean isRoot() {
		return parent == null && name == null;
	}

	public String toString() {
		if (isRoot())
			return "/";
		else if (parent == null)
			return name;
		else if (parent.isRoot())
			return "/" + name;
		else
			return parent.toString() + "/" + name;
	}
}
