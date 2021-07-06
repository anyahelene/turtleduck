package turtleduck.messaging;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import turtleduck.async.Async;
import turtleduck.util.Dict;

public interface Connection {
	
	String id();
	
	void socketSend(Message data);

	void socketSend(Message json, ByteBuffer[] buffers);

	void receiver(Router router, Consumer<Message> receiver);
	
	Router router();
	
	void addHandlers(String owner, BiConsumer<Connection, Dict> connectHandler,
			Consumer<Connection> disconnectHandler);
	
	void removeHandlers(String owner);

	Async<Dict> send(Message msg);
}
