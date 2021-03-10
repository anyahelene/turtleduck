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
import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.comms.Channel;
import turtleduck.comms.EndPoint;
import turtleduck.comms.Message;
import turtleduck.comms.Message.ConnectMessage;
import turtleduck.comms.Message.OpenMessage;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.Router;
import turtleduck.messaging.TerminalService;
import turtleduck.messaging.generated.TerminalServiceProxy;
import turtleduck.server.channels.EditorChannel;
import turtleduck.server.channels.PtyChannel;
import turtleduck.server.generated.TDSDispatch;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import io.vertx.core.Handler;

@MessageDispatch("turtleduck.server.generated.TDSDispatch")
public class TurtleDuckSession extends AbstractVerticle implements EndPoint, HelloService {
	private TShell shell;
	private TDSDispatch dispatch;
	private SockJSSocket socket;
	private Map<Integer, Channel> channels = new HashMap<>();
	private Map<String, Channel> names = new HashMap<>();
	private int nextChannelId = 1;
	private final List<Handler<Void>> todo = new ArrayList<>();
	private String user;
	private Session session;
	private final static Logger logger = LoggerFactory.getLogger(TurtleDuckSession.class);
	private VertxRouter router;
	private String sessionId;
	private ServerScreen screen;
	private TerminalService terminalService;

	public TurtleDuckSession(Session session, JsonObject userInfo) {
		this.session = session;
		user = userInfo.getString("usernick");
		dispatch = new TDSDispatch(this);
	}

	public void start() {
		context.put("verticle", "session-" + user);
		sessionId = "";
		router = new VertxRouter(sessionId, user);
		router.route("hello", msg -> dispatch.dispatch(msg));
		terminalService = new TerminalServiceProxy("terminal.server", router::send);
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

	public Router router() {
		return router;
	}

	public void connect(SockJSSocket sockjs) {
		logger.info("connect({}), socket={}", sockjs, socket);
		if (socket == sockjs) {
			return;
		} else if (socket != null) {
			logger.warn("Already connected, closing old socket");
			socket.close();
			socket = null;
			router.disconnect();
		}
		router.connect(sockjs);
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

		List<Channel> reopened = new ArrayList<>();
		for (Channel ch : channels.values()) {
			System.out.println("old channel: " + ch);
			if (!ch.name().isEmpty()) {
				OpenMessage opened = Message.createOpened(ch.channelId(), ch.name(), ch.service());
				reopened.add(ch);
				msg.addOpened(opened);
			}
		}
		msg.msg(channels.values().isEmpty() ? "WELCOME" : "WELCOME_BACK");
		msg.addInfo("username", user);
		logger.warn("OPEN: " + msg.toJson());
		createShell();
		send(msg);
//		router.send(Welcome.create("turtleduck.client", sessionId, user, Dict.create()), null);
		socket.handler(this::receive);
		// trigger reopen listeners after we've connected, since they may want to send
		// data
		for (Channel ch : reopened)
			ch.reopened();
	}

	public void receive(Buffer buf) {
		try {
			logger.info("recv context: " + vertx.getOrCreateContext().get("verticle"));
			logger.info("RECV: " + buf);
			JsonObject obj = buf.toJsonObject();
			if (!obj.containsKey("type")) {
				router.receive(turtleduck.messaging.Message.fromDict(VertxJson.decodeDict(obj)));
				return;
			}
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

	private void createShell() {
		PseudoTerminal pty = new PseudoTerminal();
		pty.terminalListener(s -> {
			terminalService.print(s);
			System.out.println("Terminal: " + s);
		});
		screen = (ServerScreen) ServerDisplayInfo.provider().startPaintScene(TurtleDuckSession.this);
		shell = new TShell(screen, null, pty.createCursor(), router);
		router.route(shell.dispatch());
		pty.hostInputListener((line) -> {
			context.executeBlocking((linePromise) -> {
				shell.enter(line);
				linePromise.complete();
			}, (r) -> {
				System.out.println(r);
			});
			return true;
		});
		pty.useHistory(0, false);
		pty.reconnectListener(() -> {
			shell.reconnect();
			shell.prompt();
		});
		shell.editorFactory((n, callback) -> {
			EditorChannel ch = new EditorChannel(n, "editor", callback);
			open(ch);
			return ch;
		});
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

	@Override
	public Async<Dict> hello(String sessionName, Dict endPoints) {
		sessionId = sessionName;
		router.init(sessionName, user);
		Dict myEndPoints = Dict.create();
		myEndPoints.put("turtleduck.server", Array.of("hello"));
		myEndPoints.put("turtleduck.shell.server", Array.of("inspect_request"));
		Dict reply = Dict.create();
		reply.put(HelloService.ENDPOINTS, myEndPoints);
		reply.put(HelloService.USERNAME, user);
		return Async.succeeded(reply);
	}

}
