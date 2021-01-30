package turtleduck.vfs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

public class VFSFileSystem extends FileSystem {
	private static final String scheme = "vfs";
	private final VFSPath root = new VFSPath(this, null, null);

	public VFSPath root() {
		return root;
	}

	public VFSPath path(String s) {
		VFSPath path = null;
		String[] components = s.split("/");
		if (s.startsWith("/"))
			path = root;
		for (String c : components) {
			if (!c.isEmpty())
				path = new VFSPath(this, path, c);
		}
		return path;
	}

	@Override
	public FileSystemProvider provider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSeparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getPath(String first, String... more) {
		StringBuilder sb = new StringBuilder();
		sb.append(first);
		for (String s : more) {
			if (!s.isEmpty()) {
				if (sb.length() > 0)
					sb.append("/");
				sb.append(s);
			}
		}
		return path(sb.toString());
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WatchService newWatchService() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public URI uri(Path path) {
		try {
			return new URI(scheme, null, path.toString(), null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Illegal path in URI: " + path);
		}
	}

}
