package turtleduck.comms.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import turtleduck.comms.Message;
import turtleduck.comms.MessageData;
import turtleduck.events.KeyCodes;
import turtleduck.events.KeyEvent;

public class MessageImpl implements Message {
	public static Supplier<MessageData> dataConstructor;
	protected final MessageData data;

	public MessageImpl(int channel, String type) {
		data = dataConstructor.get();
		if (channel != 0)
			data.put("ch", channel);
		data.put("type", type);
	}

	public MessageImpl(MessageData data) {
		this.data = data;
		data.get("ch", 0);
	}

	@Override
	public <T> T encodeAs(Class<T> clazz) {
		return data.encodeAs(clazz);
	}

	@Override
	public String toJson() {
		return data.toJson();
	}

	@Override
	public MessageData rawData() {
		return data;
	}

	@Override
	public int channel() {
		return (int) data.get("ch", 0);
	}

	@Override
	public void channel(int ch) {
		data.put("ch", ch);
	}

	@Override
	public String type() {
		return (String) data.get("type", "");
	}

	public String toString() {
		return data.toString();
	}

	/*
	 * @Override public <T> T get(String key, T defaultValue) { return (T)
	 * data.getOrDefault(key, defaultValue); }
	 * 
	 * 
	 * @Override public <T> void put(String key, T value) { check(key, value);
	 * data.put(key, value); }
	 * 
	 * private <T> void check(String key, T value) { if(key.equals("ch") && value
	 * instanceof Integer) { return; } else if(key.equals("type") && value
	 * instanceof String) { return; } throw new IllegalArgumentException(key + "=" +
	 * value); }
	 */
	public static class ConnectMsgImpl extends MessageImpl implements Message.ConnectMessage {

		public ConnectMsgImpl() {
			super(0, "Connect");
			data.putList("opened");
		}

		public ConnectMsgImpl(MessageData data) {
			super(data);
		}

		@Override
		public String msg() {
			return data.get("msg", "");
		}

		@Override
		public void msg(String msg) {
			data.put("msg", msg);
		}

		@Override
		public List<OpenMessage> opened() {
			List<OpenMessage> list = data.getList("opened");
			if (list != null)
				return list;
			else
				return Arrays.asList();
		}

		@Override
		public void addOpened(OpenMessage msg) {
			data.addToList("opened", msg);
		}

	}

	public static class OpenMsgImpl extends MessageImpl implements Message.OpenMessage {

		public OpenMsgImpl(String type, int newCh, String name, String service) {
			super(0, type);
			if (newCh != 0)
				data.put("chNum", newCh);
			if (name != null)
				data.put("name", name);
			if (service != null)
				data.put("service", service);
		}

		public OpenMsgImpl(MessageData data) {
			super(data);
		}

		@Override
		public int chNum() {
			return data.get("chNum", 0);
		}

		@Override
		public String name() {
			return data.get("name", "");
		}

		@Override
		public String service() {
			return data.get("service", "");
		}
	}

	public static class StringDataImpl extends MessageImpl implements Message.StringDataMessage {
		public StringDataImpl(int channel, String data) {
			this(channel, "Data", data);
		}

		public StringDataImpl(MessageData data) {
			super(data);
		}

		public StringDataImpl(int channel, String type, String data) {
			super(channel, type);
			this.data.put("data", data);
		}

		public String data() {
			return data.get("data", "");
		}

	}

	public static class DictDataImpl extends MessageImpl implements Message.DictDataMessage {
		public DictDataImpl(int channel, Map<String, String> data) {
			this(channel, "Dict", data);
		}

		public DictDataImpl(MessageData data) {
			super(data);
		}

		public DictDataImpl(int channel, String type, Map<String, String> data) {
			super(channel, type);
			for (Entry<String, String> e : data.entrySet())
				this.data.put(e.getKey(), e.getValue());
		}

		public String get(String key) {
			return data.get(key, "");
		}

	}

	public static class KeyEventMsgImpl extends StringDataImpl implements Message.KeyEventMessage {
		private KeyEvent event;

		public KeyEventMsgImpl(MessageData data) {
			super(data);
		}

		public KeyEventMsgImpl(int channel, int code, String data) {
			super(channel, "KeyEvent", data);
			this.data.put("code", code);
		}

		public int modifiers() {
			return data.get("mods", 0);
		}

		public int flags() {
			return data.get("flags", 0);
		}

		public int code() {
			return data.get("code", KeyCodes.Special.UNDEFINED);
		}

		@Override
		public KeyEvent keyEvent() {
			if (event == null) {
				event = KeyEvent.create(code(), data(), modifiers(), flags());
			}
			return event;
		}

		@Override
		public KeyEventMessage modifiers(int mods) {
			data.put("mods", mods);
			return this;
		}

		@Override
		public KeyEventMessage flags(int flags) {
			data.put("mods", flags);
			return this;
		}

		@Override
		public KeyEventMessage code(int code) {
			data.put("code", code);
			return this;
		}

	}

}
