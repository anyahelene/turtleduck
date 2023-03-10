package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("TerminalServiceProxy")
public interface TerminalService {
	Key<String> PROMPT = Key.strKey("prompt");
	Key<String> TEXT = Key.strKey("text");
	Key<String> STREAM = Key.strKey("stream", "out");
	Key<String> LANGUAGE = Key.strKey("language");
	Key<Dict> DATA = Key.dictKey("data");

	@Request(type = "prompt", noReply = true)
	Async<Dict> prompt(@MessageField("PROMPT") String prompt, @MessageField("LANGUAGE") String language);

	@Request(type = "print", noReply = true)
	Async<Dict> write(@MessageField("TEXT") String text, @MessageField("STREAM") String stream);
	
	@Request(type = "display", noReply = true)
	Async<Dict> display(@MessageField("DATA") Dict data, @MessageField("STREAM") String stream);

	@Request(type = "read_request", replyType = "read_reply", replyFields = { "TEXT" })
	Async<Dict> readline(@MessageField("PROMPT") String prompt);

}
