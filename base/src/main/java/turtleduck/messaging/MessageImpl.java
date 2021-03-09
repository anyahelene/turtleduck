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
		msg.require(Message.HEADER)//
				.require(Message.PARENT_HEADER)//
				.require(Message.CONTENT) //
				.require(Message.METADATA)//
				.require(Message.BUFFERS)//
				//.require(Message.TO)
				;
//		address = msg.get(Message.TO);
		header = msg.get(Message.HEADER);
		parent = msg.get(Message.PARENT_HEADER);
		content = msg.get(Message.CONTENT);
		metadata = msg.get(Message.METADATA);
		buffers = msg.get(Message.BUFFERS);
		message = msg;
		checkHeader(header);
		if (parent.get(Message.MSG_ID) != null)
			checkHeader(parent);
		frozen = true;
	}

	private static void checkHeader(Dict head) {
		head.require(Message.MSG_ID) //
				.require(Message.SESSION)//
				.require(Message.USERNAME)//
				.require(Message.DATE) //
				.require(Message.MSG_TYPE) //
				.require(Message.VERSION);

	}

	@Override
	public Dict header() {
		return header;
	}

	@Override
	public Dict parent_header() {
		return parent;
	}

	@Override
	public Dict metadata() {
		return metadata;
	}

	@Override
	public Dict content() {
		return content;
	}

//	@Override
//	public List<Buffer> buffers() {
//		return buffers;
//	}

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
	public MessageWriter parent_header(Message parent) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		this.parent = parent.header();
		checkHeader(this.parent);
		return this;
	}

	@Override
	public MessageWriter metadata(Dict dict) {
		if (frozen)
			throw new IllegalStateException("Message already written");
		this.metadata = dict;
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
		if (parent.has(Message.MSG_ID))
			checkHeader(parent);
		frozen = true;
		return this;
	}

	private void assemble() {
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
		return parent_header(Message.MSG_ID) != null;
	}

	@Override
	public String msgId() {
		return Objects.requireNonNull(header(Message.MSG_ID));
	}

	@Override
	public String msgRef() {
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

	@Override
	public MessageWriter header(String msg_type) {
		return header("", "", msg_type);
	}
}