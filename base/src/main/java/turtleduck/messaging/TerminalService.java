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
	Key<String> LANGUAGE = Key.strKey("language");

	@Request(type = "prompt", noReply = true)
	Async<Dict> prompt(@MessageField("PROMPT") String prompt, @MessageField("LANGUAGE") String language);

	@Request(type = "print", noReply = true)
	Async<Dict> write(@MessageField("TEXT") String text);

	@Request(type = "read_request", replyType = "read_reply", replyFields = { "TEXT" })
	Async<Dict> readline(@MessageField("PROMPT") String prompt);

}
