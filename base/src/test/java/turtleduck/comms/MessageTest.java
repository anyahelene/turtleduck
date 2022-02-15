package turtleduck.comms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import turtleduck.messaging.ExecuteService;
import turtleduck.messaging.MessageImpl;
import turtleduck.messaging.MessageWriter;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.generated.ShellServiceProxy;
import turtleduck.util.Dict;
import turtleduck.util.JsonUtil;
import turtleduck.vfs.drivers.FileFileSystem;
import turtleduck.messaging.Message;

import org.junit.jupiter.api.Test;

public class MessageTest {

	@Test
	void testToJson() {
		MessageWriter mw = Message.writeTo("foo").header("type");
		Message msg = mw.done();
		System.out.println(msg.toJson());
		// TODO
	}

}
