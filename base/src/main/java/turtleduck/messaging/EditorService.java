package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("EditorServiceProxy")
public interface EditorService {
	Key<String> MESSAGE = Key.strKey("message");
	Key<Integer> CURSOR_POS = CodeService.CURSOR_POS;
	Key<Integer> START = Key.intKey("start");
	Key<Integer> END = Key.intKey("end");
	Key<String> TEXT = Key.strKey("text");
	Key<String> FILENAME = Key.strKey("filename");
	Key<String> LANGUAGE = Key.strKey("language");

	@Request(type = "content", noReply = true)
	Async<Dict> content(@MessageField("TEXT") String text, @MessageField("LANGUAGE") String language);

	@Request(type = "open", noReply = true)
	Async<Dict> open(@MessageField("FILENAME") String filename, @MessageField("TEXT") String text,
			@MessageField("LANGUAGE") String language);

	@Request(type = "mark", noReply = true)
	Async<Dict> mark(@MessageField("MESSAGE") String message, @MessageField("CURSOR_POS") int cursorPos,
			@MessageField("START") int start, @MessageField("END") int end);

	@Request(type = "code", noReply = true)
	Async<Dict> code(@MessageField("TEXT") String text);

	@Request(type = "read_request", replyType = "read_reply", replyFields = { "TEXT" })
	Async<Dict> read();

}
