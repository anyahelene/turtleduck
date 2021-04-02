package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("ExecuteServiceProxy")
public interface ExecuteService {
	public static final String MSG_TYPE = "execute_request";
	Key<Boolean> SILENT = Key.boolKey("silent", false);
	Key<Boolean> STORE_HISTORY = Key.boolKey("store_history", true);
	Key<String> CODE = Key.strKey("code");
	Key<Dict> USER_EXPRESSIONS = Key.dictKey("user_expressions", Dict::create);
	Key<Boolean> STOP_ON_ERROR = Key.boolKey("stop_on_error", true);
	Key<Boolean> ALLOW_STDIN = Key.boolKey("allow_stdin", true);
	Key<Integer> EXECUTION_COUNT = Key.intKey("execution_count");
	Key<Array> PAYLOAD = Key.arrayKey("payload", () -> Array.of(Dict.class));

	@Request(type = "execute_request", replyFields = { "EXECUTION_COUNT", "USER_EXPRESSIONS", "PAYLOAD" })
	Async<Dict> executeRequest(@MessageField("CODE") String code, //
			@MessageField("SILENT") boolean silent, //
			@MessageField("STORE_HISTORY") boolean store_history, //
			@MessageField("USER_EXPRESSIONS") Dict user_expressions, //
			@MessageField("ALLOW_STDIN") boolean allow_stdin, //
			@MessageField("STOP_ON_ERROR") boolean stop_on_error);


}
