package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.events.KeyEvent;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("InputServiceProxy")
public interface InputService {
//	String MSG_TYPE = "inspect_request";
//	Key<KeyEvent> EVENT = Key.key("event", KeyEvent.class);
	Key<Dict> EVENT = Key.dictKey("event");
	Key<String> DATA = Key.strKey("data");

	@Request(type = "key_event", noReply = true)
	Async<Dict> keyEvent(@MessageField("EVENT") Dict event);

	@Request(type = "data_event", noReply = true)
	Async<Dict> dataEvent(@MessageField("DATA") String data);

}
