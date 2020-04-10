package turtleduck.comms;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GenericChannel extends AbstractChannel {

	private Consumer<Message> receiver;

	public GenericChannel(String name, String service, Consumer<Message> receiver, BiConsumer<Boolean, String> openCloseCallback) {
		super(name, service, openCloseCallback);
		this.receiver = receiver;
	}

	@Override
	public void receive(Message msg) {
		if(receiver != null)
			receiver.accept(msg);
	}

}
