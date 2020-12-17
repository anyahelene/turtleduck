package turtleduck.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;

import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import turtleduck.comms.MessageData;

import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.Readline;
import turtleduck.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractVerticle {
	protected final Logger logger = LoggerFactory.getLogger(Server.class);
	protected SessionStore sessionStore;
	protected TShell shell;
	private HttpServer server;
	ServerWebSocket webSocket;
	PseudoTerminal pty;
	Path[] serverRoot;
	protected String pathPrefix = "";
	protected String callbackPath = "/auth_callback";
	protected Readline readline;
	private SessionHandler sessionHandler;
	private LoggerHandler loggerHandler;
	private OAuth2Auth oAuth2Provider;
	private AuthenticationHandler authHandler;
	protected Buffer indexHtml;
	protected String xAccelRedirect = null;

	/**
	 * The port clients should send requests to
	 */
	private int externalPort = -1;
	/**
	 * The host address clients should send requests to
	 */
	private String externalAddress = "localhost";
	private String externalScheme = "https";
	/**
	 * The port we should bind to
	 */
	private int bindPort = 9090;
	/**
	 * The host address we should bind to
	 */
	private String bindAddress = "localhost";

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
			return new URI(externalScheme, null, externalAddress, externalPort, pathPrefix + path, null, null);
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

		String ext = System.getenv("EXTERNAL_URL");
		if (ext != null) {
			Pattern pat = Pattern.compile("([a-z]+)://([^/:]+)(?::([0-9]+))?(?:/(.*[^/]))?/?");
			Matcher matcher = pat.matcher(ext.trim());
			if (!matcher.matches()) {
				throw new IllegalArgumentException("URL syntax error: " + ext);
			}
			String scheme = matcher.group(1);
			String host = matcher.group(2);
			String port = matcher.group(3);
			String path = matcher.group(4);
			if (scheme != null)
				externalScheme = scheme;
			if (host != null)
				externalAddress = host;
			if (port != null)
				externalPort = Integer.parseInt(port);
			if (path != null)
				pathPrefix = "/" + path;
		}
		String xAccel = System.getenv("X_ACCEL_REDIRECT");
		if (xAccel != null) {
			if (xAccel.isEmpty())
				xAccelRedirect = "/_webroot/";
			else
				xAccelRedirect = xAccel.trim();
			if (!xAccelRedirect.startsWith("/"))
				xAccelRedirect = "/" + xAccelRedirect;
			if (!xAccelRedirect.endsWith("/"))
				xAccelRedirect = xAccelRedirect + "/";
		}
		String port = System.getenv("BIND_PORT");
		if (port != null)
			bindPort = Integer.parseInt(port);
		String addr = System.getenv("BIND_ADDRESS");
		if (addr != null)
			bindAddress = addr.trim();
		if (xAccelRedirect == null) {
			String root = System.getenv("SERVER_ROOT");
			if (root == null)
				root = "/srv/turtleduck/webroot";
			serverRoot = Stream.of(root.trim().split(File.pathSeparator)).filter(p -> p.length() > 0)
					.map(p -> Path.of(p)).toArray(n -> new Path[n]);
			ArrayList<Path> pathList = new ArrayList<>(Arrays.asList(serverRoot));
			pathList.add(Path.of("webroot"));
			loadFile("index.html", pathList, res -> {
				if (res.succeeded()) {
					indexHtml = res.result();
				} else {
					logger.error("can't load index.html", res.cause());
					vertx.close();
				}
			});
		}
		String prefix = System.getenv("PATH_PREFIX");
		if (prefix != null)
			pathPrefix = prefix.trim();

		if (System.getenv("USE_BASIC_AUTH") != null) {
			// JsonObject authInfo = new JsonObject().put("username",
			// "anya").put("password", "panya");
			AuthenticationHandler basicAuthHandler = BasicAuthHandler.create((authInfo, resultHandler) -> {
				System.out.println(authInfo);
				resultHandler.handle(Future.succeededFuture(User.fromName(authInfo.getString("username"))));
			});
			startWebServer(startPromise, (r) -> basicAuthHandler);
		} else {
			OAuth2Options oauth2opts = new OAuth2Options().setClientID(System.getenv("OAUTH_CLIENTID").trim()) //
					.setClientSecret(System.getenv("OAUTH_SECRET").trim()) //
					.setSite(System.getenv("OAUTH_SITE").trim()) //
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
					startWebServer(startPromise, (r) -> authHandler
							.setupCallback(r.route(pathPrefix + callbackPath).failureHandler((ctx) -> {
								if (ctx.statusCode() == 401) {
									Session session = ctx.session();
									logger.warn("request: " + ctx.request().uri() + " " + ctx.request().cookieMap());
									logger.warn("Session: " + session.value() + " " + session.data());
									HttpServerResponse response = ctx.response()//
											.putHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")//
											.putHeader("Pragma", "no-cache")//
											.putHeader(HttpHeaders.EXPIRES, "0");
//											.putHeader("Location", "/");
									response.setStatusCode(401).end(
											"<html><head><title>401 Unauthorized</title><meta http-equiv=\"refresh\" content=\"3;url=/\" /></head><body><h1>401 Unauthorized</h1></body></html>");
								}
							})).withScope("openid email"));
				} else {
					logger.error("Failed to initialize OpenID Connect", oidcRes.cause());
					startPromise.fail(oidcRes.cause());
				}
			});
		}
	}

	private void loadFile(String fileName, List<Path> pathList, Handler<AsyncResult<Buffer>> handler) {
		if (pathList.isEmpty()) {
			handler.handle(Future.failedFuture("file not found: " + fileName));
		}
		Path path = pathList.remove(0);
		logger.info("trying " + path.resolve(fileName).toString() + "...");
		vertx.fileSystem().readFile(path.resolve(fileName).toString(), res -> {
			if (res.succeeded() || pathList.isEmpty())
				handler.handle(res);
			else
				loadFile(fileName, pathList, handler);
		});

	}

	private void startWebServer(Promise<Void> startPromise, Function<Router, AuthenticationHandler> auth) {
		sessionStore = LocalSessionStore.create(vertx);
		sessionHandler = SessionHandler.create(sessionStore);
		sessionHandler.setCookieHttpOnlyFlag(true);
		sessionHandler.setCookieSameSite(CookieSameSite.LAX);
		loggerHandler = LoggerHandler.create(false, LoggerHandler.DEFAULT_FORMAT);
		MessageData.setDataConstructor(() -> new MessageRepr());
		server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.get("/favicon.ico").handler(loggerHandler).handler((ctx) -> ctx.fail(404));

		router.route().handler(loggerHandler).handler(sessionHandler);
		authHandler = auth.apply(router);
		router.route().handler(authHandler).handler(loggerHandler);

		SockJSHandlerOptions options = new SockJSHandlerOptions(); // .setHeartbeatInterval(10000);
//		BridgeOptions opts = new BridgeOptions();
//		opts.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
//		opts.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		// .addInboundPermitted();// new PermittedOptions().setAddressRegex(".*"))
		// .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		Router sockJSRouter = sockJSHandler.socketHandler((sockjs) -> {
			System.out.println("handling: " + sockjs.uri());
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
		router.mountSubRouter(pathPrefix + "/socket", sockJSRouter);
//		router.route("/terminal/*").handler(sockJSHandler);
		router.get(pathPrefix + "/hello").handler(ctx -> {
			userInfo(ctx.user(), ctx.session(), (ui) -> {
				System.out.println(ui);
				ctx.response().end("Hello, " + ui.getString("name") + "\n" + ui.encode());
			});
		});

		ResponseContentTypeHandler typeHandler = ResponseContentTypeHandler.create();
		if (xAccelRedirect != null) {
			router.routeWithRegex(pathPrefix + "/static/(?<path>(\\w\\.?|\\/)+)").method(HttpMethod.GET)
					.handler(ctx -> {
						String path = ctx.pathParam("path");
						if (path != null && !path.contains("..")) {
							try {
								String typeFromName = Files.probeContentType(Path.of(path));
								if (typeFromName != null)
									ctx.response().putHeader("Content-Type", typeFromName);
								else
									logger.error("unable to guess content type for " + path);
							} catch (IOException e) {
								logger.error("unable to guess content type for " + path, e);

							}
							ctx.response().putHeader("X-Accel-Redirect", xAccelRedirect + path).end();
						} else {
							ctx.fail(404);
						}
					});
			router.route(pathPrefix + "/").method(HttpMethod.GET).handler(ctx -> {
				ctx.response().putHeader("Content-Type", "text/html")
						.putHeader("X-Accel-Redirect", xAccelRedirect + "index.html").end();
			});
		} else {
			Route root = router.route(pathPrefix + "/").method(HttpMethod.GET).handler(typeHandler).handler(//
					ctx -> {
						ctx.reroute(pathPrefix + "/static/index.html");
					});
			Route statics = router.route(pathPrefix + "/static/*").method(HttpMethod.GET);
			if (serverRoot.length > 0) {
				for (Path p : serverRoot) {
					logger.info("Adding web root " + p);
					statics.handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(p.toString()));
				}
			} else {
				logger.info("Adding web root /webroot");
				statics.handler(StaticHandler.create());// .setAllowRootFileSystemAccess(true).setWebRoot("/webroot"));
				System.out.println(statics);
			}
			statics.handler(typeHandler);
		}
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
//			userinfo = new JsonObject();
//			userinfo.put("nickname", "anya");
//			session.put("userInfo", userinfo);
//			handler.handle(userinfo);
			oAuth2Provider.userInfo(user, res -> {
				if (res.succeeded()) {
					JsonObject userInfo = res.result();
					session.put("userInfo", userInfo);
					handler.handle(userInfo);
				} else {
					logger.error("Getting userInfo for " + user + " failed", res.cause());
				}
			});
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

	public void stop() {

	}
}
