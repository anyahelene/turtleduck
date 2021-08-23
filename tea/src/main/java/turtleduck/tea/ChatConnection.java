package turtleduck.tea;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;

import turtleduck.messaging.BaseConnection;
import turtleduck.messaging.Connection;
import turtleduck.messaging.Message;
import turtleduck.messaging.MessageWriter;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public class ChatConnection extends BaseConnection {
	public final Logger logger = Logging.getLogger(ChatConnection.class);

	private final JSMapLike<JSObject> map;

	public ChatConnection(String id, Client client) {
		super(id);

		this.map = JSObjects.create().cast();
		map.set("id", JSString.valueOf(id));
	}

	public JSMapLike<JSObject> map() {
		return map;
	}

	public void initialize() {
	}

	@Override
	public void socketSend(Message msg) {
		Dict msgDict = msg.toDict();
		logger.info("sending to worker: dict {}", msgDict);
		String msgType = msg.msgType();
		MessageWriter mw;
		if (msgType.equals("eval_request")) {
			Dict content = msg.content();
			String code = content.get(ShellService.CODE);
			mw = msg.reply("evalReply");
			Dict result = Dict.create();
			result.put(ShellService.COMPLETE, true);
			result.put(ShellService.SNIP_KIND, "chat");
			result.put(ShellService.CODE, code);
			result.put(ShellService.MULTI, Array.create());
			mw.content(result);
			Window.setTimeout(() -> {
				Message r = Message.writeTo(null,"print")//
						.content(Dict.create()//
								.put(TerminalService.TEXT, "Why do you say '" + code + "'?")//
								.put(TerminalService.STREAM, "chatout")).done();
				logger.info("sending chat reply: ", r.toDict());
				receiver.accept(r);
			}, 700);
			
		} else {
			mw = msg.errorReply(new IllegalArgumentException(msgType));
		}
		Message reply = mw.done();
		Window.setTimeout(() -> {
			logger.info("sending reply: ", reply.toDict());
			receiver.accept(reply);
		}, 100);
	}

	@Override
	public void socketSend(Message msg, ByteBuffer[] buffers) {
		socketSend(msg);
	}

}
