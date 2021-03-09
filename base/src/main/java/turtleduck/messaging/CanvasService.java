package turtleduck.messaging;

import turtleduck.annotations.MessageField;
import turtleduck.annotations.MessageProtocol;
import turtleduck.annotations.Request;
import turtleduck.async.Async;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

@MessageProtocol("CanvasServiceProxy")
public interface CanvasService {
	Key<Array> PATHS = Key.arrayKey("paths");

	@Request(type = "drawPath", replyType = "none", //
			replyFields = {})
	Async<Dict> drawPath(@MessageField("PATHS") Array paths);
}
