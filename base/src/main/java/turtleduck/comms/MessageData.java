package turtleduck.comms;

import java.util.List;
import java.util.function.Supplier;

import turtleduck.comms.Message.OpenMessage;
import turtleduck.comms.impl.MessageImpl;

public interface MessageData {
	static void setDataConstructor(Supplier<MessageData> cons) {
		MessageImpl.dataConstructor = cons;
	}

	void put(String key, int val);
	void put(String key, String val);

	String get(String key, String defaultValue);

	int get(String key, int defaultValue);

	String toJson();

	<U> U encodeAs(Class<U> clazz);

	<U extends Message> List<U> getList(String key);
	
	void putList(String key);

	<U extends Message> void addToList(String key, U msg);
}
