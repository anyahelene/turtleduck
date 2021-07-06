package turtleduck.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import turtleduck.annotations.MessageDispatch;
import turtleduck.async.Async;
import turtleduck.async.Async.Sink;
import turtleduck.messaging.HelloService;
import turtleduck.messaging.Message;
import turtleduck.messaging.Router;
import turtleduck.messaging.ShellService;
import turtleduck.messaging.TerminalService;
import turtleduck.messaging.generated.TerminalServiceProxy;
import turtleduck.server.generated.TDSDispatch;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.util.Array;
import turtleduck.util.Dict;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

@MessageDispatch("turtleduck.server.generated.TDSDispatch")
public class TurtleDuckSession extends AbstractVerticle implements HelloService, ShellService, Shareable {
	protected TShell shell;
	protected TDSDispatch dispatch;
	protected String user;
	protected final static Logger logger = LoggerFactory.getLogger(TurtleDuckSession.class);
	protected final Set<String> users = new HashSet<>();
	protected Router router;
	protected String sessionId;
	protected ServerScreen screen;
	protected TerminalService terminalService;
	protected Dict userInfo;
	protected Server server;
	protected boolean used = false;

	protected TurtleDuckSession(JsonObject userInfo, Server server) {
		this.user = userInfo.getString("username");
		this.dispatch = new TDSDispatch(this);
		this.userInfo = VertxJson.decodeDict(userInfo);
		this.server = server;
	}

	public void start() {
		context.put("verticle", "session-" + user);
		sessionId = "";
		router = new Router();
//		router.route("hello", msg -> dispatch.dispatch(msg));
		router.route(dispatch);
		terminalService = new TerminalServiceProxy("turtleduck.terminal", router);
		logger.info("Starting TurtleDuck " + context.deploymentID() + " for user " + user);
//		createShell();
	}

	public Router router() {
		return router;
	}

	public void connect(JsonObject userInfo, SockJSSocket sockjs) {
		Dict info = VertxJson.decodeDict(userInfo);
		logger.info("connect({}),, info={}", sockjs, info);
		/*
		 * if (socket == sockjs) { return; } else if (socket != null) {
		 * logger.warn("Already connected, closing old socket"); socket.close(); socket
		 * = null; router.disconnect(); }
		 */
		if (users.size() >= 9999) {
			logger.error("Too many users, giving up");
			sockjs.close();
			return;
		}
		String uname = userInfo.getString("username");
		String connName = uname;
		for (int i = 0; users.contains(connName) && i < 9999; i++) {
			connName = uname + "_" + i;
		}
		String connectionName = connName;
		users.add(connectionName);

		VertxConnection conn = new VertxConnection(connectionName, info);
		router.connect(conn, "turtleduck.terminal", "turtleduck.explorer", "turtleduck.screen", "turtleduck.editor");
		sockjs.exceptionHandler((e) -> {
			logger.error("Socket error", e);
		});
		sockjs.endHandler((x) -> {
			logger.info("Socket closed");
			users.remove(connectionName);
			router.disconnect(conn);
			conn.disconnect();
		});
		conn.connect(sockjs);

	}

	<T> Async<T> enqueue(Supplier<T> fun) {
		Sink<T> async = Async.create();
		context.executeBlocking((promise) -> {
			try {
				promise.complete(fun.get());
			} catch (Throwable t) {
				promise.fail(t);
			}
		}, res -> {
			if (res.succeeded())
				async.success((T) res.result());
			else
				async.fail(res.cause());
		});
		return async.async();
	}

	private void createShell() {
		PseudoTerminal pty = new PseudoTerminal();
		pty.terminalListener(s -> {
			terminalService.write(s, "out");
		});
		pty.buffering(true);
		screen = (ServerScreen) ServerDisplayInfo.provider().startPaintScene(TurtleDuckSession.this);
		shell = new TShell(screen, null, pty.createCursor(), router);
		shell.enqueueWith(this::enqueue);
//		router.route(shell.dispatch());
//		pty.hostInputListener((line) -> {
//			context.executeBlocking((linePromise) -> {
//				logger.info("executeBlocking(): {}", line);
//				shell.enter(line);
//				linePromise.complete();
//			}, (r) -> {
//				logger.info("executeBlocking() result: {}", r);
//				pty.flushToTerminal();
//			});
//			return true;
//		});
		pty.useHistory(0, false);

//		shell.editorFactory((n, callback) -> {
//			EditorChannel ch = new EditorChannel(n, "editor", callback);
////			open(ch);
//			return ch;
//		});
	}

	@Override
	public Async<Dict> hello(String sessionName, Dict endPoints) {
		LocalMap<String, TurtleDuckSession> sessions = vertx.sharedData().getLocalMap("sessions");
		if (sessionId != null)
			sessions.removeIfPresent(sessionId, this);
		sessionId = sessionName;
		sessions.put(sessionId, this);
		router.init(sessionName, user);
		if(shell == null) {
			Sink<Dict> sink = Async.create();
			context.executeBlocking((Promise<Dict> promise) -> {
				createShell();
				promise.complete(finishHello());
			}, res -> {
				if(res.succeeded()) {
					sink.success(res.result());
				} else {
					sink.fail(res.cause());
				}
			});
			return sink.async();
		} else {
			return Async.succeeded(finishHello());
		}

	}

	private Dict finishHello() {
		Dict myEndPoints = Dict.create();
		myEndPoints.put("turtleduck.server", Array.of("hello"));
		myEndPoints.put("turtleduck.shell.server", Array.of("inspect_request"));
		Dict reply = Dict.create();
		reply.put(HelloService.ENDPOINTS, myEndPoints);
		reply.put(HelloService.USERNAME, user);
		reply.put(HelloService.USER, userInfo);
		reply.put(HelloService.EXISTING, used);
		reply.put(HelloService.CONNECTIONS, Array.from(users, String.class));
		used = true;
		return reply;
	}

	@Override
	public Async<Dict> executeRequest(String code, boolean silent, boolean store_history, Dict user_expressions,
			boolean allow_stdin, boolean stop_on_error) {
		return shell.executeRequest(code, silent, store_history, user_expressions, allow_stdin, stop_on_error);
	}

	@Override
	public Async<Dict> inspect(String code, int cursorPos, int detailLevel) {
		return shell.inspect(code, cursorPos, detailLevel);
	}

	@Override
	public Async<Dict> eval(String code, int ref, Dict opts) {
		screen.group("g" + ref);
		return shell.eval(code, ref, opts);
	}

	@Override
	public Async<Dict> complete(String code, int cursorPos, int detailLevel) {
		return shell.complete(code, cursorPos, detailLevel);
	}

	@Override
	public Async<Dict> refresh() {
		return shell.refresh();
	}
	
	public JsonObject info() {
		JsonObject info = new JsonObject();
		info.put("username", user);
		info.put("users", new JsonArray(new ArrayList<>(users)));
		info.put("name", sessionId);
		info.put("used", used);
		return info;
	}

}
