package turtleduck.server.channels;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.comms.AbstractChannel;
import turtleduck.comms.Message;
import turtleduck.comms.Message.StringDataMessage;
import turtleduck.server.TurtleDuckSession;
import turtleduck.terminal.Editor;

public class EditorChannel extends AbstractChannel implements Editor {
	private Consumer<Message> receiver;
	private BiConsumer<Boolean, String> openCallback;
	private Consumer<String> onSave;

	public EditorChannel(String name, String service, BiConsumer<Boolean, String> callback) {
		super(name, service, callback);
	}

	public void initialize() {
	}


	public void receive(Message obj) {
		if(obj.type().equals("Data")) {
			Message.StringDataMessage dmsg = (StringDataMessage) obj;
			if(onSave != null)
				onSave.accept(dmsg.data());
		}else if (receiver != null)
			receiver.accept(obj);
	}

	@Override
	public String content() {
		return null;
	}

	@Override
	public void content(String content) {
		if(id == 0)
			throw new IllegalStateException("channel not open yet");
		send(Message.createStringData(0, content));
	}

	@Override
	public void onSave(Consumer<String> saver) {
		this.onSave = saver;
	}

}