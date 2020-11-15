package turtleduck.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;

import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.bridge.PermittedOptions;
import turtleduck.comms.MessageData;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.Readline;
import turtleduck.util.Strings;
import com.julienviet.childprocess.Process;
import com.julienviet.childprocess.ProcessOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractVerticle {
	protected final Logger logger = LoggerFactory.getLogger(Server.class);
	protected SessionStore sessionStore;
	protected TShell shell;
	private HttpServer server;
	ServerWebSocket webSocket;
	PseudoTerminal pty;
	Path[] serverRoot = { Path.of("/home/anya/git/turtleduck/tea/target/generated/js"),
			Path.of("/home/anya/git/turtleduck/tea/src/main/webapp/"),
			Path.of("/home/anya/git/turtleduck/tea/node_modules"), };
	protected String callbackPath = "/auth_callback";
	protected Readline readline;
	private SessionHandler sessionHandler;
	private LoggerHandler loggerHandler;
	private OAuth2Auth oAuth2Provider;
	private AuthenticationHandler authHandler;

	/**
	 * The port clients should send requests to
	 */
	private int externalPort = 9090;
	/**
	 * The host address clients should send requests to
	 */
	private String externalAddress = "localhost";
	private boolean externalSsl = !externalAddress.equals("localhost");
	/**
	 * The port we should bind to
	 */
	private int bindPort = 9080;
	/**
	 * The host address we should bind to
	 */
	private String bindAddress = "localhost";
	private boolean bindSsl = !bindAddress.equals("localhost");

	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
		System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
		System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

		Vertx vertx = Vertx.vertx(new VertxOptions().setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS));
		Server server = new Server();
		DeploymentOptions opts = new DeploymentOptions();
		vertx.deployVerticle(server, opts, res -> {
			if (res.succeeded()) {
				server.logger.info("Deployment id is: {}", res.result());
			} else {
				server.logger.error("Deployment failed", res.cause());
				vertx.close();
			}
		});
	}

	public Server() {
	}

	public URI externalUri(String path) {
		try {
			return new URI(externalSsl ? "https" : "http", null, externalAddress, externalPort, path, null, null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void send(String s) {
		logger.info("Output: " + Strings.escape(s));
		if (webSocket != null) {
			webSocket.writeTextMessage(s);
		}
	}

	public void start(Promise<Void> startPromise) {
		context.put("verticle", "server");
		logger.info("Server context: " + vertx.getOrCreateContext());

		logger.info("config: " + this.config());

//		JsonObject authInfo = new JsonObject().put("username", "anya").put("password", "panya");

//		AuthHandler basicAuthHandler = BasicAuthHandler.create((authInfo, resultHandler) -> {
//			System.out.println(authInfo);
//			resultHandler.handle(Future.succeededFuture(new User(authInfo.getString("username"))));
//		});

		OAuth2Options oauth2opts = new OAuth2Options()
				.setClientID(System.getenv("OAUTH_CLIENTID")) //
				.setClientSecret(System.getenv("OAUTH_SECRET")) //
				.setSite("https://retting.ii.uib.no") //
//				.setTokenPath("/oauth/token") //
//				.setAuthorizationPath("/oauth/authorize") //
//				.setFlow(OAuth2FlowType.AUTH_CODE)
		;
		logger.info("oauth2opts: " + oauth2opts);
		OpenIDConnectAuth.discover(vertx, oauth2opts, oidcRes -> {
			if (oidcRes.succeeded()) {
				oAuth2Provider = oidcRes.result();
				OAuth2AuthHandler authHandler = OAuth2AuthHandler.create(vertx, oAuth2Provider,
						externalUri(callbackPath).toString());
				startWebServer(startPromise,
						(r) -> authHandler.setupCallback(r.route(callbackPath)).withScope("openid email"));
			} else {
				logger.error("Failed to initialize OpenID Connect", oidcRes.cause());
				vertx.close();
			}
		});
	}

	private void startWebServer(Promise<Void> startPromise, Function<Router, AuthenticationHandler> auth) {
		sessionStore = LocalSessionStore.create(vertx);
		sessionHandler = SessionHandler.create(sessionStore);
		loggerHandler = LoggerHandler.create(false, LoggerHandler.DEFAULT_FORMAT);
		MessageData.setDataConstructor(() -> new MessageRepr());
		server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		AuthenticationHandler basicAuthHandler = BasicAuthHandler.create((authInfo, resultHandler) -> {
			System.out.println(authInfo);
			resultHandler.handle(Future.succeededFuture(User.fromName(authInfo.getString("username"))));
		});
		router.route().handler(LoggerHandler.create(true, LoggerHandler.DEFAULT_FORMAT)).handler(sessionHandler);

//		Route root = router.route("/");
		authHandler = auth.apply(router);
		router.route().handler(authHandler).handler(loggerHandler);
//		Route main = router.route("/*").handler(authHandler);
		// router.route().handler(sessionHandler).handler(loggerHandler);//
		// .handler(authHandler);

		SockJSHandlerOptions options = new SockJSHandlerOptions(); // .setHeartbeatInterval(10000);
//		BridgeOptions opts = new BridgeOptions();
//		opts.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
//		opts.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		// .addInboundPermitted();// new PermittedOptions().setAddressRegex(".*"))
		// .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		Router sockJSRouter = sockJSHandler.socketHandler((sockjs) -> {
			System.out.println("handling: "+  sockjs.uri());
			Session session = sockjs.webSession();
			TurtleDuckSession tds = session.get("TurtleDuckSession");
			long t = System.currentTimeMillis();
			System.out.println("start: " + t + " " + sockjs + ", " + sockjs.uri() + "\n" + sockjs.headers());
			if (tds != null) {
				try {
					tds.runOnContext((__) -> tds.connect(sockjs));
				} catch (RuntimeException e) {
					logger.error("Connecting to TurtleDuckSession failed", e);
				}
			} else {
				try {
					
					userInfo(sockjs.webUser(), session, (userInfo -> {
						logger.info("Creating TurtleDuckSession for " + session.get("userInfo") + sockjs.uri());
						TurtleDuckSession newTds = new TurtleDuckSession(session);
						session.put("TurtleDuckSession", newTds);
						vertx.deployVerticle(newTds, new DeploymentOptions(), (res) -> {
							if (res.succeeded()) {
								logger.info("Deployed TurtleDuck as " + res.result());
								newTds.runOnContext((__) -> newTds.connect(sockjs));
							} else {
								logger.error("TurtleDuck deployment failed!", res.cause());
								sockjs.close(1011, "internal error");
							}
						});
					}));
				} catch (RuntimeException e) {
					logger.error("Creating TurtleDuckSession failed", e);
					sockjs.close(1011, "internal error");
				}
			}
			System.out.println("end: " + (System.currentTimeMillis() - t));
		});
		router.mountSubRouter("/terminal", sockJSRouter);
//		router.route("/terminal/*").handler(sockJSHandler);
		router.get("/hello").handler(ctx -> {
			userInfo(ctx.user(), ctx.session(), (ui) -> {
				ctx.response().end("Hello, " + ui.getString("name"));
			});
		});
		
		Route statics = router.route().method(HttpMethod.GET);//.order(2000);
		for (Path p : serverRoot)
			statics.handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(p.toString()));

		
		server.requestHandler(router);

		/*
		 * .webSocketHandler(ws -> { System.out.println("WebSocket connection on " +
		 * ws.path()); ws.textMessageHandler(string -> { System.err.println("Received <"
		 * + string + ">"); pty.writeToHost(string); }); ws.closeHandler((_void) -> {
		 * webSocket = null; System.err.println("Connection closed."); }); ws.accept();
		 * webSocket = ws; });
		 */

		// Now bind the server:
		server.listen(bindPort, bindAddress, res -> {
			if (res.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(res.cause());
			}
		});
	}

	private void userInfo(User user, Session session, Handler<JsonObject> handler) {
		JsonObject userinfo = session.get("userInfo");
		if (userinfo != null) {
			handler.handle(userinfo);
		} else {
			userinfo = new JsonObject();
			userinfo.put("nickname", "anya");
			session.put("userInfo", userinfo);
			handler.handle(userinfo);
//			oAuth2Provider.userInfo(user, res -> {
//				if (res.succeeded()) {
//					JsonObject userInfo = res.result();
//					session.put("userInfo", userInfo);
//					handler.handle(userInfo);
//				} else {
//					logger.error("Getting userInfo for " + user + " failed", res.cause());
//				}
//			});
		}
	}

	private String contentTypeOf(Path path) {
		String name = path.getFileName().toString();
		if (name.endsWith(".html"))
			return "text/html";
		else if (name.endsWith(".js"))
			return "application/javascript";
		else if (name.endsWith(".css"))
			return "text/css";
		else
			return "text/plain";
	}

	public void spawn(SockJSSocket sockjs) {
		ProcessOptions popts = new ProcessOptions();
		popts.getEnv().put("TERM", "xterm-256color");

		Process process = Process.create(vertx, "/usr/bin/ipython", List.of("-i"), popts);
		process.stderr().handler((buf) -> {
			System.out.println("stderr: " + buf);
			sockjs.write(buf);
		});
		process.stdout().handler((buf) -> {
			System.out.println("stdout: " + buf);
			sockjs.write(buf);
		});
//	sockjs.pipeTo(process.stdin());	
		process.exitHandler((result) -> {
			sockjs.close();
		});
		process.stdin().write(Buffer.buffer("print('hello')\n"));
		process.start();
		sockjs.handler((buf) -> {
			System.out.println("stdin: " + buf);
			if (buf.getByte(0) == 4) {
				process.stdin().close();
			} else
				process.stdin().write(buf);
		});
	}

	public void stop() {

	}
}
