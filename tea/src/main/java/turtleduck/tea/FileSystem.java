package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.typedarrays.Uint8Array;

import turtleduck.async.Async;
import turtleduck.async.Async.Sink;
import turtleduck.util.Dict;

public class FileSystem {

	Async<List<String>> readdir(String path) {
		Sink<List<String>> sink = Async.create();
		StorageHelper.cwd().readdir(path).then(res -> {
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

	Async<Void> writetextfile(String path, String data) {
		Sink<Void> sink = Async.create();
		StorageHelper.cwd().writetextfile(path, data)//
				.then(res -> {
					sink.success(null);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}

	Async<Void> writebinfile(String path, byte[] data) {
		Sink<Void> sink = Async.create();
		StorageHelper.cwd().writebinfile(path, data)//
				.then(res -> {
					sink.success(null);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}

	Async<Void> mkdir(String path) {
		Sink<Void> sink = Async.create();
		StorageHelper.cwd().mkdir(path)//
				.then(res -> {
					sink.success(null);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}

	Async<Void> chdir(String path) {
		Sink<Void> sink = Async.create();
		StorageHelper.cwd().chdir(path)//
				.then(res -> {
					sink.success(null);
					return Promise.Util.resolve(res);
				})//
				.onRejected(err -> {
					String s = ((JSString) err.cast()).stringValue();
					sink.fail(s);
				});
		return sink.async();
	}

	Async<String> readtextfile(String path) {
		Sink<String> sink = Async.create();
		StorageHelper.cwd().readtextfile(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					String val = res != null ? res.stringValue() : null;
					sink.success(val);
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<byte[]> readbinfile(String path) {
		Sink<byte[]> sink = Async.create();
		StorageHelper.cwd().readbinfile(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					sink.success(JSUtil.toBytes(res));
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	Async<Dict> stat(String path) {
		Sink<Dict> sink = Async.create();
		StorageHelper.cwd().stat(path).onRejected(err -> sink.fail("not found"))//
				.then(res -> {
					sink.success(JSUtil.decodeDict(res));
					return Promise.Util.resolve(res);
				});
		return sink.async();
	}

	interface JSStorageContext extends JSObject {
		Promise<JSString> readtextfile(String path);

		Promise<Uint8Array> readbinfile(String path);

		Promise<JSString> readlink(String path);

		Promise<JSArray<JSString>> readdir(String path);

		Promise<JSObject> writetextfile(String path, String data);

		Promise<JSObject> writebinfile(String path, byte[] data);

		Promise<JSObject> rename(String oldPath, String newPath);

		Promise<JSObject> symlink(String oldPath, String newPath);

		Promise<JSObject> unlink(String path);

		Promise<JSObject> mkdir(String path);

		Promise<JSObject> rmdir(String path);

		Promise<JSNumber> du(String path);

		Promise<JSStorageContext> withCwd(String path);

		Promise<JSObject> chdir(String path);

		JSString realpath(String path);

		Promise<JSMapLike<?>> stat(String path);

		Promise<JSMapLike<?>> lstat(String path);

	}
}

class StorageHelper {
	@JSBody(params = {}, script = "return turtleduck.cwd;")
	native static FileSystem.JSStorageContext cwd();
}