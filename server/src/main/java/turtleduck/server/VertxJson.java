package turtleduck.server;




import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import turtleduck.util.Array;
import turtleduck.util.Dict;

public class VertxJson {
	public static Dict decodeDict(JsonObject jsobj) {
		Dict d = Dict.create();
		jsobj.forEach(entry -> {
			d.put(entry.getKey(), decode(entry.getValue()));
		});
		

		return d;
	}

	public static Array decodeArray(JsonArray jsobj) {
		Array a = Array.create();
		jsobj.forEach(obj -> {
			a.add(decode(obj));
		});
		return a;

	}

	public static Object decode(Object jsobj) {
		if (jsobj == null)
			return null;
		else if(jsobj instanceof JsonObject) {
			return decodeDict((JsonObject) jsobj);
		} else if(jsobj instanceof JsonArray) {
			return decodeArray((JsonArray) jsobj);
		} else {
			return jsobj;
		}
	}
}
