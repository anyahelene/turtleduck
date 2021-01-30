package turtleduck.vfs;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PathTest {
	List<String> names = IntStream.range('a', 'z' + 1).mapToObj(c -> String.valueOf((char) c))
			.collect(Collectors.toList());

	static List<String> resolveStrings() {
		return Arrays.asList("foo", "foo/bar", "foo/../bar", "foo/./bar", "./bar", "foo/.", "foo/..");
	}

	VFSFileSystem vfs = new VFSFileSystem();

	@ParameterizedTest(name = "{0}")
	@MethodSource("resolveStrings")
	void testResolve(String s) {
		VFSPath root = vfs.root();
		Path root2 = Path.of("/");
		assertEquals(root2.resolve(s).toString(), root.resolve(s).toString());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("resolveStrings")
	void testNormalize(String s) {
		VFSPath root = vfs.root();
		Path root2 = Path.of("/");
		assertEquals(root2.resolve(s).normalize().toString(), root.resolve(s).normalize().toString());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("resolveStrings")
	void testUri(String s) {
		VFSPath root = vfs.root();
		Path root2 = Path.of("/");
		assertEquals(root2.resolve(s).toUri().getPath(), root.resolve(s).toUri().getPath());
	}
}
