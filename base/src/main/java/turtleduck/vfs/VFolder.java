package turtleduck.vfs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface VFolder extends VEntry {

	VEntry mount(String string, VEntry mountable);

	VEntry lookup(Path subpath, int startIndex) throws IOException;

	Stream<String> list() throws IOException;

	Stream<String> list(String glob) throws IOException;

}
