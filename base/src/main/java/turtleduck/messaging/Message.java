package turtleduck.messaging;

import java.util.List;

import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

public interface Message {
	Key<Array> TO = Key.arrayKey("to", () -> Array.of(String.class));
	Key<String> MSG_ID = Key.strKey("msg_id");
	Key<String> SESSION = Key.strKey("session");
	Key<String> USERNAME = Key.strKey("username");
	Key<String> DATE = Key.strKey("date");
	Key<String> MSG_TYPE = Key.strKey("msg_type");
	Key<String> VERSION = Key.strKey("version");
	Key<Dict> HEADER = Key.dictKey("header");
	Key<Dict> PARENT_HEADER = Key.dictKey("parent_header", Dict::create);
	Key<Dict> METADATA = Key.dictKey("metadata", Dict::create);
	Key<Dict> CONTENT = Key.dictKey("content", Dict::create);
	Key<Array> BUFFERS = Key.arrayKey("buffers", Array::create);

	public static MessageWriter writeTo(String to) {
		return new MessageImpl(to);
	}

	public static MessageWriter writeTo(String to, String msg_type) {
		return new MessageImpl(to).header(msg_type);
	}

	public static Message fromDict(Dict msg) {
		return new MessageImpl(msg);
	}

	public static MessageWriter reply(Message msg, String reply_msg_type) {
		MessageImpl impl = new MessageImpl();
		impl.header(msg.header(SESSION), msg.header(USERNAME), reply_msg_type);
		impl.parent_header(msg);
		return impl;
	}

	default MessageWriter reply(String reply_msg_type) {
		return reply(this, reply_msg_type);
	}

	<T> T header(Key<T> key);

	<T> T parent_header(Key<T> key);

	/**
	 * @return True if the message is a reply (has a filled-in
	 *         {@link #parent_header()}
	 */
	boolean isReply();

	/**
	 * @return The unique message ID
	 * @throws NullPointerException if undefined
	 */
	String msgId();

	/**
	 * @return The message type
	 * @throws NullPointerException if undefined
	 */
	String msgType();

	Dict header();

	Dict parent_header();

	Dict metadata();

	Dict content();

//	List<Buffer> buffers();

	String toJson();

	<T> T content(Key<T> key);

	<T> T content(Key<T> key, T defaultValue);

	<T> T metadata(Key<T> key);

	<T> T metadata(Key<T> key, T defaultValue);

	/**
	 * @return The parent's unique message ID (for a reply message)
	 * @throws NullPointerException if {@link #isReply()} is false
	 */
	String msgRef();

	List<String> address();

}
