package turtleduck.vfs.drivers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import turtleduck.vfs.VEntry;
import turtleduck.vfs.drivers.AbstractFileSystem.AbstractFile;
import turtleduck.vfs.drivers.AbstractFileSystem.AbstractFolder;

public class FileFileSystem {

	public static class FileFolder extends AbstractFolder {
		private Path srcPath;

		public FileFolder(Path srcPath) {
			this(srcPath.toString(), srcPath);
		}

		public FileFolder(String name, Path srcPath) {
			super(name);
			this.srcPath = srcPath;
		}

		@Override
		public Stream<String> list() throws IOException {
			return Stream.concat(super.list(), //
					Files.list(srcPath).map(path -> path.getFileName().toString()));
		}

		@Override
		protected VEntry lookupImpl(Path path, int startIndex) throws IOException {
			Path resPath = srcPath.resolve(path.subpath(startIndex, path.getNameCount())).normalize();
			if (!resPath.startsWith(srcPath))
				throw new IllegalArgumentException("Illegal file path: " + resPath);
			BasicFileAttributeView view = Files.getFileAttributeView(resPath, BasicFileAttributeView.class);
			if (view != null) {
				BasicFileAttributes attrs = view.readAttributes();
				if (attrs.isDirectory())
					return new FileFolder(resPath.getFileName().toString(), resPath);
				else if (attrs.isRegularFile())
					return new FileFile(resPath.getFileName().toString(), resPath);
			}
			return null;
		}

		public String toTree(int depth) {
			return srcPath.toString() + super.toTree(depth);
		}

	}

	public static class FileFile extends AbstractFile {
		private Path srcPath;

		public FileFile(Path srcPath) {
			this(srcPath.toString(), srcPath);
		}

		public FileFile(String name, Path srcPath) {
			super(name);
			this.srcPath = srcPath;
		}

		public String toTree(int depth) {
			return srcPath.toString() + "\n";
		}
	}
}
