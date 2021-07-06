package turtleduck.tea;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

import turtleduck.messaging.BaseConnection;
import turtleduck.messaging.Connection;
import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public class PyConnection extends BaseConnection {
	public final Logger logger = Logging.getLogger(PyConnection.class);

	@JSBody(params = { "onmessage" }, script = "turtleduck.pyController.onmessage(onmessage)")
	native static void onMessage(JSConsumer<JSMapLike<JSObject>> onmessage);

	@JSBody(params = { "onerror" }, script = "turtleduck.pyController.onerror(onerror)")
	native static void onError(JSConsumer<JSMapLike<JSObject>> onerror);

	@JSBody(params = { "message" }, script = "turtleduck.pyController.postMessage(message)")
	native static void postMessage(JSMapLike<?> message);

	private final JSMapLike<JSObject> map;

	public PyConnection(String id, Client client) {
		super(id);
		onMessage(this::receive);
		onError(this::error);
		this.map = JSObjects.create().cast();
		map.set("id", JSString.valueOf(id));
	}

	public JSMapLike<JSObject> map() {
		return map;
	}

	public void initialize() {
	}

	protected void receive(JSMapLike<JSObject> msgObj) {
//		logger.info("received from worker: obj {}", msgObj);
		Dict msgDict = JSUtil.decodeDict((JSMapLike<?>) msgObj.get("data"));
//		logger.info("received from worker: dict {}", msgDict);
		Message msg = Message.fromDict(msgDict);
		if (map.get("debug") != null)
			logger.info("received from worker: msg {}", msg);
		if (this.receiver != null)
			receiver.accept(msg);
	}

	protected void error(JSMapLike<JSObject> msgObj) {
		logger.error("received error from worker: obj {}", msgObj);
	}

	@Override
	public void socketSend(Message msg) {
		Dict msgDict = msg.toDict();
		if (map.get("debug") != null)
			logger.info("sending to worker: dict {}", msgDict);
		JSMapLike<?> msgObj = JSUtil.encode(msgDict);

		postMessage(msgObj);
	}

	@Override
	public void socketSend(Message msg, ByteBuffer[] buffers) {
		socketSend(msg);
	}

}
