package turtleduck.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.comms.Channel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.comms.Message.ConnectMessage;
import turtleduck.comms.Message.OpenMessage;
import turtleduck.display.Screen;
import turtleduck.server.channels.EditorChannel;
import turtleduck.server.channels.PtyChannel;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import io.vertx.core.Handler;

public class TurtleDuckSession extends AbstractVerticle implements EndPoint {
	private SockJSSocket socket;
	private Map<Integer, Channel> channels = new HashMap<>();
	private Map<String, Channel> names = new HashMap<>();
	private int nextChannelId = 1;
	private final List<Handler<Void>> todo = new ArrayList<>();
	private String user;
	private Session session;
	private Logger logger = LoggerFactory.getLogger(getClass());

	protected TurtleDuckSession(Session session) {
		this.session = session;
		JsonObject userInfo = session.get("userInfo");
		user = userInfo.getString("nickname");
	}

	public void start() {
		context.put("verticle", "session-" + user);

		logger.info("Starting TurtleDuck " + context.deploymentID() + " for user " + user);
		synchronized (todo) {
			for (Handler<Void> h : todo) {
				logger.info("running delayed startup job: " + h);
				context.runOnContext(h);
			}
			todo.clear();
		}
	}

	public void runOnContext(Handler<Void> action) {
		if (context != null)
			context.runOnContext(action);
		else
			synchronized (todo) {
				todo.add(action);
			}
	}

	public void connect(SockJSSocket sockjs) {
		logger.info("connect({}), socket={}", sockjs, socket);
		if (socket == sockjs) {
			return;
		} else if (socket != null) {
			logger.warn("Already connected, closing old socket");
			socket.close();
			socket = null;
		}

		socket = sockjs;
		socket.exceptionHandler((e) -> {
			logger.error("Socket error", e);
		});
		socket.endHandler((x) -> {
			logger.info("Socket closed");
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
			logger.info("recv context: " + vertx.getOrCreateContext().get("verticle"));
			logger.info("RECV: " + buf);
			JsonObject obj = buf.toJsonObject();
			MessageRepr repr = new MessageRepr(obj);
			Message msg = Message.create(repr);
			int channel = msg.channel();
			String type = msg.type();
			if (channel > 0) {
				Channel ch = channels.get(channel);
				if (ch == null) {
					logger.warn("  dropped msg to nonexistent channel " + channel);
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
						context.executeBlocking((promise) -> {
							try {
//								Readline readline = new Readline();
//								readline.attach(pty);
								ServerScreen screen = (ServerScreen) ServerDisplayInfo.provider()
										.startPaintScene(TurtleDuckSession.this);
								TShell shell = new TShell(screen, null, pty.createCursor());
								pty.hostInputListener((line) -> {
									context.executeBlocking((linePromise) -> {
										try {
											shell.enter(line);
											screen.render();
											linePromise.complete("ok");
										} catch (Throwable t) {
											linePromise.fail(t);
										}
									}, (r) -> {
										System.out.println(r);
									});
									return true;
								});
								shell.editorFactory((n, callback) -> {
									EditorChannel ch = new EditorChannel(n, "editor", callback);
									open(ch);
									return ch;
								});
								promise.complete("ok");
							} catch (Throwable t) {
								t.printStackTrace();
								promise.fail(t);
							}
						}, (result) -> {
							logger.info("Shell started: " + result);
						});
						int id = nextChannelId();
						Channel ch = new PtyChannel(name, service, pty);
						ch.initialize();
						ch.opened(id, this);
						channels.put(id, ch);
						names.put(service + ":" + name, ch);
						OpenMessage reply = Message.createOpened(id, name, service);
						send(reply);
					}
				} else if (type.equals("Opened")) {
					OpenMessage omsg = (OpenMessage) msg;
					int newch = omsg.chNum();
					String tag = omsg.name() + ":" + omsg.service();
					logger.info("Opened: " + tag);
					Channel ch = names.get(tag);
					logger.info("Channel: " + ch.hashCode());
					if (newch != 0 && ch != null) {
						channels.put(newch, ch);
						ch.opened(newch, this);
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

	public void send(Message msg) {
		if (socket != null) {
			logger.info("send context: " + vertx.getOrCreateContext().get("verticle"));
			Buffer buffer = msg.encodeAs(Buffer.class);
			String string = msg.toString();
			if (string.length() < 80)
				logger.info("SEND: " + msg);
			else
				logger.info("SEND: " + string.substring(0, 75) + "… [" + buffer.length() + " bytes]");
			socket.write(buffer);
		}
	}

	public void open(Channel channel) {
		names.put(channel.name() + ":" + channel.service(), channel);
		Message.OpenMessage msg = Message.createOpen(channel.name(), channel.service());
		send(msg);
	}

	protected int nextChannelId() {
		int id = nextChannelId;
		nextChannelId += 2;
		return id;
	}
}
