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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.CookieSameSite;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonArray;
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
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.impl.http.SimpleHttpClient;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2Options;
import io.vertx.ext.auth.oauth2.impl.OAuth2API;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import turtleduck.comms.MessageData;
import turtleduck.server.data.AuthOptions;
import turtleduck.server.handlers.TDSHandler;
import turtleduck.server.services.AuthProvider;
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
	protected String callbackPath = "/auth_callback/";
	protected Readline readline;
	private SessionHandler sessionHandler;
	private LoggerHandler loggerHandler;
	private OAuth2Auth oAuth2Provider;
	private AuthenticationHandler authHandlr;
	protected Buffer indexHtml;
	protected String xAccelRedirect = null;
	protected List<AuthProvider> authProviders = new ArrayList<>();
	// location: https://discord.com/api/oauth2/authorize?
	// state=k3hr_bPc&
	// redirect_uri=http%3A%2F%2Flocalhost%3A9090%2Fauth_callback%2Fdiscord&
	// redirect_uri=http%3A%2F%2Flocalhost%3A9090%2Fauth_callback%2Fdiscord&
	// scope=openid+email&response_type=code&c
	// client_id=799954449543331860
	// client_id=799954449543331860&
	// https://discord.com/api/oauth2/authorize?
	// response_type=code&scope=identify%20email
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
	private Buffer loginHtml;
	private Router router;
	private TDSHandler tdsHandler;

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
		String root = System.getenv("SERVER_ROOT");
		if (root == null)
			root = "/srv/turtleduck/webroot";
		serverRoot = Stream.of(root.trim().split(File.pathSeparator)).filter(p -> p.length() > 0).map(p -> Path.of(p))
				.toArray(n -> new Path[n]);
		ArrayList<Path> pathList = new ArrayList<>(Arrays.asList(serverRoot));
		pathList.add(Path.of("webroot"));
		if (xAccelRedirect == null) {
			loadFile("index.html", pathList, res -> {
				if (res.succeeded()) {
					indexHtml = res.result();
				} else {
					logger.error("can't load index.html", res.cause());
					vertx.close();
				}
			});

		}
		loadFile("login.html", pathList, res -> {
			if (res.succeeded()) {
				loginHtml = res.result();
			} else {
				logger.error("can't load login.html", res.cause());
				vertx.close();
			}
		});
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
			startWebServer(startPromise, (route) -> {
				route.get().handler(basicAuthHandler);
			});
		} else {
			String opts = System.getenv("AUTH_OPTIONS");
			String optfile = System.getenv("AUTH_OPTION_FILE");
			if (optfile != null) {
				opts = vertx.fileSystem().readFileBlocking(optfile).toString();
				logger.info("Read options file: " + opts);
			}
			if (opts == null) {
				AuthProvider provider = new AuthProvider(new AuthOptions(), externalUri(callbackPath));
				authProviders.add(provider);
			} else {
				JsonObject obj = new JsonObject(opts);
				JsonArray providers;
				if (obj.containsKey("providers")) {
					providers = obj.getJsonArray("providers");
				} else {
					providers = new JsonArray().add(obj);
				}
				for (int i = 0; i < providers.size(); i++) {
					AuthProvider provider = new AuthProvider(AuthOptions.fromJson(providers.getJsonObject(i)),
							externalUri(callbackPath));
					authProviders.add(provider);
				}
			}
			for (AuthProvider provider : authProviders) {
				provider.init(vertx).onFailure((ex) -> {
					startPromise.fail(ex);
				});
			}
			startWebServer(startPromise, (route) -> {
				for (AuthProvider provider : authProviders) {
					provider.configure(route);
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

	private Route route() {
		return router.route().handler(loggerHandler).handler(sessionHandler).handler(tdsHandler);
	}
	private Route routeNoAuth() {
		return router.route().handler(loggerHandler).handler(sessionHandler);
	}
	private Route route(String path) {
		return router.route(path).handler(loggerHandler).handler(sessionHandler).handler(tdsHandler);
	}

	private Route get(String path) {
		return router.get(path).handler(loggerHandler).handler(sessionHandler).handler(tdsHandler);
	}
	private Route getNoAuth(String path) {
		return router.get(path).handler(loggerHandler).handler(sessionHandler);
	}
	private void startWebServer(Promise<Void> startPromise, Consumer<Supplier<Route>> auth) {
		sessionStore = LocalSessionStore.create(vertx);
		sessionHandler = SessionHandler.create(sessionStore);
		sessionHandler.setCookieHttpOnlyFlag(true);
		sessionHandler.setCookieSameSite(CookieSameSite.LAX);
		sessionHandler.setSessionTimeout(24*60*60*1000);
		loggerHandler = LoggerHandler.create(false, LoggerHandler.DEFAULT_FORMAT);
		tdsHandler = new TDSHandler(this);
		MessageData.setDataConstructor(() -> new MessageRepr());
		server = vertx.createHttpServer();
		router = Router.router(vertx);
		Router publicRouter = Router.router(vertx);
		publicRouter.route().handler(loggerHandler).handler(sessionHandler).handler((ctx) -> {
			System.out.println("req: " + ctx.request().absoluteURI());
			System.out.println("headers: " + ctx.request().headers() + "data: " + ctx.data());
			System.out.println("route: " + ctx.currentRoute());
			System.out.println("path: " + ctx.normalizedPath());
			ctx.next();
		});
		router.route("/_public/*").subRouter(publicRouter);
		get("/favicon.ico").handler((ctx) -> ctx.fail(404));
		auth.accept(this::routeNoAuth);

		SockJSHandlerOptions options = new SockJSHandlerOptions(); // .setHeartbeatInterval(10000);
//		BridgeOptions opts = new BridgeOptions();
//		opts.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
//		opts.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		// .addInboundPermitted();// new PermittedOptions().setAddressRegex(".*"))
		// .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		Router sockJSRouter = sockJSHandler.socketHandler((sockjs) -> {
			System.out.println("handling: " + sockjs.uri());
			User user = sockjs.webUser();
			Session session = sockjs.webSession();
			if (user == null || session == null) {
				logger.error("No session");
				sockjs.close(1011, "No session");
			}
			TurtleDuckSession tds = session.get("turtleDuckSession");

			long t = System.currentTimeMillis();
			System.out.println("start: " + t + " " + sockjs + ", " + sockjs.uri() + "\n" + sockjs.headers());
			try {
				tds.runOnContext((__) -> tds.connect(sockjs));
			} catch (RuntimeException e) {
				logger.error("Connecting to TurtleDuckSession failed", e);
				sockjs.close(1011, "Connecting to TurtleDuckSession failed");
			}
			System.out.println("end: " + (System.currentTimeMillis() - t));

		});

		route(pathPrefix + "/socket/*").handler((ctx) -> {
			if (ctx.user() != null)
				ctx.next();
			else
				ctx.fail(403);
		});
		router.mountSubRouter(pathPrefix + "/socket", sockJSRouter);
//		router.route("/terminal/*").handler(sockJSHandler);
		get(pathPrefix + "/hello").handler(ctx -> {
			System.out.println(ctx.user().attributes());
			System.out.println(ctx.user().principal());
			JsonObject ui = ctx.session().get("userInfo");
			System.out.println(ui);
			ctx.response().end("Hello, " + ui.getString("name") + "\n" + ui.encode());
		});
		router.get(pathPrefix + "/login").handler(loggerHandler).handler(sessionHandler).handler(ctx -> {
			String links = "";
			for (AuthProvider p : authProviders) {
				links += "<a href=\"login/" + p.providerId() + "\">Log in with " + p.name() + "</a>\n";
			}
			ctx.end(loginHtml.toString().replace("<!-- LOGIN -->", links));
		});
		router.get(pathPrefix + "/login/*").handler(loggerHandler).handler(sessionHandler).handler(ctx -> {
			System.out.println(ctx.session());
			System.out.println(ctx.session().data());
			if (ctx.user() != null) {
				String path = ctx.request().getParam("redirect");
				if (path != null && path.startsWith("/") && !path.startsWith(pathPrefix + "/login")) {
					ctx.redirect(path);
				} else {
					ctx.redirect(pathPrefix + "/");
				}
			} else {
				ctx.redirect(pathPrefix + "/login");
			}
		});
		publicRouter.get("/login.html").handler(ctx -> {
			ctx.reroute(pathPrefix + "/static/login.html");
		});
		ResponseContentTypeHandler typeHandler = ResponseContentTypeHandler.create();
		if (xAccelRedirect != null) {
			route().pathRegex(pathPrefix + "/static/(?<path>(\\w\\.?|\\/)+)").method(HttpMethod.GET).handler(ctx -> {
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
			route(pathPrefix + "/").method(HttpMethod.GET).handler(ctx -> {
				ctx.response().putHeader("Content-Type", "text/html")
						.putHeader("X-Accel-Redirect", xAccelRedirect + "index.html").end();
			});
		} else {
			Route root = get(pathPrefix + "/").handler(typeHandler).handler(//
					ctx -> {
						ctx.reroute(pathPrefix + "/static/index.html");
					});
			Route statics = getNoAuth(pathPrefix + "/static/*");
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

	/**
	 * @param path An absolute path
	 * @return The path, prefixed by the global server path prefix (if any)
	 */
	public String uriPath(String path) {
		if (!path.startsWith("/"))
			throw new IllegalArgumentException("Path must be absolute: " + path);
		return pathPrefix + path;
	}

	/**
	 * @param path   An absolute path
	 * @param params A JSON object with query parameters
	 * @return The path, prefixed by the global server path prefix (if any)
	 */
	public String uriPath(String path, JsonObject params) {
		if (!path.startsWith("/"))
			throw new IllegalArgumentException("Path must be absolute: " + path);
		if (params == null || params.isEmpty())
			return pathPrefix + path;
		else
			return pathPrefix + path + "?" + SimpleHttpClient.jsonToQuery(params).toString();
	}

	public AuthProvider authProvider(Session session) {
		AuthProvider provider = session.get("auth_provider");
		if (provider == null || provider.provider() == null) {
			throw new IllegalStateException("Session has no auth provider");
		} else {
			return provider;
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
