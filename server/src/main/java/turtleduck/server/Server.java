package turtleduck.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HttpUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.impl.URIDecoder;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.auth.impl.http.SimpleHttpClient;
import turtleduck.shell.TShell;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.Readline;
import turtleduck.util.Logging;
import org.slf4j.Logger;

public class Server extends AbstractVerticle {
	private final Logger logger = Logging.getLogger(Server.class);
	private TShell shell;
	private HttpServer server;
	private PseudoTerminal pty;
	private Path[] serverRoot;
	private String pathPrefix = "";
	private Readline readline;
	private LoggerHandler loggerHandler;
	private Buffer indexHtml;
	private String xAccelRedirect = null;
	private List<Path> pathList;
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
	private Router router;

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

		String prefix = System.getenv("PATH_PREFIX");
		if (prefix != null)
			pathPrefix = prefix.trim();

		startWebServer(startPromise);

	}

	private void startWebServer(Promise<Void> startPromise) {
		loggerHandler = LoggerHandler.create(false, LoggerHandler.DEFAULT_FORMAT);
		server = vertx.createHttpServer();
		router = Router.router(vertx);
		router.route().handler(loggerHandler).handler(ctx -> {
			System.out.println("req: " + ctx.request().absoluteURI());
//			System.out.println("headers: " + ctx.request().headers() + "data: " + ctx.data());
			System.out.println("route: " + ctx.currentRoute());
			System.out.println("path: " + ctx.normalizedPath());
			ctx.next();
		});

		router.get(pathPrefix + "/favicon.ico").handler(loggerHandler).handler((ctx) -> ctx.fail(404));

		SockJSHandlerOptions options = new SockJSHandlerOptions();
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		Router sockJSRouter = sockJSHandler.socketHandler((sockjs) -> {
			sockjs.pause();
			logger.info("handling: " + sockjs.uri());
			String h = sockjs.headers().get("X-User-Info");
			if (h == null) {
				logger.error("missing x-user-info header");
				sockjs.close(1011, "No user");
			} else {
				long t = System.currentTimeMillis();
				System.out.println("start: " + t + " " + sockjs + ", " + sockjs.uri() + "\n" + sockjs.headers());
				JsonObject info = new JsonObject(h);
				QueryStringDecoder decoder = new QueryStringDecoder(sockjs.uri());
				List<String> sess = decoder.parameters().get("session");
				if (sess != null && !sess.isEmpty()) {
					String sessName = sess.get(0);
					logger.info("Session name: {}", sessName);
					LocalMap<String,TurtleDuckSession> sessions = vertx.sharedData().getLocalMap("sessions");
					TurtleDuckSession oldTds = sessions.get(sessName);
					if (oldTds != null) {
						logger.info("Reconnecting to TurtleDuck session: " + oldTds);
						oldTds.connect(sockjs);
						return;
					}
				}
				TurtleDuckSession tds = new TurtleDuckSession(info, this);
				vertx.deployVerticle(tds, new DeploymentOptions())//
						.onSuccess(s -> {
							logger.info("Deployed TurtleDuck as " + s);
							tds.connect(sockjs);
						}) //
						.onFailure(ex -> {
							logger.error("Connecting to TurtleDuckSession failed", ex);
							sockjs.close(1011, "Connecting to TurtleDuckSession failed");
						});

				System.out.println("end: " + (System.currentTimeMillis() - t));
			}
		});

		router.get(pathPrefix + "/").handler(ctx -> {
			HttpServerResponse response = ctx.response();
			response.putHeader("X-Accel-Redirect", pathPrefix + "/static/index.html");
			ctx.end();
		});
		router.route(pathPrefix + "/socket/*").handler((ctx) -> {
			if (ctx.request().getHeader("X-User-Info") != null) {
				String sessName = ctx.request().getParam("session");
				ctx.next();
			} else
				ctx.fail(403);
		});
		router.mountSubRouter(pathPrefix + "/socket", sockJSRouter);
//		router.route("/terminal/*").handler(sockJSHandler);
		router.route(pathPrefix + "/hello").handler(ctx -> {
			HttpServerRequest req = ctx.request();
			String h = req.getHeader("X-User-Info");
			JsonObject info;
			if (h != null) {
				info = new JsonObject(h);
			} else {
				info = new JsonObject();
			}
			logger.info("User id {}, info {}", req.getHeader("X-User-Id"), info.encode());
			ctx.response().end("Hello, " + info.getString("name") + "\n" + info.encodePrettily());
		});

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

	public void stop() {

	}

	public void register(TurtleDuckSession turtleDuckSession, String sessionId) {
		// TODO Auto-generated method stub

	}
}
