package turtleduck.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.comms.Message;
import turtleduck.comms.Message.ConnectMessage;
import turtleduck.comms.Message.OpenMessage;
import turtleduck.display.DisplayInfo;
import turtleduck.display.Screen;
import turtleduck.events.KeyEvent;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.Readline;

public class TurtleDuckSession {
	private SockJSSocket socket;
	private UUID uuid;
	private Map<Integer, Channel> channels = new HashMap<>();
	private Map<String, Channel> names = new HashMap<>();
	private int nextChannelId = 1;

	public TurtleDuckSession() {
		uuid = UUID.randomUUID();
	}

	public void connect(SockJSSocket sockjs) {
		if (socket == sockjs) {
			return;
		} else if (socket != null) {
			socket.close();
			socket = null;
		}

		socket = sockjs;
		socket.exceptionHandler((e) -> {
			System.err.println("Socket error");
			e.printStackTrace();
		});
		socket.endHandler((x) -> {
			System.err.println("Socket closed");
			if (socket == sockjs) {
				socket = null;
			}
		});

		ConnectMessage msg = Message.createConnect();

		for (Channel ch : channels.values()) {
			if (!ch.name().isEmpty()) {
				OpenMessage opened = Message.createOpened(ch.channelId(), ch.name(), ch.service());
				msg.addOpened(opened);
			}
		}
		msg.msg(channels.values().isEmpty() ? "Welcome!" : "Welcome back!");
		send(msg);
		socket.handler(this::receive);
	}

	public void receive(Buffer buf) {
		try {
			System.out.println("RECV: " + buf);
			JsonObject obj = buf.toJsonObject();
			MessageRepr repr = new MessageRepr(obj);
			Message msg = Message.create(repr);
			int channel = msg.channel();
			String type = msg.type();
			if (channel > 0) {
				Channel ch = channels.get(channel);
				if (ch == null) {
					System.out.println("  dropped msg to nonexistent channel " + channel);
					return;
				}

				if (type.equals("Close")) {

					Channel removed = channels.remove(channel);
					if (removed != null)
						names.remove(removed.service() + ":" + removed.name());
					ch.close();
					return;
				}
				ch.receive(msg);
			} else {
				if (type.equals("Open")) {
					OpenMessage omsg = (OpenMessage) msg;
					String service = omsg.service();
					String name = omsg.name();
					if (!name.matches("^[a-zA-Z0-9_-]*$")) {
						sendError();
						return;
					}
					if (names.containsKey(service + ":" + name)) {
						OpenMessage reply = Message.createOpened(names.get(service + ":" + name).channelId(), name,
								service);
						send(reply);
					} else if (service.equals("jshell")) {
						PseudoTerminal pty = new PseudoTerminal();
						// TODO: do async
						Server.vertx.executeBlocking((promise) -> {
							try {
								Readline readline = new Readline();
								readline.attach(pty);
								Screen screen = ServerDisplayInfo.provider().startPaintScene(TurtleDuckSession.this);
								TShell shell = new TShell(screen, null, pty.createCursor());
								readline.handler((line) -> {
									Server.vertx.executeBlocking((linePromise) -> {
										try {
											shell.enter(line);
											linePromise.complete("ok");
										} catch (Throwable t) {
											linePromise.fail(t);
										}
									}, (r) -> {
										System.out.println(r);
									});
								});
								promise.complete("ok");
							} catch (Throwable t) {
								t.printStackTrace();
								promise.fail(t);
							}
						}, (result) -> {
							System.out.print("Shell started: " + result);
						});
						int id = nextChannelId++;
						Channel ch = new PtyChannel(id, name, service, pty);
						ch.initialize();
						channels.put(id, ch);
						names.put(service + ":" + name, ch);
						OpenMessage reply = Message.createOpened(id, name, service);
						send(reply);
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	void sendError() {
		if (socket != null) {
			socket.write(new JsonObject().put("type", "ERR").toBuffer());
		}
	}

	void send(Message msg) {
		if (socket != null) {
//			System.out.println("SEND: " + msg);
			socket.write(msg.encodeAs(Buffer.class));
		}
	}

	interface Channel {
		void receive(Message msg);

		void close();

		void initialize();

		String name();

		int channelId();

		String service();
	}

	class PtyChannel implements Channel {
		PseudoTerminal pty;
		int id;
		String name;
		private String service;

		public PtyChannel(int id, String name, String service, PseudoTerminal pty) {
			this.id = id;
			this.name = name;
			this.pty = pty;
			this.service = service;
		}

		public void initialize() {
			pty.terminalListener((s) -> {
				send(Message.createStringData(id, s));
			});
		}

		public void close() {
			pty.disconnectTerminal();
		}

		public void receive(Message obj) {
			switch (obj.type()) {
			case "Data":
				pty.writeToHost(((Message.StringDataMessage) obj).data());
				break;
			case "KeyEvent":
				pty.sendToHost(((Message.KeyEventMessage) obj).keyEvent());
				break;
			default:
				System.out.println("Unknown message type: " + obj.type());
			}
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public int channelId() {
			return id;
		}

		@Override
		public String service() {
			return service;
		}

	}
}
