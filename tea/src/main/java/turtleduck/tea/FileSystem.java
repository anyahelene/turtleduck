package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

import turtleduck.async.Async;
import turtleduck.async.Async.Sink;

public class FileSystem {
	private JSFileSystem jsfs;

	public FileSystem(JSObject jsfs) {
		this.jsfs = jsfs.cast();
	}

	Async<List<TDFile>> list(String path) {
		Sink<List<TDFile>> sink = Async.create();
		jsfs.list(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					List<TDFile> list = new ArrayList<>();
					for (int i = 0; i < res.getLength(); i++) {
						list.add(res.get(i));
					}
					sink.success(list);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<List<String>> readdir(String path) {
		Sink<List<String>> sink = Async.create();
		StorageHelper.readdir(path).then(res -> {
			Browser.consoleLog("readdir stub", res);
			if (res == null || JSObjects.isUndefined(res)) {
				sink.fail("not found");
				return Promise.Util.resolve(res);
			}
			List<String> list = new ArrayList<>();
			for (int i = 0; i < res.getLength(); i++) {
				list.add(res.get(i).stringValue());
			}
			sink.success(list);
			return Promise.Util.resolve(res);
		}).onRejected(err -> {
			String s = ((JSString) err.cast()).stringValue();
			Browser.consoleLog("readdir fail: " + s, err);
			sink.fail(s);
		});
		return sink.async();
	}

	Async<String> read(String path) {
		Sink<String> sink = Async.create();
		jsfs.read(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					sink.success(res.stringValue());
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<TDFile> stat(String path) {
		Sink<TDFile> sink = Async.create();
		jsfs.stat(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					sink.success(res);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<TDFile> write(String path, String data) {
		Sink<TDFile> sink = Async.create();
		jsfs.stat(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					sink.success(res);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<Integer> writefile(String path, String data) {
		Sink<Integer> sink = Async.create();
		StorageHelper.writefile(path, data)//
				.then(res -> {
					int val = res != null ? res.intValue() : 0;
					sink.success(val);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}

	Async<Integer> mkdir(String path) {
		Sink<Integer> sink = Async.create();
		StorageHelper.mkdir(path)//
				.then(res -> {
					int val = res != null ? res.intValue() : 0;
					sink.success(val);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}
	Async<String> chdir(String path) {
		Sink<String> sink = Async.create();
		StorageHelper.chdir(path)//
				.then(res -> {
					JSString jss = res.get("cwd").cast();
					String s = jss.stringValue();
					sink.success(s);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}

	Async<String> readfile(String path) {
		Sink<String> sink = Async.create();
		StorageHelper.readfile(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					String val = res != null ? res.stringValue() : null;
					sink.success(val);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	interface JSStorageContext extends JSObject {
		Promise<JSString> readfile(String path);

		Promise<JSString> readlink(String path);

		Promise<JSNumber> writefile(String path, String data);

		Promise<JSNumber> rename(String oldPath, String newPath);

		Promise<JSNumber> symlink(String oldPath, String newPath);

		Promise<JSNumber> unlink(String path);

		Promise<JSNumber> mkdir(String path);

		Promise<JSNumber> rmdir(String path);

		Promise<JSNumber> du(String path);

		Promise<JSStorageContext> withCwd(String path);

		Promise<JSString> chdir(String path);

		JSString realpath(String path);

		Promise<JSMapLike<?>> stat(String path);

		Promise<JSMapLike<?>> lstat(String path);

	}

	interface JSFileSystem extends JSObject {
		Promise<JSArray<TDFile>> list(String path);

		Promise<TDFile> mkdir(String path);

		Promise<JSString> read(String path);

		Promise<JSNumber> write(String path, String data);

		Promise<TDFile> stat(String path);

	}

	interface TDFile extends JSObject {

		@JSProperty("name")
		public String name();

	}

}

class StorageHelper {
	@JSBody(params = { "path" }, script = "return turtleduck.cwd.readdir(path);")
	native static Promise<JSArray<JSString>> readdir(String path);

	@JSBody(params = { "path" }, script = "return turtleduck.cwd.readfile(path);")
	native static Promise<JSString> readfile(String path);

	@JSBody(params = { "path", "data" }, script = "return turtleduck.cwd.writefile(path,data);")
	native static Promise<JSNumber> writefile(String path, String data);

	@JSBody(params = { "path"}, script = "return turtleduck.cwd.mkdir(path);")
	native static Promise<JSNumber> mkdir(String path);
	@JSBody(params = { "path"}, script = "return turtleduck.cwd.chdir(path);")
	native static Promise<JSMapLike<?>> chdir(String path);
}