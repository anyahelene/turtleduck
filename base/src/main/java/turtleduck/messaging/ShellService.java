package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("ShellServiceProxy")
public interface ShellService extends ExecuteService, CodeService {
	Key<String> LINE = Key.strKey("line");
	Key<String> SNIPPET_ID = Key.strKey("line");
	Key<String> PROMPT = TerminalService.PROMPT;


	@Request(type = "enter_request", replyType = "enter_reply", //
	replyFields = { "PROMPT", "SNIPPET_ID", "LINE" })
	Async<Dict> enter(@MessageField("LINE") String line);

}
