package turtleduck.messaging;

import turtleduck.util.Dict;
import turtleduck.util.JsonUtil;

public interface Encodable<T> {
	Dict toDict();
	T fromDict(Dict dict);
	
	default String toJson() {
		return toDict().toJson();
	}
	default T fromJson(String jsonString) {
		return fromDict((Dict) JsonUtil.decodeJson(jsonString));
	}
}
