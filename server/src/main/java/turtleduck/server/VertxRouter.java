package turtleduck.server;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.messaging.Router;

public class VertxRouter extends Router {
	private SockJSSocket socket;
	private List<String> queue = new ArrayList<>();
	private Logger logger = LoggerFactory.getLogger(getClass());

	public VertxRouter(String session, String username) {
		super(session, username);
	}

	@Override
	public void socketSend(String data) {
		if (socket != null) {
			while (!queue.isEmpty()) {

				socket.write(queue.remove(0));
			}
			if (data.length() <1024)
				logger.info("SEND: " + data);
			else
				logger.info("SEND: " + data.substring(0, 75) + "… [" + data.length() + " bytes]");
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
}
