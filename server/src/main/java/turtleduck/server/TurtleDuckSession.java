package turtleduck.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
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

@MessageDispatch("turtleduck.server.generated.TDSDispatch")
public class TurtleDuckSession extends AbstractVerticle implements HelloService, ShellService, Shareable {
	protected TShell shell;
	protected TDSDispatch dispatch;
	protected SockJSSocket socket;
	protected String user;
	protected final static Logger logger = LoggerFactory.getLogger(TurtleDuckSession.class);
	protected VertxRouter router;
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
		router = new VertxRouter(sessionId, user);
//		router.route("hello", msg -> dispatch.dispatch(msg));
		router.route(dispatch);
		terminalService = new TerminalServiceProxy("terminal.server", router::send);
		logger.info("Starting TurtleDuck " + context.deploymentID() + " for user " + user);
//		createShell();
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

		socket.handler(this::receive);
		socket.resume();
	}

	public void receive(Buffer buf) {
		logger.info("recv context: " + vertx.getOrCreateContext().get("verticle"));
		logger.info("RECV: " + buf);
		JsonObject obj = buf.toJsonObject();
		router.receive(Message.fromDict(VertxJson.decodeDict(obj)));

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
			terminalService.write(s);
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
		pty.reconnectListener(() -> {
			shell.reconnect();
			shell.prompt();
		});
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
		if(shell == null)
			createShell();
		Dict myEndPoints = Dict.create();
		myEndPoints.put("turtleduck.server", Array.of("hello"));
		myEndPoints.put("turtleduck.shell.server", Array.of("inspect_request"));
		Dict reply = Dict.create();
		reply.put(HelloService.ENDPOINTS, myEndPoints);
		reply.put(HelloService.USERNAME, user);
		reply.put(HelloService.USER, userInfo);
		reply.put(HelloService.EXISTING, used);
		used = true;
		return Async.succeeded(reply);
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

}
