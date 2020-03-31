package turtleduck.tea;

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
	@JSBody(params = { "json" }, script = "return new MessageRepr(json);")
	static native JSRepr create(String json);

	@JSBody(params = {}, script = "return new MessageRepr();")
	static native JSRepr create();

	private JSRepr repr;

	public MessageRepr(String json) {
		repr = create(json);
	}

	public MessageRepr() {
		repr = create();
	}

	interface JSRepr extends MessageData, JSObject {

	}

	@Override
	public void put(String key, int val) {
		repr.put(key, val);
	}

	@Override
	public void put(String key, String val) {
		repr.put(key, val);
	}

	@Override
	public String get(String key, String defaultValue) {
		return repr.get(key, defaultValue);
	}

	@Override
	public int get(String key, int defaultValue) {
		return repr.get(key, defaultValue);
	}

	@Override
	public String toJson() {
		return repr.toJson();
	}

	@Override
	public <U> U encodeAs(Class<U> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Message> List<U> getList(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putList(String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public <U extends Message> void addToList(String key, U msg) {
		// TODO Auto-generated method stub

	}
}
