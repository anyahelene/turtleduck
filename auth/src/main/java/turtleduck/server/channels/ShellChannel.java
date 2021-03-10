package turtleduck.server.channels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Context;
import turtleduck.comms.AbstractChannel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.comms.Message.DictDataMessage;
import turtleduck.display.Screen;
import turtleduck.server.ServerScreen;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;

public class ShellChannel extends AbstractChannel {
	List<String> buffer = new ArrayList<>();
	int len = 0;
	private TShell shell;
	private Context context;
	private ServerScreen screen;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ShellChannel(String name, String service, TShell shell, ServerScreen screen, Context context) {
		super(name, service, null);
		this.shell = shell;
		this.screen = screen;
		this.context = context;
	}

	public void opened(int id, EndPoint ep) {
		super.opened(id, ep);
	}

	public void reopened() {
		shell.reconnect();
	}

	public void close() {
		shell.close();
	}

	public void receive(Message obj) {
		switch (obj.type()) {
		case "Dict":
			DictDataMessage dmsg = (DictDataMessage) obj;
			String cmd = dmsg.get("cmd");
			switch (cmd) {
			case "enter":
				context.executeBlocking((promise) -> {
					try {
						shell.execute(dmsg.get("data"));
						shell.prompt();
						screen.render();
						DictDataMessage reply = Message.createDictData(0, new HashMap<>());
						promise.complete(reply);
					} catch (Throwable t) {
						promise.fail(t);
					}
				}).onComplete(res -> {
					if (res.succeeded()) {
					} else {
						logger.error("command failed:", res.cause());
					}
				});
				break;
			default:
				System.out.println("Unknown shell message command: " + cmd);

			}
			break;
		case "KeyEvent":
			break;
		default:
			System.out.println("Unknown message type: " + obj.type());
		}
	}

	public void write(String s) {
		if (s.isEmpty())
			return;
		boolean flush = s.contains("\n");
		if (flush && endPoint != null && buffer.isEmpty()) {
			endPoint.send(Message.createStringData(id, s));
		} else {
			buffer.add(s);
//			if (flush)
			flush();
		}
	}

	private void flush() {
		if (endPoint != null && !buffer.isEmpty()) {
			Message msg;
			synchronized (this) {
				msg = Message.createStringData(id, buffer.stream().collect(Collectors.joining()));
				buffer.clear();
			}
			endPoint.send(msg);
		}
	}
}