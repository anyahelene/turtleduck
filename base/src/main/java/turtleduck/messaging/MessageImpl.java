package turtleduck.messaging;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import turtleduck.util.Array;
import turtleduck.util.Dict;
import turtleduck.util.JsonUtil;
import turtleduck.util.Key;

public class MessageImpl implements Message, MessageWriter {
	private boolean useJupyter = false;
	private static int idCounter = 0;
//	protected static final Logger logger = LoggerFactory.getLogger(MessageImpl.class);
	boolean frozen = false;
//	private Array address;
	private Dict message;
	private Dict header;
	private Dict parent;
	private Array buffers;
	private Dict metadata;
	private Dict content;

	public MessageImpl() {
	}

	public MessageImpl(String to) {
//		address = Array.of(to);
	}

	public MessageImpl(String to, String... toMore) {
//		address = Array.of(to, toMore);
	}

	protected MessageImpl(Dict msg) {
		msg.require(Message.HEADER);
		header = msg.get(Message.HEADER);
		if (header.get(Message.VERSION) != null) {
			useJupyter = true;
			msg.require(Message.HEADER)//
					.require(Message.PARENT_HEADER)//
					.require(Message.CONTENT) //
					.require(Message.METADATA)//
					.require(Message.BUFFERS)//
			;
			parent = msg.get(Message.PARENT_HEADER);
			content = msg.get(Message.CONTENT);
			metadata = msg.get(Message.METADATA);
			buffers = msg.get(Message.BUFFERS);
			message = msg;
			checkHeader(header);
			if (parent.get(Message.MSG_ID) != null)
				checkHeader(parent);
		} else {
			msg.require(Message.HEADER)//
					.require(Message.CONTENT) //
			;
			content = msg.get(Message.CONTENT);
			message = msg;
			checkHeader(header);
		}
		frozen = true;
	}

	private void checkHeader(Dict head) {
		if (useJupyter) {
			head.require(Message.MSG_ID) //
					.require(Message.SESSION)//
					.require(Message.USERNAME)//
					.require(Message.DATE) //
					.require(Message.MSG_TYPE) //
					.require(Message.VERSION);
		} else {
			head.require(Message.MSG_ID) //
//			.require(Message.SESSION)//
//			.require(Message.USERNAME)//
//			.require(Message.DATE) //
					.require(Message.MSG_TYPE) //
//			.require(Message.VERSION);	
			;
			if (header.has(VERSION))
				throw new IllegalStateException();
		}

	}

	@Override
	public Dict header() {
		return header;
	}

	@Override
	public Dict parent_header() {
		if (parent == null) {
			parent = Dict.create();
			if (header.has(Message.REF_ID)) {
				parent.put(Message.MSG_ID, header.get(Message.REF_ID));
			}
		}
		return parent;
	}

	@Override
	public Dict metadata() {
		if (metadata == null)
			metadata = Dict.create();
		return metadata;
	}

	@Override
	public Dict content() {
		if (content == null)
			content = Dict.create();
		return content;
	}

	@Override
	public Array buffers() {
		if (buffers == null)
			buffers = Array.create();
		return buffers;
	}

	@Override
	public MessageWriter header(Message parent, String msg_type) {
		if (parent.header(VERSION) != null) {
			return header(parent.header(SESSION), parent.header(USERNAME), msg_type).parent_header(parent);
		} else {
			return header(msg_type).parent_header(parent);
		}
	}

	@Override
	public MessageWriter header(String msg_type) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		if (header == null)
			header = Dict.create();
		header.put(Message.MSG_ID, String.valueOf(idCounter++));
		header.put(Message.MSG_TYPE, msg_type);
		return this;
	}

	@Override
	public MessageWriter header(String session, String username, String msg_type) {
		if (frozen)
			throw new IllegalStateException("Message already written");

		header = Dict.create();
		header.put(Message.MSG_ID, String.valueOf(idCounter++));
		header.put(Message.SESSION, session);
		header.put(Message.DATE, String.valueOf(System.currentTimeMillis()));
		header.put(Message.USERNAME, username);
		header.put(Message.MSG_TYPE, msg_type);
		header.put(Message.VERSION, "5.0");
		return this;
	}

	@Override
	public MessageWriter parent_header(Message parentMsg) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		parent = parentMsg.header();
		if (parent.get(Message.VERSION, "").equals("5.0")) {
			useJupyter = true;
		} else {
			header.put(REF_ID, parent.get(MSG_ID));
		}
		checkHeader(this.parent);
		return this;
	}

	@Override
	public MessageWriter metadata(Dict dict) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		this.metadata = dict;
		useJupyter = true;
		return this;
	}

	@Override
	public MessageWriter content(Dict dict) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		this.content = dict;
		return this;
	}

	@Override
	public <T> MessageWriter putContent(Key<T> key, T value) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		if (content == null)
			content = Dict.create();
		content.put(key, value);
		return this;
	}

	@Override
	public <T> MessageWriter putMeta(Key<T> key, T value) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		if (metadata == null)
			metadata = Dict.create();
		metadata.put(key, value);
		useJupyter = true;
		return this;
	}

//	@Override
//	public MessageWriter buffer(Buffer buffer) {
//		if (frozen)
//			throw new IllegalStateException("Message already written");
//		buffers.add(buffer);
//		return this;
//	}

	@Override
	public Message done() {
		if (frozen)
			throw new IllegalStateException("Message already written");
		assemble();
		checkHeader(header);
		if (parent != null && parent.has(Message.MSG_ID))
			checkHeader(parent);
		frozen = true;
		return this;
	}

	private void assemble() {
		if (useJupyter) {
			if (parent == null)
				parent = Dict.create();
			if (metadata == null)
				metadata = Dict.create();
			if (content == null)
				content = Dict.create();
			if (buffers == null)
				buffers = Array.create();
			if (message == null) {
				message = Dict.create();
//			message.put(Message.TO, address);
				message.put(Message.HEADER, header);
				message.put(Message.PARENT_HEADER, parent);
				message.put(Message.CONTENT, content);
				message.put(Message.METADATA, metadata);
				message.put(Message.BUFFERS, buffers);
			}
		} else {
			if (message == null) {
				message = Dict.create();
			}
			if (content == null)
				content = Dict.create();
			message.put(Message.HEADER, header);
			message.put(Message.CONTENT, content);
		}
	}

	public String toJson() {
		assemble();
		return JsonUtil.encode(message);
	}

	public String toString() {
		return toJson();
	}

	@Override
	public <T> T header(Key<T> key) {
		if (header != null)
			return header.get(key);
		else
			return null;
	}

	@Override
	public <T> T parent_header(Key<T> key) {
		if (parent != null)
			return parent.get(key);
		else
			return null;
	}

	@Override
	public <T> T content(Key<T> key) {
		if (content != null)
			return content.get(key);
		else
			return key.defaultValue();
	}

	@Override
	public <T> T metadata(Key<T> key) {
		if (metadata != null)
			return metadata.get(key);
		else
			return key.defaultValue();
	}

	@Override
	public <T> T content(Key<T> key, T defaultValue) {
		if (content != null)
			return content.get(key, defaultValue);
		else
			return defaultValue;
	}

	@Override
	public <T> T metadata(Key<T> key, T defaultValue) {
		if (metadata != null)
			return metadata.get(key, defaultValue);
		else
			return defaultValue;
	}

	@Override
	public boolean isReply() {
		return header(Message.REF_ID) != null || parent_header(Message.MSG_ID) != null;
	}

	@Override
	public String msgId() {
		return Objects.requireNonNull(header(Message.MSG_ID));
	}

	@Override
	public String msgRef() {
		if (header.has(Message.REF_ID))
			return Objects.requireNonNull(header.get(Message.REF_ID));
		else
			return Objects.requireNonNull(parent_header(Message.MSG_ID));
	}

	@Override
	public String msgType() {
		return Objects.requireNonNull(header(Message.MSG_TYPE));
	}

	@Override
	public List<String> address() {
		return null;
//		return address.toListOf(String.class);
	}

}