package turtleduck.vfs.drivers;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import turtleduck.vfs.VEntry;
import turtleduck.vfs.VFSVisitor;
import turtleduck.vfs.VFile;
import turtleduck.vfs.VFolder;

public class AbstractFileSystem {

	public static abstract class AbstractFolder implements VFolder {
		protected Map<String, VEntry> entries = new HashMap<>();
		protected final String name;

		public AbstractFolder(String name) {
			this.name = name;
		}

		@Override
		public <T> T accept(VFSVisitor<T> visitor) {
			return visitor.visitFolder(this);
		}

		@Override
		public VEntry mount(String key, VEntry mountable) {
			return entries.put(key, mountable);
		}

		@Override
		public VEntry lookup(Path path, int startIndex) throws IOException {
			if (path.getNameCount() == startIndex)
				return this;
			String name = path.getName(startIndex).toString();
			VEntry vEntry = entries.get(name);
			if (vEntry == null) {
				return lookupImpl(path, startIndex);
			} else if (path.getNameCount() == startIndex + 1) {
				return vEntry;
			} else if (vEntry instanceof VFolder) {
				return ((VFolder) vEntry).lookup(path, startIndex + 1);
			} else {
				throw new NotDirectoryException(path.toString());
			}
		}

		protected abstract VEntry lookupImpl(Path path, int startIndex) throws IOException;

		@Override
		public Stream<String> list() throws IOException {
			return entries.keySet().stream();
		}

		@Override
		public Stream<String> list(String glob) throws IOException {
			if (!(glob.startsWith("glob:") || glob.startsWith("regex:")))
				glob = "glob:" + glob;
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob);
			return list().filter(s -> matcher.matches(Path.of(s)));
		}

		public String toTree(int depth) {
			StringBuilder sb = new StringBuilder();
			String indent = "  ".repeat(depth);
			sb.append("\n");
			for (Entry<String, VEntry> e : entries.entrySet()) {
				sb.append(indent);
				sb.append("|-  ");
				sb.append(e.getKey());
				sb.append(" → ");
				sb.append(e.getValue().toTree(depth + 1));
			}
			return sb.toString();
		}

		@Override
		public boolean isFolder() {
			return true;
		}
	}

	static class AbstractFile implements VFile {
		protected final String name;

		public AbstractFile(String name) {
			this.name = name;
		}

		@Override
		public boolean isFile() {
			return true;
		}

		@Override
		public <T> T accept(VFSVisitor<T> visitor) {
			return visitor.visitFile(this);
		}

		public String toTree(int depth) {
			return "\n";
		}
	}
}
