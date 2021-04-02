package turtleduck.terminal;

import java.util.function.Consumer;

import turtleduck.messaging.Message;

public interface Editor {

	String content();
	
	void content(String content);

//	void close();

//	void open(String name, BiConsumer<Boolean, String> callback);

	void onSave(Consumer<String> saver);

	void report(Message msg);
}
