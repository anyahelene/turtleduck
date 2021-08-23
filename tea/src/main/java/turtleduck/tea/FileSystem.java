package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSNumber;
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
					for(int i = 0; i < res.getLength(); i++) {
						list.add(res.get(i));
					}
					sink.success(list);
					return Promise.Util.resolve(res);
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
