package turtleduck.comms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import turtleduck.messaging.ExecuteReplyHandler;
import turtleduck.messaging.ExecuteService;
import turtleduck.messaging.MessageImpl;
import turtleduck.messaging.MessageWriter;
import turtleduck.util.Dict;
import turtleduck.vfs.drivers.FileFileSystem;
import turtleduck.messaging.Message;

import org.junit.jupiter.api.Test;

public class MessageTest {

	@Test
	void testToJson() {
		MessageWriter mw = Message.writeTo("foo");
		ExecuteService req = ExecuteService.message(mw, "session", "user");
		req.executeRequest(null, "2+2", null);
		Message msg = mw.done();

		System.out.println(msg.toJson());
	}

	@Test
	void testFromJson() {
		MessageWriter mw = Message.writeTo("foo");
		ExecuteService req = ExecuteService.message(mw, "session", "user");
		req.executeRequest(null, "2+2", null);
		Message msg = mw.done();
		ExecuteService handler = new ExecuteService() {

			@Override
			public void executeRequest(Message request, String code, boolean silent, boolean store_history,
					Dict user_expressions, boolean allow_stdin, boolean stop_on_error,
					ExecuteReplyHandler reply) {
				assertEquals("2+2", code);
				System.out.println(code + "," + silent + "," + store_history + "," + user_expressions + ","
						+ allow_stdin + "," + stop_on_error + "," + reply);
			}
		};
		msg.handle(handler, null);
	}

	@Test
	void testFromJson2() {
		MessageWriter mw = Message.writeTo("foo");
		ExecuteService req = ExecuteService.message(mw, "session", "user");
		req.executeRequest(null, "2+2", null);
		Message msg = mw.done();
		mw = Message.writeTo("foo");

		ExecuteService handler = ExecuteService.message(mw, "session", "user");
		msg.handle(handler, null);
		Message msg2 = mw.done();
		assertEquals(msg.content(), msg2.content());
	}
/*
	@Test
	void testHandle() {
		MessageWriter mw = Message.writer();
		ExecuteRequestHandler req = ExecuteRequestHandler.message(mw, "session", "user");
		req.executeRequest(null, "2+2", null);
		Message msg = mw.done();
		
		ExecuteRequestHandler handler = new ExecuteRequestHandler() {
			@Override
			public void executeRequest(Message request, String code, boolean silent, boolean store_history,
					JsonObject user_expressions, boolean allow_stdin, boolean stop_on_error,
					ExecuteReplyHandler reply) {
				if (code.equals("2+2")) {
					JsonObject payload = new JsonObject();
					payload.addProperty("result", "4");
					reply.okReply(List.of(payload), null, 1);
				} else {
					reply.errorReply("wrong code", "foo", List.of(), 1);
				}
			}
		};
		
		Future<Message> fut = ExecuteRequestHandler.handle(msg, handler);
		fut.onComplete(res -> {
			System.out.println(res);
		});
	}
	*/
}
