package turtleduck.messaging;

import turtleduck.util.Dict;
import turtleduck.util.Key;

//import io.vertx.core.buffer.Buffer;

public interface MessageWriter {
	MessageWriter header(String session, String username, String msg_type);

	MessageWriter header(String msg_type);

	MessageWriter parent_header(Message parent);

	MessageWriter metadata(Dict dict);

	MessageWriter content(Dict dict);

	<T> MessageWriter putContent(Key<T> key, T value);

	<T> MessageWriter putMeta(Key<T> key, T value);

//	MessageWriter buffer(Buffer buffer);

	Message done();
}
