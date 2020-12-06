package turtleduck.server;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import turtleduck.comms.Message;
import turtleduck.comms.MessageData;

public class MessageRepr implements MessageData {
	private final JsonObject data;

	public MessageRepr() {
		data = new JsonObject();
	}

	public MessageRepr(JsonObject obj) {
		data = obj;
	}

	@Override
	public void put(String key, String val) {
		data.put(key, val);
	}

	@Override
	public void put(String key, int val) {
		data.put(key, val);
	}

	@Override
	public String toJson() {
		return data.encode();
	}

	@Override
	public <U> U encodeAs(Class<U> clazz) {
		if (clazz == JsonObject.class) {
			return (U) data;
		} else if (clazz == Buffer.class) {
			return (U) data.toBuffer();
		} else {
			return null;
		}
	}

	private JsonArray getArray(String key) {
		JsonArray array = data.getJsonArray(key);
		if (array == null) {
			array = new JsonArray();
			data.put(key, array);
		}
		return array;
	}

	@Override
	public <U extends Message> List<U> getList(String key) {
		List<U> list = new ArrayList<>();
		for (Object elt : getArray(key)) {
			if (elt instanceof JsonObject) {
				list.add(Message.create(new MessageRepr((JsonObject) elt)));
			}
		}
		return list;
	}

	@Override
	public String get(String key, String defaultValue) {
		return data.getString(key, defaultValue);
	}

	@Override
	public int get(String key, int defaultValue) {
		return data.getInteger(key, defaultValue);
	}

	@Override
	public void putList(String key) {
		data.put("key", getArray(key));
	}

	@Override
	public <U extends Message> void addToList(String key, U msg) {
		getArray(key).add(((MessageRepr) msg.rawData()).data);
	}

	public String toString() {
		return data.toString();
	}
}
