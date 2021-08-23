package turtleduck.tea;

import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.messaging.FileService;
import turtleduck.util.Array;
import turtleduck.util.Dict;

@MessageDispatch("turtleduck.tea.generated.FileServiceDispatch")
public class FileServer implements FileService {

	private FileSystem fs;

	public FileServer(FileSystem fs) {
		this.fs = fs;
	}

	@Override
	public Async<Dict> list(String path) {
		return fs.list(path).map(res -> {
			Array arr = Array.create();

			res.forEach(file -> arr.add(Dict.create().put(PATH, file.name())));
			Dict dict = Dict.create();
			dict.put(FILES, arr);
			return dict;
		}).mapFailure(err -> err);
	}

	@Override
	public Async<Dict> read(String path) {
		return fs.read(path).map(res -> {
			Dict dict = Dict.create();
			dict.put(TEXT, res);
			return dict;
		}).mapFailure(err -> err);
	}

	@Override
	public Async<Dict> stat(String path) {
		return fs.stat(path).map(res -> {
			return Dict.create().put(PATH, res.name());
		}).mapFailure(err -> err);
	}

	@Override
	public Async<Dict> write(String path, String text) {
		return fs.write(path, text).map(res -> {
			return Dict.create().put(PATH, res.name());
		}).mapFailure(err -> err);
	}
}
