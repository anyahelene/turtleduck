package turtleduck.server;

import java.nio.file.Path;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.bridge.PermittedOptions;
import turtleduck.terminal.PseudoTerminal;
import turtleduck.terminal.Readline;
import turtleduck.text.Graphemizer;
import turtleduck.util.Strings;

public class Server extends AbstractVerticle {
	final Vertx vertx;
	private HttpServer server;
	ServerWebSocket webSocket;
	PseudoTerminal pty;
	Path[] serverRoot = { Path.of("/home/anya/git/turtleduck/tea/target/generated/js"),
			Path.of("/home/anya/git/turtleduck/tea/src/main/webapp/"),
			Path.of("/home/anya/git/turtleduck/tea/node_modules"), };
	private Readline readline;

	public static void main(String[] args) {
		Server server = new Server();

		server.vertx.deployVerticle(server, res -> {
			if (res.succeeded()) {
				System.out.println("Deployment id is: " + res.result());
			} else {
				System.out.println("Deployment failed!");
				res.cause().printStackTrace();
			}
		});
	}

	public Server() {
		vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(40));
	}

	public void send(String s) {
		System.out.println("Output: " + Strings.escape(s));
		if (webSocket != null) {
			webSocket.writeTextMessage(s);
		}
	}

	public void start(Promise<Void> startPromise) {
		pty = new PseudoTerminal(null);
		pty.termListener(this::send);
		readline = new Readline();
		readline.attach(pty);
		Graphemizer splitter = new Graphemizer();
		server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route().handler(LoggerHandler.create());
		Route statics = router.get();
		for (Path p : serverRoot)
			statics.handler(StaticHandler.create().setAllowRootFileSystemAccess(true).setWebRoot(p.toString()));
		/*
		 * .handler(routingContext -> { HttpServerRequest req =
		 * routingContext.request(); HttpServerResponse response =
		 * routingContext.response(); Path path = Path.of(req.path()).normalize();
		 * System.out.println("requested: " + path); if (path.getNameCount() == 0) {
		 * path = Path.of("index.html"); } else if (path.isAbsolute()) { path =
		 * path.subpath(0, path.getNameCount()); } for (Path root : serverRoot) { Path
		 * localPath = root.resolve(path); System.out.println("trying: " + localPath);
		 * if (localPath.toFile().canRead()) { System.out.println("serving: " +
		 * localPath); response.putHeader("content-type",
		 * contentTypeOf(path)).sendFile(localPath.toString()); return; } }
		 * 
		 * response.putHeader("content-type", "text/plain").setStatusCode(404).end();
		 * 
		 * // This handler will be called for every request
		 * response.putHeader("content-type", "text/plain");
		 * 
		 * // Write to the response and end it
		 * response.end("Hello World from Vert.x-Web!"); });
		 */
		SockJSHandlerOptions options = new SockJSHandlerOptions(); // .setHeartbeatInterval(10000);
		BridgeOptions opts = new BridgeOptions();
		opts.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		opts.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		// .addInboundPermitted();// new PermittedOptions().setAddressRegex(".*"))
		// .addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));

		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
		Router sjsRouter = sockJSHandler.socketHandler((sockjs) -> {
			sockjs.handler((buf) -> {
				System.out.println(buf.toString());
			});
		});
		router.route("/terminal").handler(sockJSHandler);
		router.route("/terminal/*").handler(sockJSHandler);

		server.requestHandler(router);
		
		/*
		 * .webSocketHandler(ws -> { System.out.println("WebSocket connection on " +
		 * ws.path()); ws.textMessageHandler(string -> { System.err.println("Received <"
		 * + string + ">"); pty.writeToHost(string); }); ws.closeHandler((_void) -> {
		 * webSocket = null; System.err.println("Connection closed."); }); ws.accept();
		 * webSocket = ws; });
		 */

		// Now bind the server:
		server.listen(9080, "localhost", res -> {
			if (res.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(res.cause());
			}
		});
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
