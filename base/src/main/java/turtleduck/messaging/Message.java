package turtleduck.messaging;

import java.util.List;

import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.Key;

public interface Message {
	Key<Array> TO = Key.arrayKey("to", () -> Array.of(String.class));
	Key<String> MSG_ID = Key.strKey("msg_id:MID");
	Key<String> REF_ID = Key.strKey("ref_id:RID");
	Key<String> TOPIC_ID = Key.strKey("topic_id:TID");
	Key<String> SESSION = Key.strKey("session:SESS");
	Key<String> USERNAME = Key.strKey("username:USER");
	Key<String> DATE = Key.strKey("date:DATE");
	Key<String> MSG_TYPE = Key.strKey("msg_type:TYPE");
	Key<String> VERSION = Key.strKey("version:VER");
	Key<Dict> HEADER = Key.dictKey("header:HEAD");
	Key<Dict> PARENT_HEADER = Key.dictKey("parent_header:REF", Dict::create);
	Key<Dict> METADATA = Key.dictKey("metadata:META", Dict::create);
	Key<Dict> CONTENT = Key.dictKey("content:DATA", Dict::create);
	Key<Array> BUFFERS = Key.arrayKey("buffers:BUF", Array::create);
	final int BINARY_HEAD = 0x4455434B;
	final int BINARY_DATA = 0x44415441;

	public static MessageWriter writeTo(String to) {
		return new MessageImpl(to);
	}

	public static MessageWriter writeTo(String to, String msg_type) {
		return new MessageImpl(to).header(msg_type);
	}

	public static Message fromDict(Dict msg) {
		return new MessageImpl(msg);
	}

//	public static MessageWriter reply(Message msg, String reply_msg_type) {
//		MessageImpl impl = new MessageImpl();
//		impl.header(msg, reply_msg_type);
//		return impl;
//	}

	default MessageWriter reply(String reply_msg_type) {
		MessageImpl impl = new MessageImpl();
		impl.header(this, reply_msg_type);
		return impl;
	}

	default MessageWriter errorReply(Throwable t) {
		MessageWriter reply = reply("failure");
		reply.putContent(Reply.STATUS, "error");
		reply.putContent(Reply.ENAME, t.getClass().getName());
		reply.putContent(Reply.EVALUE, t.getMessage());
		Array a = Array.of(String.class);
		try {
			for (StackTraceElement e : t.getStackTrace()) {
				a.add(e.toString());
			}
		} catch (Throwable u) {
			// ignore missing stacktrace on TeaVM
		}
		reply.putContent(Reply.TRACEBACK, a);
		return reply;
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

	Array buffers();

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
