package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("FileServiceProxy")
public interface FileService {
	Key<String> TEXT = Key.strKey("text");
	Key<String> PATH = Key.strKey("path");
	Key<String> URL = Key.strKey("url");
	Key<Dict> FILE = Key.dictKey("file");
	Key<Array> FILES = Key.arrayKey("files", () -> Array.of(Dict.class));

	@Request(type = "list", replyFields = { "FILES" })
	Async<Dict> list(@MessageField("PATH") String path);

	@Request(type = "read", replyFields = { "TEXT" })
	Async<Dict> read(@MessageField("PATH") String path);

	@Request(type = "fetch", replyFields = { "TEXT" })
	Async<Dict> fetch(@MessageField("URL") String url);

	@Request(type = "stat", replyFields = { "FILE" })
	Async<Dict> stat(@MessageField("PATH") String path);

	@Request(type = "write", replyFields = { "FILE" })
	Async<Dict> write(@MessageField("PATH") String path, @MessageField("TEXT") String text);

}
