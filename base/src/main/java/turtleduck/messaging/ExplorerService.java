package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("ExplorerServiceProxy")
public interface ExplorerService {
	Key<Dict> INFO = Key.dictKey("info");

	@Request(type = "update", noReply = true)
	Async<Dict> update(@MessageField("INFO") Dict info);

}
