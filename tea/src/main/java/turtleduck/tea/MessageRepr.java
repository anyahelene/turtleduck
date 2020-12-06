package turtleduck.tea;

import java.util.ArrayList;
import java.util.List;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSMapLike;
import org.teavm.jso.core.JSNumber;
import org.teavm.jso.core.JSObjects;
import org.teavm.jso.core.JSString;
import org.teavm.jso.json.JSON;

import turtleduck.comms.Message;
import turtleduck.comms.MessageData;

public class MessageRepr implements MessageData {
	private JSMapLike<JSObject> data;

	public MessageRepr(String json) {
		JSObject obj = JSON.parse(json);
		data = obj.cast();
	}

	public MessageRepr(JSObject obj) {
		data = obj.cast();
	}

	public MessageRepr() {
		data = JSObjects.create();
	}

	@Override
	public void put(String key, int val) {
		data.set(key, JSNumber.valueOf(val));
	}

	@Override
	public void put(String key, String val) {
		data.set(key, JSString.valueOf(val));
	}

	@Override
	public String get(String key, String defaultValue) {
		JSObject jsObject = data.get(key);

		if (JSObjects.typeOf(jsObject).equals("string")) {
			return ((JSString) jsObject).stringValue();
		} else {
			return defaultValue;
		}
	}

	@Override
	public int get(String key, int defaultValue) {
		JSObject jsObject = data.get(key);
		
		if (JSObjects.typeOf(jsObject).equals("number")) {
			return ((JSNumber) jsObject).intValue();
		} else {
			return defaultValue;
		}
	}

	@Override
	public String toJson() {
		return JSON.stringify(data);
	}

	@Override
	public <U> U encodeAs(Class<U> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Message> List<U> getList(String key) {
		JSObject jsObject = data.get(key);
		List<U> list = new ArrayList<>();
		JSArray<JSObject> array = jsObject.cast();
		for (int i = 0; i < array.getLength(); i++) {
			list.add(Message.create(new MessageRepr(array.get(i))));
		}

		return list;
	}

	@Override
	public void putList(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public <U extends Message> void addToList(String key, U msg) {
		// TODO Auto-generated method stub

	}

	public String toString() {
		return toJson();
	}
}
