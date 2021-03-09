package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("CodeServiceProxy")
public interface CodeService {
//	String MSG_TYPE = "inspect_request";
	Key<String> CODE = Key.strKey("code");
	Key<Integer> CURSOR_POS = Key.intKey("cursor_pos");
	Key<Integer> DETAIL_LEVEL = Key.intKey("detail_level", 0);
//	public static final String MSG_TYPE = "inspect_reply";
	Key<Boolean> FOUND = Key.boolKey("found");
	Key<Dict> METADATA = Key.dictKey("metadata");
	Key<Dict> DATA = Key.dictKey("data");
	Key<Dict> TRANSIENT = Key.dictKey("data");

	@Request(type = "inspect_request", replyType = "inspect_reply", //
			replyFields = { "FOUND", "METADATA", "DATA", "TRANSIENT" })
	Async<Dict> inspect(@MessageField("CODE") String code, //
			@MessageField("CURSOR_POS") int cursorPos, //
			@MessageField("DETAIL_LEVEL") int detailLevel);

}
