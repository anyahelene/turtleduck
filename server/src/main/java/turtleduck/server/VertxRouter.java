package turtleduck.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.util.Strings;

public class VertxRouter extends Router {
	private SockJSSocket socket;
	private List<Buffer> queue = new ArrayList<>();
	private Logger logger = LoggerFactory.getLogger(getClass());

	public VertxRouter(String session, String username) {
		super(session, username);
	}

	@Override
	public void socketSend(String data) {
		if (data.length() < 1024)
			logger.info("SEND: " + data);
		else
			logger.info("SEND: " + data.toString().substring(0, 75) + "… [" + data.length() + " bytes]");
		socketSend(Buffer.buffer(data));
	}

	protected void socketSend(Buffer data) {
		if (socket != null) {
			while (!queue.isEmpty()) {
				socket.write(queue.remove(0));
			}
			socket.write(data);
		} else {
			queue.add(data);
		}
	}

	public void connect(SockJSSocket socket) {
		this.socket = socket;
		while (!queue.isEmpty()) {
			socket.write(queue.remove(0));
		}
	}

	public void disconnect() {
		this.socket = null;
	}

	@Override
	protected void socketSend(String jsonMsg, ByteBuffer[] buffers) {
		Buffer byteMsg = Buffer.buffer(jsonMsg);
		byteMsg.appendString(" ".repeat(byteMsg.length() % 4));
		int size = byteMsg.length() + 8;
		for (ByteBuffer b : buffers) {
			size += b.limit() + 8;
		}
		Buffer buffer = Buffer.buffer(size);
		buffer.appendInt(Message.BINARY_HEAD);
		buffer.appendIntLE(byteMsg.length());
		buffer.appendBuffer(byteMsg);
		for (ByteBuffer b : buffers) {
			buffer.appendInt(Message.BINARY_DATA);
			buffer.appendIntLE(b.limit());
			buffer.appendBytes(b.array());
		}

		if (buffer.length() < 1024)
			logger.info("SEND: " + Strings.termEscape(buffer.toString()));
		else
			logger.info("SEND: " + Strings.termEscape(buffer.toString().substring(0, 75)) + "… [" + buffer.length()
					+ " bytes]");
		socketSend(buffer);
	}
}
