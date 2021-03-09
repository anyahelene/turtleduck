package turtleduck.messaging;

import java.util.List;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageType;
import turtleduck.async.Async;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

public interface Reply {
	Key<String> STATUS = Key.strKey("status", "error");
	Key<String> ENAME = Key.strKey("ename", "success");
	Key<String> EVALUE = Key.strKey("evalue", "no error");
	Key<Array> TRACEBACK = Key.arrayKey("traceback", () -> Array.of(String.class));

	@MessageType("error_reply")
	Async<Dict> error(@MessageField(value = "ENAME") String ename, //
			@MessageField(value = "EVALUE") String evalue, //
			@MessageField(value = "TRACEBACK") Array traceback);
}
