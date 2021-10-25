package turtleduck.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;

import turtleduck.async.Async;
import turtleduck.util.Dict;
import turtleduck.util.Logging;

public abstract class BaseConnection implements Connection {
	protected final Logger logger = Logging.getLogger(BaseConnection.class);
	protected final String id;
	protected Map<String, BiConsumer<Connection, Dict>> onConnectHandlers = new HashMap<>();
	protected Map<String, Consumer<Connection>> onDisconnectHandlers = new HashMap<>();
	protected Router router;
	protected Consumer<Message> receiver;

	public BaseConnection(String id) {
		this.id = id;
	}

	public void addHandlers(String owner, BiConsumer<Connection, Dict> connectHandler,
			Consumer<Connection> disconnectHandler) {
		onConnectHandlers.put(owner, connectHandler);
		onDisconnectHandlers.put(owner, disconnectHandler);
	}

	@Override
	public void removeHandlers(String owner) {
		onConnectHandlers.remove(owner);
		onDisconnectHandlers.remove(owner);
	}

	@Override
	public void receiver(Router router, Consumer<Message> receiver) {
		this.router = router;
		this.receiver = receiver;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public Router router() {
		return router;
	}

	@Override
	public Async<Dict> send(Message msg) {
		String to = msg.header(Message.TO);
		logger.info("Sending to {} via {}: {}", to, id, msg);
		return router.send(msg, this);
	}
}
