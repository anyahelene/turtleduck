package turtleduck.comms;

import java.util.List;

import turtleduck.comms.impl.MessageImpl;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;

public interface Message {

	/**
	 * Encode in another type
	 * 
	 * Supported types depend on the implementation, but includes ArrayBuffer (for
	 * TeaVM) and JsonObject / Buffer (for Vert.x).
	 * 
	 * @param <T>
	 * @param clazz
	 */
	<T> T encodeAs(Class<T> clazz);

	/**
	 * @return JSON representation of the message
	 */
	String toJson();

	int channel();

	void channel(int ch);

	String type();

//	<T> T get(String key, T defaultValue);

//	<T> void put(String key, T value);

	public static ConnectMessage createConnect() {
		return new MessageImpl.ConnectMsgImpl();
	}
	static StringDataMessage createStringData(int ch, String data) {
		return new MessageImpl.StringDataImpl(ch, data);
	}
	static KeyEventMessage createKeyEvent(int ch, int code, String data) {
		return new MessageImpl.KeyEventMsgImpl(ch, code, data);
	}
	static OpenMessage createOpened(int ch, String name, String service) {
		return new MessageImpl.OpenMsgImpl("Opened", ch, name, service);
	}
	static OpenMessage createOpen(String name, String service) {
		return new MessageImpl.OpenMsgImpl("Open", 0, name, service);
	}
	
	
	static Message create(MessageData data) {
		String type = data.get("type", "");
		switch(type) {
		case "Connect":
			return new MessageImpl.ConnectMsgImpl(data);
		case "Opened":
		case "Open":
			return new MessageImpl.OpenMsgImpl(data);
		case "KeyEvent":
			return new MessageImpl.KeyEventMsgImpl(data);
		case "Data":
			return new MessageImpl.StringDataImpl(data);
		default:
			return new MessageImpl(data);
//			throw new IllegalArgumentException("Illegal message: " + data);
		}
	}
	interface ConnectMessage extends Message {
		String msg();

		List<OpenMessage> opened();
		void addOpened(OpenMessage msg);

		void msg(String msg);
	}

	interface OpenMessage extends Message {
		String name();

		String service();

		int chNum();
	}

	interface StringDataMessage extends Message {
		String data();
	}

	interface KeyEventMessage extends StringDataMessage {
		int modifiers();

		int flags();

		int code();

		KeyEventMessage modifiers(int mods);

		KeyEventMessage flags(int flags);

		KeyEventMessage code(int code);

		KeyEvent keyEvent();
	}


}
