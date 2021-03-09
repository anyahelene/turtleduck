package turtleduck.messaging;

import java.util.List;

import turtleduck.async.Async;

public interface Dispatch<T> {
	Async<Message> dispatch(Message msg);

	List<String> requestTypes();

	List<String> replyTypes();
}
