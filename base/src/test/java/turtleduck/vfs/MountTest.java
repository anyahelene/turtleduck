package turtleduck.vfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import turtleduck.vfs.drivers.FileFileSystem;

import org.junit.jupiter.api.Test;

public class MountTest {
	VFS vfs = new VFS("/");

	@Test
	void testMount() {
		try {
			vfs.mount(Path.of("/examples"), new FileFileSystem.FileFolder(Path.of("/tmp")));
			vfs.mount(Path.of("/etc"), new FileFileSystem.FileFolder(Path.of("/etc")));
			VFolder courses = vfs.mkdir("courses");
			vfs.mount(Path.of("/courses"), courses);
			vfs.mount(Path.of("/courses/foo1"), new FileFileSystem.FileFolder(Path.of("/tmp/foo1")));
			vfs.mount(Path.of("/courses/foo2"), new FileFileSystem.FileFolder(Path.of("/tmp/foo2")));

			System.out.println(vfs.toTree(0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testList() {
		try {
			vfs.mount(Path.of("/examples"), new FileFileSystem.FileFolder(Path.of("/tmp")));
			vfs.mount(Path.of("/etc"), new FileFileSystem.FileFolder(Path.of("/etc")));
			VFolder courses = vfs.mkdir("courses");
			vfs.mount(Path.of("/courses"), courses);
			vfs.mount(Path.of("/courses/foo1"), new FileFileSystem.FileFolder(Path.of("/tmp/foo1")));
			vfs.mount(Path.of("/courses/foo2"), new FileFileSystem.FileFolder(Path.of("/tmp/foo2")));

			System.out.println(vfs.list().collect(Collectors.joining(", ")));
			System.out.println(((VFolder) vfs.lookup(Path.of("/courses"), 0)).list().collect(Collectors.joining(", ")));
			System.out.println(((VFolder) vfs.lookup(Path.of("/examples"), 0)).list().collect(Collectors.joining("\n")));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
