package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("HelloServiceProxy")

public interface HelloService {
	String MSG_TYPE = "welcome";
	Key<Dict> ENDPOINTS = Key.dictKey("endpoints");
	Key<String> SESSION_NAME = Key.strKey("sessionName");
	Key<String> USERNAME = Key.strKey("username");

	@Request(type = "hello", replyType = "welcome", replyFields = { "ENDPOINTS", "USERNAME" })
	Async<Dict> hello(@MessageField("SESSION_NAME") String sessionName,
			@MessageField(value = "ENDPOINTS") Dict endPoints);
}
