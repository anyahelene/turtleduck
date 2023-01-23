package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("HelloServiceProxy")

public interface HelloService {
    String MSG_TYPE = "welcome";
    Key<Dict> ENDPOINTS = Key.dictKey("endpoints");
    Key<String> SESSION_NAME = Key.strKey("sessionName");
    Key<String> USERNAME = Key.strKey("username");
    Key<Dict> USER = Key.dictKey("user");
    Key<Boolean> EXISTING = Key.boolKey("existing", false);
    Key<Array> CONNECTIONS = Key.arrayKey("connections", () -> Array.of(String.class));
    Key<Boolean> ENABLE = Key.boolKey("enable", false);
    Key<Dict> CONFIG = Key.dictKey("config");
    Key<String> TERMINAL = Key.strKey("terminal");
    Key<String> EXPLORER = Key.strKey("explorer");
    Key<String> SESSION = Key.strKey("session");

    @Request(type = "hello", replyType = "welcome", replyFields = { "ENDPOINTS", "USERNAME", "USER", "EXISTING" })
    Async<Dict> hello(@MessageField("SESSION_NAME") String sessionName,
            @MessageField(value = "ENDPOINTS") Dict endPoints);

    @Request(type = "debug", noReply = true)
    Async<Dict>  debug(@MessageField("ENABLE") boolean enable);

    @Request(type = "goodbye", noReply = true)
    Async<Dict>  goodbye();

    @Request(type = "shutdown", noReply = true)
    Async<Dict>  shutdown();

    @Request(type = "langInit", replyType = "langInit_reply")
    Async<Dict> langInit(@MessageField("CONFIG") Dict config, @MessageField("TERMINAL") String terminal, @MessageField("EXPLORER") String explorer, @MessageField("SESSION") String session);
}
