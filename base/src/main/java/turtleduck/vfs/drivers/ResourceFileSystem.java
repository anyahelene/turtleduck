package turtleduck.vfs.drivers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;

import turtleduck.vfs.VEntry;
import turtleduck.vfs.drivers.AbstractFileSystem.AbstractFile;
import turtleduck.vfs.drivers.AbstractFileSystem.AbstractFolder;

public class ResourceFileSystem {
	public static class ResourceFolder extends AbstractFolder {
		private Path srcPath;
		private Class<?> owner;

		public ResourceFolder(String name, Class<?> owner, Path srcPath) {
			super(name);
			this.owner = owner;
			this.srcPath = srcPath;
		}

		@Override
		public Stream<String> list() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		protected VEntry lookupImpl(Path path, int startIndex) throws IOException {
			path = srcPath.resolve(path.subpath(startIndex, path.getNameCount())).normalize();
			if (!path.startsWith(srcPath))
				throw new IllegalArgumentException("Illegal file path: " + path);
			URL url = owner.getResource(path.toString());
			if (url != null)
				return new ResourceFile(path.getFileName().toString(), owner, path, url);
			else
				return null;

		}

		public String toTree(int depth) {
			return srcPath.toString() + super.toTree(depth);
		}
	}

	public static class ResourceFile extends AbstractFile {
		private Path srcPath;
		private Class<?> owner;
		private URL url;

		public ResourceFile(String name, Class<?> owner, Path srcPath, URL url) {
			super(name);
			this.owner = owner;
			this.srcPath = srcPath;
			this.url = url;
		}

		public String toTree(int depth) {
			return srcPath.toString() + "\n";
		}
	}
}
